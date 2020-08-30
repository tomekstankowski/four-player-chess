package pl.tomaszstankowski.fourplayerchess.engine

import java.time.Duration
import kotlin.random.Random

class Engine(state: FenState = FenState.starting(),
             private val random: Random = Random.Default) {
    private val position: Position = Position.fromFenState(state)
    private val search = ParanoidSearch(position, Duration.ofSeconds(5))

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

    fun makeRandomMove(): Move? {
        if (isGameOver) {
            return null
        }
        val move = position.getLegalMoves().random(random)
        position.makeMove(move)
        return move.toApiMove()
    }

    fun findBestMove(): Move? {
        if (isGameOver) {
            return null
        }
        search.pos = position.copy()
        return search.findBestMove().toApiMove()
    }

}