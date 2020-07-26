package pl.tomaszstankowski.fourplayerchess.engine

internal typealias PieceList = List<Pair<Piece, Position>>

/*
Redundant state kept for performance purposes.
Maintaining piece list to avoid scanning board.
 */
internal data class HelperState(val pieceList: PieceList) {

    companion object {
        fun fromState(state: State) = HelperState(
                pieceList = Position.allPositions.mapNotNull { pos ->
                    (state.squares.byPosition(pos) as? Square.Occupied)
                            ?.let { square -> square.piece to pos }
                }
        )
    }
}