package pl.tomaszstankowski.fourplayerchess.web

import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageExceptionHandler
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pl.tomaszstankowski.fourplayerchess.game.*
import pl.tomaszstankowski.fourplayerchess.websecurity.getAuthenticatedUserId
import java.security.Principal
import java.util.*

@RestController
@RequestMapping("/games")
class GameController(private val gameControlService: GameControlService) {

    @GetMapping("/{id}")
    fun getGame(@PathVariable id: UUID) =
            gameControlService.getGame(id) ?: throw ApiException.resourceNotFound("game", id)

    @GetMapping("/{id}/players")
    fun getPlayersOfTheGame(@PathVariable id: UUID): List<GamePlayerDto> =
            gameControlService.getPlayersOfTheGame(id)
                    ?: throw ApiException.resourceNotFound("game", id)

    @GetMapping("/active-for-me")
    fun getActiveGamesForPlayer(): List<GameDto> {
        val userId = getAuthenticatedUserId()
        return gameControlService.getActiveGamesForPlayer(userId)
    }

    @GetMapping("/{id}/state")
    fun getGameState(@PathVariable id: UUID): GameStateDto =
            when (val result = gameControlService.getGameState(id)) {
                is GetGameStateResult.Success -> result.gameState
                is GetGameStateResult.GameNotFound -> throw ApiException.resourceNotFound("game", id)
                is GetGameStateResult.GameNotActive -> throw ApiException.unprocessableEntity("Game is not active")
            }

    @MessageMapping("/games/{gameId}/moves")
    fun makeMove(@DestinationVariable gameId: UUID, msg: MakeMoveMessage, principal: Principal): MoveMadeMessage {
        val dto = MakeMoveDto(
                gameId = gameId,
                playerId = UUID.fromString(principal.name),
                from = msg.from,
                to = msg.to,
                promotionPiece = msg.promotionPiece
        )
        return when (val result = gameControlService.makeMove(dto)) {
            is MakeMoveResult.Success -> MoveMadeMessage(
                    newGameState = result.newGameState,
                    move = LegalMoveDto(from = dto.from, to = dto.to)
            )
            is MakeMoveResult.Error -> throw MakeMoveException(result.message)
        }
    }

    @MessageExceptionHandler
    fun handleUnknownException(e: Exception): String = "Internal server error"

    @MessageExceptionHandler
    fun handleMakeMoveException(e: MakeMoveException): String =
            e.message ?: ""
}

class MakeMoveException(msg: String) : RuntimeException(msg)

data class MakeMoveMessage(val from: String, val to: String, val promotionPiece: String?)

data class MoveMadeMessage(val newGameState: GameStateDto,
                           val move: LegalMoveDto)