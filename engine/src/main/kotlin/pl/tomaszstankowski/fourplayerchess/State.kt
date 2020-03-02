package pl.tomaszstankowski.fourplayerchess

import com.github.h0tk3y.betterParse.combinators.and
import com.github.h0tk3y.betterParse.combinators.map
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.tryParseToEnd
import com.github.h0tk3y.betterParse.parser.ErrorResult
import com.github.h0tk3y.betterParse.parser.Parsed
import com.github.h0tk3y.betterParse.parser.Parser
import pl.tomaszstankowski.fourplayerchess.Castling.KingSide
import pl.tomaszstankowski.fourplayerchess.Castling.QueenSide
import pl.tomaszstankowski.fourplayerchess.Color.*
import pl.tomaszstankowski.fourplayerchess.PieceType.*

enum class Color {
    Red, Blue, Green, Yellow
}

enum class Castling {
    KingSide, QueenSide
}

data class CastlingOptions(private val colorToCastlingSet: Map<Color, Set<Castling>>) {

    operator fun get(color: Color): Set<Castling> = colorToCastlingSet[color] ?: emptySet()

    companion object {
        fun default() = CastlingOptions(
                Color.values().map { color ->
                    color to setOf(QueenSide, KingSide)
                }.toMap()
        )
    }
}

enum class PieceType {
    Pawn, Knight, Bishop, Rook, Queen, King
}

data class Piece internal constructor(val type: PieceType, val color: Color) {

    companion object {
        val allPieces = PieceType.values().map { pieceType ->
            Color.values().map { color ->
                Piece(pieceType, color)
            }
        }
                .flatten()

        fun get(type: PieceType, color: Color) = allPieces.first { it.type == type && it.color == color }
    }
}

data class Position internal constructor(val file: Int, val rank: Int) {

    companion object {
        private val availablePositions: Array<Array<Position?>> = Array(BOARD_SIZE) { arrayOfNulls<Position?>(BOARD_SIZE) }
        val allPositions: List<Position>

        init {
            for (file in 0 until BOARD_SIZE)
                for (rank in DISABLED_AREA_SIZE until BOARD_SIZE - DISABLED_AREA_SIZE)
                    availablePositions[file][rank] = Position(file, rank)
            for (file in DISABLED_AREA_SIZE until BOARD_SIZE - DISABLED_AREA_SIZE)
                for (rank in 0 until BOARD_SIZE)
                    availablePositions[file][rank] = Position(file, rank)
            allPositions = availablePositions.flatten().filterNotNull()
        }

        private fun ofFileAndRankOrNull(file: Int, rank: Int): Position? {
            if (file in 0 until BOARD_SIZE && rank in 0 until BOARD_SIZE)
                return availablePositions[file][rank]
            return null
        }

        fun ofFileAndRank(file: Int, rank: Int): Position =
                ofFileAndRankOrNull(file, rank) ?: throw illegalPositionException(file, rank)

        private fun illegalPositionException(file: Int, rank: Int) =
                IllegalArgumentException("Position ($file,$rank) is out of board")

        fun parse(str: String): Position =
                when (val result = PositionGrammar.tryParseToEnd(str)) {
                    is Parsed -> result.value
                    else -> throw IllegalArgumentException("Invalid position string: $result")
                }

        private object PositionGrammar : Grammar<Position>() {
            private val file by token("[a-n]")
            private val rank by token("1[0-4]|[1-9]")

            private val pFile by file map { tokenMatch -> tokenMatch.text[0] - 'a' }
            private val pRank by rank map { tokenMatch -> tokenMatch.text.toInt().dec() }

            override val rootParser: Parser<Position>
                get() = pFile and pRank map { (file, rank) -> ofFileAndRank(file, rank) }
        }
    }

    fun offset(fileOffset: Int = 0, rankOffset: Int = 0): Position =
            offsetOrNull(fileOffset, rankOffset) ?: throw illegalPositionException(fileOffset, rankOffset)

    fun offsetOrNull(fileOffset: Int = 0, rankOffset: Int = 0): Position? {
        val newFile = file + fileOffset
        val newRank = rank + rankOffset
        if (newFile in 0 until BOARD_SIZE && newRank in 0 until BOARD_SIZE)
            return availablePositions[newFile][newRank]
        return null
    }

    fun offset(vector: Pair<Int, Int>): Position =
            offsetOrNull(vector) ?: throw illegalPositionException(vector.first, vector.second)

    fun offsetOrNull(vector: Pair<Int, Int>): Position? {
        val (fileOffset, rankOffset) = vector
        return offsetOrNull(fileOffset, rankOffset)
    }


    override fun toString() = "($file,$rank)"

    fun toHumanReadableString() = "(${file.inc().toChar()},${rank.inc()})"

}

