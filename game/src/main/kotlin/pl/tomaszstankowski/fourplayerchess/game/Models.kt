package pl.tomaszstankowski.fourplayerchess.game

import pl.tomaszstankowski.fourplayerchess.engine.Color
import java.time.Instant
import java.util.*

internal data class Game(val id: UUID,
                         val createdAt: Instant,
                         val isCommitted: Boolean,
                         val isCancelled: Boolean,
                         val isFinished: Boolean) {

    internal val isActive: Boolean
        get() = isCommitted && !isCancelled && !isFinished
}

internal sealed class Player {
    abstract val gameId: UUID
    abstract val color: Color

    data class HumanPlayer(override val gameId: UUID,
                           override val color: Color,
                           val userId: UUID) : Player()

    data class RandomBot(override val gameId: UUID,
                         override val color: Color) : Player()
}