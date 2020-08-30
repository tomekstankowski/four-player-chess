package pl.tomaszstankowski.fourplayerchess.game

import com.google.common.util.concurrent.MoreExecutors
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionOperations
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.util.IdGenerator
import org.springframework.util.JdkIdGenerator
import org.springframework.util.SimpleIdGenerator
import pl.tomaszstankowski.fourplayerchess.engine.*
import pl.tomaszstankowski.fourplayerchess.engine.PieceType.*
import pl.tomaszstankowski.fourplayerchess.game.Player.HumanPlayer
import pl.tomaszstankowski.fourplayerchess.game.Player.RandomBot
import pl.tomaszstankowski.fourplayerchess.game.data.*
import java.time.Clock
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.sql.DataSource
import kotlin.random.Random

class GameControlService internal constructor(private val gameFactory: GameFactory,
                                              private val random: Random,
                                              private val transactionOperations: TransactionOperations,
                                              private val gameRepository: GameRepository,
                                              private val humanPlayerRepository: HumanPlayerRepository,
                                              private val randomBotRepository: RandomBotRepository,
                                              private val engineInstanceStore: EngineInstanceStore,
                                              private val gameMessageBroker: GameMessageBroker,
                                              private val botMoveExecutor: BotMoveExecutor) {

    companion object {
        fun create(clock: Clock = Clock.systemUTC(),
                   dataSource: DataSource,
                   transactionManager: PlatformTransactionManager,
                   botMoveExecutor: ExecutorService = Executors.newFixedThreadPool(8),
                   messageSendingOperations: SimpMessageSendingOperations) =
                create(
                        idGenerator = JdkIdGenerator(),
                        clock = clock,
                        random = Random.Default,
                        transactionOperations = TransactionTemplate(transactionManager),
                        gameRepository = JdbcGameRepository(dataSource),
                        humanPlayerRepository = JdbcHumanPlayerRepository(dataSource),
                        randomBotRepository = JdbcRandomBotRepository(dataSource),
                        messageSendingOperations = messageSendingOperations,
                        botMoveExecutor = botMoveExecutor
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
                    humanPlayerRepository = InMemoryHumanPlayerRepository(dataSource),
                    randomBotRepository = InMemoryRandomBotRepository(dataSource),
                    messageSendingOperations = messageSendingOperations,
                    botMoveExecutor = MoreExecutors.newDirectExecutorService()
            )
        }

        private fun create(idGenerator: IdGenerator,
                           clock: Clock,
                           random: Random,
                           transactionOperations: TransactionOperations,
                           gameRepository: GameRepository,
                           humanPlayerRepository: HumanPlayerRepository,
                           randomBotRepository: RandomBotRepository,
                           messageSendingOperations: SimpMessageSendingOperations,
                           botMoveExecutor: ExecutorService): GameControlService {
            val engineInstanceStore = EngineInstanceStore()
            return GameControlService(
                    gameFactory = GameFactory(idGenerator, clock),
                    random = random,
                    transactionOperations = transactionOperations,
                    gameRepository = gameRepository,
                    humanPlayerRepository = humanPlayerRepository,
                    randomBotRepository = randomBotRepository,
                    engineInstanceStore = engineInstanceStore,
                    gameMessageBroker = GameMessageBroker(messageSendingOperations),
                    botMoveExecutor = BotMoveExecutor(
                            executorService = botMoveExecutor,
                            gameMessageBroker = GameMessageBroker(messageSendingOperations),
                            engineInstanceStore = engineInstanceStore
                    )
            )
        }
    }

    fun createGame(dto: CreateGameDto): CreateGameResult {
        val game = gameFactory.createGame()
        val actualPlayersCount = dto.humanPlayersIds.size + dto.randomBotsCount
        if (actualPlayersCount > Color.values().size) {
            return CreateGameResult.Error.TooManyPlayers(actualPlayersCount)
        }
        if (actualPlayersCount < Color.values().size) {
            return CreateGameResult.Error.NotEnoughPlayers(actualPlayersCount)
        }
        val shuffledColors = Color.values().toList().shuffled(random)
        val humanPlayers = dto.humanPlayersIds.mapIndexed { index, id ->
            HumanPlayer(
                    gameId = game.id,
                    color = shuffledColors[index],
                    userId = id
            )
        }
        val randomBots = List(dto.randomBotsCount) { index ->
            RandomBot(
                    gameId = game.id,
                    color = shuffledColors[humanPlayers.size + index]
            )
        }
        transactionOperations.executeWithoutResult {
            gameRepository.insert(game)
            humanPlayers.forEach { humanPlayerRepository.insert(it) }
            randomBots.forEach { randomBotRepository.insert(it) }
        }

        return CreateGameResult.Success(game.toDto())
    }

    fun commitGame(gameId: UUID): Boolean {
        val game = gameRepository.findById(gameId)
                ?.takeIf { !it.isCommitted }
                ?: return false
        val randomBots = randomBotRepository.findByGameId(gameId)
        val updatedGame = game.copy(isCommitted = true)
        gameRepository.update(updatedGame)
        val engine = Engine(
                random = random,
                state = FenState.starting()
        )
        engineInstanceStore.put(game.id, engine)
        val state = engine.getUIState()
        botMoveExecutor.executeBotMoveIfHasNextMove(randomBots, gameId, state)
        return true
    }

    fun getGame(gameId: UUID): GameDto? =
            getCommittedGame(gameId)?.toDto()

    fun getPlayersOfTheGame(gameId: UUID): List<GamePlayerDto>? {
        getCommittedGame(gameId) ?: return null
        val humanPlayers = humanPlayerRepository.findByGameId(gameId).map { it as Player }
        val randomBots = randomBotRepository.findByGameId(gameId).map { it as Player }
        return (humanPlayers + randomBots)
                .sortedBy { it.color.ordinal }
                .map { it.toDto() }
    }

    fun getActiveGamesForPlayer(playerId: UUID): List<GameDto> {
        return gameRepository.findByHumanPlayerUserId(playerId)
                .filter { it.isActive }
                .map { it.toDto() }
    }

    fun getGameState(gameId: UUID): GetGameStateResult {
        val game = getCommittedGame(gameId) ?: return GetGameStateResult.GameNotFound(gameId)
        if (!game.isActive) {
            return GetGameStateResult.GameNotActive
        }
        val state = engineInstanceStore.synchronized(gameId) { engine -> engine.getUIState() }
                ?: throw IllegalStateException()
        val gameState = GameStateDto.of(state)
        return GetGameStateResult.Success(gameState)
    }

    fun makeMove(dto: MakeMoveDto): MakeMoveResult {
        val gameId = dto.gameId
        val game = getCommittedGame(gameId) ?: return MakeMoveResult.Error.GameNotFound(gameId)
        if (!game.isActive) {
            return MakeMoveResult.Error.GameNotActive
        }
        val state = engineInstanceStore.synchronized(gameId) { engine -> engine.getUIState() }
                ?: throw IllegalStateException()
        val humanPlayers = humanPlayerRepository.findByGameId(gameId)
        val randomBots = randomBotRepository.findByGameId(gameId)
        val requestingPlayer = humanPlayers.find { it.userId == dto.playerId }
                ?: return MakeMoveResult.Error.PlayerIsNotInTheGame
        val nextMoveColor = state.fenState.nextMoveColor
        if (requestingPlayer.color != nextMoveColor) {
            return MakeMoveResult.Error.PlayerDoesNotHaveNextMove(
                    nextMoveColor = nextMoveColor.toJsonStr(),
                    playerColor = requestingPlayer.color.toJsonStr()
            )
        }

        val from = Coordinates.parseOrNull(dto.from) ?: return MakeMoveResult.Error.InvalidPosition(dto.from)
        val to = Coordinates.parseOrNull(dto.to) ?: return MakeMoveResult.Error.InvalidPosition(dto.to)

        val move = (
                if (dto.promotionPiece == null)
                    RegularMove(from, to)
                else
                    dto.promotionPiece.toPromotionPieceTypeOrNull()
                            ?.let { Promotion(from, to, it) }
                )
                ?: return MakeMoveResult.Error.IllegalPromotionPiece(
                        legalPieces = setOf(Queen, Rook, Bishop, Knight)
                                .map { it.toJsonStr() },
                        given = dto.promotionPiece!!
                )
        val (isMoveMade, newState) = engineInstanceStore.synchronized(gameId) { engine ->
            val isMoveMade = engine.makeMove(move)
            isMoveMade to engine.getUIState()
        } ?: throw IllegalStateException()
        if (!isMoveMade) {
            return MakeMoveResult.Error.IllegalMove
        }
        if (newState.isGameOver) {
            handleGameFinished(game)
        }
        botMoveExecutor.executeBotMoveIfHasNextMove(randomBots, game.id, newState)
        val newGameStateDto = GameStateDto.of(newState)
        gameMessageBroker.sendMoveMadeMessage(game.id, newGameStateDto, move.toDto())
        return MakeMoveResult.Success(newGameStateDto)
    }

    fun submitResignation(dto: SubmitResignationDto): SubmitResignationResult {
        val gameId = dto.gameId
        val game = getCommittedGame(gameId) ?: return SubmitResignationResult.Error.GameNotFound(gameId)
        if (!game.isActive) {
            return SubmitResignationResult.Error.GameNotActive
        }
        val humanPlayers = humanPlayerRepository.findByGameId(gameId)
        val randomBots = randomBotRepository.findByGameId(gameId)
        val requestingPlayerColor = humanPlayers.firstOrNull { it.userId == dto.requestingPlayerId }?.color
                ?: return SubmitResignationResult.Error.PlayerNotInTheGame
        val (isResignationAllowed, state, prevState) = engineInstanceStore.synchronized(gameId) { engine ->
            val prevState = engine.getUIState()
            val isResignationAllowed = engine.submitResignation(requestingPlayerColor)
            val state = engine.getUIState()
            Triple(isResignationAllowed, state, prevState)
        } ?: throw IllegalStateException()
        if (!isResignationAllowed) {
            return SubmitResignationResult.Error.NotAllowed
        }
        if (state.isGameOver) {
            handleGameFinished(game)
        }
        if (prevState.fenState.nextMoveColor == requestingPlayerColor) {
            botMoveExecutor.executeBotMoveIfHasNextMove(randomBots, gameId, state)
        }

        val newGameStateDto = GameStateDto.of(state)
        gameMessageBroker.sendResignationSubmittedMessage(gameId, newGameStateDto, requestingPlayerColor.toJsonStr())
        return SubmitResignationResult.Success(newGameStateDto, requestingPlayerColor.toJsonStr())
    }

    fun claimDraw(dto: ClaimDrawDto): ClaimDrawResult {
        val gameId = dto.gameId
        val game = getCommittedGame(gameId) ?: return ClaimDrawResult.Error.GameNotFound(gameId)
        if (!game.isActive) {
            return ClaimDrawResult.Error.GameNotActive
        }
        val players = humanPlayerRepository.findByGameId(gameId)
        val requestingPlayerColor = players.firstOrNull { it.userId == dto.requestingPlayerId }?.color
                ?: return ClaimDrawResult.Error.PlayerNotInTheGame
        val (isDrawClaimed, state) = engineInstanceStore.synchronized(gameId) { engine ->
            val isDrawClaimed = engine.claimDraw()
            isDrawClaimed to engine.getUIState()
        }
                ?: throw IllegalStateException()
        if (!isDrawClaimed) {
            return ClaimDrawResult.Error.NotAllowed
        }
        handleGameFinished(game)
        val newGameStateDto = GameStateDto.of(state)
        gameMessageBroker.sendDrawClaimedMessage(gameId, newGameStateDto, claimingColor = requestingPlayerColor.toJsonStr())
        return ClaimDrawResult.Success(newGameStateDto, claimingColor = requestingPlayerColor.toJsonStr())
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

    private fun handleGameFinished(game: Game) {
        val finishedGame = game.copy(isFinished = true)
        gameRepository.update(finishedGame)
        engineInstanceStore.remove(game.id)
    }

}