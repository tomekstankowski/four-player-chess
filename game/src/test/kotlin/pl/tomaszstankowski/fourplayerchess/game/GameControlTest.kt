package pl.tomaszstankowski.fourplayerchess.game

import org.amshove.kluent.mock
import org.amshove.kluent.should
import org.amshove.kluent.shouldBeEqualTo
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import pl.tomaszstankowski.fourplayerchess.game.Fixture.CLAIM_DRAW_DTO
import pl.tomaszstankowski.fourplayerchess.game.Fixture.CREATE_GAME_DTO
import pl.tomaszstankowski.fourplayerchess.game.Fixture.MAKE_MOVE_DTO
import pl.tomaszstankowski.fourplayerchess.game.Fixture.NOW
import pl.tomaszstankowski.fourplayerchess.game.Fixture.RANDOM_SEED
import pl.tomaszstankowski.fourplayerchess.game.Fixture.RESIGN_DTO
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.*
import kotlin.random.Random

class GameControlTest : Spek({

    fun createServiceForTesting(random: Random = Random(RANDOM_SEED)) =
            GameControlService.create(
                    clock = Clock.fixed(Instant.parse(NOW), ZoneId.of("UTC")),
                    random = random,
                    messageSendingOperations = mock()
            )

    describe("creating game") {

        it("should return error when too many players given") {
            val service = createServiceForTesting()

            val result = service.createGame(
                    CreateGameDto(
                            humanPlayersIds = setOf(
                                    UUID.fromString("925a168e-c60c-40db-bd51-ae62e40c9625")
                            ),
                            randomBotsCount = 4
                    )
            )

            result shouldBeEqualTo CreateGameResult.Error.TooManyPlayers(playersCount = 5)
        }

        it("should return error when not enough players given") {
            val service = createServiceForTesting()

            val result = service.createGame(
                    CreateGameDto(
                            humanPlayersIds = setOf(
                                    UUID.fromString("925a168e-c60c-40db-bd51-ae62e40c9625")
                            ),
                            randomBotsCount = 2
                    )
            )

            result shouldBeEqualTo CreateGameResult.Error.NotEnoughPlayers(playersCount = 3)
        }

        it("should return game") {
            val service = createServiceForTesting()

            val result = service.createGame(CREATE_GAME_DTO)

            result shouldBeEqualTo CreateGameResult.Success(
                    GameDto(
                            id = UUID.fromString("00000000-0000-0000-0000-000000000001"),
                            createdAt = Instant.parse(NOW),
                            isCancelled = false,
                            isFinished = false
                    )
            )
        }

        it("game should not be accessible before commit") {
            val service = createServiceForTesting()
            service.createGame(CREATE_GAME_DTO)
            val game = service.getGame(UUID.fromString("00000000-0000-0000-0000-000000000001"))

            game shouldBeEqualTo null
        }

        it("game should be accessible after commit") {
            val service = createServiceForTesting()
            service.createGame(CREATE_GAME_DTO)
            service.commitGame(UUID.fromString("00000000-0000-0000-0000-000000000001"))
            val game = service.getGame(UUID.fromString("00000000-0000-0000-0000-000000000001"))

            game shouldBeEqualTo GameDto(
                    id = UUID.fromString("00000000-0000-0000-0000-000000000001"),
                    createdAt = Instant.parse(NOW),
                    isCancelled = false,
                    isFinished = false
            )
        }

        it("should assign random colors to players") {
            val service = createServiceForTesting()
            service.createGame(
                    CREATE_GAME_DTO.copy(
                            humanPlayersIds = setOf(
                                    UUID.fromString("c37d4c5f-3880-4689-ac2f-8eaf46583a4c"),
                                    UUID.fromString("496bbf40-a106-49cc-9c4b-ab466b79d7de"),
                                    UUID.fromString("d4fd97e0-7321-4e30-b72a-349d79bc9798")
                            ),
                            randomBotsCount = 1
                    )
            )
            service.commitGame(UUID.fromString("00000000-0000-0000-0000-000000000001"))

            val players = service.getPlayersOfTheGame(UUID.fromString("00000000-0000-0000-0000-000000000001"))

            players shouldBeEqualTo listOf(
                    GamePlayerDto(playerId = UUID.fromString("c37d4c5f-3880-4689-ac2f-8eaf46583a4c"), color = "red", type = "human"),
                    GamePlayerDto(playerId = UUID.fromString("496bbf40-a106-49cc-9c4b-ab466b79d7de"), color = "blue", type = "human"),
                    GamePlayerDto(playerId = null, color = "yellow", type = "randomBot"),
                    GamePlayerDto(playerId = UUID.fromString("d4fd97e0-7321-4e30-b72a-349d79bc9798"), color = "green", type = "human")
            )
        }

    }

    describe("committing game") {

        it("should return true if game committed") {
            val service = createServiceForTesting()
            service.createGame(CREATE_GAME_DTO)

            val result = service.commitGame(UUID.fromString("00000000-0000-0000-0000-000000000001"))

            result shouldBeEqualTo true
        }

        it("should return false if game already committed") {
            val service = createServiceForTesting()
            service.createGame(CREATE_GAME_DTO)

            service.commitGame(UUID.fromString("00000000-0000-0000-0000-000000000001"))
            val result = service.commitGame(UUID.fromString("00000000-0000-0000-0000-000000000001"))

            result shouldBeEqualTo false
        }

        it("should return false if game does not exist") {
            val service = createServiceForTesting()

            val result = service.commitGame(UUID.fromString("00000000-0000-0000-0000-000000000001"))

            result shouldBeEqualTo false
        }

        it("should return false if game cancelled") {
            val service = createServiceForTesting()
            service.createGame(CREATE_GAME_DTO)

            service.commitGame(UUID.fromString("00000000-0000-0000-0000-000000000001"))
            service.cancelAllActiveGames()
            val result = service.commitGame(UUID.fromString("00000000-0000-0000-0000-000000000001"))

            result shouldBeEqualTo false
        }

        it("should play bot move if bot opens game") {
            // human player should play as blue
            val service = createServiceForTesting(random = Random(8))
            service.createGame(
                    CreateGameDto(
                            humanPlayersIds = setOf(
                                    UUID.fromString("2fb0441c-1b7d-4c4e-b55f-688553358458")
                            ),
                            randomBotsCount = 3
                    )
            )

            service.commitGame(UUID.fromString("00000000-0000-0000-0000-000000000001"))
            val state = service.getGameState(UUID.fromString("00000000-0000-0000-0000-000000000001"))

            state should {
                this is GetGameStateResult.Success && this.gameState.nextMoveColor == "blue"
            }
        }
    }

    describe("getting active games for player") {

        it("should return player's active games") {
            val service = createServiceForTesting()
            service.createGame(
                    CREATE_GAME_DTO.copy(
                            humanPlayersIds = setOf(
                                    UUID.fromString("c37d4c5f-3880-4689-ac2f-8eaf46583a4c"),
                                    UUID.fromString("496bbf40-a106-49cc-9c4b-ab466b79d7de"),
                                    UUID.fromString("d4fd97e0-7321-4e30-b72a-349d79bc9798"),
                                    UUID.fromString("446cc4d9-ca17-4e6f-be8e-b32d3367f5a8")
                            )
                    )
            )
            service.commitGame(UUID.fromString("00000000-0000-0000-0000-000000000001"))
            service.createGame(
                    CREATE_GAME_DTO.copy(
                            humanPlayersIds = setOf(
                                    UUID.fromString("c37d4c5f-3880-4689-ac2f-8eaf46583a4c"),
                                    UUID.fromString("f2dabe9d-7fe1-4e02-a78c-f95512f39d5d"),
                                    UUID.fromString("63668539-e0ce-41f7-9cfd-a0a36143d687"),
                                    UUID.fromString("99007f41-6468-4288-833f-cdfa8a3e98cc")
                            )
                    )
            )
            service.commitGame(UUID.fromString("00000000-0000-0000-0000-000000000002"))
            service.createGame(
                    CREATE_GAME_DTO.copy(
                            humanPlayersIds = setOf(
                                    UUID.fromString("67508179-ba0b-4ffc-b616-9d989961bdcf"),
                                    UUID.fromString("7b2a2e38-7ea8-4aa1-ace4-ce9dbf8d290e"),
                                    UUID.fromString("8ac54453-9536-436a-b892-c4c936c6f372"),
                                    UUID.fromString("97ccda31-9a8a-4aaa-adce-64c2428e4e8d")
                            )
                    )
            )
            service.commitGame(UUID.fromString("00000000-0000-0000-0000-000000000003"))

            val result = service.getActiveGamesForPlayer(UUID.fromString("c37d4c5f-3880-4689-ac2f-8eaf46583a4c"))

            result shouldBeEqualTo listOf(
                    GameDto(
                            id = UUID.fromString("00000000-0000-0000-0000-000000000001"),
                            createdAt = Instant.parse(NOW),
                            isCancelled = false,
                            isFinished = false
                    ),
                    GameDto(
                            id = UUID.fromString("00000000-0000-0000-0000-000000000002"),
                            createdAt = Instant.parse(NOW),
                            isCancelled = false,
                            isFinished = false
                    )
            )
        }

        it("should not return uncommitted games") {
            val service = createServiceForTesting()
            service.createGame(
                    CREATE_GAME_DTO.copy(
                            humanPlayersIds = setOf(
                                    UUID.fromString("c37d4c5f-3880-4689-ac2f-8eaf46583a4c"),
                                    UUID.fromString("496bbf40-a106-49cc-9c4b-ab466b79d7de"),
                                    UUID.fromString("d4fd97e0-7321-4e30-b72a-349d79bc9798"),
                                    UUID.fromString("446cc4d9-ca17-4e6f-be8e-b32d3367f5a8")
                            )
                    )
            )

            val result = service.getActiveGamesForPlayer(UUID.fromString("c37d4c5f-3880-4689-ac2f-8eaf46583a4c"))

            result shouldBeEqualTo emptyList()
        }

        it("should not return cancelled games") {
            val service = createServiceForTesting()
            service.createGame(
                    CREATE_GAME_DTO.copy(
                            humanPlayersIds = setOf(
                                    UUID.fromString("c37d4c5f-3880-4689-ac2f-8eaf46583a4c"),
                                    UUID.fromString("496bbf40-a106-49cc-9c4b-ab466b79d7de"),
                                    UUID.fromString("d4fd97e0-7321-4e30-b72a-349d79bc9798"),
                                    UUID.fromString("446cc4d9-ca17-4e6f-be8e-b32d3367f5a8")
                            )
                    )
            )
            service.commitGame(UUID.fromString("00000000-0000-0000-0000-000000000001"))
            service.cancelAllActiveGames()

            val result = service.getActiveGamesForPlayer(UUID.fromString("c37d4c5f-3880-4689-ac2f-8eaf46583a4c"))

            result shouldBeEqualTo emptyList()
        }
    }

    describe("getting game state") {

        it("should return error when game not found") {
            val service = createServiceForTesting()

            val result = service.getGameState(UUID.fromString("00000000-0000-0000-0000-000000000001"))

            result shouldBeEqualTo GetGameStateResult.GameNotFound(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        }

        it("should return error when game not committed") {
            val service = createServiceForTesting()
            service.createGame(CREATE_GAME_DTO)
            val result = service.getGameState(UUID.fromString("00000000-0000-0000-0000-000000000001"))

            result shouldBeEqualTo GetGameStateResult.GameNotFound(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        }

        it("should return error when game cancelled") {
            val service = createServiceForTesting()
            service.createGame(CREATE_GAME_DTO)
            service.commitGame(UUID.fromString("00000000-0000-0000-0000-000000000001"))
            service.cancelAllActiveGames()
            val result = service.getGameState(UUID.fromString("00000000-0000-0000-0000-000000000001"))

            result shouldBeEqualTo GetGameStateResult.GameNotActive
        }

        it("should return game state for active game") {
            val service = createServiceForTesting()
            service.createGame(CREATE_GAME_DTO)
            service.commitGame(UUID.fromString("00000000-0000-0000-0000-000000000001"))

            val result = service.getGameState(UUID.fromString("00000000-0000-0000-0000-000000000001"))

            result shouldBeEqualTo GetGameStateResult.Success(
                    GameStateDto(
                            board = listOf(
                                    listOf("rR", "rN", "rB", "rQ", "rK", "rB", "rN", "rR"),
                                    listOf("rP", "rP", "rP", "rP", "rP", "rP", "rP", "rP"),
                                    listOf("", "", "", "", "", "", "", ""),
                                    listOf("bR", "bP", "", "", "", "", "", "", "", "", "", "", "gP", "gR"),
                                    listOf("bN", "bP", "", "", "", "", "", "", "", "", "", "", "gP", "gN"),
                                    listOf("bB", "bP", "", "", "", "", "", "", "", "", "", "", "gP", "gB"),
                                    listOf("bQ", "bP", "", "", "", "", "", "", "", "", "", "", "gP", "gK"),
                                    listOf("bK", "bP", "", "", "", "", "", "", "", "", "", "", "gP", "gQ"),
                                    listOf("bB", "bP", "", "", "", "", "", "", "", "", "", "", "gP", "gB"),
                                    listOf("bN", "bP", "", "", "", "", "", "", "", "", "", "", "gP", "gN"),
                                    listOf("bR", "bP", "", "", "", "", "", "", "", "", "", "", "gP", "gR"),
                                    listOf("", "", "", "", "", "", "", ""),
                                    listOf("yP", "yP", "yP", "yP", "yP", "yP", "yP", "yP"),
                                    listOf("yR", "yN", "yB", "yK", "yQ", "yB", "yN", "yR")
                            ),
                            eliminatedColors = emptyList(),
                            nextMoveColor = "red",
                            colorsInCheck = emptyList(),
                            legalMoves = listOf(
                                    LegalMoveDto("d2", "d3"),
                                    LegalMoveDto("d2", "d4"),
                                    LegalMoveDto("e1", "f3"),
                                    LegalMoveDto("e1", "d3"),
                                    LegalMoveDto("e2", "e3"),
                                    LegalMoveDto("e2", "e4"),
                                    LegalMoveDto("f2", "f3"),
                                    LegalMoveDto("f2", "f4"),
                                    LegalMoveDto("g2", "g3"),
                                    LegalMoveDto("g2", "g4"),
                                    LegalMoveDto("h2", "h3"),
                                    LegalMoveDto("h2", "h4"),
                                    LegalMoveDto("i2", "i3"),
                                    LegalMoveDto("i2", "i4"),
                                    LegalMoveDto("j1", "k3"),
                                    LegalMoveDto("j1", "i3"),
                                    LegalMoveDto("j2", "j3"),
                                    LegalMoveDto("j2", "j4"),
                                    LegalMoveDto("k2", "k3"),
                                    LegalMoveDto("k2", "k4")
                            ),
                            isDrawByClaimAllowed = false,
                            isFinished = false,
                            winningColor = null
                    )
            )
        }
    }

    describe("making move") {

        it("should return error when game not found") {
            val service = createServiceForTesting()

            val result = service.makeMove(
                    MAKE_MOVE_DTO.copy(
                            gameId = UUID.fromString("673c5595-a797-444c-b5a8-4f7634c0f242")
                    )
            )

            result shouldBeEqualTo MakeMoveResult.Error.GameNotFound(UUID.fromString("673c5595-a797-444c-b5a8-4f7634c0f242"))
        }

        it("should return error when game is not committed") {
            val service = createServiceForTesting()
            service.createGame(CREATE_GAME_DTO)

            val result = service.makeMove(
                    MAKE_MOVE_DTO.copy(
                            gameId = UUID.fromString("00000000-0000-0000-0000-000000000001")
                    )
            )

            result shouldBeEqualTo MakeMoveResult.Error.GameNotFound(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        }

        it("should return error when requesting player is not player in this game") {
            val service = createServiceForTesting()
            service.createGame(
                    CREATE_GAME_DTO.copy(
                            humanPlayersIds = setOf(
                                    UUID.fromString("c37d4c5f-3880-4689-ac2f-8eaf46583a4c"),
                                    UUID.fromString("496bbf40-a106-49cc-9c4b-ab466b79d7de"),
                                    UUID.fromString("d4fd97e0-7321-4e30-b72a-349d79bc9798"),
                                    UUID.fromString("446cc4d9-ca17-4e6f-be8e-b32d3367f5a8")
                            )
                    )
            )
            service.commitGame(UUID.fromString("00000000-0000-0000-0000-000000000001"))

            val result = service.makeMove(
                    MAKE_MOVE_DTO.copy(
                            playerId = UUID.fromString("de734ccb-4b8c-42f0-8406-27c5c32418ff")
                    )
            )

            result shouldBeEqualTo MakeMoveResult.Error.PlayerIsNotInTheGame
        }

        it("should return error when requesting player does not have next move") {
            val service = createServiceForTesting()
            service.createGame(
                    CREATE_GAME_DTO.copy(
                            humanPlayersIds = setOf(
                                    UUID.fromString("c37d4c5f-3880-4689-ac2f-8eaf46583a4c"),
                                    UUID.fromString("496bbf40-a106-49cc-9c4b-ab466b79d7de"),
                                    UUID.fromString("d4fd97e0-7321-4e30-b72a-349d79bc9798"),
                                    UUID.fromString("446cc4d9-ca17-4e6f-be8e-b32d3367f5a8")
                            )
                    )
            )
            service.commitGame(UUID.fromString("00000000-0000-0000-0000-000000000001"))

            val result = service.makeMove(
                    MAKE_MOVE_DTO.copy(
                            playerId = UUID.fromString("496bbf40-a106-49cc-9c4b-ab466b79d7de")
                    )
            )

            result shouldBeEqualTo MakeMoveResult.Error.PlayerDoesNotHaveNextMove(
                    nextMoveColor = "red",
                    playerColor = "blue"
            )
        }

        it("should return error when position is out of board") {
            val service = createServiceForTesting()
            service.createGame(CREATE_GAME_DTO)
            service.commitGame(UUID.fromString("00000000-0000-0000-0000-000000000001"))

            val result = service.makeMove(
                    MAKE_MOVE_DTO.copy(
                            from = "d15"
                    )
            )

            result shouldBeEqualTo MakeMoveResult.Error.InvalidPosition("d15")
        }

        it("should return error when position is invalid") {
            val service = createServiceForTesting()
            service.createGame(CREATE_GAME_DTO)
            service.commitGame(UUID.fromString("00000000-0000-0000-0000-000000000001"))

            val result = service.makeMove(
                    MAKE_MOVE_DTO.copy(
                            from = "z01"
                    )
            )

            result shouldBeEqualTo MakeMoveResult.Error.InvalidPosition("z01")
        }

        it("should return error when move is illegal") {
            val service = createServiceForTesting()
            service.createGame(CREATE_GAME_DTO)
            service.commitGame(UUID.fromString("00000000-0000-0000-0000-000000000001"))

            val result = service.makeMove(
                    MAKE_MOVE_DTO.copy(
                            from = "d1",
                            to = "d3"
                    )
            )

            result shouldBeEqualTo MakeMoveResult.Error.IllegalMove
        }

        it("should return error given illegal promotion piece") {
            val service = createServiceForTesting()
            service.createGame(CREATE_GAME_DTO)
            service.commitGame(UUID.fromString("00000000-0000-0000-0000-000000000001"))

            val result = service.makeMove(
                    MAKE_MOVE_DTO.copy(promotionPiece = "king")
            )

            result shouldBeEqualTo MakeMoveResult.Error.IllegalPromotionPiece(
                    legalPieces = listOf("queen", "rook", "bishop", "knight"),
                    given = "king"
            )
        }

        it("should return error given invalid promotion piece") {
            val service = createServiceForTesting()
            service.createGame(CREATE_GAME_DTO)
            service.commitGame(UUID.fromString("00000000-0000-0000-0000-000000000001"))

            val result = service.makeMove(
                    MAKE_MOVE_DTO.copy(promotionPiece = "quuuuen")
            )

            result shouldBeEqualTo MakeMoveResult.Error.IllegalPromotionPiece(
                    legalPieces = listOf("queen", "rook", "bishop", "knight"),
                    given = "quuuuen"
            )
        }

        it("should return new game state") {
            val service = createServiceForTesting()
            service.createGame(CREATE_GAME_DTO)
            service.commitGame(UUID.fromString("00000000-0000-0000-0000-000000000001"))

            val result = service.makeMove(MAKE_MOVE_DTO)

            result shouldBeEqualTo MakeMoveResult.Success(
                    GameStateDto(
                            board = listOf(
                                    listOf("rR", "rN", "rB", "rQ", "rK", "rB", "rN", "rR"),
                                    listOf("", "rP", "rP", "rP", "rP", "rP", "rP", "rP"),
                                    listOf("rP", "", "", "", "", "", "", ""),
                                    listOf("bR", "bP", "", "", "", "", "", "", "", "", "", "", "gP", "gR"),
                                    listOf("bN", "bP", "", "", "", "", "", "", "", "", "", "", "gP", "gN"),
                                    listOf("bB", "bP", "", "", "", "", "", "", "", "", "", "", "gP", "gB"),
                                    listOf("bQ", "bP", "", "", "", "", "", "", "", "", "", "", "gP", "gK"),
                                    listOf("bK", "bP", "", "", "", "", "", "", "", "", "", "", "gP", "gQ"),
                                    listOf("bB", "bP", "", "", "", "", "", "", "", "", "", "", "gP", "gB"),
                                    listOf("bN", "bP", "", "", "", "", "", "", "", "", "", "", "gP", "gN"),
                                    listOf("bR", "bP", "", "", "", "", "", "", "", "", "", "", "gP", "gR"),
                                    listOf("", "", "", "", "", "", "", ""),
                                    listOf("yP", "yP", "yP", "yP", "yP", "yP", "yP", "yP"),
                                    listOf("yR", "yN", "yB", "yK", "yQ", "yB", "yN", "yR")
                            ),
                            eliminatedColors = emptyList(),
                            nextMoveColor = "blue",
                            colorsInCheck = emptyList(),
                            legalMoves = listOf(
                                    LegalMoveDto("a5", "c6"),
                                    LegalMoveDto("a5", "c4"),
                                    LegalMoveDto("a10", "c11"),
                                    LegalMoveDto("a10", "c9"),
                                    LegalMoveDto("b4", "c4"),
                                    LegalMoveDto("b4", "d4"),
                                    LegalMoveDto("b5", "c5"),
                                    LegalMoveDto("b5", "d5"),
                                    LegalMoveDto("b6", "c6"),
                                    LegalMoveDto("b6", "d6"),
                                    LegalMoveDto("b7", "c7"),
                                    LegalMoveDto("b7", "d7"),
                                    LegalMoveDto("b8", "c8"),
                                    LegalMoveDto("b8", "d8"),
                                    LegalMoveDto("b9", "c9"),
                                    LegalMoveDto("b9", "d9"),
                                    LegalMoveDto("b10", "c10"),
                                    LegalMoveDto("b10", "d10"),
                                    LegalMoveDto("b11", "c11"),
                                    LegalMoveDto("b11", "d11")
                            ),
                            isDrawByClaimAllowed = false,
                            isFinished = false,
                            winningColor = null
                    )
            )
        }

        it("should play bot move if bot has next move") {
            val service = createServiceForTesting(random = Random(10))
            service.createGame(
                    CreateGameDto(
                            humanPlayersIds = setOf(
                                    UUID.fromString("c93ffe6e-8177-4be1-ab01-176c678c2837"), // yellow
                                    UUID.fromString("110cf939-5ba1-4866-a4d1-e93cbf56f244") // red
                            ),
                            randomBotsCount = 2
                    )
            )
            service.commitGame(UUID.fromString("00000000-0000-0000-0000-000000000001"))

            service.makeMove(
                    MAKE_MOVE_DTO.copy(playerId = UUID.fromString("110cf939-5ba1-4866-a4d1-e93cbf56f244"))
            )
            val result = service.getGameState(UUID.fromString("00000000-0000-0000-0000-000000000001"))

            result should {
                this is GetGameStateResult.Success && this.gameState.nextMoveColor == "yellow"
            }
        }
    }

    describe("submitting resignation") {

        it("should return error when game not found") {
            val service = createServiceForTesting()
            service.createGame(CREATE_GAME_DTO)
            service.commitGame(UUID.fromString("00000000-0000-0000-0000-000000000001"))

            val result = service.submitResignation(
                    RESIGN_DTO.copy(gameId = UUID.fromString("be548e58-1e66-4e73-acf9-0647ca457999"))
            )

            result shouldBeEqualTo SubmitResignationResult.Error.GameNotFound(
                    id = UUID.fromString("be548e58-1e66-4e73-acf9-0647ca457999")
            )
        }

        it("should return error when game is not active") {
            val service = createServiceForTesting()
            service.createGame(CREATE_GAME_DTO)
            service.commitGame(UUID.fromString("00000000-0000-0000-0000-000000000001"))
            service.cancelAllActiveGames()

            val result = service.submitResignation(RESIGN_DTO)

            result shouldBeEqualTo SubmitResignationResult.Error.GameNotActive
        }

        it("should return error when requesting player is not in the game") {
            val service = createServiceForTesting()
            service.createGame(
                    CREATE_GAME_DTO.copy(
                            humanPlayersIds = setOf(
                                    UUID.fromString("c37d4c5f-3880-4689-ac2f-8eaf46583a4c"),
                                    UUID.fromString("496bbf40-a106-49cc-9c4b-ab466b79d7de"),
                                    UUID.fromString("d4fd97e0-7321-4e30-b72a-349d79bc9798"),
                                    UUID.fromString("446cc4d9-ca17-4e6f-be8e-b32d3367f5a8")
                            )
                    )
            )
            service.commitGame(UUID.fromString("00000000-0000-0000-0000-000000000001"))

            val result = service.submitResignation(
                    RESIGN_DTO.copy(requestingPlayerId = UUID.fromString("ef6f9394-4c3a-4e36-aa8d-145ab06d3b51"))
            )

            result shouldBeEqualTo SubmitResignationResult.Error.PlayerNotInTheGame
        }

        it("should return error when resignation is not allowed") {
            val service = createServiceForTesting()
            service.createGame(CREATE_GAME_DTO)
            service.commitGame(UUID.fromString("00000000-0000-0000-0000-000000000001"))

            service.submitResignation(RESIGN_DTO)
            val result = service.submitResignation(RESIGN_DTO)

            result shouldBeEqualTo SubmitResignationResult.Error.NotAllowed
        }

        it("should return new game state on success") {
            val service = createServiceForTesting()
            service.createGame(CREATE_GAME_DTO)
            service.commitGame(UUID.fromString("00000000-0000-0000-0000-000000000001"))

            val result = service.submitResignation(RESIGN_DTO)

            result shouldBeEqualTo SubmitResignationResult.Success(
                    GameStateDto(
                            board = listOf(
                                    listOf("rR", "rN", "rB", "rQ", "rK", "rB", "rN", "rR"),
                                    listOf("rP", "rP", "rP", "rP", "rP", "rP", "rP", "rP"),
                                    listOf("", "", "", "", "", "", "", ""),
                                    listOf("bR", "bP", "", "", "", "", "", "", "", "", "", "", "gP", "gR"),
                                    listOf("bN", "bP", "", "", "", "", "", "", "", "", "", "", "gP", "gN"),
                                    listOf("bB", "bP", "", "", "", "", "", "", "", "", "", "", "gP", "gB"),
                                    listOf("bQ", "bP", "", "", "", "", "", "", "", "", "", "", "gP", "gK"),
                                    listOf("bK", "bP", "", "", "", "", "", "", "", "", "", "", "gP", "gQ"),
                                    listOf("bB", "bP", "", "", "", "", "", "", "", "", "", "", "gP", "gB"),
                                    listOf("bN", "bP", "", "", "", "", "", "", "", "", "", "", "gP", "gN"),
                                    listOf("bR", "bP", "", "", "", "", "", "", "", "", "", "", "gP", "gR"),
                                    listOf("", "", "", "", "", "", "", ""),
                                    listOf("yP", "yP", "yP", "yP", "yP", "yP", "yP", "yP"),
                                    listOf("yR", "yN", "yB", "yK", "yQ", "yB", "yN", "yR")
                            ),
                            eliminatedColors = listOf("red"),
                            nextMoveColor = "blue",
                            colorsInCheck = emptyList(),
                            legalMoves = listOf(
                                    LegalMoveDto("a5", "c6"),
                                    LegalMoveDto("a5", "c4"),
                                    LegalMoveDto("a10", "c11"),
                                    LegalMoveDto("a10", "c9"),
                                    LegalMoveDto("b4", "c4"),
                                    LegalMoveDto("b4", "d4"),
                                    LegalMoveDto("b5", "c5"),
                                    LegalMoveDto("b5", "d5"),
                                    LegalMoveDto("b6", "c6"),
                                    LegalMoveDto("b6", "d6"),
                                    LegalMoveDto("b7", "c7"),
                                    LegalMoveDto("b7", "d7"),
                                    LegalMoveDto("b8", "c8"),
                                    LegalMoveDto("b8", "d8"),
                                    LegalMoveDto("b9", "c9"),
                                    LegalMoveDto("b9", "d9"),
                                    LegalMoveDto("b10", "c10"),
                                    LegalMoveDto("b10", "d10"),
                                    LegalMoveDto("b11", "c11"),
                                    LegalMoveDto("b11", "d11")
                            ),
                            isDrawByClaimAllowed = false,
                            isFinished = false,
                            winningColor = null
                    ),
                    resignedColor = "red"
            )
        }

        it("should play bot move if bot has next move") {
            val service = createServiceForTesting(random = Random(10))
            service.createGame(
                    CreateGameDto(
                            humanPlayersIds = setOf(
                                    UUID.fromString("c93ffe6e-8177-4be1-ab01-176c678c2837"), // yellow
                                    UUID.fromString("110cf939-5ba1-4866-a4d1-e93cbf56f244") // red
                            ),
                            randomBotsCount = 2
                    )
            )
            service.commitGame(UUID.fromString("00000000-0000-0000-0000-000000000001"))

            service.submitResignation(
                    RESIGN_DTO.copy(requestingPlayerId = UUID.fromString("110cf939-5ba1-4866-a4d1-e93cbf56f244"))
            )
            val result = service.getGameState(UUID.fromString("00000000-0000-0000-0000-000000000001"))

            result should {
                this is GetGameStateResult.Success && this.gameState.nextMoveColor == "yellow"
            }
        }
    }

    describe("cancelling all active games") {

        it("should not touch uncommitted games") {
            val service = createServiceForTesting()
            service.createGame(CREATE_GAME_DTO)
            service.createGame(CREATE_GAME_DTO)

            service.cancelAllActiveGames()
            service.commitGame(UUID.fromString("00000000-0000-0000-0000-000000000001"))
            service.commitGame(UUID.fromString("00000000-0000-0000-0000-000000000002"))

            service.getGame(UUID.fromString("00000000-0000-0000-0000-000000000001"))?.isCancelled shouldBeEqualTo false
            service.getGame(UUID.fromString("00000000-0000-0000-0000-000000000002"))?.isCancelled shouldBeEqualTo false
        }

        it("should cancel committed games") {
            val service = createServiceForTesting()
            service.createGame(CREATE_GAME_DTO)
            service.createGame(CREATE_GAME_DTO)
            service.commitGame(UUID.fromString("00000000-0000-0000-0000-000000000001"))
            service.commitGame(UUID.fromString("00000000-0000-0000-0000-000000000002"))

            service.cancelAllActiveGames()

            service.getGame(UUID.fromString("00000000-0000-0000-0000-000000000001"))?.isCancelled shouldBeEqualTo true
            service.getGame(UUID.fromString("00000000-0000-0000-0000-000000000002"))?.isCancelled shouldBeEqualTo true
        }
    }

    describe("claiming draw") {

        it("should return error when game not found") {
            val service = createServiceForTesting()

            val result = service.claimDraw(
                    CLAIM_DRAW_DTO.copy(gameId = UUID.fromString("6fb1505d-f00b-4d3a-92db-1c165d73a040"))
            )

            result shouldBeEqualTo ClaimDrawResult.Error.GameNotFound(
                    id = UUID.fromString("6fb1505d-f00b-4d3a-92db-1c165d73a040")
            )
        }

        it("should return error when game is not active") {
            val service = createServiceForTesting()
            service.createGame(CREATE_GAME_DTO)
            service.commitGame(UUID.fromString("00000000-0000-0000-0000-000000000001"))
            service.cancelAllActiveGames()

            val result = service.claimDraw(CLAIM_DRAW_DTO)

            result shouldBeEqualTo ClaimDrawResult.Error.GameNotActive
        }

        it("should return error when requesting player is not in game") {
            val service = createServiceForTesting()
            service.createGame(
                    CREATE_GAME_DTO.copy(
                            humanPlayersIds = setOf(
                                    UUID.fromString("c37d4c5f-3880-4689-ac2f-8eaf46583a4c"),
                                    UUID.fromString("496bbf40-a106-49cc-9c4b-ab466b79d7de"),
                                    UUID.fromString("d4fd97e0-7321-4e30-b72a-349d79bc9798"),
                                    UUID.fromString("446cc4d9-ca17-4e6f-be8e-b32d3367f5a8")
                            )
                    )
            )
            service.commitGame(UUID.fromString("00000000-0000-0000-0000-000000000001"))

            val result = service.claimDraw(
                    CLAIM_DRAW_DTO.copy(requestingPlayerId = UUID.fromString("41799370-f974-48c8-9a23-aa2423f26bb7"))
            )

            result shouldBeEqualTo ClaimDrawResult.Error.PlayerNotInTheGame
        }

        it("should return error when draw by claim is not allowed") {
            val service = createServiceForTesting()
            service.createGame(CREATE_GAME_DTO)
            service.commitGame(UUID.fromString("00000000-0000-0000-0000-000000000001"))

            val result = service.claimDraw(CLAIM_DRAW_DTO)

            result shouldBeEqualTo ClaimDrawResult.Error.NotAllowed

        }
    }
})