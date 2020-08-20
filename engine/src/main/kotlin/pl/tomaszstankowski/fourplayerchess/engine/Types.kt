package pl.tomaszstankowski.fourplayerchess.engine

import com.github.h0tk3y.betterParse.combinators.and
import com.github.h0tk3y.betterParse.combinators.map
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.tryParseToEnd
import com.github.h0tk3y.betterParse.parser.Parsed
import com.github.h0tk3y.betterParse.parser.Parser
import pl.tomaszstankowski.fourplayerchess.engine.Castling.KingSide
import pl.tomaszstankowski.fourplayerchess.engine.Castling.QueenSide
import pl.tomaszstankowski.fourplayerchess.engine.Color.*
import java.util.*

enum class Color {
    Red, Blue, Yellow, Green
}

enum class Castling {
    KingSide, QueenSide
}

private fun Castling.binary(color: Color): Int {
    val base = when (this) {
        KingSide -> 1
        QueenSide -> 2
    }
    return base shl (color.ordinal * 2)
}

internal typealias CastlingOptionsBits = Int

internal val castlingOptionsNone = emptySet<Castling>()
internal val castlingOptionsAny = setOf(KingSide, QueenSide)
internal val castlingOptionsKingSide = setOf(KingSide)
internal val castlingOptionsQueenSide = setOf(QueenSide)

internal operator fun CastlingOptionsBits.get(color: Color): Set<Castling> {
    val kingSideBinary = KingSide.binary(color)
    val queenSideBinary = QueenSide.binary(color)
    val anySideBinary = kingSideBinary or queenSideBinary
    if (this and anySideBinary == anySideBinary) {
        return castlingOptionsAny
    }
    if (this and queenSideBinary == queenSideBinary) {
        return castlingOptionsQueenSide
    }
    if (this and kingSideBinary == kingSideBinary) {
        return castlingOptionsKingSide
    }
    return castlingOptionsNone
}

internal fun CastlingOptionsBits.withCastlingForColor(color: Color, castling: Castling): CastlingOptionsBits =
        this or castling.binary(color)

internal fun CastlingOptionsBits.dropCastlingForColor(color: Color, castling: Castling): CastlingOptionsBits =
        (this and castling.binary(color).inv())

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

data class Piece internal constructor(val type: PieceType, val color: Color)

data class Coordinates internal constructor(val file: Int, val rank: Int) {

    companion object {
        private val availableCoordinates: Array<Array<Coordinates?>> = Array(BOARD_SIZE) { arrayOfNulls<Coordinates?>(BOARD_SIZE) }
        val allCoordinates: List<Coordinates>

        init {
            for (file in 0 until BOARD_SIZE)
                for (rank in DISABLED_AREA_SIZE until BOARD_SIZE - DISABLED_AREA_SIZE)
                    availableCoordinates[file][rank] = Coordinates(file, rank)
            for (file in DISABLED_AREA_SIZE until BOARD_SIZE - DISABLED_AREA_SIZE)
                for (rank in 0 until BOARD_SIZE)
                    availableCoordinates[file][rank] = Coordinates(file, rank)
            allCoordinates = availableCoordinates.flatten().filterNotNull()
        }

        private fun ofFileAndRankOrNull(file: Int, rank: Int): Coordinates? {
            if (file in 0 until BOARD_SIZE && rank in 0 until BOARD_SIZE)
                return availableCoordinates[file][rank]
            return null
        }

        fun ofFileAndRank(file: Int, rank: Int): Coordinates =
                ofFileAndRankOrNull(file, rank)
                        ?: throw illegalCoordinatesException(file, rank)

        private fun illegalCoordinatesException(file: Int, rank: Int) =
                IllegalArgumentException("Coordinates ($file,$rank) are out of board")

        fun parse(str: String): Coordinates =
                when (val result = CoordinatesGrammar.tryParseToEnd(str)) {
                    is Parsed -> result.value
                    else -> throw IllegalArgumentException("Invalid coordinates string: $result")
                }

        fun parseOrNull(str: String): Coordinates? =
                when (val result = CoordinatesGrammar.tryParseToEnd(str)) {
                    is Parsed -> result.value
                    else -> null
                }

        private object CoordinatesGrammar : Grammar<Coordinates>() {
            private val file by token("[a-n]")
            private val rank by token("1[0-4]|[1-9]")

            private val pFile by file map { tokenMatch -> tokenMatch.text[0] - 'a' }
            private val pRank by rank map { tokenMatch -> tokenMatch.text.toInt().dec() }

            override val rootParser: Parser<Coordinates>
                get() = pFile and pRank map { (file, rank) -> ofFileAndRank(file, rank) }
        }
    }

