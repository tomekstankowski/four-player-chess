package pl.tomaszstankowski.fourplayerchess.common.data

import java.util.*

class SimpleIdGenerator : IdGenerator {

    override fun generateId(): UUID = UUID.randomUUID()
}