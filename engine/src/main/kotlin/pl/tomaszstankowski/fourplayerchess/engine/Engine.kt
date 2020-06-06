package pl.tomaszstankowski.fourplayerchess.engine

import pl.tomaszstankowski.fourplayerchess.engine.MoveClaim.PromotionMoveClaim

class Engine(state: State) {
    var state: State
        private set

    var stateFeatures: StateFeatures
        private set

    private val transpositionTable = HashMap<Int, TranspositionTableData>()

    private var isThreeFoldRepetition = false

    private var isFiftyMoveRule = false

    val isDrawByClaimAllowed: Boolean
        get() = (isThreeFoldRepetition || isFiftyMoveRule) && !stateFeatures.isGameOver

    private var isDrawByClaim = false

    val isDraw: Boolean
        get() = isDrawByClaim || stateFeatures.isStaleMate

    val isGameOver: Boolean
        get() = isDrawByClaim || stateFeatures.isGameOver

    val winningColor: Color?
        get() {
            if (!stateFeatures.isGameOver || stateFeatures.isStaleMate) {
                return null
            }
            return Color.values().find { !state.eliminatedColors.contains(it) }
        }

    init {
        this.state = state
        this.stateFeatures = getStateFeatures(state)
    }

    fun claimDraw(): Boolean {
        if (isDrawByClaimAllowed) {
            isDrawByClaim = true
        }
        return isDrawByClaim
    }

    fun makeMove(moveClaim: MoveClaim): Pair<State, StateFeatures>? {
        val isValidMove = stateFeatures.legalMoves.any { move ->
            move == moveClaim.move
                    && (moveClaim !is PromotionMoveClaim || move.isPawnPromotion(state))
                    && (!move.isPawnPromotion(state) || moveClaim is PromotionMoveClaim)
        }
        if (isValidMove) {
            val (newState, newStateFeatures) = makeMove(moveClaim, state)
            state = newState
            stateFeatures = newStateFeatures

            val key = state.transpositionTableKey
            val data = transpositionTable.getOrPut(key) { TranspositionTableData() }
            data.positionOccurrenceCount++
            if (data.positionOccurrenceCount == 3) {
                isThreeFoldRepetition = true
            }

            if (newState.plyCount.count >= 50) {
                isFiftyMoveRule = true
            }

            return newState to newStateFeatures
        }
        return null
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

    private val StateFeatures.isGameOver: Boolean
        get() = legalMoves.isEmpty() || state.eliminatedColors.size == Color.values().size - 1

    private val StateFeatures.isStaleMate: Boolean
        get() = legalMoves.isEmpty()
}