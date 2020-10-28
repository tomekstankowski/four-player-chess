package pl.tomaszstankowski.fourplayerchess.engine.hypermax

import pl.tomaszstankowski.fourplayerchess.engine.*
import pl.tomaszstankowski.fourplayerchess.engine.hypermax.MoveGenerator.Scores.EQUAL_CAPTURE
import pl.tomaszstankowski.fourplayerchess.engine.hypermax.MoveGenerator.Scores.EXACT_TT_NODE
import pl.tomaszstankowski.fourplayerchess.engine.hypermax.MoveGenerator.Scores.KILLER_MOVE
import pl.tomaszstankowski.fourplayerchess.engine.hypermax.MoveGenerator.Scores.LOSING_CAPTURE
import pl.tomaszstankowski.fourplayerchess.engine.hypermax.MoveGenerator.Scores.PROMOTION
import pl.tomaszstankowski.fourplayerchess.engine.hypermax.MoveGenerator.Scores.QUIET_MOVE
import pl.tomaszstankowski.fourplayerchess.engine.hypermax.MoveGenerator.Scores.TT_NODE
import pl.tomaszstankowski.fourplayerchess.engine.hypermax.MoveGenerator.Scores.WINNING_CAPTURE

internal data class ScoredMove(val move: MoveBits, val score: Int)

internal class MoveGenerator(private val tt: TranspositionTable,
                             private val killerMoveTable: KillerMoveTable,
                             private val historyTable: HistoryTable) {

    private object Scores {
        const val EXACT_TT_NODE = 1_000_000
        const val TT_NODE = 900_000
        const val WINNING_CAPTURE = 800_000
        const val EQUAL_CAPTURE = 700_000
        const val PROMOTION = 600_000
        const val KILLER_MOVE = 500_000
        const val QUIET_MOVE = 100_000
        const val LOSING_CAPTURE = 400_000
    }

    fun generateMoves(position: Position, plyFromRoot: Int): List<ScoredMove> =
            position.getLegalMoves()
                    .map { move ->
                        val score = getScore(move, position, plyFromRoot)
                        ScoredMove(move, score)
                    }
                    .toMutableList().apply {
                        shuffle()
                        sortByDescending { it.score }
                    }

    private fun getScore(move: MoveBits, pos: Position, plyFromRoot: Int): Int {
        val ttEntry = tt.get(pos.hash)
        if (ttEntry != null && ttEntry.move == move) {
            return if (ttEntry.nodeType == TranspositionTable.NodeType.EXACT) EXACT_TT_NODE else TT_NODE
        }
        val capturedPiece = pos.getCapturedPiece(move)
        if (capturedPiece != null && !pos.isEliminated(capturedPiece.color)) {
            val capturingPiece = pos.getMovedPiece(move)
            val exchangeValue = capturedPiece.type.materialValue - capturingPiece.type.materialValue
            // prefer value of victim rather than value of exchange, captures by king do not break score due to mul and div
            val scoreMod = capturedPiece.type.materialValue * 10 - capturingPiece.type.materialValue / 10
            return when {
                exchangeValue > 0 -> WINNING_CAPTURE + scoreMod
                exchangeValue < 0 -> LOSING_CAPTURE + scoreMod
                else -> EQUAL_CAPTURE + scoreMod
            }
        }
        if (move.promotionPieceType != null) {
            return PROMOTION
        }
        if (killerMoveTable.isKillerMove(move, plyFromRoot)) {
            return KILLER_MOVE
        }
        return QUIET_MOVE + historyTable.get(move, pos.nextMoveColor)
    }
}