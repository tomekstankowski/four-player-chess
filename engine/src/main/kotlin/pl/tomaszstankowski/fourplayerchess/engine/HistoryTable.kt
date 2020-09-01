package pl.tomaszstankowski.fourplayerchess.engine

internal class HistoryTable {
    private val table = Array(Color.values().size) {
        Array(BOARD_SIZE * BOARD_SIZE) {
            IntArray(BOARD_SIZE * BOARD_SIZE)
        }
    }

    fun increase(move: MoveBits, color: Color, depth: Byte) {
        table[color.ordinal][move.from][move.to] += depth * depth
    }

    fun get(move: MoveBits, color: Color) = table[color.ordinal][move.from][move.to]
}