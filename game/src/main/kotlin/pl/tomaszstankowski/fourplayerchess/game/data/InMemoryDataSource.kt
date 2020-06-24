package pl.tomaszstankowski.fourplayerchess.game.data

import pl.tomaszstankowski.fourplayerchess.game.Game
import pl.tomaszstankowski.fourplayerchess.game.GamePlayer

internal class InMemoryDataSource {
    val games = ArrayList<Game>()
    val gamePlayers = ArrayList<GamePlayer>()
}