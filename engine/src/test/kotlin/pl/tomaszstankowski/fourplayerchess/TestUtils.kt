package pl.tomaszstankowski.fourplayerchess

import pl.tomaszstankowski.fourplayerchess.engine.Engine
import pl.tomaszstankowski.fourplayerchess.engine.ParseStateFromFenResult
import pl.tomaszstankowski.fourplayerchess.engine.Position
import pl.tomaszstankowski.fourplayerchess.engine.State

fun Engine.getStateAfterMove(from: Position, to: Position) =
        makeMove(from, to)!!.first

fun createEngineWithStateFromFen(fen: String): Engine {
    val state = parseStateFromFenOrThrow(fen)
    return Engine(state)
}

fun parseStateFromFenOrThrow(input: String): State =
        (State.parseFromFen(input) as ParseStateFromFenResult.Parsed).state