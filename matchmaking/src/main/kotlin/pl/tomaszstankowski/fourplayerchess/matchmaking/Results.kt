package pl.tomaszstankowski.fourplayerchess.matchmaking

import pl.tomaszstankowski.fourplayerchess.common.validation.ValidationError
import java.util.*

sealed class CreateLobbyResult {
    data class Success(val lobby: LobbyDto) : CreateLobbyResult()
    data class LobbyDetailsNotValid(val errors: Set<ValidationError>) : CreateLobbyResult()
}

sealed class UpdateLobbyResult {
    data class Success(val lobby: LobbyDto) : UpdateLobbyResult()
    data class LobbyNotFound(val lobbyId: UUID) : UpdateLobbyResult()
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

sealed class AddRandomBotResult {
    data class Success(val membership: LobbyMembershipDto) : AddRandomBotResult()
    data class LobbyNotFound(val lobbyId: UUID) : AddRandomBotResult()
    object LobbyIsFull : AddRandomBotResult()
    object RequestingPlayerNotAnOwner : AddRandomBotResult()
}

sealed class RemoveRandomBotResult {
    object Removed : RemoveRandomBotResult()
    data class LobbyNotFound(val lobbyId: UUID) : RemoveRandomBotResult()
    data class BotNotFound(val botId: UUID) : RemoveRandomBotResult()
    object RequestingPlayerNotAnOwner : RemoveRandomBotResult()
}

sealed class StartGameResult {
    data class Success(val game: GameDto) : StartGameResult()
    data class LobbyNotFound(val lobbyId: UUID) : StartGameResult()
    data class NotEnoughPlayers(val currentPlayersCount: Int) : StartGameResult()
    object RequestingPlayerNotAnOwner : StartGameResult()
}