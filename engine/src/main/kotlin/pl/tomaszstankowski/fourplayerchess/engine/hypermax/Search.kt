package pl.tomaszstankowski.fourplayerchess.engine.hypermax

import pl.tomaszstankowski.fourplayerchess.engine.*
import pl.tomaszstankowski.fourplayerchess.engine.hypermax.TranspositionTable.NodeType.EXACT
import pl.tomaszstankowski.fourplayerchess.engine.hypermax.TranspositionTable.NodeType.LOWER_BOUND
import pl.tomaszstankowski.fourplayerchess.engine.hypermax.TranspositionTable.NodeType.UPPER_BOUND
import java.time.Duration
import java.util.*
import java.util.concurrent.ExecutorService
import kotlin.math.min

internal class HyperMaxSearch(private val position: Position,
                              private val searchExecutorService: ExecutorService,
                              private val ttOptions: TranspositionTableOptions) : Search {
    private lateinit var pos: Position
    private val tt = TranspositionTable()
    private val killerMoveTable = KillerMoveTable(maxPly = MAX_DEPTH, killerMovesPerPly = 3)
    private val historyTable = HistoryTable()
    private val moveGenerator = MoveGenerator(tt, killerMoveTable, historyTable)
    private var nodeCount = 0
    private var leafCount = 0

    @Volatile
    private var isStopRequested = false
    private var gamePly = 0
    private val alpha = Array(MAX_DEPTH) {
        FloatArray(allColors.size)
    }

    override fun startSearch(maxDepth: Int): SearchTask {
        pos = position.copy()
        gamePly = position.gamePly
        isStopRequested = false
        val task = SearchTask.new()
        searchExecutorService.submit {
            try {
                iterativeDeepening(min(maxDepth, MAX_DEPTH), task)
                task.finish()
            } catch (e: Throwable) {
                task.finish(e)
            }
        }
        return task
    }

    override fun stopSearch() {
        isStopRequested = true
    }

    private fun iterativeDeepening(maxDepth: Int, task: SearchTask) {
        val startDepth = min(3, maxDepth)
        for (depth in startDepth..maxDepth) {
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
            val searchResult = SearchResult(
                    principalVariation = collectPV(depth).map { SearchResult.PVMove(it.move.toApiMove(), it.moveText) },
                    evaluation = eval[pos.nextMoveColor.ordinal] / 100f,
                    depth = depth,
                    duration = Duration.ofMillis(iterationDurationMs),
                    nodeCount = nodeCount,
                    leafCount = leafCount
            )
            task.postSearchResult(searchResult)
        }
    }

    private fun hypermax(depth: Int, plyFromRoot: Int): FloatArray {
        nodeCount++
        if (isStopRequested) {
            return EQUAL_EVAL
        }
        if (depth == 0 || pos.winner != null || (pos.isDrawByClaimPossible && plyFromRoot > 0) || pos.isDraw) {
            leafCount++
            return evaluateHyperMaxPosition(pos)
        }
        val color = pos.nextMoveColor
        val a = alpha[plyFromRoot]
        val originAlpha = a[color.ordinal]
        if (ttOptions.isPositionEvaluationFetchAllowed) {
            val ttEntry = tt.get(pos.hash)
            if (ttEntry != null && ttEntry.depth >= depth) {
                if (ttEntry.nodeType == EXACT) {
                    return ttEntry.eval
                }
            }
        }
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
                historyTable.increase(move, color, depth)
                killerMoveTable.addKillerMove(move, plyFromRoot)
                isCut = true
                break
            }
        }
        if (!isStopRequested) {
            val nodeType = when {
                isCut -> LOWER_BOUND
                a[color.ordinal] > originAlpha -> EXACT
                else -> UPPER_BOUND
            }
            tt.put(
                    key = pos.hash,
                    move = bestMove,
                    nodeType = nodeType,
                    eval = bestScores,
                    gamePly = gamePly.toShort(),
                    depth = depth.toByte()
            )
        }
        return bestScores
    }

    private fun collectPV(depth: Int): List<PVMove> {
        val tmpPos = position.copy()
        val pgnFormatter = PGNFormatter(tmpPos)
        val pv = LinkedList<PVMove>()
        // to avoid cycle
        val pvHashes = LinkedList<Long>()
        val rootEntry = tt.get(tmpPos.hash) ?: return emptyList()
        var currEntry: TranspositionTable.Entry = rootEntry
        do {
            pvHashes.add(tmpPos.hash)
            val move = currEntry.move
            val pvMove = PVMove(move, moveText = pgnFormatter.formatMove(move))
            pv.add(pvMove)
            tmpPos.makeMove(move)
            currEntry = tt.get(tmpPos.hash)
                    ?.takeIf { e -> e.nodeType == EXACT }
                    ?: break
        } while (tmpPos.hash !in pvHashes && pv.size < depth)
        return pv
    }
}

private const val MAX_DEPTH = 30
private val INITIAL_SCORES = FloatArray(allColors.size) { Float.NEGATIVE_INFINITY }
private val EQUAL_EVAL = FloatArray(allColors.size)
