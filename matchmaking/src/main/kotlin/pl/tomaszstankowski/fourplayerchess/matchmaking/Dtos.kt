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

data class CreateLobbyDto(val lobbyEditableDetails: LobbyEditableDetails,
                          val requestingPlayerId: UUID)

data class UpdateLobbyDto(val lobbyId: UUID,
                          val lobbyEditableDetails: LobbyEditableDetails,
                          val requestingPlayerId: UUID)

data class DeleteLobbyDto(val lobbyId: UUID,
                          val requestingPlayerId: UUID)

data class JoinLobbyDto(val lobbyId: UUID,
                        val requestingPlayerId: UUID)

data class LeaveLobbyDto(val lobbyId: UUID,
                         val requestingPlayerId: UUID)

data class LobbyMembershipDto(val playerId: UUID, val joinedAt: Instant)

internal fun LobbyMembership.toDto() = LobbyMembershipDto(
        playerId = playerId,
        joinedAt = joinedAt
)