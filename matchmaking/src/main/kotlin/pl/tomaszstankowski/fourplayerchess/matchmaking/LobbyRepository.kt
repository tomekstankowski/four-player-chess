package pl.tomaszstankowski.fourplayerchess.matchmaking

import java.util.*

internal interface LobbyRepository {

    fun create(lobby: Lobby)

    fun findById(id: UUID): Lobby?

    fun findByName(name: String): Lobby?

    fun findByPlayerId(playerId: UUID): List<Lobby>

    fun findAll(): List<Lobby>

    fun updateIfVersionEquals(lobby: Lobby, version: Int): Boolean

    fun delete(id: UUID)

}