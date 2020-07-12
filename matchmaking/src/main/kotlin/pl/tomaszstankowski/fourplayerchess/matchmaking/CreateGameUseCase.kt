package pl.tomaszstankowski.fourplayerchess.matchmaking

import java.util.*

interface CreateGameUseCase {
    fun createGame(playersIds: Set<UUID>, randomBotsCount: Int): UUID
    fun commitGame(gameId: UUID)
}