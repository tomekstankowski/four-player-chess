package pl.tomaszstankowski.fourplayerchess.game

import pl.tomaszstankowski.fourplayerchess.engine.Move
import pl.tomaszstankowski.fourplayerchess.engine.UIState
import pl.tomaszstankowski.fourplayerchess.game.Player.RandomBot
import java.util.*
import java.util.concurrent.ExecutorService

internal class BotMoveExecutor(private val executorService: ExecutorService,
                               private val engineInstanceStore: EngineInstanceStore,
                               private val gameMessageBroker: GameMessageBroker) {

    fun executeBotMoveIfHasNextMove(randomBots: List<RandomBot>, gameId: UUID, uiState: UIState) {
        if (isNextMoveMadeByRandomBot(randomBots, uiState)) {
            executorService.submit { doMove(randomBots, gameId) }
        }
    }

    private fun isNextMoveMadeByRandomBot(randomBots: List<RandomBot>, uiState: UIState): Boolean =
            !uiState.isGameOver && randomBots.any { it.color == uiState.fenState.nextMoveColor }

    private fun doMove(randomBots: List<RandomBot>, gameId: UUID) {
        val (move, newState) = engineInstanceStore.synchronized(gameId) { engine ->
            Thread.sleep(1000)
            when (val move = engine.makeRandomMove()) {
                is Move -> move to engine.getUIState()
                else -> null
            }
        } ?: return
        gameMessageBroker.sendMoveMadeMessage(gameId, GameStateDto.of(newState), move.toDto())
        if (isNextMoveMadeByRandomBot(randomBots, newState)) {
            doMove(randomBots, gameId)
        }
    }
}