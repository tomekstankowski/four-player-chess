package pl.tomaszstankowski.fourplayerchess.engine

import pl.tomaszstankowski.fourplayerchess.engine.TranspositionTable.NodeType.EXACT
import pl.tomaszstankowski.fourplayerchess.engine.TranspositionTable.NodeType.LOWER_BOUND
import pl.tomaszstankowski.fourplayerchess.engine.TranspositionTable.NodeType.UPPER_BOUND
import java.time.Duration
import kotlin.math.max
import kotlin.math.min

internal class ParanoidSearch(var pos: Position,
                              private val maxSearchDuration: Duration) {
    private val transpositionTables = Array(Color.values().size) {
        TranspositionTable()
    }
    private var gamePly: Short = 0
    var nodesCnt = 0
    var leafCnt = 0


    fun findBestMove(): MoveBits {
        val searchStartTime = System.currentTimeMillis()
        for (depth in 1..MAX_DEPTH) {
            val iterationStartTime = System.currentTimeMillis()
            leafCnt = 0
            nodesCnt = 0
            val value = alphaBeta(
                    maxColor = pos.nextMoveColor,
                    alpha = Int.MIN_VALUE,
                    beta = Int.MAX_VALUE,
                    depth = depth.toByte(),
                    isMax = true
            )
            val iterationEndTime = System.currentTimeMillis()
            val iterationDurationMs = iterationEndTime - iterationStartTime
            println("Depth $depth took $iterationDurationMs ms")
            println("${pos.nextMoveColor} position evaluation: $value")
            val searchDurationMs = iterationEndTime - searchStartTime
            val timeRemainingMs = maxSearchDuration.toMillis() - searchDurationMs
            if (20 * iterationDurationMs > timeRemainingMs) {
                break
            }
        }
        val currTime = System.currentTimeMillis()
        val searchDurationMs = currTime - searchStartTime
        println("Found best move in $searchDurationMs ms")
        val nodesPerSec: Float = nodesCnt.toFloat() / searchDurationMs * 1000
        println("nodes/s: $nodesPerSec")
        println("leaves: $leafCnt")
        val tt = transpositionTables[pos.nextMoveColor.ordinal]
        tt.logState()
        gamePly++
        return tt.get(pos.hash)
                ?.move
                ?: pos.getLegalMoves().random()
    }

    private fun alphaBeta(maxColor: Color,
                          alpha: Int,
                          beta: Int,
                          depth: Byte,
                          isMax: Boolean): Int {
        nodesCnt++
        if (pos.isDrawByClaimPossible || pos.isDraw) {
            return DRAW_VALUE
        }
        if (pos.isEliminated(maxColor)) {
            return LOSE_VALUE
        }
        if (pos.winner == maxColor) {
            return WIN_VALUE
        }
        if (depth == 0.toByte()) {
            leafCnt++
            return evaluatePosition(pos, maxColor)
        }
        var newAlpha = alpha
        var newBeta = beta
        val tt = transpositionTables[maxColor.ordinal]
        val ttEntry = tt.get(pos.hash)
        if (ttEntry != null && ttEntry.depth >= depth) {
            if (ttEntry.nodeType == EXACT) {
                return ttEntry.eval
            } else if (ttEntry.nodeType == LOWER_BOUND) {
                if (ttEntry.eval > alpha) {
                    newAlpha = ttEntry.eval
                }
            } else if (ttEntry.nodeType == UPPER_BOUND) {
                if (ttEntry.eval < beta) {
                    newBeta = ttEntry.eval
                }
            }
            if (newAlpha >= newBeta) {
                return newAlpha
            }
        }
        var bestScore: Int
        var bestMove: MoveBits = NULL_MOVE
        val moves = getOrderedMoves(tt)
        if (isMax) {
            bestScore = Int.MIN_VALUE
            var a = newAlpha
            for (move in moves) {
                pos.makeMove(move)
                val score = alphaBeta(maxColor, a, newBeta, depth.dec(), !isMax)
                pos.unmakeMove()
                if (score > bestScore) {
                    bestScore = score
                    bestMove = move
                }
                a = max(score, a)
                if (score >= newBeta) {
                    break
                }
            }
        } else {
            bestScore = Int.MAX_VALUE
            var b = newBeta
            for (move in moves) {
                pos.makeMove(move)
                val newIsMax = pos.nextMoveColor == maxColor
                val score = alphaBeta(maxColor, newAlpha, b, depth.dec(), newIsMax)
                pos.unmakeMove()
                if (score < bestScore) {
                    bestScore = score
                    bestMove = move
                }
                b = min(score, b)
                if (score <= newAlpha) {
                    break
                }
            }
        }
        val nodeType = when {
            bestScore >= newBeta -> LOWER_BOUND
            bestScore <= newAlpha -> UPPER_BOUND
            else -> EXACT
        }
        tt.put(
                key = pos.hash,
                eval = bestScore,
                gamePly = gamePly,
                nodeType = nodeType,
                depth = depth,
                move = bestMove
        )
        return bestScore
    }

    private fun getOrderedMoves(tt: TranspositionTable): List<MoveBits> {
        // TODO SEE
        return pos.getLegalMoves().sortedBy { move ->
            val ttEntry = tt.get(pos.hash)
            if (ttEntry != null && ttEntry.move == move) {
                if (ttEntry.nodeType == EXACT)
                    return@sortedBy 0
                else
                    return@sortedBy 1
            }
            return@sortedBy 2
        }
    }

}

private const val DRAW_VALUE = 0
private const val WIN_VALUE = 10000000
private const val LOSE_VALUE = -10000000
private const val MAX_DEPTH: Byte = 20
