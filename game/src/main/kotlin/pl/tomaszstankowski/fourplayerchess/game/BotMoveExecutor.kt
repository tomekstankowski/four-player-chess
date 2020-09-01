package pl.tomaszstankowski.fourplayerchess.game

import pl.tomaszstankowski.fourplayerchess.engine.Move
import pl.tomaszstankowski.fourplayerchess.engine.UIState
import pl.tomaszstankowski.fourplayerchess.game.Player.RandomBot
import java.time.Duration
import java.util.*
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.random.Random

internal class BotMoveExecutor(private val executorService: ScheduledExecutorService,
                               private val engineInstanceStore: EngineInstanceStore,
                               private val gameMessageBroker: GameMessageBroker,
                               private val random: Random,
                               private val botMoveDuration: Duration) {

    fun executeBotMoveIfHasNextMove(randomBots: List<RandomBot>, gameId: UUID, uiState: UIState) {
        if (isNextMoveMadeByRandomBot(randomBots, uiState)) {
            engineInstanceStore.synchronized(gameId) { engine ->
                engine.search()
            }
            executorService.schedule({
                try {
                    val (move, newState) = stopSearchAndPlayBestMove(gameId) ?: return@schedule
                    gameMessageBroker.sendMoveMadeMessage(gameId, GameStateDto.of(newState), move.toDto())
                    Thread.sleep(100)
                    executeBotMoveIfHasNextMove(randomBots, gameId, newState)
                } catch (e: Throwable) {
                    println("Something went wrong while making move")
                    e.printStackTrace()
                }
            }, botMoveDuration.toMillis(), TimeUnit.MILLISECONDS)
        }
    }

    private fun isNextMoveMadeByRandomBot(randomBots: List<RandomBot>, uiState: UIState): Boolean =
            !uiState.isGameOver && randomBots.any { it.color == uiState.fenState.nextMoveColor }

    private fun stopSearchAndPlayBestMove(gameId: UUID): Pair<Move, UIState>? {
        return engineInstanceStore.synchronized(gameId) { engine ->
            engine.stopSearching()
            val stateEvaluation = engine.getStateEvaluation()
            val bestMove = stateEvaluation?.principalVariation?.firstOrNull()
                    ?.move
                    ?: engine.getUIState().legalMoves.random(random = random)
            if (stateEvaluation != null) {
                val pv = stateEvaluation.principalVariation
                val eval = stateEvaluation.value
                val pvStr = pv.joinToString { (_, moveText) -> moveText }
                println("Eval: ${"%.2f".format(eval)}, PV: $pvStr")
            }
            if (!engine.makeMove(bestMove)) {
                throw IllegalStateException("Move $bestMove is illegal")
            }
            return@synchronized bestMove to engine.getUIState()
        }
    }
}