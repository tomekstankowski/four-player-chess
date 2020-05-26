package pl.tomaszstankowski.fourplayerchess.matchmaking

import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionOperations
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.util.IdGenerator
import org.springframework.util.JdkIdGenerator
import pl.tomaszstankowski.fourplayerchess.data.executeWithResult
import pl.tomaszstankowski.fourplayerchess.matchmaking.data.JdbcLobbyMembershipRepository
import pl.tomaszstankowski.fourplayerchess.matchmaking.data.JdbcLobbyRepository
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
        private val lobbyFactory: LobbyFactory
) {

    companion object {
        fun create(clock: Clock,
                   dataSource: DataSource,
                   transactionManager: PlatformTransactionManager) =
                create(
                        clock = clock,
                        idGenerator = JdkIdGenerator(),
                        lobbyRepository = JdbcLobbyRepository(dataSource),
                        lobbyMembershipRepository = JdbcLobbyMembershipRepository(dataSource),
                        transactionOperations = TransactionTemplate(transactionManager)
                )

        internal fun create(
                clock: Clock,
                idGenerator: IdGenerator,
                lobbyRepository: LobbyRepository,
                lobbyMembershipRepository: LobbyMembershipRepository,
                transactionOperations: TransactionOperations
        ) =
                MatchmakingService(
                        clock = clock,
                        lobbyRepository = lobbyRepository,
                        lobbyMembershipRepository = lobbyMembershipRepository,
                        transactionOperations = transactionOperations,
                        lobbyFactory = LobbyFactory(
                                clock = clock,
                                idGenerator = idGenerator
                        )
                )
    }

    fun createLobby(dto: CreateLobbyDto): CreateLobbyResult {
        val details = dto.lobbyEditableDetails
        val validationErrors = validate(details)
        if (validationErrors.isNotEmpty()) {
            return CreateLobbyResult.LobbyDetailsNotValid(validationErrors)
        }
        return transactionOperations.executeWithResult {
            val isAlreadyInLobby = lobbyMembershipRepository.findByPlayerId(dto.requestingPlayerId) != null
            if (isAlreadyInLobby) {
                return@executeWithResult CreateLobbyResult.RequestingPlayerAlreadyInLobby
            }
            if (lobbyRepository.findByName(details.name) != null) {
                return@executeWithResult CreateLobbyResult.NameConflict(details.name)
            }

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
            val lobby = lobbyRepository.findById(lobbyId)
                    ?: return@executeWithResult UpdateLobbyResult.LobbyNotFound(lobbyId)
            if (lobby.ownerId != dto.requestingPlayerId) {
                return@executeWithResult UpdateLobbyResult.RequestingPlayerNotAnOwner
            }
            if (lobbyRepository.findByName(details.name)?.takeIf { it.id != lobby.id } != null) {
                return@executeWithResult UpdateLobbyResult.NameConflict(details.name)
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

    fun getLobby(lobbyId: UUID): LobbyDto? = lobbyRepository.findById(lobbyId)?.toDto()

    fun getAllLobbies(): List<LobbyDto> = lobbyRepository.findAll().map { it.toDto() }

    fun deleteLobby(dto: DeleteLobbyDto): DeleteLobbyResult {
        val lobbyId = dto.lobbyId
        val lobby = lobbyRepository.findById(lobbyId)
                ?: return DeleteLobbyResult.LobbyNotFound(lobbyId)
        if (lobby.ownerId != dto.requestingPlayerId) {
            return DeleteLobbyResult.RequestingPlayerNotAnOwner
        }
        transactionOperations.executeWithoutResult {
            lobbyMembershipRepository.deleteByLobbyId(lobbyId)
            lobbyRepository.delete(lobbyId)
        }
        return DeleteLobbyResult.Deleted
    }

    fun joinLobby(dto: JoinLobbyDto): JoinLobbyResult {
        val lobbyId = dto.lobbyId
        val requestingPlayerId = dto.requestingPlayerId

        return transactionOperations.executeWithResult {
            val lobby = lobbyRepository.findById(lobbyId)
                    ?: return@executeWithResult JoinLobbyResult.LobbyNotFound(lobbyId)
            val isPlayerAlreadyInLobby = lobbyMembershipRepository.findByPlayerId(requestingPlayerId) != null
            if (isPlayerAlreadyInLobby) {
                return@executeWithResult JoinLobbyResult.PlayerAlreadyInLobby
            }
            val isLobbyFull = lobbyMembershipRepository.findByLobbyIdOrderByCreatedAtDesc(lobbyId).size == 4
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
    }

    fun leaveLobby(dto: LeaveLobbyDto): LeaveLobbyResult {
        val lobbyId = dto.lobbyId
        val requestingPlayerId = dto.requestingPlayerId
        return transactionOperations.executeWithResult {
            val lobby = lobbyRepository.findById(lobbyId)
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
    }

    fun getPlayersInLobby(lobbyId: UUID): List<LobbyMembershipDto>? {
        lobbyRepository.findById(lobbyId) ?: return null
        return lobbyMembershipRepository.findByLobbyIdOrderByCreatedAtDesc(lobbyId)
                .map { it.toDto() }
    }

    fun getCurrentLobbyOfPLayer(playerId: UUID): LobbyDto? {
        val playerMembership = lobbyMembershipRepository.findByPlayerId(playerId) ?: return null
        return lobbyRepository.findById(playerMembership.lobbyId)?.toDto()
    }
}

