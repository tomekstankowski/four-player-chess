package pl.tomaszstankowski.fourplayerchess.engine

import gnu.trove.list.linked.TIntLinkedList
import pl.tomaszstankowski.fourplayerchess.engine.Castling.KingSide
import pl.tomaszstankowski.fourplayerchess.engine.Castling.QueenSide
import pl.tomaszstankowski.fourplayerchess.engine.Color.*

enum class Color {
    Red, Blue, Yellow, Green
}

internal val allColors = Color.values()

enum class Castling {
    KingSide, QueenSide
}

internal val allCastlings = Castling.values()

private val castlingBit = arrayOf(
        intArrayOf(1, 2),
        intArrayOf(4, 8),
        intArrayOf(16, 32),
        intArrayOf(64, 128)
)

internal typealias CastlingOptionsBits = Int

internal val castlingOptionsNone = emptySet<Castling>()
internal val castlingOptionsAny = setOf(KingSide, QueenSide)
internal val castlingOptionsKingSide = setOf(KingSide)
internal val castlingOptionsQueenSide = setOf(QueenSide)

internal operator fun CastlingOptionsBits.get(color: Color): Set<Castling> {
    val kingSideBit = castlingBit[color.ordinal][KingSide.ordinal]
    val queenSideBit = castlingBit[color.ordinal][QueenSide.ordinal]
    val anySideBits = kingSideBit or queenSideBit

    if (this and anySideBits == anySideBits) {
        return castlingOptionsAny
    }
    if (this and queenSideBit == queenSideBit) {
        return castlingOptionsQueenSide
    }
    if (this and kingSideBit == kingSideBit) {
        return castlingOptionsKingSide
    }
    return castlingOptionsNone
}

internal fun CastlingOptionsBits.withCastlingForColor(color: Color, castling: Castling): CastlingOptionsBits =
        this or castlingBit[color.ordinal][castling.ordinal]

internal fun CastlingOptionsBits.dropCastlingForColor(color: Color, castling: Castling): CastlingOptionsBits =
        this and castlingBit[color.ordinal][castling.ordinal].inv()

private const val EMPTY_CASTLING_OPTIONS = 0x00

internal fun emptyCastlingOptions() = EMPTY_CASTLING_OPTIONS

internal typealias EliminatedColorsBits = Int

private fun Color.asEliminatedColorFlag() = 1 shl ordinal

internal fun EliminatedColorsBits.withColorEliminated(color: Color): EliminatedColorsBits = this or color.asEliminatedColorFlag()

internal fun EliminatedColorsBits.isEliminated(color: Color): Boolean = (this and color.asEliminatedColorFlag()) != 0

internal val EliminatedColorsBits.eliminatedColorsCount: Int
    get() = this.countSetBits()

internal fun initialEliminatedColors() = 0x00

enum class PieceType {
    Pawn, Knight, Bishop, Rook, Queen, King
}

internal val allPieceTypes = PieceType.values()

data class Piece internal constructor(val type: PieceType, val color: Color)

internal typealias EnPassantSquaresBits = Int

internal fun EnPassantSquaresBits.getEnPassantSquareByColor(color: Color): Int {
    val bitsForColor = (this shr (8 * color.ordinal)) and 0xff
    val indexOfBit = bitsForColor.indexOfSingleSetBitOfFirstByte()
    if (indexOfBit == -1) {
        return NULL_SQUARE
    }
    val coord = DISABLED_AREA_SIZE + indexOfBit
    return when (color) {
        Red -> indexOfSquare(coord, 2)
        Yellow -> indexOfSquare(coord, BOARD_SIZE - 3)
        Blue -> indexOfSquare(2, coord)
        Green -> indexOfSquare(BOARD_SIZE - 3, coord)
    }
}

internal fun EnPassantSquaresBits.withEnPassantSquareForColor(color: Color, squareIndex: Int): EnPassantSquaresBits {
    val mask = 0xff shl (8 * color.ordinal)
    val cleared = this and mask.inv()
    val indexOfBit = when (color) {
        Red, Yellow -> squareFile(squareIndex) - DISABLED_AREA_SIZE
        Blue, Green -> squareRank(squareIndex) - DISABLED_AREA_SIZE
    }
    val bitsForColor = (1 shl indexOfBit) shl (8 * color.ordinal)
    return cleared or bitsForColor
}

