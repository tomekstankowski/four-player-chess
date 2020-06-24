package pl.tomaszstankowski.fourplayerchess.matchmaking

import java.time.Instant
import java.util.*

internal data class Lobby(
        val id: UUID,
        val name: String,
        val createdAt: Instant,
        val ownerId: UUID,
        val gameId: UUID?,
        val isDeleted: Boolean,
        val version: Int
) {

    internal fun incrementVersion() = copy(version = version + 1)

    internal val isActive get() = gameId == null && !isDeleted
}

internal data class LobbyMembership(
        val lobbyId: UUID,
        val playerId: UUID,
        val joinedAt: Instant
)