package pl.tomaszstankowski.fourplayerchess.game

import org.springframework.util.IdGenerator
import java.time.Clock

internal class GameFactory(private val idGenerator: IdGenerator,
                           private val clock: Clock) {

    fun createGame() =
            Game(
                    id = idGenerator.generateId(),
                    createdAt = clock.instant(),
                    isCommitted = false,
                    isCancelled = false,
                    isFinished = false
            )
}