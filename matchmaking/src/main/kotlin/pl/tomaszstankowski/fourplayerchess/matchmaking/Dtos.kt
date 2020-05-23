package pl.tomaszstankowski.fourplayerchess.matchmaking

import java.time.Instant
import java.util.*

data class LobbyDto(val id: UUID, val name: String, val createdAt: Instant)

internal fun Lobby.toDto() = LobbyDto(
        id = this.id,
        name = this.name,
        createdAt = this.createdAt
)

data class LobbyEditableDetails(val name: String)

data class CreateLobbyDto(val lobbyEditableDetails: LobbyEditableDetails)

data class UpdateLobbyDto(val lobbyId: UUID, val lobbyEditableDetails: LobbyEditableDetails)