typealias EnPassantSquares = Map<Color, Position>

sealed class Square {
    data class Occupied internal constructor(val piece: Piece) : Square() {
        companion object {
            val allSquares = Piece.allPieces.map { piece -> Occupied(piece) }
        }
    }

    object Empty : Square()
}

fun squareOf(color: Color, pieceType: PieceType) =
        Square.Occupied.allSquares.first { square -> square.piece == Piece.get(pieceType, color) }

fun emptySquare() = Square.Empty

typealias Row = List<Square>

typealias Board = List<Row>

fun Board.getSquareByPosition(position: Position): Square =
        this[position.rank][position.file]

data class PlyCount internal constructor(val count: Int) {

    companion object {
        fun of(count: Int): PlyCount {
            check(count >= 0) { "Ply count must be a positive integer" }
            return PlyCount(count)
        }
    }
}

sealed class ParseStateFromFenResult {
    data class Parsed(val state: State) : ParseStateFromFenResult()
    data class ParseError(val message: String) : ParseStateFromFenResult()
    sealed class IllegalState : ParseStateFromFenResult() {
        data class IllegalRowLength(val length: Int) : IllegalState()
        data class IllegalKingCount(val color: Color, val count: Int) : IllegalState()
        data class PiecesWithoutKing(val color: Color) : IllegalState()
    }
}

data class State(
        val squares: Board,
        val nextMoveColor: Color,
        val enPassantSquares: EnPassantSquares,
        val colorToCastlingOptions: CastlingOptions,
        val plyCount: PlyCount
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

        fun starting(): State =
                State(
                        squares = listOf(
                                listOf(emptySquare(), emptySquare(), emptySquare(), squareOf(Red, Rook), squareOf(Red, Knight), squareOf(Red, Bishop), squareOf(Red, Queen), squareOf(Red, King), squareOf(Red, Bishop), squareOf(Red, Knight), squareOf(Red, Rook), emptySquare(), emptySquare(), emptySquare()),
                                listOf(emptySquare(), emptySquare(), emptySquare(), squareOf(Red, Pawn), squareOf(Red, Pawn), squareOf(Red, Pawn), squareOf(Red, Pawn), squareOf(Red, Pawn), squareOf(Red, Pawn), squareOf(Red, Pawn), squareOf(Red, Pawn), emptySquare(), emptySquare(), emptySquare()),
                                listOf<Square>(emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare()),
                                listOf(squareOf(Blue, Rook), squareOf(Blue, Pawn), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), squareOf(Green, Pawn), squareOf(Green, Rook)),
                                listOf(squareOf(Blue, Knight), squareOf(Blue, Pawn), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), squareOf(Green, Pawn), squareOf(Green, Knight)),
                                listOf(squareOf(Blue, Bishop), squareOf(Blue, Pawn), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), squareOf(Green, Pawn), squareOf(Green, Bishop)),
                                listOf(squareOf(Blue, Queen), squareOf(Blue, Pawn), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), squareOf(Green, Pawn), squareOf(Green, King)),
                                listOf(squareOf(Blue, King), squareOf(Blue, Pawn), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), squareOf(Green, Pawn), squareOf(Green, Queen)),
                                listOf(squareOf(Blue, Bishop), squareOf(Blue, Pawn), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), squareOf(Green, Pawn), squareOf(Green, Bishop)),
                                listOf(squareOf(Blue, Knight), squareOf(Blue, Pawn), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), squareOf(Green, Pawn), squareOf(Green, Knight)),
                                listOf(squareOf(Blue, Rook), squareOf(Blue, Pawn), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), squareOf(Green, Pawn), squareOf(Green, Rook)),
                                listOf<Square>(emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare()),
                                listOf(emptySquare(), emptySquare(), emptySquare(), squareOf(Yellow, Pawn), squareOf(Yellow, Pawn), squareOf(Yellow, Pawn), squareOf(Yellow, Pawn), squareOf(Yellow, Pawn), squareOf(Yellow, Pawn), squareOf(Yellow, Pawn), squareOf(Yellow, Pawn), emptySquare(), emptySquare(), emptySquare()),
                                listOf(emptySquare(), emptySquare(), emptySquare(), squareOf(Yellow, Rook), squareOf(Yellow, Knight), squareOf(Yellow, Bishop), squareOf(Yellow, King), squareOf(Yellow, Queen), squareOf(Yellow, Bishop), squareOf(Yellow, Knight), squareOf(Yellow, Rook), emptySquare(), emptySquare(), emptySquare())
                        ),
                        nextMoveColor = Red,
                        enPassantSquares = emptyMap(),
                        colorToCastlingOptions = CastlingOptions.default(),
                        plyCount = PlyCount.of(0)
                )

    }
}
