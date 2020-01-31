package pl.tomaszstankowski.fourplayerchess.common.data

import java.util.*

interface IdGenerator {
    fun generateId(): UUID
}