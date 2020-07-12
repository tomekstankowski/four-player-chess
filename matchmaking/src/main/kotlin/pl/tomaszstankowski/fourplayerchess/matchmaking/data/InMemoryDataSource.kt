package pl.tomaszstankowski.fourplayerchess.matchmaking.data

import pl.tomaszstankowski.fourplayerchess.matchmaking.Lobby
import pl.tomaszstankowski.fourplayerchess.matchmaking.LobbyMembership.HumanPlayerMembership
import pl.tomaszstankowski.fourplayerchess.matchmaking.LobbyMembership.RandomBotMembership

internal class InMemoryDataSource {
    val lobbies: MutableList<Lobby> = ArrayList()
    val humanPlayerMemberships: MutableList<HumanPlayerMembership> = ArrayList()
    val randomBotsMemberships: MutableList<RandomBotMembership> = ArrayList()
}