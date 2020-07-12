package pl.tomaszstankowski.fourplayerchess.matchmaking

import org.amshove.kluent.mock
import org.amshove.kluent.shouldBeEqualTo
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import pl.tomaszstankowski.fourplayerchess.common.validation.ValidationError
import pl.tomaszstankowski.fourplayerchess.matchmaking.Fixture.ADD_RANDOM_BOT_DTO
import pl.tomaszstankowski.fourplayerchess.matchmaking.Fixture.CREATE_LOBBY_DTO
import pl.tomaszstankowski.fourplayerchess.matchmaking.Fixture.DELETE_LOBBY_DTO
import pl.tomaszstankowski.fourplayerchess.matchmaking.Fixture.JOIN_LOBBY_DTO
import pl.tomaszstankowski.fourplayerchess.matchmaking.Fixture.LEAVE_LOBBY_DTO
import pl.tomaszstankowski.fourplayerchess.matchmaking.Fixture.NEW_GAME_ID
import pl.tomaszstankowski.fourplayerchess.matchmaking.Fixture.NOW
import pl.tomaszstankowski.fourplayerchess.matchmaking.Fixture.REMOVE_RANDOM_BOT_DTO
import pl.tomaszstankowski.fourplayerchess.matchmaking.Fixture.START_GAME_DTO
import pl.tomaszstankowski.fourplayerchess.matchmaking.Fixture.UPDATE_LOBBY_DTO
import java.time.Clock
import java.time.ZoneId
import java.util.*

