package pl.tomaszstankowski.fourplayerchess.matchmaking

import java.util.*

class TestCreateGameUseCase(private val newGameId: UUID) : CreateGameUseCase {

    override fun createGame(playersIds: Set<UUID>, randomBotsCount: Int): UUID = newGameId

    override fun commitGame(gameId: UUID) {
    }
}