package pl.tomaszstankowski.fourplayerchess.game.data

import pl.tomaszstankowski.fourplayerchess.game.Game
import pl.tomaszstankowski.fourplayerchess.game.GameRepository
import java.util.*

internal class InMemoryGameRepository(private val dataSource: InMemoryDataSource) : GameRepository {

    override fun insert(game: Game) {
        check(
                dataSource.games.none { it.id == game.id }
        )
        dataSource.games += game
    }

    override fun update(game: Game) {
        val index = dataSource.games.indexOfFirst { it.id == game.id }
        check(index != -1)
        dataSource.games[index] = game
    }

    override fun findById(id: UUID): Game? =
            dataSource.games.find { it.id == id }

    override fun findByPlayerId(playerId: UUID): List<Game> =
            dataSource.games.filter { game ->
                dataSource.gamePlayers.any { it.playerId == playerId && it.gameId == game.id }
            }

    override fun findByIsCommittedIsTrueAndIsCancelledIsFalse(): List<Game> =
            dataSource.games.filter {
                it.isCommitted && !it.isCancelled
            }
}