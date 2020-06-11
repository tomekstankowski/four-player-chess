package pl.tomaszstankowski.fourplayerchess

import pl.tomaszstankowski.fourplayerchess.engine.*

fun Engine.getStateAfterMove(from: String, to: String): State {
    val result = makeMove(from, to)
    assert(result)
    return state
}

fun Engine.makeMove(from: String, to: String) =
        makeMove(
                moveClaim = MoveClaim.RegularMoveClaim(
                        move = Move(
                                from = Position.parse(from),
                                to = Position.parse(to)
                        )
                )
        )

fun createEngineWithStateFromFen(fen: String): Engine {
    val state = parseStateFromFenOrThrow(fen)
    return Engine(state)
}

fun parseStateFromFenOrThrow(input: String): State =
        (State.parseFromFen(input) as ParseStateFromFenResult.Parsed).state