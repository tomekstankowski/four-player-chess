package pl.tomaszstankowski.fourplayerchess.matchmaking

import org.springframework.transaction.support.TransactionOperations
import org.springframework.util.IdGenerator
import java.time.Clock
import java.time.Instant
import java.util.*

class MatchmakingService internal constructor(
        private val clock: Clock,
        private val idGenerator: IdGenerator,
        private val lobbyRepository: LobbyRepository,
        private val transactionOperations: TransactionOperations
) {

    companion object {

        internal fun create(
                clock: Clock,
                idGenerator: IdGenerator,
                lobbyRepository: LobbyRepository,
                transactionOperations: TransactionOperations
        ) =
                MatchmakingService(
                        clock = clock,
                        idGenerator = idGenerator,
                        lobbyRepository = lobbyRepository,
                        transactionOperations = transactionOperations
                )
    }

    fun createLobby(dto: CreateLobbyDto): CreateLobbyResult {
        val details = dto.lobbyEditableDetails
        val validationErrors = validate(details)
        if (validationErrors.isNotEmpty()) {
            return CreateLobbyResult.LobbyDetailsNotValid(validationErrors)
        }
        return transactionOperations.executeWithResult {
            if (lobbyRepository.findByName(details.name) != null) {
                return@executeWithResult CreateLobbyResult.NameConflict(details.name)
            }

            val lobby = Lobby(
                    id = idGenerator.generateId(),
                    name = details.name,
                    createdAt = Instant.now(clock)
            )
            lobbyRepository.create(lobby)
            return@executeWithResult CreateLobbyResult.Success(lobby.toDto())
        }
    }

    fun updateLobby(dto: UpdateLobbyDto): UpdateLobbyResult {
        val details = dto.lobbyEditableDetails
        val validationErrors = validate(details)
        if (validationErrors.isNotEmpty()) {
            return UpdateLobbyResult.LobbyDetailsNotValid(validationErrors)
        }
        val lobby = lobbyRepository.findById(dto.lobbyId) ?: return UpdateLobbyResult.LobbyNotFound(dto.lobbyId)
        return transactionOperations.executeWithResult {
            if (lobbyRepository.findByName(dto.lobbyEditableDetails.name)?.takeIf { it.id != lobby.id } != null) {
                return@executeWithResult UpdateLobbyResult.NameConflict(dto.lobbyEditableDetails.name)
            }

            val updatedLobby = lobby.copy(
                    name = details.name
            )
            lobbyRepository.update(updatedLobby)
            return@executeWithResult UpdateLobbyResult.Success(updatedLobby.toDto())
        }

    }

    fun getLobby(lobbyId: UUID): LobbyDto? = lobbyRepository.findById(lobbyId)?.toDto()

    fun getAllLobbies(): List<LobbyDto> = lobbyRepository.findAll().map { it.toDto() }

    fun deleteLobby(lobbyId: UUID): Boolean =
            lobbyRepository.findById(lobbyId)
                    ?.let {
                        lobbyRepository.delete(lobbyId)
                        true
                    }
                    ?: false
}

