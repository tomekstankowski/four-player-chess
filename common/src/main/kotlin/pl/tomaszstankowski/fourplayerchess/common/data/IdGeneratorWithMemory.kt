package pl.tomaszstankowski.fourplayerchess.common.data

import java.util.*

class IdGeneratorWithMemory : IdGenerator {
    private var _lastId = UUID.randomUUID()
    val lastId: UUID
        get() = _lastId

    override fun generateId(): UUID {
        _lastId = UUID.randomUUID()
        return _lastId
    }
}