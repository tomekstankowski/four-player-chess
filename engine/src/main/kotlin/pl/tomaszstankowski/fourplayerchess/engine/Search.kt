package pl.tomaszstankowski.fourplayerchess.engine

internal data class PositionEvaluation(val pv: List<PVMove>, val evaluation: Float) {

    data class PVMove(val move: MoveBits, val moveText: String)
}

internal interface Search {

    fun startSearch()

    fun stopSearch()

    fun getPositionEvaluation(): PositionEvaluation?
}