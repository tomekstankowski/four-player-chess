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
    object RequestingPlayerNotAnOwner : UpdateLobbyResult()
}

sealed class DeleteLobbyResult {
    object Deleted : DeleteLobbyResult()
    object RequestingPlayerNotAnOwner : DeleteLobbyResult()
    data class LobbyNotFound(val lobbyId: UUID) : DeleteLobbyResult()
}

sealed class JoinLobbyResult {
    data class Success(val membership: LobbyMembershipDto) : JoinLobbyResult()
    object PlayerAlreadyInLobby : JoinLobbyResult()
    data class LobbyNotFound(val lobbyId: UUID) : JoinLobbyResult()
    object LobbyIsFull : JoinLobbyResult()
}

sealed class LeaveLobbyResult {
    object Left : LeaveLobbyResult()
    object OwnerMemberMustNotLeaveLobby : LeaveLobbyResult()
    object RequestingPlayerNotAMember : LeaveLobbyResult()
    data class LobbyNotFound(val lobbyId: UUID) : LeaveLobbyResult()
}