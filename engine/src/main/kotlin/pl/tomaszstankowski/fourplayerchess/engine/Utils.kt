package pl.tomaszstankowski.fourplayerchess.engine

import pl.tomaszstankowski.fourplayerchess.engine.Color.*
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

internal val Color.defaultKingCoordinates: Coordinates
    get() = when (this) {
        Red -> Coordinates.ofFileAndRank(7, 0)
        Blue -> Coordinates.ofFileAndRank(0, 7)
        Yellow -> Coordinates.ofFileAndRank(6, 13)
        Green -> Coordinates.ofFileAndRank(13, 6)
    }

internal fun Coordinates.isPromotionSquareForColor(color: Color) =
        when (color) {
            Red -> rank == PAWN_PROMOTION_RANK - 1
            Blue -> file == PAWN_PROMOTION_RANK - 1
            Yellow -> rank == BOARD_SIZE - PAWN_PROMOTION_RANK
            Green -> file == BOARD_SIZE - PAWN_PROMOTION_RANK
        }

internal fun Coordinates.isAdjacentTo(other: Coordinates) =
        allDirectionsVectors.any { vector -> this.offsetOrNull(vector) == other }

internal fun Coordinates.isOnLineBetween(a: Coordinates, b: Coordinates): Boolean {
    if (a.file == b.file)
        return file == a.file && rank > min(a.rank, b.rank) && rank < max(a.rank, b.rank)
    if (a.rank == b.rank)
        return rank == a.rank && file > min(a.file, b.file) && file < max(a.file, b.file)
    return (rank - a.rank) * (b.file - a.file) == (b.rank - a.rank) * (file - a.file)
            && rank > min(a.rank, b.rank) && rank < max(a.rank, b.rank)
            && file > min(a.file, b.file) && file < max(a.file, b.file)
}

internal val Coordinates.isLightSquare: Boolean
    get() = (file + rank) % 2 == 0

internal fun Coordinates.isOnPawnStartingRowForColor(color: Color) =
        when (color) {
            Red -> rank == 1
            Green -> file == BOARD_SIZE - 2
            Blue -> file == 1
            Yellow -> rank == BOARD_SIZE - 2
        }

internal fun getRookCoordinatesBeforeCastling(color: Color, castling: Castling): Coordinates =
        when (castling) {
            Castling.KingSide -> color.defaultKingCoordinates.offset(color.kingSideVector, 3)
            Castling.QueenSide -> color.defaultKingCoordinates.offset(color.queenSideVector, 4)
        }

internal fun getRookCoordinatesAfterCastling(color: Color, castling: Castling): Coordinates =
        when (castling) {
            Castling.KingSide -> color.defaultKingCoordinates.offset(color.kingSideVector, 1)
            Castling.QueenSide -> color.defaultKingCoordinates.offset(color.queenSideVector, 1)
        }

internal fun Int.countSetBits(): Int {
    var count = 0
    var n = this
    while (n > 0) {
        count += n and 1
        n = n shr 1
    }
    return count
}

internal fun Int.indexOfSingleSetBit(): Int =
        when (this) {
            0x01 -> 0
            0x02 -> 1
            0x04 -> 2
            0x08 -> 3
            0x10 -> 4
            0x20 -> 5
            0x40 -> 6
            0x80 -> 7
            0x0100 -> 8
            0x0200 -> 9
            0x0400 -> 10
            0x0800 -> 11
            0x1000 -> 12
            0x2000 -> 13
            0x4000 -> 14
            0x8000 -> 15
            0x010000 -> 16
            0x020000 -> 17
            0x040000 -> 18
            0x080000 -> 19
            0x100000 -> 20
            0x200000 -> 21
            0x400000 -> 22
            0x800000 -> 23
            0x01000000 -> 24
            0x02000000 -> 25
            0x04000000 -> 26
            0x08000000 -> 27
            0x10000000 -> 28
            0x20000000 -> 29
            0x40000000 -> 30
            -0x80000000 -> 31
            else -> -1
        }