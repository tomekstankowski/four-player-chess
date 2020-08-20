package pl.tomaszstankowski.fourplayerchess

import pl.tomaszstankowski.fourplayerchess.engine.*

fun Engine.getStateAfterMove(from: String, to: String): UIState {
    val result = makeMove(from, to)
    assert(result)
    return getUIState()
}

val Engine.legalMoves: List<Move>
    get() = getUIState().legalMoves

fun Engine.makeMove(from: String, to: String) =
        makeMove(
                RegularMove(from = Coordinates.parse(from), to = Coordinates.parse(to))
        )

fun createEngineWithStateFromFen(fen: String): Engine {
    val state = parseStateFromFenOrThrow(fen)
    return Engine(state)
}

fun parseStateFromFenOrThrow(input: String): FenState =
        (FenState.parseFromFen(input) as ParseStateFromFenResult.Parsed).state