package pl.tomaszstankowski.fourplayerchess.engine

import gnu.trove.list.TIntList
import gnu.trove.list.TShortList
import pl.tomaszstankowski.fourplayerchess.engine.Color.*
import kotlin.math.max
import kotlin.math.min

val mailbox = intArrayOf(
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, 3, 4, 5, 6, 7, 8, 9, 10, -1, -1, -1, -1,
        -1, -1, -1, -1, 17, 18, 19, 20, 21, 22, 23, 24, -1, -1, -1, -1,
        -1, -1, -1, -1, 31, 32, 33, 34, 35, 36, 37, 38, -1, -1, -1, -1,
        -1, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, -1,
        -1, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, -1,
        -1, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, -1,
        -1, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, -1,
        -1, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, -1,
        -1, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, -1,
        -1, 126, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 139, -1,
        -1, 140, 141, 142, 143, 144, 145, 146, 147, 148, 149, 150, 151, 152, 153, -1,
        -1, -1, -1, -1, 157, 158, 159, 160, 161, 162, 163, 164, -1, -1, -1, -1,
        -1, -1, -1, -1, 171, 172, 173, 174, 175, 176, 177, 178, -1, -1, -1, -1,
        -1, -1, -1, -1, 185, 186, 187, 188, 189, 190, 191, 192, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
)

val mailbox14x14 = intArrayOf(
        33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46,
        49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62,
        65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78,
        81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94,
        97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110,
        113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 126,
        129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 139, 140, 141, 142,
        145, 146, 147, 148, 149, 150, 151, 152, 153, 154, 155, 156, 157, 158,
        161, 162, 163, 164, 165, 166, 167, 168, 169, 170, 171, 172, 173, 174,
        177, 178, 179, 180, 181, 182, 183, 184, 185, 186, 187, 188, 189, 190,
        193, 194, 195, 196, 197, 198, 199, 200, 201, 202, 203, 204, 205, 206,
        209, 210, 211, 212, 213, 214, 215, 216, 217, 218, 219, 220, 221, 222,
        225, 226, 227, 228, 229, 230, 231, 232, 233, 234, 235, 236, 237, 238,
        241, 242, 243, 244, 245, 246, 247, 248, 249, 250, 251, 252, 253, 255
)

private const val topOffset = BOARD_SIZE + 2
private const val bottomOffset = -(BOARD_SIZE + 2)
private const val rightOffset = 1
private const val leftOffset = -1
private const val topRightOffset = BOARD_SIZE + 2 + 1
private const val topLeftOffset = BOARD_SIZE + 2 - 1
private const val bottomRightOffset = -(BOARD_SIZE + 2) + 1
private const val bottomLeftOffset = -(BOARD_SIZE + 2) - 1

internal val allDirectionsOffsets = intArrayOf(
        topOffset,
        topRightOffset,
        rightOffset,
        bottomRightOffset,
        bottomOffset,
        bottomLeftOffset,
        leftOffset, topLeftOffset)

internal val Color.pawnForwardOffset: Int
    get() = when (this) {
        Red -> topOffset
        Blue -> rightOffset
        Yellow -> bottomOffset
        Green -> leftOffset
    }

private val redPawnCapturingOffsets = intArrayOf(topLeftOffset, topRightOffset)
private val bluePawnCapturingOffsets = intArrayOf(topRightOffset, bottomRightOffset)
private val yellowPawnCapturingOffsets = intArrayOf(bottomRightOffset, bottomLeftOffset)
private val greenPawnCapturingOffsets = intArrayOf(bottomLeftOffset, topLeftOffset)

internal val pawnAttackOffsets = arrayOf(
        redPawnCapturingOffsets,
        bluePawnCapturingOffsets,
        yellowPawnCapturingOffsets,
        greenPawnCapturingOffsets
)

internal val bishopMoveOffsets = intArrayOf(topRightOffset, topLeftOffset, bottomLeftOffset, bottomRightOffset)

internal val knightMoveOffsets = intArrayOf(
        rightOffset + 2 * topOffset,
        2 * rightOffset + topOffset,
        2 * rightOffset + bottomOffset,
        rightOffset + 2 * bottomOffset,
        leftOffset + 2 * bottomOffset,
        2 * leftOffset + bottomOffset,
        2 * leftOffset + topOffset,
        leftOffset + 2 * topOffset
)

internal val rookMoveOffsets = intArrayOf(topOffset, rightOffset, bottomOffset, leftOffset)

internal fun offsetSquareBy(squareIndex: Int, mailboxOffset: Int) =
        mailbox[mailbox14x14[squareIndex] + mailboxOffset]

