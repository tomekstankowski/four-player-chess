package pl.tomaszstankowski.fourplayerchess.matchmaking.data

import pl.tomaszstankowski.fourplayerchess.matchmaking.Lobby
import pl.tomaszstankowski.fourplayerchess.matchmaking.LobbyMembership

internal class InMemoryDataSource {
    val lobbies: MutableList<Lobby> = ArrayList()
    val lobbyMemberships: MutableList<LobbyMembership> = ArrayList()
}