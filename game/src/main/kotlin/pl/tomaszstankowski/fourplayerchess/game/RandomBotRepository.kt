package pl.tomaszstankowski.fourplayerchess.game

import pl.tomaszstankowski.fourplayerchess.game.Player.RandomBot
import java.util.*

internal interface RandomBotRepository {

    fun insert(randomBot: RandomBot)

    fun findByGameId(gameId: UUID): List<RandomBot>
}