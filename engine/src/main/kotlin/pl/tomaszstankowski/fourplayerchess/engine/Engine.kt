package pl.tomaszstankowski.fourplayerchess.engine

import pl.tomaszstankowski.fourplayerchess.engine.MoveClaim.PromotionMoveClaim

class Engine(state: State) {
    var state: State
        private set

    var stateFeatures: StateFeatures
        private set

    var legalMoves: List<Move>
        private set

    private val transpositionTable = HashMap<Int, TranspositionTableData>()

    private var isThreeFoldRepetition = false

    private var isFiftyMoveRule = false

    val isDrawByClaimAllowed: Boolean
        get() = (isThreeFoldRepetition || isFiftyMoveRule) && !isGameOver

    private var isDrawByClaim = false

    val isDraw: Boolean
        get() = isDrawByClaim || legalMoves.isEmpty()

    val isGameOver: Boolean
        get() = isDraw || state.eliminatedColors.size == Color.values().size - 1

    val winningColor: Color?
        get() {
            if (!isGameOver || isDraw) {
                return null
            }
            return Color.values().find { !state.eliminatedColors.contains(it) }
        }

    init {
        val stateFeatures = getStateFeatures(state)
        val legalMoves = genLegalMoves(state, stateFeatures)
        this.state = state
        this.stateFeatures = stateFeatures
        this.legalMoves = legalMoves
    }

    fun claimDraw(): Boolean {
        if (isDrawByClaimAllowed) {
            isDrawByClaim = true
        }
        return isDrawByClaim
    }

    fun makeMove(moveClaim: MoveClaim): Boolean {
        if (isGameOver) {
            return false
        }
        val isValidMove = legalMoves.any { move ->
            move == moveClaim.move
                    && (moveClaim !is PromotionMoveClaim || move.isPawnPromotion(state))
                    && (!move.isPawnPromotion(state) || moveClaim is PromotionMoveClaim)
        }
        if (isValidMove) {
            val (newState, newStateFeatures, newLegalMoves) = makeMove(moveClaim, state)
            state = newState
            stateFeatures = newStateFeatures
            legalMoves = newLegalMoves

            val key = state.transpositionTableKey
            val data = transpositionTable.getOrPut(key) { TranspositionTableData() }
            data.positionOccurrenceCount++
            if (data.positionOccurrenceCount == 3) {
                isThreeFoldRepetition = true
            }

            if (newState.plyCount.count >= 50) {
                isFiftyMoveRule = true
            }

            return true
        }
        return false
    }

    private val State.transpositionTableKey: Int
        get() {
            var result = squares.hashCode()
            result = 31 * result + eliminatedColors.hashCode()
            result = 31 * result + nextMoveColor.hashCode()
            result = 31 * result + enPassantSquares.hashCode()
            result = 31 * result + castlingOptions.hashCode()
            return result
        }

    private data class TranspositionTableData(var positionOccurrenceCount: Int = 0)
}