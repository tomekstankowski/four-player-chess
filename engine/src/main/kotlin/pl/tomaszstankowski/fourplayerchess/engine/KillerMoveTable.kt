package pl.tomaszstankowski.fourplayerchess.engine

internal class KillerMoveTable(maxPly: Int, private val killerMovesPerPly: Int) {
    private val killerMoves = Array(maxPly) {
        IntArray(killerMovesPerPly) { NULL_MOVE }
    }

    fun isKillerMove(move: MoveBits, ply: Int) = move in killerMoves[ply]

    fun addKillerMove(move: MoveBits, ply: Int) {
        val moves = killerMoves[ply]
        if (move !in moves) {
            for (i in killerMovesPerPly - 2..0) {
                moves[i + 1] = moves[i]
            }
            moves[0] = move
        }
    }

}