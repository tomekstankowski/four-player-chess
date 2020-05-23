package pl.tomaszstankowski.fourplayerchess

import pl.tomaszstankowski.fourplayerchess.Color.*
import pl.tomaszstankowski.fourplayerchess.PieceType.King
import pl.tomaszstankowski.fourplayerchess.PieceType.Pawn
import kotlin.math.max
import kotlin.math.min

internal typealias Vector = Pair<Int, Int>

internal val topV = 0 to 1
internal val topRightV = 1 to 1
internal val topLeftV = -1 to 1
internal val rightV = 1 to 0
internal val leftV = -1 to 0
internal val bottomV = 0 to -1
internal val bottomRightV = 1 to -1
internal val bottomLeftV = -1 to -1

internal val allDirectionsVectors = listOf(
        topV,
        topRightV,
        rightV,
        bottomRightV,
        bottomV,
        bottomLeftV,
        leftV,
        topLeftV
)

internal val Color.pawnForwardVector: Vector
    get() = when (this) {
        Red -> topV
        Green -> leftV
        Blue -> rightV
        Yellow -> bottomV
    }

private val redPawnCapturingVectors = listOf(topLeftV, topRightV)
private val greenPawnCapturingVectors = listOf(bottomLeftV, topLeftV)
private val bluePawnCapturingVectors = listOf(topRightV, bottomRightV)
private val yellowPawnCapturingVectors = listOf(bottomRightV, bottomLeftV)

internal val Color.pawnCapturingVectors: List<Vector>
    get() = when (this) {
        Red -> redPawnCapturingVectors
        Green -> greenPawnCapturingVectors
        Blue -> bluePawnCapturingVectors
        Yellow -> yellowPawnCapturingVectors
    }

internal val bishopMoveVectors = listOf(topRightV, topLeftV, bottomLeftV, bottomRightV)

internal val knightMoveVectors = listOf(
        1 to 2,
        2 to 1,
        2 to -1,
        1 to -2,
        -1 to -2,
        -2 to -1,
        -2 to 1,
        -1 to 2)

internal val rookMoveVectors = listOf(topV, rightV, bottomV, leftV)

internal val Color.kingSideVector: Vector
    get() = when (this) {
        Red -> rightV
        Green -> bottomV
        Blue -> topV
        Yellow -> leftV
    }

internal val Color.queenSideVector: Vector
    get() = when (this) {
        Red -> leftV
        Green -> topV
        Blue -> bottomV
        Yellow -> rightV
    }

internal fun Move.isKingSideCastling(state: State): Boolean {
    val square = state.squares.byPosition(from)
    val movedPieceType = (square as Square.Occupied).piece.type
    return movedPieceType == King && from.offsetOrNull(state.nextMoveColor.kingSideVector, 2) == to
}

internal fun Move.isCaptureByEnPassant(state: State): Boolean {
    val srcSquare = state.squares.byPosition(from)
    val movedPieceType = (srcSquare as Square.Occupied).piece.type
    return movedPieceType == Pawn && state.enPassantSquares.containsValue(to)
}

internal fun Move.isQueenSideCastling(state: State): Boolean {
    val srcSquare = state.squares.byPosition(from)
    val movedPieceType = (srcSquare as Square.Occupied).piece.type
    return movedPieceType == King && from.offsetOrNull(state.nextMoveColor.queenSideVector, 2) == to
}

internal fun Position.isPawnPromotionPositionForColor(color: Color) =
        when (color) {
            Red -> rank == PAWN_PROMOTION_RANK - 1
            Blue -> file == PAWN_PROMOTION_RANK - 1
            Yellow -> rank == BOARD_SIZE - PAWN_PROMOTION_RANK
            Green -> file == BOARD_SIZE - PAWN_PROMOTION_RANK
        }

internal fun Position.isAdjacentTo(other: Position) =
        allDirectionsVectors.any { vector -> this.offsetOrNull(vector) == other }

internal fun Position.isOnLineBetween(a: Position, b: Position): Boolean {
    if (a.file == b.file)
        return file == a.file && rank > min(a.rank, b.rank) && rank < max(a.rank, b.rank)
    if (a.rank == b.rank)
        return rank == a.rank && file > min(a.file, b.file) && file < max(a.file, b.file)
    return (rank - a.rank) * (b.file - a.file) == (b.rank - a.rank) * (file - a.file)
            && rank > min(a.rank, b.rank) && rank < max(a.rank, b.rank)
            && file > min(a.file, b.file) && file < max(a.file, b.file)
}

internal fun Position.isPawnStartingPositionForColor(color: Color) =
        when (color) {
            Red -> rank == 1
            Green -> rank == BOARD_SIZE - 2
            Blue -> file == 1
            Yellow -> file == BOARD_SIZE - 2
        }

internal fun Move.isPawnPromotion(state: State): Boolean {
    return this.isPawnAdvance(state) && to.isPawnPromotionPositionForColor(state.nextMoveColor)
}

internal fun Move.isCapture(state: State): Boolean {
    val targetSquare = state.squares.byPosition(to)
    return targetSquare is Square.Occupied
            && targetSquare.piece.color != state.nextMoveColor
}

internal fun Move.isPawnAdvance(state: State): Boolean {
    val srcSquare = state.squares.byPosition(from)
    val movedPieceType = (srcSquare as Square.Occupied).piece.type
    return movedPieceType == Pawn
}

internal fun squareOf(color: Color, pieceType: PieceType) =
        Square.Occupied.by(color, pieceType)

internal fun emptySquare() = Square.Empty