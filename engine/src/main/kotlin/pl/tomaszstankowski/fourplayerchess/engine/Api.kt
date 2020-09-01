package pl.tomaszstankowski.fourplayerchess.engine

import com.github.h0tk3y.betterParse.combinators.and
import com.github.h0tk3y.betterParse.combinators.map
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.tryParseToEnd
import com.github.h0tk3y.betterParse.parser.ErrorResult
import com.github.h0tk3y.betterParse.parser.Parsed
import com.github.h0tk3y.betterParse.parser.Parser

data class Coordinates internal constructor(val file: Int, val rank: Int) {

    companion object {
        private val availableCoordinates: Array<Array<Coordinates?>> = Array(BOARD_SIZE) { arrayOfNulls<Coordinates?>(BOARD_SIZE) }
        private val allCoordinates: List<Coordinates>

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

        internal fun ofSquareIndex(index: Int): Coordinates =
                ofFileAndRank(squareFile(index), squareRank(index))

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

    internal val squareIndex: Int
        get() = rank * BOARD_SIZE + file

    override fun toString() = "($file,$rank)"

    fun toHumanReadableString() = "${'a' + file}${rank.inc()}"

}

data class Check(
        val checkingPieceCoordinates: Coordinates,
        val checkedKingCoordinates: Coordinates
)

internal fun CheckBits.toApiCheck() =
        Check(
                checkingPieceCoordinates = Coordinates.ofFileAndRank(
                        file = squareFile(this.checkingPieceSquareIndex),
                        rank = squareRank(this.checkingPieceSquareIndex)
                ),
                checkedKingCoordinates = Coordinates.ofFileAndRank(
                        file = squareFile(this.checkedPieceSquareIndex),
                        rank = squareRank(this.checkedPieceSquareIndex)
                )
        )

sealed class Move {
    abstract val from: Coordinates
    abstract val to: Coordinates
}

data class RegularMove(override val from: Coordinates, override val to: Coordinates) : Move()

data class Promotion(override val from: Coordinates,
                     override val to: Coordinates,
                     val pieceType: PromotionPieceType) : Move()

internal fun MoveBits.toApiMove(): Move {
    val promotionPieceType = this.promotionPieceType
    if (promotionPieceType != null) {
        return Promotion(
                from = Coordinates.ofSquareIndex(this.from),
                to = Coordinates.ofSquareIndex(this.to),
                pieceType = promotionPieceType)
    }
    return RegularMove(
            from = Coordinates.ofSquareIndex(this.from),
            to = Coordinates.ofSquareIndex(this.to)
    )
}

internal fun Move.toBits(): MoveBits =
        when (this) {
            is RegularMove -> moveOf(from.squareIndex, to.squareIndex)
            is Promotion -> moveOf(from.squareIndex, to.squareIndex, pieceType)
        }

sealed class ParseStateFromFenResult {
    data class Parsed(val state: FenState) : ParseStateFromFenResult()
    data class ParseError(val message: String) : ParseStateFromFenResult()
    sealed class IllegalState : ParseStateFromFenResult() {
        data class IllegalRowLength(val length: Int) : IllegalState()
        data class IllegalKingCount(val color: Color, val count: Int) : IllegalState()
        data class PiecesWithoutKing(val color: Color) : IllegalState()
    }
}

internal typealias Row = Array<Square?>

internal typealias Board = Array<Row>

data class FenState(
        val board: Board,
        val eliminatedColors: Set<Color>,
        val nextMoveColor: Color,
        val enPassantSquares: Map<Color, Coordinates>,
        val castlingOptions: Map<Color, Set<Castling>>,
        val plyCount: Int
) {

    companion object {

        fun parseFromFen(input: String): ParseStateFromFenResult {
            return when (val result = FenGrammar.tryParseToEnd(input)) {
                is Parsed -> ParseStateFromFenResult.Parsed(result.value)
                is IllegalRowLength -> ParseStateFromFenResult.IllegalState.IllegalRowLength(result.length)
                is IllegalKingCount -> ParseStateFromFenResult.IllegalState.IllegalKingCount(
                        result.color, result.count)
                is PiecesWithoutKing -> ParseStateFromFenResult.IllegalState.PiecesWithoutKing(result.color)
                is ErrorResult -> ParseStateFromFenResult.ParseError(result.toString())
            }
        }

        fun starting(): FenState =
                FenState(
                        board = arrayOf<Row>(
                                arrayOf(null, null, null, squareOf(Color.Red, PieceType.Rook), squareOf(Color.Red, PieceType.Knight), squareOf(Color.Red, PieceType.Bishop), squareOf(Color.Red, PieceType.Queen), squareOf(Color.Red, PieceType.King), squareOf(Color.Red, PieceType.Bishop), squareOf(Color.Red, PieceType.Knight), squareOf(Color.Red, PieceType.Rook), null, null, null),
                                arrayOf(null, null, null, squareOf(Color.Red, PieceType.Pawn), squareOf(Color.Red, PieceType.Pawn), squareOf(Color.Red, PieceType.Pawn), squareOf(Color.Red, PieceType.Pawn), squareOf(Color.Red, PieceType.Pawn), squareOf(Color.Red, PieceType.Pawn), squareOf(Color.Red, PieceType.Pawn), squareOf(Color.Red, PieceType.Pawn), null, null, null),
                                arrayOf(null, null, null, emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), null, null, null),
                                arrayOf(squareOf(Color.Blue, PieceType.Rook), squareOf(Color.Blue, PieceType.Pawn), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), squareOf(Color.Green, PieceType.Pawn), squareOf(Color.Green, PieceType.Rook)),
                                arrayOf(squareOf(Color.Blue, PieceType.Knight), squareOf(Color.Blue, PieceType.Pawn), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), squareOf(Color.Green, PieceType.Pawn), squareOf(Color.Green, PieceType.Knight)),
                                arrayOf(squareOf(Color.Blue, PieceType.Bishop), squareOf(Color.Blue, PieceType.Pawn), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), squareOf(Color.Green, PieceType.Pawn), squareOf(Color.Green, PieceType.Bishop)),
                                arrayOf(squareOf(Color.Blue, PieceType.Queen), squareOf(Color.Blue, PieceType.Pawn), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), squareOf(Color.Green, PieceType.Pawn), squareOf(Color.Green, PieceType.King)),
                                arrayOf(squareOf(Color.Blue, PieceType.King), squareOf(Color.Blue, PieceType.Pawn), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), squareOf(Color.Green, PieceType.Pawn), squareOf(Color.Green, PieceType.Queen)),
                                arrayOf(squareOf(Color.Blue, PieceType.Bishop), squareOf(Color.Blue, PieceType.Pawn), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), squareOf(Color.Green, PieceType.Pawn), squareOf(Color.Green, PieceType.Bishop)),
                                arrayOf(squareOf(Color.Blue, PieceType.Knight), squareOf(Color.Blue, PieceType.Pawn), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), squareOf(Color.Green, PieceType.Pawn), squareOf(Color.Green, PieceType.Knight)),
                                arrayOf(squareOf(Color.Blue, PieceType.Rook), squareOf(Color.Blue, PieceType.Pawn), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), squareOf(Color.Green, PieceType.Pawn), squareOf(Color.Green, PieceType.Rook)),
                                arrayOf(null, null, null, emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), null, null, null),
                                arrayOf(null, null, null, squareOf(Color.Yellow, PieceType.Pawn), squareOf(Color.Yellow, PieceType.Pawn), squareOf(Color.Yellow, PieceType.Pawn), squareOf(Color.Yellow, PieceType.Pawn), squareOf(Color.Yellow, PieceType.Pawn), squareOf(Color.Yellow, PieceType.Pawn), squareOf(Color.Yellow, PieceType.Pawn), squareOf(Color.Yellow, PieceType.Pawn), null, null, null),
                                arrayOf(null, null, null, squareOf(Color.Yellow, PieceType.Rook), squareOf(Color.Yellow, PieceType.Knight), squareOf(Color.Yellow, PieceType.Bishop), squareOf(Color.Yellow, PieceType.King), squareOf(Color.Yellow, PieceType.Queen), squareOf(Color.Yellow, PieceType.Bishop), squareOf(Color.Yellow, PieceType.Knight), squareOf(Color.Yellow, PieceType.Rook), null, null, null)
                        ),
                        eliminatedColors = emptySet(),
                        nextMoveColor = Color.Red,
                        enPassantSquares = emptyMap(),
                        castlingOptions = allColors.map { color -> color to setOf(Castling.KingSide, Castling.QueenSide) }.toMap(),
                        plyCount = 0
                )

    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FenState

        if (!board.contentDeepEquals(other.board)) return false
        if (eliminatedColors != other.eliminatedColors) return false
        if (nextMoveColor != other.nextMoveColor) return false
        if (enPassantSquares != other.enPassantSquares) return false
        if (castlingOptions != other.castlingOptions) return false
        if (plyCount != other.plyCount) return false

        return true
    }

    override fun hashCode(): Int {
        var result = board.contentDeepHashCode()
        result = 31 * result + eliminatedColors.hashCode()
        result = 31 * result + nextMoveColor.hashCode()
        result = 31 * result + enPassantSquares.hashCode()
        result = 31 * result + castlingOptions.hashCode()
        result = 31 * result + plyCount
        return result
    }

    fun toFen(): String = formatState(this)
}

data class UIState(
        val fenState: FenState,
        val checks: Map<Color, Set<Check>>,
        val isDrawByClaimAllowed: Boolean,
        val isGameOver: Boolean,
        val winningColor: Color?,
        val legalMoves: Set<Move>
)

data class StateEvaluation(val principalVariation: List<PVMove>, val value: Int) {
    data class PVMove(
            val move: Move,
            val moveText: String)
}