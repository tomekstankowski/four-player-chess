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

internal data class GamePlayer(val gameId: UUID,
                               val playerId: UUID,
                               val color: Color)