package pl.tomaszstankowski.fourplayerchess.lobby

import pl.tomaszstankowski.fourplayerchess.common.data.IdGenerator
import pl.tomaszstankowski.fourplayerchess.common.data.TransactionExecutor
import pl.tomaszstankowski.fourplayerchess.common.validation.ValidationError
import java.time.Clock
import java.time.Instant
import java.util.*

class LobbyService internal constructor(
        private val clock: Clock,
        private val idGenerator: IdGenerator,
        private val lobbyRepository: LobbyRepository,
        private val transactionExecutor: TransactionExecutor
) {

    companion object {
        fun create(
            clock: Clock,
            idGenerator: IdGenerator,
            lobbyRepository: LobbyRepository,
            transactionExecutor: TransactionExecutor
        ) =
            LobbyService(
                clock = clock,
                idGenerator = idGenerator,
                lobbyRepository = lobbyRepository,
                    transactionExecutor = transactionExecutor
            )
    }

    fun createLobby(cmd: CreateLobbyCommand): CreateLobbyResult {
        val details = cmd.lobbyDetails
        val validationErrors = validate(details)
        if (validationErrors.isNotEmpty()) {
            return CreateLobbyResult.LobbyDetailsNotValid(validationErrors)
        }
        return transactionExecutor.executeTransaction {
            if (lobbyRepository.findByName(details.name) != null) {
                return@executeTransaction CreateLobbyResult.NameConflict(details.name)
            }

            val lobby = Lobby(
                id = idGenerator.generateId(),
                name = details.name,
                createdAt = Instant.now(clock)
            )
            lobbyRepository.create(lobby)
            return@executeTransaction CreateLobbyResult.Success(lobby.toDTO())
        }
    }

    fun updateLobby(cmd: UpdateLobbyCommand): UpdateLobbyResult {
        val details = cmd.lobbyDetails
        val validationErrors = validate(details)
        if (validationErrors.isNotEmpty()) {
            return UpdateLobbyResult.LobbyDetailsNotValid(validationErrors)
        }
        val lobby = lobbyRepository.findById(cmd.lobbyId) ?: return UpdateLobbyResult.LobbyNotFound(cmd.lobbyId)
        return transactionExecutor.executeTransaction {
            if (lobbyRepository.findByName(cmd.lobbyDetails.name)?.takeIf { it.id != lobby.id } != null) {
                return@executeTransaction UpdateLobbyResult.NameConflict(cmd.lobbyDetails.name)
            }

            val updatedLobby = lobby.copy(
                name = details.name
            )
            lobbyRepository.update(updatedLobby)
            return@executeTransaction UpdateLobbyResult.Success(updatedLobby.toDTO())
        }

    }

    fun getLobby(lobbyId: UUID): LobbyDTO? = lobbyRepository.findById(lobbyId)?.toDTO()

    fun getAllLobbies(): List<LobbyDTO> = lobbyRepository.findAll().map { it.toDTO() }

    fun deleteLobby(lobbyId: UUID): Boolean =
        lobbyRepository.findById(lobbyId)
            ?.let {
                lobbyRepository.delete(lobbyId)
                true
            }
            ?: false
}

data class LobbyDTO(val id: UUID, val name: String, val createdAt: Instant)

private fun Lobby.toDTO() = LobbyDTO(
    id = this.id,
    name = this.name,
    createdAt = this.createdAt
)

data class LobbyDetails(val name: String)

data class CreateLobbyCommand(val lobbyDetails: LobbyDetails)

data class UpdateLobbyCommand(val lobbyId: UUID, val lobbyDetails: LobbyDetails)

sealed class CreateLobbyResult {
    data class Success(val lobby: LobbyDTO) : CreateLobbyResult()
    data class NameConflict(val name: String) : CreateLobbyResult()
    data class LobbyDetailsNotValid(val errors: Set<ValidationError>) : CreateLobbyResult()
}

sealed class UpdateLobbyResult {
    data class Success(val lobby: LobbyDTO) : UpdateLobbyResult()
    data class LobbyNotFound(val lobbyId: UUID) : UpdateLobbyResult()
    data class NameConflict(val name: String) : UpdateLobbyResult()
    data class LobbyDetailsNotValid(val errors: Set<ValidationError>) : UpdateLobbyResult()
}