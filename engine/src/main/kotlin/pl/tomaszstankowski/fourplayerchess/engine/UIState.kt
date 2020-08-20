package pl.tomaszstankowski.fourplayerchess.engine

data class UIState(
        val fenState: FenState,
        val checks: Map<Color, List<Check>>,
        val isDrawByClaimAllowed: Boolean,
        val isGameOver: Boolean,
        val winningColor: Color?,
        val legalMoves: List<Move>
)