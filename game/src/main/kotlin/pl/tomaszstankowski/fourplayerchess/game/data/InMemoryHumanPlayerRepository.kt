package pl.tomaszstankowski.fourplayerchess.game.data

import pl.tomaszstankowski.fourplayerchess.game.HumanPlayerRepository
import pl.tomaszstankowski.fourplayerchess.game.Player.HumanPlayer
import java.util.*

internal class InMemoryHumanPlayerRepository(private val dataSource: InMemoryDataSource) : HumanPlayerRepository {

    override fun insert(player: HumanPlayer) {
        check(
                dataSource.humanPlayers.none { it.gameId == player.gameId && it.color == player.color }
        )
        dataSource.humanPlayers += player
    }

    override fun findByGameId(gameId: UUID): List<HumanPlayer> =
            dataSource.humanPlayers.filter { it.gameId == gameId }
}