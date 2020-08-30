package pl.tomaszstankowski.fourplayerchess.engine

import pl.tomaszstankowski.fourplayerchess.engine.TranspositionTable.NodeType.EXACT
import pl.tomaszstankowski.fourplayerchess.engine.TranspositionTable.NodeType.LOWER_BOUND


internal class TranspositionTable {
    private val entries = Array(0x100000) {
        InternalEntry(
                key = -1,
                move = -1,
                depth = -1,
                gamePly = -1,
                eval = -1,
                nodeType = LOWER_BOUND
        )
    }

    object NodeType {
        const val EXACT: Byte = 0
        const val LOWER_BOUND: Byte = 1
        const val UPPER_BOUND: Byte = 2
    }

    interface Entry {
        val move: MoveBits
        val depth: Byte
        val eval: Int
        val nodeType: Byte
        val gamePly: Short
    }

    private class InternalEntry(var key: Long,
                                override var move: MoveBits,
                                override var depth: Byte,
                                override var eval: Int,
                                override var nodeType: Byte,
                                override var gamePly: Short) : Entry

    fun put(key: Long,
            move: MoveBits,
            depth: Byte,
            eval: Int,
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

    fun logState() {
        var cnt = 0
        for (i in 0 until entries.size) {
            if (entries[i].gamePly > -1) {
                cnt++
            }
        }
        val ratio = cnt.toFloat() / entries.size * 100
        println("TT entries in use: $cnt, ${ratio}%")
    }
}