internal val Color.kingSideOffset: Int
    get() = when (this) {
        Red -> rightOffset
        Blue -> topOffset
        Yellow -> leftOffset
        Green -> bottomOffset
    }

internal val Color.queenSideOffset: Int
    get() = when (this) {
        Red -> leftOffset
        Blue -> bottomOffset
        Yellow -> rightOffset
        Green -> topOffset
    }

internal val castlingSideOffsets = arrayOf(
        intArrayOf(rightOffset, leftOffset),
        intArrayOf(topOffset, bottomOffset),
        intArrayOf(leftOffset, rightOffset),
        intArrayOf(bottomOffset, topOffset)
)

internal val Color.defaultKingSquare: Int
    get() = when (this) {
        Red -> 7
        Blue -> 98
        Yellow -> 188
        Green -> 97
    }

internal val kingSquareAfterCastling = arrayOf(
        intArrayOf(9, 5), // red
        intArrayOf(126, 70), // blue
        intArrayOf(186, 190), // yellow
        intArrayOf(69, 125) // green
)

internal fun isLightSquare(index: Int) = (squareFile(index) + squareRank(index)) % 2 == 0

internal fun indexOfSquare(file: Int, rank: Int) = BOARD_SIZE * rank + file

internal fun squareFile(index: Int) = index % BOARD_SIZE

internal fun squareRank(index: Int) = index / BOARD_SIZE

internal fun isPromotionSquareForColor(index: Int, color: Color) =
        when (color) {
            Red -> squareRank(index) == PAWN_PROMOTION_RANK - 1
            Blue -> squareFile(index) == PAWN_PROMOTION_RANK - 1
            Yellow -> squareRank(index) == BOARD_SIZE - PAWN_PROMOTION_RANK
            Green -> squareFile(index) == BOARD_SIZE - PAWN_PROMOTION_RANK
        }

internal fun areSquaresAdjacent(firstSquareIndex: Int, secondSquareIndex: Int) =
        allDirectionsOffsets.any { offset -> offsetSquareBy(firstSquareIndex, offset) == secondSquareIndex }

internal fun isSquareOnLineBetween(x: Int, a: Int, b: Int): Boolean {
    val xF = squareFile(x)
    val xR = squareRank(x)
    val aF = squareFile(a)
    val aR = squareRank(a)
    val bF = squareFile(b)
    val bR = squareRank(b)
    if (aF == bF) {
        return xF == aF && xR > min(aR, bR) && xR < max(aR, bR)
    }
    if (aR == bR) {
        return xR == aR && xF > min(aF, bF) && xF < max(aF, bF)
    }
    return (xR - aR) * (bF - aF) == (bR - aR) * (xF - aF)
            && xR > min(aR, bR) && xR < max(aR, bR)
            && xF > min(aF, bF) && xF < max(aF, bF)
}

internal fun isSquareOnStartingPawnRowForColor(squareIndex: Int, color: Color) =
        when (color) {
            Red -> squareRank(squareIndex) == 1
            Blue -> squareFile(squareIndex) == 1
            Yellow -> squareRank(squareIndex) == BOARD_SIZE - 2
            Green -> squareFile(squareIndex) == BOARD_SIZE - 2
        }

internal val rookSquareBeforeCastling = arrayOf(
        intArrayOf(10, 3), // red
        intArrayOf(140, 42), // blue
        intArrayOf(185, 192), // yellow
        intArrayOf(55, 153) // green
)

internal val rookSquareAfterCastling = arrayOf(
        intArrayOf(8, 6), // red
        intArrayOf(112, 84), // blue
        intArrayOf(187, 189), // yellow
        intArrayOf(83, 111) // green
)

internal fun Int.countSetBits(): Int {
    var count = 0
    var n = this
    while (n > 0) {
        count += n and 1
        n = n shr 1
    }
    return count
}

internal fun Int.indexOfSingleSetBitOfFirstByte(): Int =
        when (this) {
            0x01 -> 0
            0x02 -> 1
            0x04 -> 2
            0x08 -> 3
            0x10 -> 4
            0x20 -> 5
            0x40 -> 6
            0x80 -> 7
            else -> -1
        }

internal inline fun TShortList.any(crossinline predicate: (Short) -> Boolean): Boolean =
        !this.forEach { item -> !predicate(item) }

internal inline fun TIntList.forEachDo(crossinline func: (Int) -> Unit) {
    this.forEach { item ->
        func(item)
        true
    }
}

internal val PieceType.materialValue: Int
    get() = when (this) {
        PieceType.Pawn -> 100
        PieceType.Knight -> 300
        PieceType.Bishop -> 500
        PieceType.Rook -> 500
        PieceType.Queen -> 900
        PieceType.King -> 2000
    }