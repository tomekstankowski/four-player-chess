package pl.tomaszstankowski.fourplayerchess.lobby

import java.time.Instant
import java.util.*

data class Lobby(
    val id: UUID,
    val name: String,
    val createdAt: Instant
)