class MatchmakingTest : Spek({

    fun createServiceForTesting() =
            MatchmakingService.create(
                    clock = Clock.fixed(NOW, ZoneId.of("UTC")),
                    createGameUseCase = TestCreateGameUseCase(NEW_GAME_ID),
                    simpMessageSendingOperations = mock()
            )

    describe("creating lobby") {

        it("should return error when lobby details are not valid") {
            val service = createServiceForTesting()
            val dto = CREATE_LOBBY_DTO.copy(
                    lobbyEditableDetails = LobbyEditableDetails(name = "Pok")
            )

            val result = service.createLobby(dto)

            result shouldBeEqualTo CreateLobbyResult.LobbyDetailsNotValid(
                    errors = setOf(
                            ValidationError("name", "Size must be between 5 and 50")
                    )
            )
        }

        it("should return created lobby") {
            val service = createServiceForTesting()
            val dto = CREATE_LOBBY_DTO.copy(
                    lobbyEditableDetails = LobbyEditableDetails(name = "Pokój testowy!"),
                    requestingPlayerId = UUID.fromString("085076e1-c697-4a1a-aa42-c0055e0749a3")
            )
            val result = service.createLobby(dto)

            result shouldBeEqualTo CreateLobbyResult.Success(
                    LobbyDto(
                            id = UUID.fromString("00000000-0000-0000-0000-000000000001"),
                            name = "Pokój testowy!",
                            createdAt = NOW,
                            ownerId = UUID.fromString("085076e1-c697-4a1a-aa42-c0055e0749a3")
                    )
            )
        }

        it("should persist created lobby") {
            val service = createServiceForTesting()
            val dto = CREATE_LOBBY_DTO.copy(
                    lobbyEditableDetails = LobbyEditableDetails(name = "Pokój testowy!"),
                    requestingPlayerId = UUID.fromString("b4c7e97c-3991-40e4-b632-62fcbd1cb0ce")
            )

            service.createLobby(dto)
            val lobby = service.getLobby(UUID.fromString("00000000-0000-0000-0000-000000000001"))

            lobby shouldBeEqualTo
                    LobbyDto(
                            id = UUID.fromString("00000000-0000-0000-0000-000000000001"),
                            name = "Pokój testowy!",
                            createdAt = NOW,
                            ownerId = UUID.fromString("b4c7e97c-3991-40e4-b632-62fcbd1cb0ce")
                    )
        }

        it("should add owner membership") {
            val service = createServiceForTesting()

            service.createLobby(
                    CREATE_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("d3f3f0e7-ea2d-4a3e-9942-774138e588be"))
            )
            val memberships = service.getPlayersInLobby(UUID.fromString("00000000-0000-0000-0000-000000000001"))

            memberships shouldBeEqualTo listOf(
                    LobbyMembershipDto(
                            userId = UUID.fromString("d3f3f0e7-ea2d-4a3e-9942-774138e588be"),
                            joinedAt = NOW,
                            type = "human",
                            botId = null
                    )
            )
        }
    }

    describe("updating lobby") {

        it("should return error when lobby not found") {
            val service = createServiceForTesting()
            service.createLobby(CREATE_LOBBY_DTO)
            val dto = UPDATE_LOBBY_DTO.copy(lobbyId = UUID.fromString("8c3d359b-6f40-44b1-8d8c-e69958c8ac02"))

            val result = service.updateLobby(dto)

            result shouldBeEqualTo UpdateLobbyResult.LobbyNotFound(
                    lobbyId = UUID.fromString("8c3d359b-6f40-44b1-8d8c-e69958c8ac02")
            )
        }

        it("should return error when lobby details are invalid") {
            val service = createServiceForTesting()
            service.createLobby(CREATE_LOBBY_DTO)

            val result = service.updateLobby(
                    UPDATE_LOBBY_DTO.copy(
                            lobbyEditableDetails = LobbyEditableDetails(name = "  ")
                    )
            )

            result shouldBeEqualTo UpdateLobbyResult.LobbyDetailsNotValid(
                    errors = setOf(
                            ValidationError("name", "Must not be blank"),
                            ValidationError("name", "Size must be between 5 and 50")
                    )
            )
        }

        it("should return error when requesting player is not an owner of the lobby") {
            val service = createServiceForTesting()
            service.createLobby(
                    CREATE_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("8a5c65f4-7d0d-4c28-93e1-e396e0d0c3ef"))
            )

            val result = service.updateLobby(
                    UPDATE_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("c1b508b6-fa46-417a-896f-09ba93ab8d26"))
            )

            result shouldBeEqualTo UpdateLobbyResult.RequestingPlayerNotAnOwner
        }

        it("should return updated lobby") {
            val service = createServiceForTesting()
            service.createLobby(
                    CREATE_LOBBY_DTO.copy(
                            lobbyEditableDetails = LobbyEditableDetails(name = "Pokój testowy")
                    )
            )

            val result = service.updateLobby(
                    UPDATE_LOBBY_DTO.copy(
                            lobbyEditableDetails = LobbyEditableDetails(name = "Pokój produkcyjny")
                    )
            )

            result shouldBeEqualTo UpdateLobbyResult.Success(
                    LobbyDto(
                            id = UUID.fromString("00000000-0000-0000-0000-000000000001"),
                            name = "Pokój produkcyjny",
                            createdAt = NOW,
                            ownerId = CREATE_LOBBY_DTO.requestingPlayerId
                    )
            )
        }

        it("should persist updated lobby") {
            val service = createServiceForTesting()
            service.createLobby(
                    CREATE_LOBBY_DTO.copy(
                            lobbyEditableDetails = LobbyEditableDetails(name = "Pokój testowy")
                    )
            )

            service.updateLobby(
                    UPDATE_LOBBY_DTO.copy(
                            lobbyEditableDetails = LobbyEditableDetails(name = "Pokój produkcyjny")
                    )
            )
            val lobby = service.getLobby(
                    UUID.fromString("00000000-0000-0000-0000-000000000001")
            )

            lobby shouldBeEqualTo LobbyDto(
                    id = UUID.fromString("00000000-0000-0000-0000-000000000001"),
                    name = "Pokój produkcyjny",
                    createdAt = NOW,
                    ownerId = CREATE_LOBBY_DTO.requestingPlayerId
            )
        }
    }

    describe("deleting lobby") {

        it("should return error when lobby not found") {
            val service = createServiceForTesting()
            service.createLobby(CREATE_LOBBY_DTO)

            val result = service.deleteLobby(
                    DELETE_LOBBY_DTO.copy(lobbyId = UUID.fromString("2c985725-0600-4913-a33e-4cc3a9766b2e"))
            )

            result shouldBeEqualTo DeleteLobbyResult.LobbyNotFound(UUID.fromString("2c985725-0600-4913-a33e-4cc3a9766b2e"))
        }

        it("should return error when requesting player is not member of the lobby") {
            val service = createServiceForTesting()
            service.createLobby(
                    CREATE_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("592b327a-6309-439b-8c3f-de0893539390"))
            )

            val result = service.deleteLobby(
                    DELETE_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("3542cc04-5cae-4400-a708-21660a9548aa"))
            )

            result shouldBeEqualTo DeleteLobbyResult.RequestingPlayerNotAnOwner
        }

        it("should return error when requesting player is not owner of the lobby") {
            val service = createServiceForTesting()
            service.createLobby(
                    CREATE_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("255e1317-0628-4d7d-859c-108906635ed6"))
            )
            service.joinLobby(
                    JOIN_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("83d888a7-6531-45e3-aa90-02729239d064"))
            )

            val result = service.deleteLobby(
                    DELETE_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("f90fd0ec-1ec0-40cb-bf5b-be6fc41e8ad6"))
            )

            result shouldBeEqualTo DeleteLobbyResult.RequestingPlayerNotAnOwner
        }

        it("should return deleted result") {
            val service = createServiceForTesting()
            service.createLobby(CREATE_LOBBY_DTO)

            val result = service.deleteLobby(DELETE_LOBBY_DTO)

            result shouldBeEqualTo DeleteLobbyResult.Deleted
        }

        it("should remove lobby from db") {
            val service = createServiceForTesting()
            service.createLobby(CREATE_LOBBY_DTO)

            service.deleteLobby(DELETE_LOBBY_DTO)

            val lobby = service.getLobby(UUID.fromString("00000000-0000-0000-0000-000000000001"))

            lobby shouldBeEqualTo null
        }

        it("should remove lobby after some players joined it") {
            val service = createServiceForTesting()
            service.createLobby(
                    CREATE_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("84a76df7-532c-46c6-b951-c680dddd5b30"))
            )
            service.joinLobby(
                    JOIN_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("641c5f76-1225-4642-a9db-80f18dc3125e"))
            )

            val result = service.deleteLobby(
                    DELETE_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("84a76df7-532c-46c6-b951-c680dddd5b30"))
            )

            result shouldBeEqualTo DeleteLobbyResult.Deleted
        }

        it("should remove memberships") {
            val service = createServiceForTesting()
            service.createLobby(
                    CREATE_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("84a76df7-532c-46c6-b951-c680dddd5b30"))
            )
            service.joinLobby(
                    JOIN_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("641c5f76-1225-4642-a9db-80f18dc3125e"))
            )

            service.deleteLobby(
                    DELETE_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("84a76df7-532c-46c6-b951-c680dddd5b30"))
            )
            val lobbiesOfCurrentPlayer = service.getActiveLobbiesOfAPLayer(UUID.fromString("641c5f76-1225-4642-a9db-80f18dc3125e"))

            lobbiesOfCurrentPlayer shouldBeEqualTo emptyList()
        }
    }

    describe("joining lobby") {

        it("should return error if lobby not found") {
            val service = createServiceForTesting()
            service.createLobby(CREATE_LOBBY_DTO)

            val result = service.joinLobby(
                    JOIN_LOBBY_DTO.copy(lobbyId = UUID.fromString("fd489d64-a1e7-4375-bd2c-3b1d42b0470f"))
            )

            result shouldBeEqualTo JoinLobbyResult.LobbyNotFound(UUID.fromString("fd489d64-a1e7-4375-bd2c-3b1d42b0470f"))
        }

        it("should return error if player is already in the same lobby") {
            val service = createServiceForTesting()
            service.createLobby(CREATE_LOBBY_DTO)
            service.joinLobby(JOIN_LOBBY_DTO)

            val result = service.joinLobby(JOIN_LOBBY_DTO)

            result shouldBeEqualTo JoinLobbyResult.PlayerAlreadyInLobby
        }

        it("should return error if player is owner of the lobby") {
            val service = createServiceForTesting()
            service.createLobby(
                    CREATE_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("6bb4e4bf-a59f-4bbf-b625-0b4d470f3eda"))
            )

            val result = service.joinLobby(
                    JOIN_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("6bb4e4bf-a59f-4bbf-b625-0b4d470f3eda"))
            )

            result shouldBeEqualTo JoinLobbyResult.PlayerAlreadyInLobby
        }

        it("should return error if lobby is full") {
            val service = createServiceForTesting()
            service.createLobby(
                    CREATE_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("6bb4e4bf-a59f-4bbf-b625-0b4d470f3eda"))
            )
            service.joinLobby(
                    JOIN_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("fcd7ab99-d75f-47e7-a6e1-52aac4c851a7"))
            )
            service.joinLobby(
                    JOIN_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("46e4ff66-b034-4701-9126-7724c1ecb9a5"))
            )
            service.joinLobby(
                    JOIN_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("1936636d-80c1-4509-8338-1b25591b9d20"))
            )

            val result = service.joinLobby(
                    JOIN_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("04beb701-65af-4977-af21-bddb8b66860e"))
            )

            result shouldBeEqualTo JoinLobbyResult.LobbyIsFull
        }

        it("should return membership") {
            val service = createServiceForTesting()
            service.createLobby(CREATE_LOBBY_DTO)

            val result = service.joinLobby(
                    JOIN_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("94c866cd-299f-4894-a1bc-a3a2e4b243dc"))
            )

            result shouldBeEqualTo JoinLobbyResult.Success(
                    LobbyMembershipDto(
                            userId = UUID.fromString("94c866cd-299f-4894-a1bc-a3a2e4b243dc"),
                            joinedAt = NOW,
                            type = "human",
                            botId = null
                    )
            )
        }

        it("should persist membership") {
            val service = createServiceForTesting()
            service.createLobby(
                    CREATE_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("d4542f81-4659-4d42-b01f-095542db705e"))
            )

            service.joinLobby(
                    JOIN_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("94c866cd-299f-4894-a1bc-a3a2e4b243dc"))
            )
            val memberships = service.getPlayersInLobby(UUID.fromString("00000000-0000-0000-0000-000000000001"))

            memberships shouldBeEqualTo listOf(
                    LobbyMembershipDto(
                            userId = UUID.fromString("d4542f81-4659-4d42-b01f-095542db705e"),
                            joinedAt = NOW,
                            type = "human",
                            botId = null
                    ),
                    LobbyMembershipDto(
                            userId = UUID.fromString("94c866cd-299f-4894-a1bc-a3a2e4b243dc"),
                            joinedAt = NOW,
                            type = "human",
                            botId = null
                    )
            )
        }
    }

    describe("leaving lobby") {

        it("should return error when lobby not found") {
            val service = createServiceForTesting()
            service.createLobby(CREATE_LOBBY_DTO)

            val result = service.leaveLobby(
                    LEAVE_LOBBY_DTO.copy(lobbyId = UUID.fromString("1d81af2c-b8d2-49db-a341-69ab6c69d8ae"))
            )

            result shouldBeEqualTo LeaveLobbyResult.LobbyNotFound(UUID.fromString("1d81af2c-b8d2-49db-a341-69ab6c69d8ae"))
        }

        it("should return error when requesting player is owner") {
            val service = createServiceForTesting()
            service.createLobby(
                    CREATE_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("5848c22d-fcef-4d2f-87c8-913931b5412e"))
            )

            val result = service.leaveLobby(
                    LEAVE_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("5848c22d-fcef-4d2f-87c8-913931b5412e"))
            )

            result shouldBeEqualTo LeaveLobbyResult.OwnerMemberMustNotLeaveLobby
        }

        it("should return error when requesting player is not a member") {
            val service = createServiceForTesting()
            service.createLobby(
                    CREATE_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("5848c22d-fcef-4d2f-87c8-913931b5412e"))
            )
            service.joinLobby(
                    JOIN_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("b0e314b3-3fa0-4b36-8ea8-9f694af1c35c"))
            )
            service.leaveLobby(
                    LEAVE_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("b0e314b3-3fa0-4b36-8ea8-9f694af1c35c"))
            )

            val result = service.leaveLobby(
                    LEAVE_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("b0e314b3-3fa0-4b36-8ea8-9f694af1c35c"))
            )

            result shouldBeEqualTo LeaveLobbyResult.RequestingPlayerNotAMember
        }

        it("should return left") {
            val service = createServiceForTesting()
            service.createLobby(
                    CREATE_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("5848c22d-fcef-4d2f-87c8-913931b5412e"))
            )
            service.joinLobby(
                    JOIN_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("b0e314b3-3fa0-4b36-8ea8-9f694af1c35c"))
            )

            val result = service.leaveLobby(
                    LEAVE_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("b0e314b3-3fa0-4b36-8ea8-9f694af1c35c"))
            )

            result shouldBeEqualTo LeaveLobbyResult.Left
        }

        it("should remove membership") {
            val service = createServiceForTesting()
            service.createLobby(
                    CREATE_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("5848c22d-fcef-4d2f-87c8-913931b5412e"))
            )
            service.joinLobby(
                    JOIN_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("b0e314b3-3fa0-4b36-8ea8-9f694af1c35c"))
            )

            service.leaveLobby(
                    LEAVE_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("b0e314b3-3fa0-4b36-8ea8-9f694af1c35c"))
            )
            val playersInLobby = service.getPlayersInLobby(UUID.fromString("00000000-0000-0000-0000-000000000001"))

            playersInLobby shouldBeEqualTo listOf(
                    LobbyMembershipDto(
                            userId = UUID.fromString("5848c22d-fcef-4d2f-87c8-913931b5412e"),
                            joinedAt = NOW,
                            type = "human",
                            botId = null
                    )
            )
        }
    }

    describe("adding random bot to lobby") {

        it("should return error when lobby not found") {
            val service = createServiceForTesting()
            service.createLobby(CREATE_LOBBY_DTO)

            val result = service.addRandomBot(
                    ADD_RANDOM_BOT_DTO.copy(lobbyId = UUID.fromString("1c4462d2-ba32-446e-947b-012377e92ba4"))
            )

            result shouldBeEqualTo AddRandomBotResult.LobbyNotFound(UUID.fromString("1c4462d2-ba32-446e-947b-012377e92ba4"))
        }

        it("should return error when requesting player is not owner of the lobby") {
            val service = createServiceForTesting()
            service.createLobby(
                    CREATE_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("8c4b953b-8d3a-4194-b4e4-461b9de84f65"))
            )

            val result = service.addRandomBot(
                    ADD_RANDOM_BOT_DTO.copy(requestingPlayerId = UUID.fromString("10b5793f-5f63-4596-bf08-264d6c862110"))
            )

            result shouldBeEqualTo AddRandomBotResult.RequestingPlayerNotAnOwner
        }

        it("should return error when lobby is full") {
            val service = createServiceForTesting()
            service.createLobby(CREATE_LOBBY_DTO)
            service.joinLobby(
                    JOIN_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("e1b6ced7-bbb1-4753-87d0-33d71527501f"))
            )
            service.joinLobby(
                    JOIN_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("41ae1a74-4ce0-4af9-806e-088b303693d7"))
            )
            service.addRandomBot(ADD_RANDOM_BOT_DTO)

            val result = service.addRandomBot(ADD_RANDOM_BOT_DTO)

            result shouldBeEqualTo AddRandomBotResult.LobbyIsFull
        }

        it("should return bot membership on success") {
            val service = createServiceForTesting()
            service.createLobby(CREATE_LOBBY_DTO)
            service.addRandomBot(ADD_RANDOM_BOT_DTO)
            service.addRandomBot(ADD_RANDOM_BOT_DTO)

            val result = service.addRandomBot(ADD_RANDOM_BOT_DTO)

            result shouldBeEqualTo AddRandomBotResult.Success(
                    membership = LobbyMembershipDto(
                            type = "randomBot",
                            joinedAt = NOW,
                            userId = null,
                            botId = UUID.fromString("00000000-0000-0000-0000-000000000004")
                    )
            )
        }

        it("should save bot in db on success") {
            val service = createServiceForTesting()
            service.createLobby(
                    CREATE_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("c81d7e84-9126-4b73-9276-a899aba56652"))
            )

            service.addRandomBot(
                    ADD_RANDOM_BOT_DTO.copy(requestingPlayerId = UUID.fromString("c81d7e84-9126-4b73-9276-a899aba56652"))
            )
            val players = service.getPlayersInLobby(UUID.fromString("00000000-0000-0000-0000-000000000001"))

            players shouldBeEqualTo listOf(
                    LobbyMembershipDto(
                            type = "human",
                            joinedAt = NOW,
                            userId = UUID.fromString("c81d7e84-9126-4b73-9276-a899aba56652"),
                            botId = null
                    ),
                    LobbyMembershipDto(
                            type = "randomBot",
                            joinedAt = NOW,
                            userId = null,
                            botId = UUID.fromString("00000000-0000-0000-0000-000000000002")
                    )
            )
        }
    }

    describe("removing random bot") {

        it("should return error when lobby not found") {
            val service = createServiceForTesting()

            val result = service.removeRandomBot(
                    REMOVE_RANDOM_BOT_DTO.copy(lobbyId = UUID.fromString("538354b1-2c51-4635-bfcb-5acd8d4ab168"))
            )

            result shouldBeEqualTo RemoveRandomBotResult.LobbyNotFound(UUID.fromString("538354b1-2c51-4635-bfcb-5acd8d4ab168"))
        }

        it("should return error when bot not found") {
            val service = createServiceForTesting()
            service.createLobby(CREATE_LOBBY_DTO)
            service.addRandomBot(ADD_RANDOM_BOT_DTO)

            val result = service.removeRandomBot(
                    REMOVE_RANDOM_BOT_DTO.copy(botId = UUID.fromString("f0d3d9f6-b29e-415c-aaa9-0876d0141e57"))
            )

            result shouldBeEqualTo RemoveRandomBotResult.BotNotFound(UUID.fromString("f0d3d9f6-b29e-415c-aaa9-0876d0141e57"))
        }

        it("should return error when requesting player is not owner of the lobby") {
            val service = createServiceForTesting()
            service.createLobby(
                    CREATE_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("f4fc0a84-b736-4810-b0a1-343c95605e3a"))
            )
            service.addRandomBot(ADD_RANDOM_BOT_DTO)

            val result = service.removeRandomBot(
                    REMOVE_RANDOM_BOT_DTO.copy(requestingPlayerId = UUID.fromString("31ea8194-adb1-46b3-b293-b413a1b1d6ef"))
            )

            result shouldBeEqualTo RemoveRandomBotResult.RequestingPlayerNotAnOwner
        }

        it("should return success when removed") {
            val service = createServiceForTesting()
            service.createLobby(CREATE_LOBBY_DTO)
            service.addRandomBot(ADD_RANDOM_BOT_DTO)

            val result = service.removeRandomBot(
                    REMOVE_RANDOM_BOT_DTO.copy(botId = UUID.fromString("00000000-0000-0000-0000-000000000002"))
            )

            result shouldBeEqualTo RemoveRandomBotResult.Removed
        }

        it("should remove bot from db") {
            val service = createServiceForTesting()
            service.createLobby(
                    CREATE_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("da732570-ae6d-473f-9f84-1b3b27a2eb7f"))
            )
            service.addRandomBot(
                    ADD_RANDOM_BOT_DTO.copy(requestingPlayerId = UUID.fromString("da732570-ae6d-473f-9f84-1b3b27a2eb7f"))
            )

            service.removeRandomBot(
                    REMOVE_RANDOM_BOT_DTO.copy(
                            botId = UUID.fromString("00000000-0000-0000-0000-000000000002"),
                            requestingPlayerId = UUID.fromString("da732570-ae6d-473f-9f84-1b3b27a2eb7f")
                    )
            )
            val players = service.getPlayersInLobby(UUID.fromString("00000000-0000-0000-0000-000000000001"))

            players shouldBeEqualTo listOf(
                    LobbyMembershipDto(
                            type = "human",
                            joinedAt = NOW,
                            userId = UUID.fromString("da732570-ae6d-473f-9f84-1b3b27a2eb7f"),
                            botId = null
                    )
            )
        }
    }

    describe("starting game") {

        it("should return error if lobby not found") {
            val service = createServiceForTesting()

            val result = service.startGame(
                    START_GAME_DTO.copy(lobbyId = UUID.fromString("800d8d75-e202-40e3-9d39-f44abdd6c4d4"))
            )

            result shouldBeEqualTo StartGameResult.LobbyNotFound(
                    UUID.fromString("800d8d75-e202-40e3-9d39-f44abdd6c4d4")
            )
        }

        it("should return error if lobby does not have enough players") {
            val service = createServiceForTesting()
            service.createLobby(
                    CREATE_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("7e96c0c5-f2d3-487e-a98e-ba1cbb30032d"))
            )
            service.joinLobby(
                    JOIN_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("ce87bf42-863b-4399-8395-9bb88e41d9eb"))
            )
            service.joinLobby(
                    JOIN_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("df0c3aec-5015-4dcc-87a7-3c56555ba685"))
            )

            val result = service.startGame(
                    START_GAME_DTO.copy(requestingPlayerId = UUID.fromString("7e96c0c5-f2d3-487e-a98e-ba1cbb30032d"))
            )

            result shouldBeEqualTo StartGameResult.NotEnoughPlayers(currentPlayersCount = 3)
        }

        it("should return error if requesting player is not an owner") {
            val service = createServiceForTesting()
            service.createLobby(
                    CREATE_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("7e96c0c5-f2d3-487e-a98e-ba1cbb30032d"))
            )
            service.joinLobby(
                    JOIN_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("ce87bf42-863b-4399-8395-9bb88e41d9eb"))
            )
            service.joinLobby(
                    JOIN_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("df0c3aec-5015-4dcc-87a7-3c56555ba685"))
            )
            service.joinLobby(
                    JOIN_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("00a4c233-81ed-42e4-a7ca-77da6074f6a7"))
            )

            val result = service.startGame(
                    START_GAME_DTO.copy(requestingPlayerId = UUID.fromString("df0c3aec-5015-4dcc-87a7-3c56555ba685"))
            )

            result shouldBeEqualTo StartGameResult.RequestingPlayerNotAnOwner
        }

        it("should return new game") {
            val service = createServiceForTesting()
            service.createLobby(
                    CREATE_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("7e96c0c5-f2d3-487e-a98e-ba1cbb30032d"))
            )
            service.joinLobby(
                    JOIN_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("ce87bf42-863b-4399-8395-9bb88e41d9eb"))
            )
            service.joinLobby(
                    JOIN_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("df0c3aec-5015-4dcc-87a7-3c56555ba685"))
            )
            service.joinLobby(
                    JOIN_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("00a4c233-81ed-42e4-a7ca-77da6074f6a7"))
            )

            val result = service.startGame(
                    START_GAME_DTO.copy(requestingPlayerId = UUID.fromString("7e96c0c5-f2d3-487e-a98e-ba1cbb30032d"))
            )

            result shouldBeEqualTo StartGameResult.Success(GameDto(NEW_GAME_ID))
        }

        it("should return new game when bots added to lobby") {
            val service = createServiceForTesting()
            service.createLobby(
                    CREATE_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("7e96c0c5-f2d3-487e-a98e-ba1cbb30032d"))
            )
            service.joinLobby(
                    JOIN_LOBBY_DTO.copy(requestingPlayerId = UUID.fromString("ce87bf42-863b-4399-8395-9bb88e41d9eb"))
            )
            service.addRandomBot(
                    ADD_RANDOM_BOT_DTO.copy(requestingPlayerId = UUID.fromString("7e96c0c5-f2d3-487e-a98e-ba1cbb30032d"))
            )
            service.addRandomBot(
                    ADD_RANDOM_BOT_DTO.copy(requestingPlayerId = UUID.fromString("7e96c0c5-f2d3-487e-a98e-ba1cbb30032d"))
            )

            val result = service.startGame(
                    START_GAME_DTO.copy(requestingPlayerId = UUID.fromString("7e96c0c5-f2d3-487e-a98e-ba1cbb30032d"))
            )

            result shouldBeEqualTo StartGameResult.Success(GameDto(NEW_GAME_ID))
        }
    }
})