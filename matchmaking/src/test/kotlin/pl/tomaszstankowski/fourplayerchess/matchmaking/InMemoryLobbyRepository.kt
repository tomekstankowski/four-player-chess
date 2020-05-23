package pl.tomaszstankowski.fourplayerchess.matchmaking

import java.util.*

internal class InMemoryLobbyRepository(private val dataSource: InMemoryDataSource) : LobbyRepository {

    override fun create(lobby: Lobby) {
        check(dataSource.lobbies.none { it.id == lobby.id })
        dataSource.lobbies += lobby
    }

    override fun findById(id: UUID): Lobby? = dataSource.lobbies.find { it.id == id }

    override fun findByName(name: String): Lobby? = dataSource.lobbies.find { it.name == name }

    override fun findAll(): List<Lobby> = dataSource.lobbies

    override fun update(lobby: Lobby) {
        val index = dataSource.lobbies.indexOfFirst { it.id == lobby.id }
        check(index != -1)
        dataSource.lobbies[index] = lobby
    }

    override fun delete(id: UUID) {
        dataSource.lobbies.removeIf { it.id == id }
    }
}
