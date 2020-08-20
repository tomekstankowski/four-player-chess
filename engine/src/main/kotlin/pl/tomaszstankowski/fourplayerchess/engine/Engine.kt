package pl.tomaszstankowski.fourplayerchess.engine

import kotlin.random.Random

class Engine(state: FenState = FenState.starting(),
             private val random: Random = Random.Default) {
    private val position: Position = Position(state)

    private var isDrawByClaim = false

    private val isDraw: Boolean
        get() = isDrawByClaim || position.isDraw

    private val isGameOver: Boolean
        get() = isDraw || position.winner != null

    fun getUIState() =
            UIState(
                    fenState = position.toState(),
                    legalMoves = this.position.legalMoves.toList(),
                    checks = Color.values()
                            .map { color -> color to position.checks[color.ordinal].toList() }
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
        val isValidMove = move in position.legalMoves
        if (isValidMove) {
            makeValidMove(move)
            return true
        }
        return false
    }

    fun makeRandomMove(): Move? {
        if (isGameOver) {
            return null
        }
        val move = position.legalMoves.random(random)
        makeValidMove(move)
        return move
    }

    private fun makeValidMove(move: Move) {
        position.makeMove(move)
    }

}