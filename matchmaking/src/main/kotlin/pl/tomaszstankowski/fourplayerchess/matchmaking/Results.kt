package pl.tomaszstankowski.fourplayerchess.matchmaking

import pl.tomaszstankowski.fourplayerchess.common.validation.ValidationError
import java.util.*

sealed class CreateLobbyResult {
    data class Success(val lobby: LobbyDto) : CreateLobbyResult()
    data class NameConflict(val name: String) : CreateLobbyResult()
    data class LobbyDetailsNotValid(val errors: Set<ValidationError>) : CreateLobbyResult()
}

sealed class UpdateLobbyResult {
    data class Success(val lobby: LobbyDto) : UpdateLobbyResult()
    data class LobbyNotFound(val lobbyId: UUID) : UpdateLobbyResult()
    data class NameConflict(val name: String) : UpdateLobbyResult()
    data class LobbyDetailsNotValid(val errors: Set<ValidationError>) : UpdateLobbyResult()
}