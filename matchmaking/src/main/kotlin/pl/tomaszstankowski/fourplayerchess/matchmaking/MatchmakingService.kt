package pl.tomaszstankowski.fourplayerchess.matchmaking

import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionOperations
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.util.IdGenerator
import org.springframework.util.JdkIdGenerator
import org.springframework.util.SimpleIdGenerator
import pl.tomaszstankowski.fourplayerchess.data.executeWithResult
import pl.tomaszstankowski.fourplayerchess.matchmaking.LobbyMembership.HumanPlayerMembership
import pl.tomaszstankowski.fourplayerchess.matchmaking.data.*
import java.time.Clock
import java.time.Instant
import java.util.*
import javax.sql.DataSource
import kotlin.ConcurrentModificationException

class MatchmakingService internal constructor(
        private val clock: Clock,
        private val lobbyRepository: LobbyRepository,
        private val humanPlayerMembershipRepository: HumanPlayerMembershipRepository,
        private val randomBotMembershipRepository: RandomBotMembershipRepository,
        private val transactionOperations: TransactionOperations,
        private val lobbyFactory: LobbyFactory,
        private val randomBotFactory: RandomBotFactory,
        private val createGame: CreateGameUseCase,
        private val lobbyMessageBroker: LobbyMessageBroker
) {

    companion object {
        const val REQUIRED_PLAYERS_COUNT = 4

        fun create(clock: Clock,
                   dataSource: DataSource,
                   transactionManager: PlatformTransactionManager,
                   createGameUseCase: CreateGameUseCase,
                   simpMessageSendingOperations: SimpMessageSendingOperations) =
                create(
                        clock = clock,
                        idGenerator = JdkIdGenerator(),
                        lobbyRepository = JdbcLobbyRepository(dataSource),
                        humanPlayerMembershipRepository = JdbcHumanPlayerMembershipRepository(dataSource),
                        randomBotMembershipRepository = JdbcRandomBotMembershipRepository(dataSource),
                        transactionOperations = TransactionTemplate(transactionManager),
                        createGameUseCase = createGameUseCase,
                        simpMessageSendingOperations = simpMessageSendingOperations
                )

        fun create(clock: Clock,
                   createGameUseCase: CreateGameUseCase,
                   simpMessageSendingOperations: SimpMessageSendingOperations): MatchmakingService {
            val dataSource = InMemoryDataSource()
            return create(
                    clock = clock,
                    idGenerator = SimpleIdGenerator(),
                    lobbyRepository = InMemoryLobbyRepository(dataSource),
                    humanPlayerMembershipRepository = InMemoryHumanPlayerMembershipRepository(dataSource),
                    randomBotMembershipRepository = InMemoryRandomBotMembershipRepository(dataSource),
                    transactionOperations = TransactionOperations.withoutTransaction(),
                    createGameUseCase = createGameUseCase,
                    simpMessageSendingOperations = simpMessageSendingOperations
            )
        }

        private fun create(
                clock: Clock,
                idGenerator: IdGenerator,
                lobbyRepository: LobbyRepository,
                humanPlayerMembershipRepository: HumanPlayerMembershipRepository,
                randomBotMembershipRepository: RandomBotMembershipRepository,
                transactionOperations: TransactionOperations,
                createGameUseCase: CreateGameUseCase,
                simpMessageSendingOperations: SimpMessageSendingOperations
        ) =
                MatchmakingService(
                        clock = clock,
                        lobbyRepository = lobbyRepository,
                        humanPlayerMembershipRepository = humanPlayerMembershipRepository,
                        randomBotMembershipRepository = randomBotMembershipRepository,
                        transactionOperations = transactionOperations,
                        lobbyFactory = LobbyFactory(
                                clock = clock,
                                idGenerator = idGenerator
                        ),
                        randomBotFactory = RandomBotFactory(
                                clock = clock,
                                idGenerator = idGenerator
                        ),
                        createGame = createGameUseCase,
                        lobbyMessageBroker = LobbyMessageBroker(simpMessageSendingOperations)
                )
    }

    fun createLobby(dto: CreateLobbyDto): CreateLobbyResult {
        val details = dto.lobbyEditableDetails
        val validationErrors = validate(details)
        if (validationErrors.isNotEmpty()) {
            return CreateLobbyResult.LobbyDetailsNotValid(validationErrors)
        }
        return transactionOperations.executeWithResult {
            val lobby = lobbyFactory.create(
                    lobbyEditableDetails = details,
                    ownerId = dto.requestingPlayerId
            )
            val ownerMembership = HumanPlayerMembership(
                    lobbyId = lobby.id,
                    userId = lobby.ownerId,
                    joinedAt = Instant.now(clock)
            )
            lobbyRepository.create(lobby)
            humanPlayerMembershipRepository.insert(ownerMembership)
            return@executeWithResult CreateLobbyResult.Success(lobby.toDto())
        }
    }

    fun updateLobby(dto: UpdateLobbyDto): UpdateLobbyResult {
        val lobbyId = dto.lobbyId
        val details = dto.lobbyEditableDetails
        val validationErrors = validate(details)
        if (validationErrors.isNotEmpty()) {
            return UpdateLobbyResult.LobbyDetailsNotValid(validationErrors)
        }
        return transactionOperations.executeWithResult {
            val lobby = findActiveLobby(lobbyId)
                    ?: return@executeWithResult UpdateLobbyResult.LobbyNotFound(lobbyId)
            if (lobby.ownerId != dto.requestingPlayerId) {
                return@executeWithResult UpdateLobbyResult.RequestingPlayerNotAnOwner
            }
            val updatedLobby = lobby.copy(name = details.name)
            val savedLobby = updateVersionAndSave(updatedLobby)
            return@executeWithResult UpdateLobbyResult.Success(savedLobby.toDto())
        }
    }

    fun getLobby(lobbyId: UUID): LobbyDto? = findActiveLobby(lobbyId)?.toDto()

    fun deleteLobby(dto: DeleteLobbyDto): DeleteLobbyResult {
        val lobbyId = dto.lobbyId
        val lobby = findActiveLobby(lobbyId)
                ?: return DeleteLobbyResult.LobbyNotFound(lobbyId)
        if (lobby.ownerId != dto.requestingPlayerId) {
            return DeleteLobbyResult.RequestingPlayerNotAnOwner
        }
        val deletedLobby = lobby.copy(isDeleted = true)
        updateVersionAndSave(deletedLobby)
        lobbyMessageBroker.sendLobbyDeletedMessage(lobbyId)
        return DeleteLobbyResult.Deleted
    }

    fun joinLobby(dto: JoinLobbyDto): JoinLobbyResult {
        val lobbyId = dto.lobbyId
        val requestingPlayerId = dto.requestingPlayerId

        val txResult = transactionOperations.executeWithResult {
            val lobby = findActiveLobby(lobbyId)
                    ?: return@executeWithResult JoinLobbyResult.LobbyNotFound(lobbyId)
            val humanMemberships = humanPlayerMembershipRepository.findByLobbyId(lobbyId)
            val isPlayerAlreadyInLobby = humanMemberships.any { membership -> membership.userId == requestingPlayerId }
            if (isPlayerAlreadyInLobby) {
                return@executeWithResult JoinLobbyResult.PlayerAlreadyInLobby
            }
            val randomBots = randomBotMembershipRepository.findByLobbyId(lobbyId)
            val isLobbyFull = humanMemberships.size + randomBots.size == REQUIRED_PLAYERS_COUNT
            if (isLobbyFull) {
                return@executeWithResult JoinLobbyResult.LobbyIsFull
            }
            val newMembership = HumanPlayerMembership(
                    lobbyId = lobbyId,
                    userId = requestingPlayerId,
                    joinedAt = Instant.now(clock)
            )
            humanPlayerMembershipRepository.insert(newMembership)
            updateVersionAndSave(lobby)
            return@executeWithResult JoinLobbyResult.Success(newMembership.toDto())
        }
        if (txResult is JoinLobbyResult.Success) {
            lobbyMessageBroker.sendPlayerJoinedLobbyMessage(lobbyId, txResult.membership)
        }
        return txResult
    }

    fun leaveLobby(dto: LeaveLobbyDto): LeaveLobbyResult {
        val lobbyId = dto.lobbyId
        val requestingPlayerId = dto.requestingPlayerId
        val txResult = transactionOperations.executeWithResult {
            val lobby = findActiveLobby(lobbyId)
                    ?: return@executeWithResult LeaveLobbyResult.LobbyNotFound(lobbyId)
            if (lobby.ownerId == requestingPlayerId) {
                return@executeWithResult LeaveLobbyResult.OwnerMemberMustNotLeaveLobby
            }
            val lobbyMemberships = humanPlayerMembershipRepository.findByLobbyId(lobbyId)
            val isMember = lobbyMemberships.any { membership -> membership.userId == requestingPlayerId }
            if (!isMember) {
                return@executeWithResult LeaveLobbyResult.RequestingPlayerNotAMember
            }
            humanPlayerMembershipRepository.deleteByLobbyIdAndPlayerId(lobbyId = lobbyId, playerId = requestingPlayerId)
            return@executeWithResult LeaveLobbyResult.Left
        }
        if (txResult is LeaveLobbyResult.Left) {
            lobbyMessageBroker.sendPlayerLeftLobbyMessage(playerId = requestingPlayerId, lobbyId = lobbyId)
        }
        return txResult
    }

    fun addRandomBot(dto: AddRandomBotDto): AddRandomBotResult {
        val lobbyId = dto.lobbyId
        val txResult = transactionOperations.executeWithResult {
            val lobby = findActiveLobby(lobbyId)
                    ?: return@executeWithResult AddRandomBotResult.LobbyNotFound(lobbyId)
            if (dto.requestingPlayerId != lobby.ownerId) {
                return@executeWithResult AddRandomBotResult.RequestingPlayerNotAnOwner
            }
            val humanPlayers = humanPlayerMembershipRepository.findByLobbyId(lobbyId)
            val randomBots = randomBotMembershipRepository.findByLobbyId(lobbyId)
            val playersInLobbyCount = humanPlayers.size + randomBots.size
            if (playersInLobbyCount == REQUIRED_PLAYERS_COUNT) {
                return@executeWithResult AddRandomBotResult.LobbyIsFull
            }
            val newRandomBot = randomBotFactory.createBotMembership(lobbyId)
            randomBotMembershipRepository.insert(newRandomBot)
            updateVersionAndSave(lobby)
            return@executeWithResult AddRandomBotResult.Success(newRandomBot.toDto())
        }
        if (txResult is AddRandomBotResult.Success) {
            lobbyMessageBroker.sendRandomBotAddedToLobbyMessage(lobbyId, txResult.membership)
        }
        return txResult
    }

    fun removeRandomBot(dto: RemoveRandomBotDto): RemoveRandomBotResult {
        val lobbyId = dto.lobbyId
        val botId = dto.botId
        val txResult = transactionOperations.executeWithResult {
            val lobby = findActiveLobby(lobbyId)
                    ?: return@executeWithResult RemoveRandomBotResult.LobbyNotFound(lobbyId)
            if (dto.requestingPlayerId != lobby.ownerId) {
                return@executeWithResult RemoveRandomBotResult.RequestingPlayerNotAnOwner
            }
            val randomBots = randomBotMembershipRepository.findByLobbyId(lobbyId)
            val isBotFound = randomBots.any { it.botId == botId }
            if (!isBotFound) {
                return@executeWithResult RemoveRandomBotResult.BotNotFound(botId)
            }
            randomBotMembershipRepository.deleteByLobbyIdAndBotId(lobbyId, botId)
            return@executeWithResult RemoveRandomBotResult.Removed
        }
        if (txResult is RemoveRandomBotResult.Removed) {
            lobbyMessageBroker.sendRandomBotRemovedFromLobby(lobbyId = lobbyId, botId = botId)
        }
        return txResult
    }

    fun getPlayersInLobby(lobbyId: UUID): List<LobbyMembershipDto>? {
        findActiveLobby(lobbyId) ?: return null
        val humanPlayers = humanPlayerMembershipRepository.findByLobbyId(lobbyId).map { it as LobbyMembership }
        val randomBots = randomBotMembershipRepository.findByLobbyId(lobbyId).map { it as LobbyMembership }
        return (humanPlayers + randomBots)
                .sortedBy { it.joinedAt }
                .map { it.toDto() }
    }

    fun getActiveLobbiesOfAPLayer(playerId: UUID): List<LobbyDto> {
        return lobbyRepository.findByPlayerId(playerId)
                .filter { it.isActive }
                .sortedByDescending { lobby -> lobby.createdAt }
                .map { lobby -> lobby.toDto() }
    }

    fun startGame(dto: StartGameDto): StartGameResult {
        val lobbyId = dto.lobbyId
        val lobby = findActiveLobby(lobbyId) ?: return StartGameResult.LobbyNotFound(lobbyId)
        if (dto.requestingPlayerId != lobby.ownerId) {
            return StartGameResult.RequestingPlayerNotAnOwner
        }
        val humanPlayers = humanPlayerMembershipRepository.findByLobbyId(lobbyId)
        val randomBots = randomBotMembershipRepository.findByLobbyId(lobbyId)
        val playersCount = humanPlayers.size + randomBots.size
        if (playersCount < REQUIRED_PLAYERS_COUNT) {
            return StartGameResult.NotEnoughPlayers(currentPlayersCount = humanPlayers.size)
        }
        val playersIds = humanPlayers.map { membership -> membership.userId }.toSet()
        val gameId = createGame.createGame(playersIds = playersIds, randomBotsCount = randomBots.size)
        val updatedLobby = lobby.incrementVersion()
                .copy(gameId = gameId)
        val isUpdated = lobbyRepository.updateIfVersionEquals(updatedLobby, lobby.version)
        if (!isUpdated) {
            throw ConcurrentModificationException()
        }
        createGame.commitGame(gameId)
        lobbyMessageBroker.sendGameStartedMessage(lobbyId = lobbyId, gameId = gameId)
        return StartGameResult.Success(GameDto(gameId))
    }

    private fun findActiveLobby(id: UUID) =
            lobbyRepository.findById(id)
                    ?.takeIf { it.isActive }

    private fun updateVersionAndSave(lobby: Lobby): Lobby {
        val updatedLobby = lobby.incrementVersion()
        val isUpdated = lobbyRepository.updateIfVersionEquals(updatedLobby, lobby.version)
        if (!isUpdated) {
            throw ConcurrentModificationException()
        }
        return updatedLobby
    }
}

