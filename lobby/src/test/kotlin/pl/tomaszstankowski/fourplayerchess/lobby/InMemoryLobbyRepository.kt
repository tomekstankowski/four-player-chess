package pl.tomaszstankowski.fourplayerchess.lobby

import java.util.*
import kotlin.collections.HashMap

class InMemoryLobbyRepository : LobbyRepository {
    private val idToLobby = HashMap<UUID, Lobby>()

    override fun create(lobby: Lobby) {
        check(!idToLobby.containsKey(lobby.id))
        idToLobby[lobby.id] = lobby
    }

    override fun findById(id: UUID): Lobby? = idToLobby[id]

    override fun findByName(name: String): Lobby? = idToLobby.values.firstOrNull { it.name == name }

    override fun findAll(): List<Lobby> = idToLobby.values.toList()

    override fun update(lobby: Lobby) {
        check(idToLobby.containsKey(lobby.id))
        idToLobby[lobby.id] = lobby
    }

    override fun delete(id: UUID) {
        idToLobby.remove(id)
    }

    fun clear() {
        idToLobby.clear()
    }
}
