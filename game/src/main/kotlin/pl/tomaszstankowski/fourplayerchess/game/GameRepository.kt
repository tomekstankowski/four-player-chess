package pl.tomaszstankowski.fourplayerchess.game

import java.util.*

internal interface GameRepository {

    fun insert(game: Game)

    fun update(game: Game)

    fun findById(id: UUID): Game?

    fun findByHumanPlayerUserId(playerId: UUID): List<Game>

    fun findByIsCommittedIsTrueAndIsCancelledIsFalseAndIsFinishedIsFalse(): List<Game>
}