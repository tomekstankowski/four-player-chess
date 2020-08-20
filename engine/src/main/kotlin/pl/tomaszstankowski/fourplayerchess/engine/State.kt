package pl.tomaszstankowski.fourplayerchess.engine

// Contains properties that are not incrementally updated
internal data class State(val eliminatedColors: EliminatedColorsBits,
                          val nextMoveColor: Color,
                          val enPassantSquares: EnPassantSquaresBits,
                          val castlingOptions: CastlingOptionsBits,
                          val plyCount: Int,
                          val hash: Long) {

    companion object {

        fun of(fenState: FenState, hash: Long): State {
            return State(
                    eliminatedColors = fenState.eliminatedColors
                            .fold(initialEliminatedColors()) { eliminatedColorBits, color ->
                                eliminatedColorBits.withColorEliminated(color)
                            },
                    castlingOptions = fenState.castlingOptions.entries
                            .fold(emptyCastlingOptions()) { castlingOptionsBits, (color, castlingOptions) ->
                                castlingOptions.fold(castlingOptionsBits) { bits, castling ->
                                    bits.withCastlingForColor(color, castling)
                                }
                            },
                    nextMoveColor = fenState.nextMoveColor,
                    enPassantSquares = fenState.enPassantSquares.entries
                            .fold(initialEnPassantSquares()) { enPassantSquaresBits, (color, coords) ->
                                enPassantSquaresBits.withEnPassantSquareForColor(color, coords)
                            },
                    plyCount = fenState.plyCount,
                    hash = hash
            )
        }
    }
}