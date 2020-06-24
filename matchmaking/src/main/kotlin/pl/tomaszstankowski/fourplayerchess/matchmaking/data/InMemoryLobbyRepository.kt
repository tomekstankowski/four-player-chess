package pl.tomaszstankowski.fourplayerchess.matchmaking.data

import pl.tomaszstankowski.fourplayerchess.matchmaking.Lobby
import pl.tomaszstankowski.fourplayerchess.matchmaking.LobbyRepository
import java.util.*

internal class InMemoryLobbyRepository(private val dataSource: InMemoryDataSource) : LobbyRepository {

    override fun create(lobby: Lobby) {
        check(dataSource.lobbies.none { it.id == lobby.id })
        dataSource.lobbies += lobby
    }

    override fun findById(id: UUID): Lobby? = dataSource.lobbies.find { it.id == id }

    override fun findByName(name: String): Lobby? = dataSource.lobbies.find { it.name == name }

    override fun findByPlayerId(playerId: UUID): List<Lobby> =
            dataSource.lobbies.filter { lobby ->
                dataSource.lobbyMemberships.any { membership ->
                    membership.playerId == playerId && membership.lobbyId == lobby.id
                }
            }

    override fun updateIfVersionEquals(lobby: Lobby, version: Int): Boolean {
        val index = dataSource.lobbies.indexOfFirst { it.id == lobby.id }
        check(index != -1)
        if (dataSource.lobbies[index].version != version) {
            return false
        }
        dataSource.lobbies[index] = lobby
        return true
    }

    override fun delete(id: UUID) {
        dataSource.lobbies.removeIf { it.id == id }
    }

}
