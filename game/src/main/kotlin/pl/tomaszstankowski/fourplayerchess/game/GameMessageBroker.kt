package pl.tomaszstankowski.fourplayerchess.game

import org.springframework.messaging.simp.SimpMessageSendingOperations
import java.util.*

internal class GameMessageBroker(private val messageSendingOperations: SimpMessageSendingOperations) {

    fun sendMoveMadeMessage(gameId: UUID, newGameState: GameStateDto, move: LegalMoveDto) {
        val payload = MoveMadePayload(newGameState, move)
        messageSendingOperations.convertAndSend("/topic/games/$gameId/moves", payload)
    }

    internal data class MoveMadePayload(val newGameState: GameStateDto, val move: LegalMoveDto)
}