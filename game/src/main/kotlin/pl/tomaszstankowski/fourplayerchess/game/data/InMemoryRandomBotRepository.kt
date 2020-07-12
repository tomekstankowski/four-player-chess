package pl.tomaszstankowski.fourplayerchess.game.data

import pl.tomaszstankowski.fourplayerchess.game.Player
import pl.tomaszstankowski.fourplayerchess.game.RandomBotRepository
import java.util.*

internal class InMemoryRandomBotRepository(private val dataSource: InMemoryDataSource) : RandomBotRepository {

    override fun insert(randomBot: Player.RandomBot) {
        check(
                dataSource.randomBots.none { it.gameId == randomBot.gameId && it.color == randomBot.color }
        )
        dataSource.randomBots += randomBot
    }

    override fun findByGameId(gameId: UUID): List<Player.RandomBot> =
            dataSource.randomBots.filter { it.gameId == gameId }
}