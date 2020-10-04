package pl.tomaszstankowski.fourplayerchess.engine

import pl.tomaszstankowski.fourplayerchess.engine.hypermax.HyperMaxSearch
import pl.tomaszstankowski.fourplayerchess.engine.paranoid.ParanoidSearch
import pl.tomaszstankowski.fourplayerchess.engine.random.RandomSearch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.random.Random

class Engine internal constructor(
        private val position: Position,
        private val search: Search) {

    companion object {
        private val defaultTTOptions = TranspositionTableOptions()

        fun withParanoidSearch(state: FenState,
                               searchExecutorService: ExecutorService = Executors.newSingleThreadExecutor(),
                               transpositionTableOptions: TranspositionTableOptions = defaultTTOptions): Engine {
            val position = Position.fromFenState(state)
            return Engine(
                    position = position,
                    search = ParanoidSearch(position, searchExecutorService, transpositionTableOptions)
            )
        }

        fun withRandomSearch(state: FenState, random: Random = Random.Default): Engine {
            val position = Position.fromFenState(state)
            return Engine(
                    position = position,
                    search = RandomSearch(position, random)
            )
        }

        fun withHypermax(state: FenState,
                         searchExecutorService: ExecutorService = Executors.newSingleThreadExecutor(),
                         transpositionTableOptions: TranspositionTableOptions = defaultTTOptions): Engine {
            val position = Position.fromFenState(state)
            return Engine(
                    position = position,
                    search = HyperMaxSearch(position, searchExecutorService, transpositionTableOptions)
            )
        }
    }

    private var isDrawByClaim = false

    private val isDraw: Boolean
        get() = isDrawByClaim || position.isDraw

    private val isGameOver: Boolean
        get() = isDraw || position.winner != null

    fun getUIState() =
            UIState(
                    fenState = position.toFenState(),
                    legalMoves = this.position.getLegalMoves().map { it.toApiMove() }.toSet(),
                    checks = allColors
                            .map { color ->
                                color to position.checks[color.ordinal]
                                        .toArray()
                                        .map { it.toApiCheck() }
                                        .toSet()
                            }
                            .toMap(),
                    isDrawByClaimAllowed = (this.position.isDrawByClaimPossible) && !isGameOver,
                    isGameOver = this.isGameOver,
                    winningColor = position.winner
            )

    fun submitResignation(color: Color): Boolean {
        if (isGameOver || position.isEliminated(color)) {
            return false
        }
        position.makeResignation(color)
        return true
    }

    fun claimDraw(): Boolean {
        if (position.isDrawByClaimPossible) {
            isDrawByClaim = true
            return true
        }
        return false
    }

    fun makeMove(move: Move): Boolean {
        if (isGameOver) {
            return false
        }
        val moveBits = move.toBits()
        val isValidMove = moveBits in position.getLegalMoves()
        if (isValidMove) {
            position.makeMove(moveBits)
            return true
        }
        return false
    }

    fun unmakeMove(): Boolean {
        if (isGameOver) {
            return false
        }
        return position.unmakeMove()
    }

    fun search(maxDepth: Int = 20): SearchTask? {
        if (isGameOver) {
            return null
        }
        return search.startSearch(maxDepth)
    }

    fun stopSearching() {
        search.stopSearch()
    }
}