package pl.tomaszstankowski.fourplayerchess.game.data

import pl.tomaszstankowski.fourplayerchess.game.GamePlayer
import pl.tomaszstankowski.fourplayerchess.game.GamePlayerRepository
import java.util.*

internal class InMemoryGamePlayerRepository(private val dataSource: InMemoryDataSource) : GamePlayerRepository {

    override fun insert(gamePlayer: GamePlayer) {
        check(
                dataSource.gamePlayers.none { it.gameId == gamePlayer.gameId && it.playerId == gamePlayer.playerId }
        )
        dataSource.gamePlayers += gamePlayer
    }

    override fun findByGameId(gameId: UUID): List<GamePlayer> =
            dataSource.gamePlayers.filter { it.gameId == gameId }
}