package pl.tomaszstankowski.fourplayerchess.game.data

import pl.tomaszstankowski.fourplayerchess.game.Game
import pl.tomaszstankowski.fourplayerchess.game.Player.HumanPlayer
import pl.tomaszstankowski.fourplayerchess.game.Player.RandomBot

internal class InMemoryDataSource {
    val games = ArrayList<Game>()
    val humanPlayers = ArrayList<HumanPlayer>()
    val randomBots = ArrayList<RandomBot>()
}