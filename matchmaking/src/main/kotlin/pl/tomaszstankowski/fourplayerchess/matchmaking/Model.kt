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

internal sealed class LobbyMembership {
    abstract val lobbyId: UUID
    abstract val joinedAt: Instant

    data class HumanPlayerMembership(override val lobbyId: UUID,
                                     override val joinedAt: Instant,
                                     val userId: UUID) : LobbyMembership()

    data class RandomBotMembership(override val lobbyId: UUID,
                                   override val joinedAt: Instant,
                                   val botId: UUID) : LobbyMembership()
}