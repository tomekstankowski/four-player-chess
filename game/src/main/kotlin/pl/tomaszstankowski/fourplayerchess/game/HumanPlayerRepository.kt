package pl.tomaszstankowski.fourplayerchess.game

import pl.tomaszstankowski.fourplayerchess.game.Player.HumanPlayer
import java.util.*

internal interface HumanPlayerRepository {

    fun insert(player: HumanPlayer)

    fun findByGameId(gameId: UUID): List<HumanPlayer>
}