internal fun EnPassantSquaresBits.dropEnPassantSquareForColor(color: Color): EnPassantSquaresBits {
    val mask = 0xff shl (8 * color.ordinal)
    return this and mask.inv()
}

internal fun EnPassantSquaresBits.getColorByEnPassantSquare(squareIndex: Int): Color? =
        allColors.firstOrNull { this.getEnPassantSquareByColor(it) == squareIndex }

internal fun initialEnPassantSquares(): EnPassantSquaresBits = 0

sealed class Square {
    data class Occupied internal constructor(val piece: Piece) : Square() {

        companion object {

            private val squares = Array(allPieceTypes.size) { i ->
                Array(allColors.size) { j ->
                    Occupied(
                            Piece(
                                    type = allPieceTypes[i],
                                    color = allColors[j]
                            )
                    )
                }
            }

            fun by(color: Color, pieceType: PieceType): Occupied = squares[pieceType.ordinal][color.ordinal]

            fun by(piece: Piece): Occupied = squares[piece.type.ordinal][piece.color.ordinal]
        }
    }

    object Empty : Square() {

        override fun toString(): String {
            return "Empty"
        }
    }
}

internal fun squareOf(color: Color, pieceType: PieceType) =
        Square.Occupied.by(color, pieceType)

internal fun emptySquare() = Square.Empty

// avoiding primitives boxing
internal typealias PieceList = TIntLinkedList

internal typealias CheckBits = Short

internal fun checkOf(checkingPieceSquareIndex: Int, checkedPieceSquareIndex: Int): CheckBits {
    val result = (checkedPieceSquareIndex shl 8) or checkingPieceSquareIndex
    return result.toShort()
}

internal val CheckBits.checkingPieceSquareIndex: Int
    get() = this.toInt() and 0xff

internal val CheckBits.checkedPieceSquareIndex: Int
    get() = (this.toInt() shr 8) and 0xff

internal typealias PinBits = Short

internal fun pinOf(pinningPieceSquareIndex: Int, pinnedPieceSquareIndex: Int): PinBits {
    val result = (pinnedPieceSquareIndex shl 8) or pinningPieceSquareIndex
    return result.toShort()
}

internal val PinBits.pinningPieceSquareIndex: Int
    get() = this.toInt() and 0xff

internal val PinBits.pinnedPieceSquareIndex: Int
    get() = (this.toInt() shr 8) and 0xff

internal typealias MoveBits = Int

internal const val NULL_MOVE = -1

internal fun moveOf(fromSquareIndex: Int, toSquareIndex: Int): MoveBits =
        (toSquareIndex shl 8) or fromSquareIndex

internal fun moveOf(fromSquareIndex: Int, toSquareIndex: Int, promotionPieceType: PromotionPieceType): MoveBits {
    val pieceTypeBit = when (promotionPieceType) {
        PromotionPieceType.Queen -> 1
        PromotionPieceType.Rook -> 2
        PromotionPieceType.Bishop -> 4
        PromotionPieceType.Knight -> 8
    }
    return (pieceTypeBit shl 16) or (toSquareIndex shl 8) or fromSquareIndex
}

internal val MoveBits.from: Int
    get() = this and 0xff

internal val MoveBits.to: Int
    get() = this and 0xff00 shr 8

internal val MoveBits.promotionPieceType: PromotionPieceType?
    get() = when ((this and 0xff0000) shr 16) {
        1 -> PromotionPieceType.Queen
        2 -> PromotionPieceType.Rook
        4 -> PromotionPieceType.Bishop
        8 -> PromotionPieceType.Knight
        else -> null
    }

enum class PromotionPieceType {
    Queen, Rook, Bishop, Knight;

    fun toPieceType() =
            when (this) {
                Queen -> PieceType.Queen
                Rook -> PieceType.Rook
                Bishop -> PieceType.Bishop
                Knight -> PieceType.Knight
            }
}

internal data class PVMove(val move: MoveBits, val moveText: String)