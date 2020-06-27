package pl.tomaszstankowski.fourplayerchess.game

import pl.tomaszstankowski.fourplayerchess.engine.Engine
import java.util.*
import java.util.concurrent.ConcurrentHashMap

internal class EngineInstanceStore {
    private val gameToEngine = ConcurrentHashMap<UUID, Engine>()

    fun put(gameId: UUID, engine: Engine) {
        gameToEngine[gameId] = engine
    }

    fun remove(gameId: UUID) {
        gameToEngine.remove(gameId)
    }

    fun get(gameId: UUID): Engine? = gameToEngine[gameId]

    inline fun <T> synchronized(gameId: UUID, crossinline func: (Engine) -> T): T? {
        var result: T? = null
        gameToEngine.computeIfPresent(gameId) { _, engine ->
            result = func(engine)
            engine
        }
        return result
    }
}