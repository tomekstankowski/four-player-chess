package pl.tomaszstankowski.fourplayerchess.engine

import com.github.h0tk3y.betterParse.grammar.tryParseToEnd
import com.github.h0tk3y.betterParse.parser.ErrorResult
import com.github.h0tk3y.betterParse.parser.Parsed
import pl.tomaszstankowski.fourplayerchess.engine.Castling.KingSide
import pl.tomaszstankowski.fourplayerchess.engine.Castling.QueenSide
import pl.tomaszstankowski.fourplayerchess.engine.Color.*
import pl.tomaszstankowski.fourplayerchess.engine.PieceType.*

sealed class ParseStateFromFenResult {
    data class Parsed(val state: FenState) : ParseStateFromFenResult()
    data class ParseError(val message: String) : ParseStateFromFenResult()
    sealed class IllegalState : ParseStateFromFenResult() {
        data class IllegalRowLength(val length: Int) : IllegalState()
        data class IllegalKingCount(val color: Color, val count: Int) : IllegalState()
        data class PiecesWithoutKing(val color: Color) : IllegalState()
    }
}

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
                                arrayOf(null, null, null, squareOf(Red, Rook), squareOf(Red, Knight), squareOf(Red, Bishop), squareOf(Red, Queen), squareOf(Red, King), squareOf(Red, Bishop), squareOf(Red, Knight), squareOf(Red, Rook), null, null, null),
                                arrayOf(null, null, null, squareOf(Red, Pawn), squareOf(Red, Pawn), squareOf(Red, Pawn), squareOf(Red, Pawn), squareOf(Red, Pawn), squareOf(Red, Pawn), squareOf(Red, Pawn), squareOf(Red, Pawn), null, null, null),
                                arrayOf(null, null, null, emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), null, null, null),
                                arrayOf(squareOf(Blue, Rook), squareOf(Blue, Pawn), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), squareOf(Green, Pawn), squareOf(Green, Rook)),
                                arrayOf(squareOf(Blue, Knight), squareOf(Blue, Pawn), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), squareOf(Green, Pawn), squareOf(Green, Knight)),
                                arrayOf(squareOf(Blue, Bishop), squareOf(Blue, Pawn), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), squareOf(Green, Pawn), squareOf(Green, Bishop)),
                                arrayOf(squareOf(Blue, Queen), squareOf(Blue, Pawn), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), squareOf(Green, Pawn), squareOf(Green, King)),
                                arrayOf(squareOf(Blue, King), squareOf(Blue, Pawn), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), squareOf(Green, Pawn), squareOf(Green, Queen)),
                                arrayOf(squareOf(Blue, Bishop), squareOf(Blue, Pawn), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), squareOf(Green, Pawn), squareOf(Green, Bishop)),
                                arrayOf(squareOf(Blue, Knight), squareOf(Blue, Pawn), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), squareOf(Green, Pawn), squareOf(Green, Knight)),
                                arrayOf(squareOf(Blue, Rook), squareOf(Blue, Pawn), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), squareOf(Green, Pawn), squareOf(Green, Rook)),
                                arrayOf(null, null, null, emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), null, null, null),
                                arrayOf(null, null, null, squareOf(Yellow, Pawn), squareOf(Yellow, Pawn), squareOf(Yellow, Pawn), squareOf(Yellow, Pawn), squareOf(Yellow, Pawn), squareOf(Yellow, Pawn), squareOf(Yellow, Pawn), squareOf(Yellow, Pawn), null, null, null),
                                arrayOf(null, null, null, squareOf(Yellow, Rook), squareOf(Yellow, Knight), squareOf(Yellow, Bishop), squareOf(Yellow, King), squareOf(Yellow, Queen), squareOf(Yellow, Bishop), squareOf(Yellow, Knight), squareOf(Yellow, Rook), null, null, null)
                        ),
                        eliminatedColors = emptySet(),
                        nextMoveColor = Red,
                        enPassantSquares = emptyMap(),
                        castlingOptions = Color.values().map { color -> color to setOf(KingSide, QueenSide) }.toMap(),
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
}
