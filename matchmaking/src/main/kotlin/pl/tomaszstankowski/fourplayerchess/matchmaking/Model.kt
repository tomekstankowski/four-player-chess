package pl.tomaszstankowski.fourplayerchess.matchmaking

import java.time.Instant
import java.util.*

internal data class Lobby(
        val id: UUID,
        val name: String,
        val createdAt: Instant,
        val ownerId: UUID,
        val version: Int
) {

    internal fun incrementVersion() = copy(version = version + 1)
}

internal data class LobbyMembership(
        val lobbyId: UUID,
        val playerId: UUID,
        val joinedAt: Instant
)