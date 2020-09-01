package pl.tomaszstankowski.fourplayerchess.engine

internal class PGNFormatter(private val position: Position) {

    fun formatMove(move: MoveBits): String {
        val builder = StringBuilder()

        val castling = position.getCastling(move)
        if (castling == Castling.KingSide) {
            builder.append("O-O")
        } else if (castling == Castling.QueenSide) {
            builder.append("O-O-O")
        } else {
            val movingPiece = (position.getSquare(move.from) as Square.Occupied).piece
            if (movingPiece.type != PieceType.Pawn) {
                builder.append(movingPiece.type.literal)
            }

            var isSrcFileShown = false
            var isSrcRankShown = false
            position.getLegalMoves().forEach { m ->
                if (move != m && m.to == move.to) {
                    val piece = (position.getSquare(m.from) as Square.Occupied).piece
                    if (piece == movingPiece) {
                        if (squareFile(m.from) == squareFile(move.from)) {
                            isSrcRankShown = true
                        }
                        if (squareRank(m.from) == squareRank(move.from)) {
                            isSrcFileShown = true
                        }
                    }
                }
            }
            val fromStr = Coordinates.ofSquareIndex(move.from).toHumanReadableString()
            if (isSrcFileShown) {
                builder.append(fromStr[0])
            }
            if (isSrcRankShown) {
                builder.append(fromStr.substring(1))
            }

            val capturedPiece = position.getCapturedPiece(move)
            if (capturedPiece != null) {
                builder.append("x")
            }

            val toStr = Coordinates.ofSquareIndex(move.to).toHumanReadableString()
            builder.append(toStr)

            val promotionPieceType = move.promotionPieceType
            if (promotionPieceType != null) {
                builder.append("=")
                builder.append(promotionPieceType.toPieceType().literal)
            }
        }

        if (position.isCheck(move)) {
            if (position.isCheckMate(move)) {
                builder.append("#")
            } else {
                builder.append("+")
            }
        }

        return builder.toString()
    }

    private val PieceType.literal: Char
        get() = when (this) {
            PieceType.Pawn -> 'P'
            PieceType.Knight -> 'N'
            PieceType.Bishop -> 'B'
            PieceType.Rook -> 'R'
            PieceType.Queen -> 'Q'
            PieceType.King -> 'K'
        }
}