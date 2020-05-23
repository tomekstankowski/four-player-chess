package pl.tomaszstankowski.fourplayerchess

fun Engine.getStateAfterMove(from: Position, to: Position) =
        makeMove(from, to)!!.first

fun createEngineWithStateFromFen(fen: String): Engine {
    val state = parseStateFromFenOrThrow(fen)
    return Engine(state)
}

fun parseStateFromFenOrThrow(input: String): State =
        (State.parseFromFen(input) as ParseStateFromFenResult.Parsed).state