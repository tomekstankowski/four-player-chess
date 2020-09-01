package pl.tomaszstankowski.fourplayerchess.engine.paranoid

import pl.tomaszstankowski.fourplayerchess.engine.*
import pl.tomaszstankowski.fourplayerchess.engine.paranoid.TranspositionTable.NodeType.EXACT
import pl.tomaszstankowski.fourplayerchess.engine.paranoid.TranspositionTable.NodeType.LOWER_BOUND
import pl.tomaszstankowski.fourplayerchess.engine.paranoid.TranspositionTable.NodeType.UPPER_BOUND
import java.util.*
import java.util.concurrent.ExecutorService
import kotlin.math.max
import kotlin.math.min

internal class ParanoidSearch(private val position: Position,
                              private val executorService: ExecutorService) : Search {
    // Copy of original position used for search
    private lateinit var pos: Position
    private val transpositionTables = Array(Color.values().size) {
        TranspositionTable()
    }
    private val killerMoveTables = Array(Color.values().size) {
        KillerMoveTable(MAX_DEPTH.toInt(), 3)
    }
    private val historyTable = HistoryTable()
    private lateinit var moveGenerator: MoveGenerator
    private var gamePly: Short = 0
    private var nodeCnt = 0
    private var leafCnt = 0

    private var isStopRequested = false

    override fun getPositionEvaluation(): PositionEvaluation? {
        val tmpPos = position.copy()
        val pgnFormatter = PGNFormatter(tmpPos)
        val pv = LinkedList<PositionEvaluation.PVMove>()
        val tt = transpositionTables[tmpPos.nextMoveColor.ordinal]
        // to avoid cycle
        val pvHashes = LinkedList<Long>()
        val rootEntry = tt.get(tmpPos.hash) ?: return null
        var currEntry: TranspositionTable.Entry = rootEntry
        do {
            pvHashes.add(tmpPos.hash)
            val move = currEntry.move
            val pvMove = PositionEvaluation.PVMove(move, moveText = pgnFormatter.formatMove(move))
            pv.add(pvMove)
            tmpPos.makeMove(move)
            currEntry = tt.get(tmpPos.hash)
                    ?.takeIf { e -> e.nodeType == EXACT }
                    ?: break
        } while (tmpPos.hash !in pvHashes)
        return PositionEvaluation(pv, rootEntry.eval / 100f)
    }

    override fun stopSearch() {
        isStopRequested = true
    }

    override fun startSearch() {
        isStopRequested = false
        pos = position.copy()
        executorService.submit {
            try {
                search()
            } catch (e: Throwable) {
                println("Search failed")
                e.printStackTrace()
            }
        }
    }

    private fun search() {
        moveGenerator = MoveGenerator(
                transpositionTables[pos.nextMoveColor.ordinal],
                killerMoveTables[pos.nextMoveColor.ordinal],
                historyTable
        )
        println("Evaluation for ${pos.nextMoveColor}")
        for (depth in 1..MAX_DEPTH) {
            val iterationStartTime = System.currentTimeMillis()
            leafCnt = 0
            nodeCnt = 0
            alphaBeta(
                    maxColor = pos.nextMoveColor,
                    alpha = Int.MIN_VALUE,
                    beta = Int.MAX_VALUE,
                    depth = depth.toByte(),
                    isMax = true,
                    plyFromRoot = 0
            )
            if (isStopRequested) {
                break
            }
            val iterationEndTime = System.currentTimeMillis()
            val iterationDurationMs = iterationEndTime - iterationStartTime
            val nodesPerSec: Float = nodeCnt.toFloat() / iterationDurationMs * 1000
            println("Depth $depth took $iterationDurationMs ms")
            println("nodes: $nodeCnt, leaves: $leafCnt, nodes/s: $nodesPerSec")
        }

        val tt = transpositionTables[pos.nextMoveColor.ordinal]
        tt.logState()

        println()

        gamePly++
    }

    private fun alphaBeta(maxColor: Color,
                          alpha: Int,
                          beta: Int,
                          depth: Byte,
                          isMax: Boolean,
                          plyFromRoot: Int): Int {
        if (isStopRequested) {
            return if (isMax) beta else alpha
        }
        nodeCnt++
        if (pos.isEliminated(maxColor)) {
            leafCnt++
            return LOSE_VALUE
        }
        if (pos.winner == maxColor) {
            leafCnt++
            return WIN_VALUE
        }
        if (pos.isDrawByClaimPossible || pos.isDraw) {
            leafCnt++
            return DRAW_VALUE
        }
        if (depth == 0.toByte()) {
            leafCnt++
            return evaluateParanoidPosition(pos, maxColor)
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
        val moves = moveGenerator.generateMoves(pos, plyFromRoot)
        val killerMoveTable = killerMoveTables[maxColor.ordinal]
        if (isMax) {
            bestScore = Int.MIN_VALUE
            var a = newAlpha
            for ((move) in moves) {
                pos.makeMove(move)
                val score = alphaBeta(maxColor, a, newBeta, depth.dec(), !isMax, plyFromRoot + 1)
                pos.unmakeMove()
                if (score > bestScore) {
                    bestScore = score
                    bestMove = move
                }
                a = max(score, a)
                if (score >= newBeta) {
                    if (pos.isQuietMove(move)) {
                        killerMoveTable.addKillerMove(move, plyFromRoot)
                        historyTable.increase(move, pos.nextMoveColor, depth)
                    }
                    break
                }
            }
        } else {
            bestScore = Int.MAX_VALUE
            var b = newBeta
            for ((move) in moves) {
                pos.makeMove(move)
                val newIsMax = pos.nextMoveColor == maxColor
                val score = alphaBeta(maxColor, newAlpha, b, depth.dec(), newIsMax, plyFromRoot + 1)
                pos.unmakeMove()
                if (score < bestScore) {
                    bestScore = score
                    bestMove = move
                }
                b = min(score, b)
                if (score <= newAlpha) {
                    if (pos.isQuietMove(move)) {
                        killerMoveTable.addKillerMove(move, plyFromRoot)
                        historyTable.increase(move, pos.nextMoveColor, depth)
                    }
                    break
                }
            }
        }
        if (!isStopRequested) {
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
        }
        return bestScore
    }

}

private const val DRAW_VALUE = 0
private const val WIN_VALUE = 10000000
private const val LOSE_VALUE = -10000000
private const val MAX_DEPTH: Byte = 20
