package pl.tomaszstankowski.fourplayerchess.game

import org.springframework.messaging.simp.SimpMessageSendingOperations
import pl.tomaszstankowski.fourplayerchess.engine.Color
import java.util.*

internal class GameMessageBroker(private val messageSendingOperations: SimpMessageSendingOperations) {

    fun sendMoveMadeMessage(gameId: UUID, newGameState: GameStateDto, move: LegalMoveDto) {
        val payload = MoveMadePayload(newGameState, move)
        messageSendingOperations.convertAndSend("/topic/games/$gameId/moves", payload)
    }

    fun sendResignationSubmittedMessage(gameId: UUID, newGameState: GameStateDto, resignedColor: Color) {
        val payload = ResignationSubmittedPayload(newGameState, resignedColor)
        messageSendingOperations.convertAndSend("/topic/games/$gameId/resignations", payload)
    }

    internal data class MoveMadePayload(val newGameState: GameStateDto, val move: LegalMoveDto)

    internal data class ResignationSubmittedPayload(val newGameState: GameStateDto, val resignedColor: Color)
}