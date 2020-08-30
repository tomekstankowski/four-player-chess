package pl.tomaszstankowski.fourplayerchess.engine

private const val FIELD_SEP = "-"
private const val SEP = ","
private const val ROW_SEP = "/\n"

private val PieceType.fenLiteral: String
    get() = when (this) {
        PieceType.Pawn -> "P"
        PieceType.Knight -> "N"
        PieceType.Bishop -> "B"
        PieceType.Rook -> "R"
        PieceType.Queen -> "Q"
        PieceType.King -> "K"
    }

internal fun formatState(state: FenState): String {
    val builder = StringBuilder()

    builder.append(state.nextMoveColor.name.toUpperCase().first())
    builder.append(FIELD_SEP)

    Color.values().forEachIndexed { index, color ->
        if (color in state.eliminatedColors) {
            builder.append(1)
        } else {
            builder.append(0)
        }
        if (index < Color.values().size - 1) {
            builder.append(SEP)
        }
    }
    builder.append(FIELD_SEP)

    Color.values().forEachIndexed { index, color ->
        if (Castling.KingSide in state.castlingOptions[color] ?: emptySet()) {
            builder.append(1)
        } else {
            builder.append(0)
        }
        if (index < Color.values().size - 1) {
            builder.append(SEP)
        }
    }
    builder.append(FIELD_SEP)

    Color.values().forEach { color ->
        if (Castling.QueenSide in state.castlingOptions[color] ?: emptySet()) {
            builder.append(1)
        } else {
            builder.append(0)
        }
        builder.append(SEP)
    }
    builder.append(FIELD_SEP)

    Color.values().forEach { color ->
        val enPassantSquare = state.enPassantSquares[color]
        if (enPassantSquare == null) {
            builder.append(0)
        } else {
            builder.append(enPassantSquare.toHumanReadableString())
        }
        builder.append(SEP)
    }
    builder.append(FIELD_SEP)

    builder.append(state.plyCount)
    builder.append(FIELD_SEP)

    builder.append("\n")

    for (i in (BOARD_SIZE - 1) downTo 0) {
        var emptySquaresCount = 0
        for (j in 0 until BOARD_SIZE) {
            val square = state.board[i][j]
            if (square is Square.Occupied) {
                builder.append(emptySquaresCount)
                emptySquaresCount = 0
                builder.append(SEP)
                builder.append(square.piece.color.name.toLowerCase().first())
                builder.append(square.piece.type.fenLiteral)
                if (j < BOARD_SIZE - 1) {
                    builder.append(SEP)
                }
            } else {
                emptySquaresCount++
            }
        }
        if (emptySquaresCount > 0) {
            builder.append(emptySquaresCount)
        }
        builder.append(ROW_SEP)
    }

    return builder.toString()
}