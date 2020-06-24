package pl.tomaszstankowski.fourplayerchess.game

import pl.tomaszstankowski.fourplayerchess.engine.Engine
import java.util.*
import kotlin.collections.HashMap

internal class EngineInstanceStore {
    private val gameToEngine = HashMap<UUID, Engine>()

    fun put(gameId: UUID, engine: Engine) {
        gameToEngine[gameId] = engine
    }

    fun remove(gameId: UUID) {
        gameToEngine.remove(gameId)
    }

    fun get(gameId: UUID): Engine? = gameToEngine[gameId]
}