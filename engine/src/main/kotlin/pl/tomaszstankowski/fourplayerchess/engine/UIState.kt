package pl.tomaszstankowski.fourplayerchess.engine

data class UIState(
        val fenState: FenState,
        val checks: Map<Color, Set<Check>>,
        val isDrawByClaimAllowed: Boolean,
        val isGameOver: Boolean,
        val winningColor: Color?,
        val legalMoves: Set<Move>
)