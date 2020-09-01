package pl.tomaszstankowski.fourplayerchess.engine.random

import pl.tomaszstankowski.fourplayerchess.engine.PGNFormatter
import pl.tomaszstankowski.fourplayerchess.engine.Position
import pl.tomaszstankowski.fourplayerchess.engine.PositionEvaluation
import pl.tomaszstankowski.fourplayerchess.engine.Search
import kotlin.random.Random

internal class RandomSearch(private val position: Position,
                            private val random: Random) : Search {

    override fun startSearch() {
    }

    override fun stopSearch() {
    }

    override fun getPositionEvaluation(): PositionEvaluation? {
        val legalMoves = position.getLegalMoves()
        val pgnFormatter = PGNFormatter(position)
        if (legalMoves.isEmpty()) {
            return null
        }
        val move = legalMoves.random(random)
        val pvMove = PositionEvaluation.PVMove(
                move = move,
                moveText = pgnFormatter.formatMove(move)
        )
        return PositionEvaluation(
                pv = listOf(pvMove),
                evaluation = 0f
        )
    }
}