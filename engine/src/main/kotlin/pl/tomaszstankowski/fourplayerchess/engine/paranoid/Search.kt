package pl.tomaszstankowski.fourplayerchess.engine.paranoid

import pl.tomaszstankowski.fourplayerchess.engine.*
import pl.tomaszstankowski.fourplayerchess.engine.paranoid.TranspositionTable.NodeType.EXACT
import pl.tomaszstankowski.fourplayerchess.engine.paranoid.TranspositionTable.NodeType.LOWER_BOUND
import pl.tomaszstankowski.fourplayerchess.engine.paranoid.TranspositionTable.NodeType.UPPER_BOUND
import java.time.Duration
import java.util.*
import java.util.concurrent.ExecutorService
import kotlin.math.max
import kotlin.math.min

internal class ParanoidSearch(private val position: Position,
                              private val executorService: ExecutorService,
                              private val ttOptions: TranspositionTableOptions) : Search {
    private lateinit var searchPos: Position
    private val transpositionTables = Array(Color.values().size) {
        TranspositionTable()
    }
    private val killerMoveTables = Array(Color.values().size) {
        KillerMoveTable(MAX_DEPTH, 3)
    }
    private val historyTable = HistoryTable()
    private val moveGenerators = Array(Color.values().size) { index ->
        MoveGenerator(
                tt = transpositionTables[index],
                killerMoveTable = killerMoveTables[index],
                historyTable = historyTable
        )
    }
    private var gamePly = 0
    private var nodeCnt = 0
    private var leafCnt = 0

    @Volatile
    private var isStopRequested = false

    override fun stopSearch() {
        isStopRequested = true
    }

    override fun startSearch(maxDepth: Int): SearchTask {
        isStopRequested = false
        searchPos = position.copy()
        gamePly = position.gamePly
        val task = SearchTask.new()
        executorService.submit {
            try {
                iterativeDeepening(min(MAX_DEPTH, maxDepth), task)
                task.finish()
            } catch (e: Throwable) {
                task.finish(e)
            }
        }
        return task
    }

    private fun iterativeDeepening(maxDepth: Int, task: SearchTask) {
        for (depth in 1..maxDepth) {
            val iterationStartTime = System.currentTimeMillis()
            leafCnt = 0
            nodeCnt = 0
            val eval = alphaBeta(
                    maxColor = searchPos.nextMoveColor,
                    alpha = Int.MIN_VALUE,
                    beta = Int.MAX_VALUE,
                    depth = depth,
                    isMax = true,
                    plyFromRoot = 0
            )
            if (isStopRequested) {
                break
            }
            val iterationEndTime = System.currentTimeMillis()
            val iterationDurationMs = iterationEndTime - iterationStartTime
            val searchResult = SearchResult(
                    principalVariation = collectPV(depth).map { SearchResult.PVMove(it.move.toApiMove(), it.moveText) },
                    evaluation = eval / 100f,
                    depth = depth,
                    duration = Duration.ofMillis(iterationDurationMs),
                    nodeCount = nodeCnt,
                    leafCount = leafCnt
            )
            task.postSearchResult(searchResult)
        }
    }

    private fun alphaBeta(maxColor: Color,
                          alpha: Int,
                          beta: Int,
                          depth: Int,
                          isMax: Boolean,
                          plyFromRoot: Int): Int {
        if (isStopRequested) {
            return if (isMax) beta else alpha
        }
        nodeCnt++
        if (searchPos.isEliminated(maxColor)) {
            leafCnt++
            return LOSE_VALUE
        }
        if (searchPos.winner == maxColor) {
            leafCnt++
            return WIN_VALUE
        }
        if (((searchPos.isRepeated || searchPos.isDrawByClaimPossible) && plyFromRoot > 0) || searchPos.isDraw) {
            leafCnt++
            return DRAW_VALUE
        }
        if (depth == 0) {
            leafCnt++
            return evaluateParanoidPosition(searchPos, maxColor)
        }
        var newAlpha = alpha
        var newBeta = beta
        val tt = transpositionTables[maxColor.ordinal]

        if (ttOptions.isPositionEvaluationFetchAllowed) {
            val ttEntry = tt.get(searchPos.hash)
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
        }
        var bestScore: Int
        var bestMove: MoveBits = NULL_MOVE
        val moves = moveGenerators[maxColor.ordinal].generateMoves(searchPos, plyFromRoot)
        val killerMoveTable = killerMoveTables[maxColor.ordinal]
        if (isMax) {
            bestScore = Int.MIN_VALUE
            var a = newAlpha
            for ((move) in moves) {
                searchPos.makeMove(move)
                val score = alphaBeta(maxColor, a, newBeta, depth - 1, !isMax, plyFromRoot + 1)
                searchPos.unmakeMove()
                if (score > bestScore) {
                    bestScore = score
                    bestMove = move
                }
                a = max(score, a)
                if (score >= newBeta) {
                    if (searchPos.isQuietMove(move)) {
                        killerMoveTable.addKillerMove(move, plyFromRoot)
                        historyTable.increase(move, searchPos.nextMoveColor, depth)
                    }
                    break
                }
            }
        } else {
            bestScore = Int.MAX_VALUE
            var b = newBeta
            for ((move) in moves) {
                searchPos.makeMove(move)
                val newIsMax = searchPos.nextMoveColor == maxColor
                val score = alphaBeta(maxColor, newAlpha, b, depth - 1, newIsMax, plyFromRoot + 1)
                searchPos.unmakeMove()
                if (score < bestScore) {
                    bestScore = score
                    bestMove = move
                }
                b = min(score, b)
                if (score <= newAlpha) {
                    if (searchPos.isQuietMove(move)) {
                        killerMoveTable.addKillerMove(move, plyFromRoot)
                        historyTable.increase(move, searchPos.nextMoveColor, depth)
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
                    key = searchPos.hash,
                    eval = bestScore,
                    gamePly = gamePly.toShort(),
                    nodeType = nodeType,
                    depth = depth.toByte(),
                    move = bestMove
            )
        }
        return bestScore
    }

    private fun collectPV(depth: Int): List<PVMove> {
        val tmpPos = searchPos.copy()
        val pgnFormatter = PGNFormatter(tmpPos)
        val pv = LinkedList<PVMove>()
        val tt = transpositionTables[tmpPos.nextMoveColor.ordinal]
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

private const val DRAW_VALUE = 0
private const val WIN_VALUE = 10000000
private const val LOSE_VALUE = -10000000
private const val MAX_DEPTH: Int = 30
