package pl.tomaszstankowski.fourplayerchess.game

import java.util.*

internal interface GamePlayerRepository {

    fun insert(gamePlayer: GamePlayer)

    fun findByGameId(gameId: UUID): List<GamePlayer>
}