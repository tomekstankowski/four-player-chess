package pl.tomaszstankowski.fourplayerchess.engine.hypermax

import pl.tomaszstankowski.fourplayerchess.engine.*
import pl.tomaszstankowski.fourplayerchess.engine.hypermax.TranspositionTable.NodeType.EXACT
import pl.tomaszstankowski.fourplayerchess.engine.hypermax.TranspositionTable.NodeType.LOWER_BOUND
import java.util.*
import java.util.concurrent.ExecutorService

internal class HyperMaxSearch(private val position: Position,
                              private val searchExecutorService: ExecutorService) : Search {
    private lateinit var pos: Position
    private val tt = TranspositionTable()
    private val killerMoveTable = KillerMoveTable(maxPly = MAX_DEPTH, killerMovesPerPly = 3)
    private val historyTable = HistoryTable()
    private val moveGenerator = MoveGenerator(tt, killerMoveTable, historyTable)
    private var nodeCount = 0
    private var leafCount = 0
    private var isStopRequested = false
    private var gamePly: Short = 0
    private val alpha = Array(MAX_DEPTH) {
        FloatArray(allColors.size)
    }

    override fun startSearch() {
        pos = position.copy()
        isStopRequested = false
        searchExecutorService.submit {
            try {
                search()
            } catch (e: Throwable) {
                println("Search failed")
                e.printStackTrace()
            }
        }
    }

    override fun stopSearch() {
        isStopRequested = true
    }

    override fun getPositionEvaluation(): PositionEvaluation? {
        val tmpPos = position.copy()
        val pgnFormatter = PGNFormatter(tmpPos)
        val pv = LinkedList<PositionEvaluation.PVMove>()
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
        return PositionEvaluation(pv, rootEntry.eval[position.nextMoveColor.ordinal] / 100)
    }

    private fun search() {
        println()
        println("Next move: ${position.nextMoveColor}")
        for (depth in 1..MAX_DEPTH) {
            nodeCount = 0
            leafCount = 0
            val iterationStartTime = System.currentTimeMillis()

            allColors.forEach { color ->
                alpha[0][color.ordinal] = Float.NEGATIVE_INFINITY
            }
            val eval = hypermax(depth, plyFromRoot = 0)
            if (isStopRequested) {
                break
            }

            val iterationEndTime = System.currentTimeMillis()
            val iterationDurationMs = iterationEndTime - iterationStartTime
            val nodesPerSecond = nodeCount.toFloat() / iterationDurationMs * 1000
            println("Depth $depth of iterative deepening took ${iterationDurationMs}ms")
            println("nodes: $nodeCount, leaves: $leafCount, nodes/s: $nodesPerSecond, eval: ${eval.map { it / 100f }.map { "%.2f".format(it) }}")
        }

        gamePly++
    }

    private fun hypermax(depth: Int, plyFromRoot: Int): FloatArray {
        nodeCount++
        if (isStopRequested) {
            return EQUAL_EVAL
        }
        if (depth == 0 || pos.winner != null || pos.isDraw || pos.isDrawByClaimPossible) {
            leafCount++
            return evaluateHyperMaxPosition(pos)
        }
        val a = alpha[plyFromRoot]
        val color = pos.nextMoveColor
        val moves = moveGenerator.generateMoves(pos, plyFromRoot)
        // initialize only to avoid compilation error
        var bestScores: FloatArray = INITIAL_SCORES
        var bestMove = NULL_MOVE
        var isCut = false
        for (i in moves.indices) {
            val (move) = moves[i]
            pos.makeMove(move)
            // reduce unnecessary allocations
            System.arraycopy(a, 0, alpha[plyFromRoot + 1], 0, allColors.size)
            val scores = hypermax(depth - 1, plyFromRoot + 1)
            pos.unmakeMove()
            if (bestScores[color.ordinal] < scores[color.ordinal]) {
                bestScores = scores
                bestMove = move
            }
            if (a[color.ordinal] < scores[color.ordinal]) {
                a[color.ordinal] = scores[color.ordinal]
            }
            if (a.sum() >= 0f) {
                historyTable.increase(move, color, depth.toByte())
                killerMoveTable.addKillerMove(move, plyFromRoot)
                isCut = true
                break
            }
        }
        if (!isStopRequested) {
            tt.put(
                    key = pos.hash,
                    move = bestMove,
                    nodeType = if (isCut) LOWER_BOUND else EXACT,
                    eval = bestScores,
                    gamePly = gamePly,
                    depth = depth.toByte()
            )
        }
        return bestScores
    }
}

private const val MAX_DEPTH = 20
private val INITIAL_SCORES = FloatArray(allColors.size) { Float.NEGATIVE_INFINITY }
private val EQUAL_EVAL = FloatArray(allColors.size)
