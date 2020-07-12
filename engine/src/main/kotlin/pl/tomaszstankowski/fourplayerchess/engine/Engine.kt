package pl.tomaszstankowski.fourplayerchess.engine

import pl.tomaszstankowski.fourplayerchess.engine.MoveClaim.PromotionMoveClaim
import pl.tomaszstankowski.fourplayerchess.engine.MoveClaim.RegularMoveClaim
import kotlin.random.Random

class Engine(state: State = State.starting(), private val random: Random = Random.Default) {
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

    fun submitResignation(color: Color): Boolean {
        if (isGameOver || state.eliminatedColors.contains(color)) {
            return false
        }
        val (newState, newStateFeatures, newLegalMoves) = makeResignation(color, state)
        state = newState
        stateFeatures = newStateFeatures
        legalMoves = newLegalMoves

        updateTranspositionTable()

        return true
    }

    fun claimDraw(): Boolean {
        if (isDrawByClaimAllowed) {
            isDrawByClaim = true
            return true
        }
        return false
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
            makeValidMove(moveClaim)
            return true
        }
        return false
    }

    fun makeRandomMove(): Move? {
        if (isGameOver) {
            return null
        }
        val move = legalMoves.random(random)
        val moveClaim = if (move.isPawnPromotion(state)) PromotionMoveClaim(move, PieceType.Queen) else RegularMoveClaim(move)
        makeValidMove(moveClaim)
        return move
    }

    private fun makeValidMove(moveClaim: MoveClaim) {
        val (newState, newStateFeatures, newLegalMoves) = makeMove(moveClaim, state)
        state = newState
        stateFeatures = newStateFeatures
        legalMoves = newLegalMoves

        updateTranspositionTable()

        if (newState.plyCount.count >= 50) {
            isFiftyMoveRule = true
        }
    }

    private fun updateTranspositionTable() {
        val key = state.transpositionTableKey
        val data = transpositionTable.getOrPut(key) { TranspositionTableData() }
        data.positionOccurrenceCount++
        if (data.positionOccurrenceCount == 3) {
            isThreeFoldRepetition = true
        }
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