    private fun offsetOrNull(fileOffset: Int = 0, rankOffset: Int = 0): Coordinates? {
        val newFile = file + fileOffset
        val newRank = rank + rankOffset
        if (newFile in 0 until BOARD_SIZE && newRank in 0 until BOARD_SIZE)
            return availableCoordinates[newFile][newRank]
        return null
    }

    fun offset(vector: Vector, factor: Int = 1): Coordinates =
            offsetOrNull(vector, factor) ?: throw illegalCoordinatesException(vector.first, vector.second)

    fun offsetOrNull(vector: Vector, factor: Int = 1): Coordinates? {
        val fileOffset = vector.first * factor
        val rankOffset = vector.second * factor
        return offsetOrNull(fileOffset, rankOffset)
    }

    override fun toString() = "($file,$rank)"

    fun toHumanReadableString() = "${'a' + file}${rank.inc()}"

}

internal typealias EnPassantSquaresBits = Int

internal fun EnPassantSquaresBits.getEnPassantSquareByColor(color: Color): Coordinates? {
    val bitsForColor = (this shr (8 * color.ordinal)) and 0xff
    val indexOfBit = bitsForColor.indexOfSingleSetBit()
    if (indexOfBit == -1) {
        return null
    }
    val coord = DISABLED_AREA_SIZE + indexOfBit
    return when (color) {
        Red -> Coordinates.ofFileAndRank(coord, 2)
        Yellow -> Coordinates.ofFileAndRank(coord, BOARD_SIZE - 3)
        Blue -> Coordinates.ofFileAndRank(2, coord)
        Green -> Coordinates.ofFileAndRank(BOARD_SIZE - 3, coord)
    }
}

internal fun EnPassantSquaresBits.withEnPassantSquareForColor(color: Color, coords: Coordinates): EnPassantSquaresBits {
    val mask = 0xff shl (8 * color.ordinal)
    val cleared = this and mask.inv()
    val indexOfBit = when (color) {
        Red, Yellow -> coords.file - DISABLED_AREA_SIZE
        Blue, Green -> coords.rank - DISABLED_AREA_SIZE
    }
    val bitsForColor = (1 shl indexOfBit) shl (8 * color.ordinal)
    return cleared or bitsForColor
}

internal fun EnPassantSquaresBits.dropEnPassantSquareForColor(color: Color): EnPassantSquaresBits {
    val mask = 0xff shl (8 * color.ordinal)
    return this and mask.inv()
}

internal fun EnPassantSquaresBits.getColorByEnPassantSquare(coords: Coordinates): Color? =
        Color.values().firstOrNull { this.getEnPassantSquareByColor(it) == coords }

internal fun initialEnPassantSquares(): EnPassantSquaresBits = 0

sealed class Square {
    data class Occupied internal constructor(val piece: Piece) : Square() {

        companion object {

            private val squares = Array(PieceType.values().size) { i ->
                Array(Color.values().size) { j ->
                    Occupied(
                            Piece(
                                    type = PieceType.values()[i],
                                    color = Color.values()[j]
                            )
                    )
                }
            }

            fun by(color: Color, pieceType: PieceType): Occupied = squares[pieceType.ordinal][color.ordinal]
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

internal typealias Row = Array<Square?>

internal typealias Board = Array<Row>

internal fun Board.byCoordinates(coordinates: Coordinates): Square =
        this[coordinates.rank][coordinates.file]!!

internal fun Board.set(coords: Coordinates, square: Square) {
    this[coords.rank][coords.file] = square
}

internal typealias PieceList = LinkedList<Coordinates>

data class Check(
        val checkingPieceCoordinates: Coordinates,
        val checkedKingCoordinates: Coordinates
)

internal data class Pin(val pinningPieceCoordinates: Coordinates, val pinnedPieceCoordinates: Coordinates)

sealed class Move {
    abstract val from: Coordinates
    abstract val to: Coordinates
}

data class RegularMove(override val from: Coordinates, override val to: Coordinates) : Move()

data class Promotion(override val from: Coordinates,
                     override val to: Coordinates,
                     val pieceType: PromotionPieceType) : Move()

enum class PromotionPieceType {
    Queen, Rook, Bishop, Knight;

    fun toPieceType() = PieceType.valueOf(name)
}