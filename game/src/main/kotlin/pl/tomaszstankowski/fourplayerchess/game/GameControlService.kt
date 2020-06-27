package pl.tomaszstankowski.fourplayerchess.game

import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionOperations
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.util.IdGenerator
import org.springframework.util.JdkIdGenerator
import org.springframework.util.SimpleIdGenerator
import pl.tomaszstankowski.fourplayerchess.engine.*
import pl.tomaszstankowski.fourplayerchess.engine.MoveClaim.PromotionMoveClaim
import pl.tomaszstankowski.fourplayerchess.engine.MoveClaim.RegularMoveClaim
import pl.tomaszstankowski.fourplayerchess.engine.PieceType.*
import pl.tomaszstankowski.fourplayerchess.game.data.*
import java.time.Clock
import java.util.*
import javax.sql.DataSource
import kotlin.random.Random

class GameControlService internal constructor(private val gameFactory: GameFactory,
                                              private val random: Random,
                                              private val transactionOperations: TransactionOperations,
                                              private val gameRepository: GameRepository,
                                              private val gamePlayerRepository: GamePlayerRepository,
                                              private val engineInstanceStore: EngineInstanceStore,
                                              private val gameMessageBroker: GameMessageBroker) {

    companion object {
        fun create(clock: Clock,
                   dataSource: DataSource,
                   transactionManager: PlatformTransactionManager,
                   messageSendingOperations: SimpMessageSendingOperations) =
                create(
                        idGenerator = JdkIdGenerator(),
                        clock = clock,
                        random = Random.Default,
                        transactionOperations = TransactionTemplate(transactionManager),
                        gameRepository = JdbcGameRepository(dataSource),
                        gamePlayerRepository = JdbcGamePlayerRepository(dataSource),
                        messageSendingOperations = messageSendingOperations
                )

        fun create(clock: Clock,
                   random: Random,
                   messageSendingOperations: SimpMessageSendingOperations): GameControlService {
            val dataSource = InMemoryDataSource()
            return create(
                    idGenerator = SimpleIdGenerator(),
                    clock = clock,
                    random = random,
                    transactionOperations = TransactionOperations.withoutTransaction(),
                    gameRepository = InMemoryGameRepository(dataSource),
                    gamePlayerRepository = InMemoryGamePlayerRepository(dataSource),
                    messageSendingOperations = messageSendingOperations
            )
        }

        private fun create(idGenerator: IdGenerator,
                           clock: Clock,
                           random: Random,
                           transactionOperations: TransactionOperations,
                           gameRepository: GameRepository,
                           gamePlayerRepository: GamePlayerRepository,
                           messageSendingOperations: SimpMessageSendingOperations): GameControlService {
            return GameControlService(
                    gameFactory = GameFactory(idGenerator, clock),
                    random = random,
                    transactionOperations = transactionOperations,
                    gameRepository = gameRepository,
                    gamePlayerRepository = gamePlayerRepository,
                    engineInstanceStore = EngineInstanceStore(),
                    gameMessageBroker = GameMessageBroker(messageSendingOperations)
            )
        }
    }

    fun createGame(dto: CreateGameDto): GameDto {
        val game = gameFactory.createGame()
        val gamePlayers = dto.playersIds
                .shuffled(random)
                .mapIndexed { index, id ->
                    GamePlayer(
                            gameId = game.id,
                            playerId = id,
                            color = Color.values()[index]
                    )
                }
        transactionOperations.executeWithoutResult {
            gameRepository.insert(game)
            gamePlayers.forEach { gamePlayerRepository.insert(it) }
        }

        return game.toDto()
    }

    fun commitGame(gameId: UUID): Boolean {
        val game = gameRepository.findById(gameId)
                ?.takeIf { !it.isCommitted }
                ?: return false
        val updatedGame = game.copy(isCommitted = true)
        gameRepository.update(updatedGame)
        val gameState = State.starting()
        val engine = Engine(gameState)
        engineInstanceStore.put(game.id, engine)
        return true
    }

    fun getGame(gameId: UUID): GameDto? =
            getCommittedGame(gameId)?.toDto()

    fun getPlayersOfTheGame(gameId: UUID): List<GamePlayerDto>? {
        getCommittedGame(gameId) ?: return null
        return gamePlayerRepository.findByGameId(gameId)
                .sortedBy { it.color.ordinal }
                .map { it.toDto() }
    }

    fun getActiveGamesForPlayer(playerId: UUID): List<GameDto> {
        return gameRepository.findByPlayerId(playerId)
                .filter { it.isActive }
                .map { it.toDto() }
    }

    fun getGameState(gameId: UUID): GetGameStateResult {
        val game = getCommittedGame(gameId) ?: return GetGameStateResult.GameNotFound(gameId)
        if (!game.isActive) {
            return GetGameStateResult.GameNotActive
        }
        val engineInstance = getEngineInstance(gameId)
        val gameState = engineInstance.getGameStateDto()
        return GetGameStateResult.Success(gameState)
    }

    fun makeMove(dto: MakeMoveDto): MakeMoveResult {
        val gameId = dto.gameId
        val game = getCommittedGame(gameId) ?: return MakeMoveResult.Error.GameNotFound(gameId)
        if (!game.isActive) {
            return MakeMoveResult.Error.GameNotActive
        }
        val engineInstance = getEngineInstance(gameId)
        val gamePlayers = gamePlayerRepository.findByGameId(gameId)
        val isRequestingPlayerInTheGame = gamePlayers.any { it.playerId == dto.playerId }
        if (!isRequestingPlayerInTheGame) {
            return MakeMoveResult.Error.PlayerIsNotInTheGame
        }

        val playerWithNextTurn = gamePlayers.first { it.color == engineInstance.state.nextMoveColor }
        val hasNextTurn = playerWithNextTurn.playerId == dto.playerId
        if (!hasNextTurn) {
            return MakeMoveResult.Error.NoPlayerTurn(playerWithNextTurnId = playerWithNextTurn.playerId)
        }

        val from = Position.parseOrNull(dto.from) ?: return MakeMoveResult.Error.InvalidPosition(dto.from)
        val to = Position.parseOrNull(dto.to) ?: return MakeMoveResult.Error.InvalidPosition(dto.to)
        val move = Move(from = from, to = to)

        val moveClaim = (
                if (dto.promotionPiece == null)
                    RegularMoveClaim(move)
                else
                    dto.promotionPiece
                            .toPieceTypeOrNull()
                            ?.let { PromotionMoveClaim.getOrNull(move, it) }
                )
                ?: return MakeMoveResult.Error.IllegalPromotionPiece(
                        legalPieces = setOf(Queen, Rook, Bishop, Knight)
                                .map { it.toJsonStr() },
                        given = dto.promotionPiece!!
                )
        val isMoveValid = engineInstanceStore.synchronized(gameId) { engine -> engine.makeMove(moveClaim) }
                ?: throw IllegalStateException()
        if (!isMoveValid) {
            return MakeMoveResult.Error.IllegalMove
        }
        if (engineInstance.isGameOver) {
            val finishedGame = game.copy(isFinished = true)
            gameRepository.update(finishedGame)
            engineInstanceStore.remove(gameId)
        }
        val newGameState = engineInstance.getGameStateDto()
        gameMessageBroker.sendMoveMadeMessage(gameId, newGameState, move.toDto())
        return MakeMoveResult.Success(newGameState)
    }

    fun submitResignation(dto: SubmitResignationDto): SubmitResignationResult {
        val gameId = dto.gameId
        val game = getCommittedGame(gameId) ?: return SubmitResignationResult.Error.GameNotFound(gameId)
        if (!game.isActive) {
            return SubmitResignationResult.Error.GameNotActive
        }
        val gamePlayers = gamePlayerRepository.findByGameId(gameId)
        val requestingPlayerColor = gamePlayers.firstOrNull { it.playerId == dto.requestingPlayerId }?.color
                ?: return SubmitResignationResult.Error.PlayerNotInTheGame
        val isResignationAllowed = engineInstanceStore.synchronized(gameId) { engine ->
            engine.submitResignation(requestingPlayerColor)
        } ?: throw IllegalStateException()
        if (!isResignationAllowed) {
            return SubmitResignationResult.Error.NotAllowed
        }
        val engineInstance = getEngineInstance(gameId)
        if (engineInstance.isGameOver) {
            val finishedGame = game.copy(isFinished = true)
            gameRepository.update(finishedGame)
            engineInstanceStore.remove(gameId)
        }
        val newGameState = engineInstance.getGameStateDto()
        gameMessageBroker.sendResignationSubmittedMessage(gameId, newGameState, requestingPlayerColor)
        return SubmitResignationResult.Success(newGameState, requestingPlayerColor.toJsonStr())
    }

    fun cancelAllActiveGames() {
        transactionOperations.executeWithoutResult {
            gameRepository.findByIsCommittedIsTrueAndIsCancelledIsFalseAndIsFinishedIsFalse()
                    .forEach { game ->
                        val cancelledGame = game.copy(isCancelled = true)
                        gameRepository.update(cancelledGame)
                        engineInstanceStore.remove(game.id)
                    }
        }
    }

    private fun getCommittedGame(id: UUID) =
            gameRepository.findById(id)
                    ?.takeIf { it.isCommitted }

    private fun Engine.getGameStateDto() =
            GameStateDto.create(
                    state = state,
                    stateFeatures = stateFeatures,
                    legalMoves = legalMoves,
                    isFinished = isGameOver,
                    winningColor = winningColor
            )

    private fun getEngineInstance(gameId: UUID) =
            engineInstanceStore.get(gameId) ?: throw IllegalStateException("No engine instance found for game $gameId")
}