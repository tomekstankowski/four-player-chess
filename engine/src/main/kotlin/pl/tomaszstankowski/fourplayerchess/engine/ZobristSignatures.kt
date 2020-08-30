package pl.tomaszstankowski.fourplayerchess.engine

import kotlin.random.Random

internal class ZobristSignatures(random: Random) {
    private val pieceSquare: Array<LongArray> = Array(allPieceTypes.size * allColors.size) {
        LongArray(BOARD_SIZE * BOARD_SIZE) { random.nextLong() }
    }

    fun getPieceSquareVal(piece: Piece, squareIndex: Int): Long {
        val i = piece.color.ordinal * allPieceTypes.size + piece.type.ordinal
        return pieceSquare[i][squareIndex]
    }

    private val nextMoveColor: LongArray = LongArray(allColors.size) {
        random.nextLong()
    }

    fun getNextMoveColorVal(color: Color): Long =
            nextMoveColor[color.ordinal]

    private val castling: Array<LongArray> = Array(allColors.size) {
        LongArray(4) { random.nextLong() }
    }

    fun getCastlingOptionsValue(color: Color, castlingOptions: Set<Castling>): Long {
        val i = when (castlingOptions) {
            castlingOptionsAny -> 3
            castlingOptionsQueenSide -> 2
            castlingOptionsKingSide -> 1
            else -> 0
        }
        return castling[color.ordinal][i]
    }

    private val enPassantSquare: Array<LongArray> = Array(allColors.size) {
        LongArray(BOARD_SIZE) { random.nextLong() }
    }

    fun getEnPassantVal(color: Color, enPassantSquareIndex: Int): Long {
        val i = when (color) {
            Color.Red, Color.Yellow -> squareFile(enPassantSquareIndex)
            Color.Blue, Color.Green -> squareRank(enPassantSquareIndex)
        }
        return enPassantSquare[color.ordinal][i]
    }

    private val eliminatedColor: LongArray = LongArray(allColors.size) {
        random.nextLong()
    }

    fun getEliminatedColorValue(color: Color) =
            eliminatedColor[color.ordinal]
}