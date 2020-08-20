package pl.tomaszstankowski.fourplayerchess.engine

import kotlin.random.Random

internal class ZobristSignatures(random: Random) {
    private val pieceSquare: Array<LongArray> = Array(PieceType.values().size * Color.values().size) {
        LongArray(BOARD_SIZE * BOARD_SIZE) { random.nextLong() }
    }

    fun getPieceSquareVal(piece: Piece, coordinates: Coordinates): Long {
        val i = piece.color.ordinal * PieceType.values().size + piece.type.ordinal
        val j = coordinates.rank * BOARD_SIZE + coordinates.file
        return pieceSquare[i][j]
    }

    private val nextMoveColor: LongArray = LongArray(Color.values().size) {
        random.nextLong()
    }

    fun getNextMoveColorVal(color: Color): Long =
            nextMoveColor[color.ordinal]

    private val castling: Array<LongArray> = Array(Color.values().size) {
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

    private val enPassantSquare: Array<LongArray> = Array(Color.values().size) {
        LongArray(BOARD_SIZE) { random.nextLong() }
    }

    fun getEnPassantVal(color: Color, enPassantCoords: Coordinates): Long {
        val i = when (color) {
            Color.Red, Color.Yellow -> enPassantCoords.file
            Color.Blue, Color.Green -> enPassantCoords.rank
        }
        return enPassantSquare[color.ordinal][i]
    }

    private val eliminatedColor: LongArray = LongArray(Color.values().size) {
        random.nextLong()
    }

    fun getEliminatedColorValue(color: Color) =
            eliminatedColor[color.ordinal]
}