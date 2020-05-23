package pl.tomaszstankowski.fourplayerchess.matchmaking

import java.time.Instant
import java.util.*

internal data class Lobby(
        val id: UUID,
        val name: String,
        val createdAt: Instant
)