package pl.tomaszstankowski.fourplayerchess.engine.random

import pl.tomaszstankowski.fourplayerchess.engine.*
import java.time.Duration
import kotlin.random.Random

internal class RandomSearch(private val position: Position,
                            private val random: Random) : Search {

    override fun startSearch(maxDepth: Int): SearchTask {
        val legalMoves = position.getLegalMoves()
        val pgnFormatter = PGNFormatter(position)
        val pv: List<PVMove> = if (legalMoves.isEmpty()) {
            emptyList()
        } else {
            val move = legalMoves.random(random)
            val pvMove = PVMove(
                    move = move,
                    moveText = pgnFormatter.formatMove(move)
            )
            listOf(pvMove)
        }
        return SearchTask.finished(
                SearchResult(
                        principalVariation = pv.map { SearchResult.PVMove(it.move.toApiMove(), it.moveText) },
                        evaluation = 0f,
                        depth = 1,
                        duration = Duration.ZERO,
                        nodeCount = 1,
                        leafCount = legalMoves.size
                ),
                error = null
        )
    }

    override fun stopSearch() {
    }
}