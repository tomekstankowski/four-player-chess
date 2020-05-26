package pl.tomaszstankowski.fourplayerchess.matchmaking

internal class InMemoryDataSource {
    val lobbies: MutableList<Lobby> = ArrayList()
    val lobbyMemberships: MutableList<LobbyMembership> = ArrayList()
}