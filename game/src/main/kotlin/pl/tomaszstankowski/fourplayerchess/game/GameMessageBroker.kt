package pl.tomaszstankowski.fourplayerchess.game

import org.springframework.messaging.simp.SimpMessageSendingOperations
import java.util.*

internal class GameMessageBroker(private val messageSendingOperations: SimpMessageSendingOperations) {

    fun sendMoveMadeMessage(gameId: UUID, newGameState: GameStateDto, move: LegalMoveDto) {
        val payload = MoveMadePayload(newGameState, move)
        messageSendingOperations.convertAndSend("/topic/games/$gameId/moves", payload)
    }

    fun sendResignationSubmittedMessage(gameId: UUID, newGameState: GameStateDto, resignedColor: String) {
        val payload = ResignationSubmittedPayload(newGameState, resignedColor)
        messageSendingOperations.convertAndSend("/topic/games/$gameId/resignations", payload)
    }

    fun sendDrawClaimedMessage(gameId: UUID, newGameState: GameStateDto, claimingColor: String) {
        val payload = DrawClaimedPayload(newGameState, claimingColor)
        messageSendingOperations.convertAndSend("/topic/games/$gameId/draw-claimed", payload)
    }

    internal data class MoveMadePayload(val newGameState: GameStateDto, val move: LegalMoveDto)

    internal data class ResignationSubmittedPayload(val newGameState: GameStateDto, val resignedColor: String)

    internal data class DrawClaimedPayload(val newGameState: GameStateDto, val claimingColor: String)
}