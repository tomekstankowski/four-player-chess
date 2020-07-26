package pl.tomaszstankowski.fourplayerchess.engine

import pl.tomaszstankowski.fourplayerchess.engine.MoveClaim.PromotionMoveClaim
import pl.tomaszstankowski.fourplayerchess.engine.MoveClaim.RegularMoveClaim
import pl.tomaszstankowski.fourplayerchess.engine.PieceType.*
import kotlin.random.Random

class Engine(state: State = State.starting(), private val random: Random = Random.Default) {
    var state: State
        private set

    private var helperState: HelperState

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
        get() = isDrawByClaim || legalMoves.isEmpty() || isMaterialInsufficient

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
        val helperState = HelperState.fromState(state)
        val stateFeatures = getStateFeatures(state, helperState)
        val legalMoves = genLegalMoves(state, helperState, stateFeatures)
        this.state = state
        this.helperState = helperState
        this.stateFeatures = stateFeatures
        this.legalMoves = legalMoves
    }

    fun submitResignation(color: Color): Boolean {
        if (isGameOver || state.eliminatedColors.contains(color)) {
            return false
        }
        val (newState, newHelperState, newStateFeatures, newLegalMoves) = makeResignation(color, state, helperState)
        state = newState
        helperState = newHelperState
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
        val moveClaim = if (move.isPawnPromotion(state)) PromotionMoveClaim(move, Queen) else RegularMoveClaim(move)
        makeValidMove(moveClaim)
        return move
    }

    private fun makeValidMove(moveClaim: MoveClaim) {
        val (newState, newHelperState, newStateFeatures, newLegalMoves) = makeMove(moveClaim, state, helperState)
        state = newState
        helperState = newHelperState
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

    private val isMaterialInsufficient: Boolean
        get() {
            val isEachKingAlone = helperState.pieceList.all { (piece, _) -> piece.type == King }
            if (isEachKingAlone)
                return true
            val isOneVsOne = state.eliminatedColors.size == Color.values().size - 2
            if (isOneVsOne) {
                return Color.values()
                        .filter { !state.eliminatedColors.contains(it) }
                        .zipWithNext()
                        .any { (firstColor, secondColor) ->
                            val firstColorPieces = helperState.pieceList.filter { (piece, _) -> piece.color == firstColor }
                            val secondColorPieces = helperState.pieceList.filter { (piece, _) -> piece.color == secondColor }
                            isKingVsKingAndBishop(firstColorPieces, secondColorPieces)
                                    || isKingVsKingAndBishop(secondColorPieces, firstColorPieces)
                                    || isKingVsKingAndKnight(firstColorPieces, secondColorPieces)
                                    || isKingVsKingAndKnight(secondColorPieces, firstColorPieces)
                                    || isKingAndBishopVsKingAndBishopOfSameType(firstColorPieces, secondColorPieces)
                        }
            }
            return false
        }

    private fun isKingVsKingAndBishop(firstColorPieces: PieceList, secondColorPieces: PieceList) =
            firstColorPieces.size == 1
                    && secondColorPieces.size == 2 && secondColorPieces.any { (piece, _) -> piece.type == Bishop }

    private fun isKingVsKingAndKnight(firstColorPieces: PieceList, secondColorPieces: PieceList) =
            firstColorPieces.size == 1
                    && secondColorPieces.size == 2 && secondColorPieces.any { (piece, _) -> piece.type == Knight }

    private fun isKingAndBishopVsKingAndBishopOfSameType(firstColorPieces: PieceList, secondColorPieces: PieceList): Boolean {
        if (firstColorPieces.size != 2)
            return false
        if (secondColorPieces.size != 2)
            return false
        val firstBishopPosition = firstColorPieces.firstOrNull { (piece, _) -> piece.type == Bishop }
                ?.let { (_, pos) -> pos }
                ?: return false
        val secondBishopPosition = secondColorPieces.firstOrNull { (piece, _) -> piece.type == Bishop }
                ?.let { (_, pos) -> pos }
                ?: return false
        val material = firstBishopPosition.isLightSquare == secondBishopPosition.isLightSquare
        return material
    }

}