package pl.tomaszstankowski.fourplayerchess.game

import pl.tomaszstankowski.fourplayerchess.engine.Move
import pl.tomaszstankowski.fourplayerchess.game.Player.RandomBot
import java.util.*
import java.util.concurrent.ExecutorService

internal class BotMoveExecutor(private val executorService: ExecutorService,
                               private val engineInstanceStore: EngineInstanceStore,
                               private val gameMessageBroker: GameMessageBroker) {

    fun executeBotMoveIfHasNextMove(randomBots: List<RandomBot>, gameId: UUID, engineState: EngineStateSnapshot) {
        if (isNextMoveMadeByRandomBot(randomBots, engineState)) {
            executorService.submit { doMove(randomBots, gameId) }
        }
    }

    private fun isNextMoveMadeByRandomBot(randomBots: List<RandomBot>, engineState: EngineStateSnapshot): Boolean =
            !engineState.isGameOver && randomBots.any { it.color == engineState.state.nextMoveColor }

    private fun doMove(randomBots: List<RandomBot>, gameId: UUID) {
        val (move, newEngineState) = engineInstanceStore.synchronized(gameId) { engine ->
            when (val move = engine.makeRandomMove()) {
                is Move -> move to engine.getStateSnapshot()
                else -> null
            }
        } ?: return
        gameMessageBroker.sendMoveMadeMessage(gameId, newEngineState.toGameStateDto(), move.toDto())
        if (isNextMoveMadeByRandomBot(randomBots, newEngineState)) {
            doMove(randomBots, gameId)
        }
    }
}