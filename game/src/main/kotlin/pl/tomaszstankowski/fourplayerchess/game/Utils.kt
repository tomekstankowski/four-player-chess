package pl.tomaszstankowski.fourplayerchess.game

import pl.tomaszstankowski.fourplayerchess.engine.*

internal data class EngineStateSnapshot(val state: State,
                                        val stateFeatures: StateFeatures,
                                        val legalMoves: List<Move>,
                                        val isDrawByClaimAllowed: Boolean,
                                        val isDraw: Boolean,
                                        val isGameOver: Boolean,
                                        val winningColor: Color?)

internal fun Engine.getStateSnapshot() = EngineStateSnapshot(
        state, stateFeatures, legalMoves, isDrawByClaimAllowed, isDraw, isGameOver, winningColor
)