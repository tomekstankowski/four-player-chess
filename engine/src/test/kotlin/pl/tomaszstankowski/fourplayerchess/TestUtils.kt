package pl.tomaszstankowski.fourplayerchess

import pl.tomaszstankowski.fourplayerchess.engine.*

fun Engine.getStateAfterMove(from: String, to: String): UIState {
    makeMoveWithAssert(from, to)
    return getUIState()
}

val Engine.legalMoves: Set<Move>
    get() = getUIState().legalMoves

fun Engine.makeMoveWithAssert(from: String, to: String) {
    val result = makeMove(
            RegularMove(from = Coordinates.parse(from), to = Coordinates.parse(to))
    )
    assert(result)
}

fun Engine.unmakeMoveWithAssert() {
    val result = unmakeMove()
    assert(result)
}


fun Set<Move>.filterByMovedPieceType(state: FenState, pieceType: PieceType): Set<Move> =
        filter { move ->
            (state.board[move.from.rank][move.from.file] as? Square.Occupied)?.piece?.type == pieceType
        }
                .toSet()

fun createEngineWithStateFromFen(fen: String): Engine {
    val state = parseStateFromFenOrThrow(fen)
    return Engine.withRandomSearch(state)
}

fun parseStateFromFenOrThrow(input: String): FenState =
        (FenState.parseFromFen(input) as ParseStateFromFenResult.Parsed).state