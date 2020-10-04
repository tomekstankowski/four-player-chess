package pl.tomaszstankowski.fourplayerchess.game

import pl.tomaszstankowski.fourplayerchess.engine.Move
import pl.tomaszstankowski.fourplayerchess.engine.SearchTask
import pl.tomaszstankowski.fourplayerchess.engine.UIState
import pl.tomaszstankowski.fourplayerchess.game.Player.RandomBot
import java.time.Duration
import java.util.*
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.SECONDS
import kotlin.random.Random

internal class BotMoveExecutor(private val executorService: ScheduledExecutorService,
                               private val engineInstanceStore: EngineInstanceStore,
                               private val gameMessageBroker: GameMessageBroker,
                               private val random: Random,
                               private val botMoveDuration: Duration) {

    fun executeBotMoveIfHasNextMove(randomBots: List<RandomBot>, gameId: UUID, uiState: UIState) {
        if (isNextMoveMadeByRandomBot(randomBots, uiState)) {
            val searchTask = engineInstanceStore.synchronized(gameId) { engine -> engine.search() } ?: return
            executorService.schedule({
                try {
                    val (move, newState) = stopSearchAndPlayBestMove(gameId, searchTask) ?: return@schedule
                    gameMessageBroker.sendMoveMadeMessage(gameId, GameStateDto.of(newState), move.toDto())
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

    private fun stopSearchAndPlayBestMove(gameId: UUID, searchTask: SearchTask): Pair<Move, UIState>? {
        return engineInstanceStore.synchronized(gameId) { engine ->
            engine.stopSearching()
            val currentState = engine.getUIState()
            val isFinished = searchTask.await(1, SECONDS)
            if (!isFinished) {
                throw IllegalStateException("Engine did not finish searching")
            }
            val searchResult = searchTask.depthSearchResults.lastOrNull()
            if (searchResult != null) {
                println("Last iteration of search for ${currentState.fenState.nextMoveColor}")
                println()
                searchResult.print()
                println()
            }
            val bestMove = searchResult?.principalVariation?.firstOrNull()?.move
                    ?: currentState.legalMoves.random(random = random)
            if (!engine.makeMove(bestMove)) {
                throw IllegalStateException("Move $bestMove is illegal")
            }
            val newState = engine.getUIState()
            return@synchronized bestMove to newState
        }
    }
}