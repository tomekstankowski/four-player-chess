package pl.tomaszstankowski.fourplayerchess.engine.hypermax

import pl.tomaszstankowski.fourplayerchess.engine.MoveBits
import pl.tomaszstankowski.fourplayerchess.engine.NULL_MOVE
import pl.tomaszstankowski.fourplayerchess.engine.allColors
import pl.tomaszstankowski.fourplayerchess.engine.hypermax.TranspositionTable.NodeType.EXACT
import pl.tomaszstankowski.fourplayerchess.engine.hypermax.TranspositionTable.NodeType.LOWER_BOUND

internal class TranspositionTable {
    private val entries = Array(0x100000) {
        InternalEntry(
                key = -1,
                move = NULL_MOVE,
                depth = -1,
                gamePly = -1,
                eval = FloatArray(allColors.size),
                nodeType = LOWER_BOUND
        )
    }

    object NodeType {
        const val EXACT: Byte = 0
        const val LOWER_BOUND: Byte = 1
    }

    interface Entry {
        val move: MoveBits
        val depth: Byte
        val eval: FloatArray
        val nodeType: Byte
        val gamePly: Short
    }

    private class InternalEntry(var key: Long,
                                override var move: MoveBits,
                                override var depth: Byte,
                                override var eval: FloatArray,
                                override var nodeType: Byte,
                                override var gamePly: Short) : Entry

    fun put(key: Long,
            move: MoveBits,
            depth: Byte,
            eval: FloatArray,
            nodeType: Byte,
            gamePly: Short) {
        val index = key.toInt() and 0xfffff
        val entry = entries[index]
        if (gamePly > entry.gamePly
                || ((entry.nodeType != EXACT || nodeType == EXACT) && depth > entry.depth)) {
            entry.key = key
            entry.move = move
            entry.depth = depth
            entry.eval = eval
            entry.nodeType = nodeType
            entry.gamePly = gamePly
        }
    }

    fun get(key: Long): Entry? {
        val index = key.toInt() and 0xfffff
        val entry = entries[index]
        if (entry.key == key && entry.gamePly > -1) {
            return entry
        }
        return null
    }
}