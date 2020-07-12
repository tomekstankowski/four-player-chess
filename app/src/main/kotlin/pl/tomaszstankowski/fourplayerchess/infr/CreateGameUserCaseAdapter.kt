package pl.tomaszstankowski.fourplayerchess.infr

import pl.tomaszstankowski.fourplayerchess.game.CreateGameDto
import pl.tomaszstankowski.fourplayerchess.game.CreateGameResult
import pl.tomaszstankowski.fourplayerchess.game.GameControlService
import pl.tomaszstankowski.fourplayerchess.matchmaking.CreateGameUseCase
import java.util.*

class CreateGameUserCaseAdapter(private val gameControlService: GameControlService) : CreateGameUseCase {

    override fun createGame(playersIds: Set<UUID>, randomBotsCount: Int): UUID {
        val result = gameControlService.createGame(
                CreateGameDto(playersIds, randomBotsCount)
        ) as CreateGameResult.Success
        return result.game.id
    }

    override fun commitGame(gameId: UUID) {
        gameControlService.commitGame(gameId)
    }
}