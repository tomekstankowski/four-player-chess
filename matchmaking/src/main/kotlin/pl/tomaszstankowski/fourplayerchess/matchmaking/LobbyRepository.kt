package pl.tomaszstankowski.fourplayerchess.matchmaking

import java.util.*

internal interface LobbyRepository {

    fun create(lobby: Lobby)

    fun findById(id: UUID): Lobby?

    fun findByName(name: String): Lobby?

    fun findAll(): List<Lobby>

    fun update(lobby: Lobby)

    fun delete(id: UUID)

}