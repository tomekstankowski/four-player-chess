package pl.tomaszstankowski.fourplayerchess.matchmaking

import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionOperations
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.util.IdGenerator
import org.springframework.util.JdkIdGenerator
import org.springframework.util.SimpleIdGenerator
import pl.tomaszstankowski.fourplayerchess.data.executeWithResult
import pl.tomaszstankowski.fourplayerchess.matchmaking.data.*
import java.time.Clock
import java.time.Instant
import java.util.*
import javax.sql.DataSource
import kotlin.ConcurrentModificationException

class MatchmakingService internal constructor(
        private val clock: Clock,
        private val lobbyRepository: LobbyRepository,
        private val lobbyMembershipRepository: LobbyMembershipRepository,
        private val transactionOperations: TransactionOperations,
        private val lobbyFactory: LobbyFactory,
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
                        lobbyMembershipRepository = JdbcLobbyMembershipRepository(dataSource),
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
                    lobbyMembershipRepository = InMemoryLobbyMembershipRepository(dataSource),
                    transactionOperations = TransactionOperations.withoutTransaction(),
                    createGameUseCase = createGameUseCase,
                    simpMessageSendingOperations = simpMessageSendingOperations
            )
        }

        private fun create(
                clock: Clock,
                idGenerator: IdGenerator,
                lobbyRepository: LobbyRepository,
                lobbyMembershipRepository: LobbyMembershipRepository,
                transactionOperations: TransactionOperations,
                createGameUseCase: CreateGameUseCase,
                simpMessageSendingOperations: SimpMessageSendingOperations
        ) =
                MatchmakingService(
                        clock = clock,
                        lobbyRepository = lobbyRepository,
                        lobbyMembershipRepository = lobbyMembershipRepository,
                        transactionOperations = transactionOperations,
                        lobbyFactory = LobbyFactory(
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
            val ownerMembership = LobbyMembership(
                    lobbyId = lobby.id,
                    playerId = lobby.ownerId,
                    joinedAt = Instant.now(clock)
            )
            lobbyRepository.create(lobby)
            lobbyMembershipRepository.insert(ownerMembership)
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

            val updatedLobby = lobby.copy(
                    name = details.name,
                    version = lobby.version + 1
            )
            val isUpdated = lobbyRepository.updateIfVersionEquals(updatedLobby, lobby.version)
            if (!isUpdated) {
                throw ConcurrentModificationException()
            }
            return@executeWithResult UpdateLobbyResult.Success(updatedLobby.toDto())
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
        val deletedLobby = lobby.incrementVersion()
                .copy(isDeleted = true)
        val isUpdated = lobbyRepository.updateIfVersionEquals(deletedLobby, lobby.version)
        if (!isUpdated) {
            throw ConcurrentModificationException()
        }
        lobbyMessageBroker.sendLobbyDeletedMessage(lobbyId)
        return DeleteLobbyResult.Deleted
    }

    fun joinLobby(dto: JoinLobbyDto): JoinLobbyResult {
        val lobbyId = dto.lobbyId
        val requestingPlayerId = dto.requestingPlayerId

        val txResult = transactionOperations.executeWithResult {
            val lobby = findActiveLobby(lobbyId)
                    ?: return@executeWithResult JoinLobbyResult.LobbyNotFound(lobbyId)
            val memberships = lobbyMembershipRepository.findByLobbyIdOrderByCreatedAtDesc(lobbyId)
            val isPlayerAlreadyInLobby = memberships.any { membership -> membership.playerId == requestingPlayerId }
            if (isPlayerAlreadyInLobby) {
                return@executeWithResult JoinLobbyResult.PlayerAlreadyInLobby
            }
            val isLobbyFull = memberships.size == REQUIRED_PLAYERS_COUNT
            if (isLobbyFull) {
                return@executeWithResult JoinLobbyResult.LobbyIsFull
            }
            val newMembership = LobbyMembership(
                    lobbyId,
                    requestingPlayerId,
                    joinedAt = Instant.now(clock)
            )
            lobbyMembershipRepository.insert(newMembership)
            val updatedLobby = lobby.incrementVersion()
            val isUpdated = lobbyRepository.updateIfVersionEquals(updatedLobby, lobby.version)
            if (!isUpdated) {
                throw ConcurrentModificationException()
            }
            return@executeWithResult JoinLobbyResult.Success(newMembership.toDto())
        }
        if (txResult is JoinLobbyResult.Success) {
            lobbyMessageBroker.sendPlayerJoinedLobbyMessage(playerId = requestingPlayerId, lobbyId = lobbyId)
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
            val lobbyMemberships = lobbyMembershipRepository.findByLobbyIdOrderByCreatedAtDesc(lobbyId)
            val isMember = lobbyMemberships.any { membership -> membership.playerId == requestingPlayerId }
            if (!isMember) {
                return@executeWithResult LeaveLobbyResult.RequestingPlayerNotAMember
            }
            lobbyMembershipRepository.deleteByLobbyIdAndPlayerId(lobbyId = lobbyId, playerId = requestingPlayerId)
            return@executeWithResult LeaveLobbyResult.Left
        }
        if (txResult is LeaveLobbyResult.Left) {
            lobbyMessageBroker.sendPlayerLeftLobbyMessage(playerId = requestingPlayerId, lobbyId = lobbyId)
        }
        return txResult
    }

    fun getPlayersInLobby(lobbyId: UUID): List<LobbyMembershipDto>? {
        findActiveLobby(lobbyId) ?: return null
        return lobbyMembershipRepository.findByLobbyIdOrderByCreatedAtDesc(lobbyId)
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
        val memberships = lobbyMembershipRepository.findByLobbyIdOrderByCreatedAtDesc(lobbyId)
        if (memberships.size < REQUIRED_PLAYERS_COUNT) {
            return StartGameResult.NotEnoughPlayers(currentPlayersCount = memberships.size)
        }
        val playersIds = memberships.map { membership -> membership.playerId }.toSet()
        val gameId = createGame.createGame(playersIds)
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
}

