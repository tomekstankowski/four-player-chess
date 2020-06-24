package pl.tomaszstankowski.fourplayerchess.matchmaking

import org.springframework.messaging.simp.SimpMessageSendingOperations
import java.util.*

internal class LobbyMessageBroker(private val simpMessagingOps: SimpMessageSendingOperations) {

    fun sendPlayerJoinedLobbyMessage(playerId: UUID, lobbyId: UUID) {
        val payload = PlayerJoinedLobbyPayload(playerId)
        simpMessagingOps.convertAndSend("/topic/lobbies/$lobbyId/joining-players", payload)
    }

    fun sendPlayerLeftLobbyMessage(playerId: UUID, lobbyId: UUID) {
        val payload = PlayerLeftLobbyPayload(playerId)
        simpMessagingOps.convertAndSend("/topic/lobbies/$lobbyId/leaving-players", payload)
    }

    fun sendLobbyDeletedMessage(lobbyId: UUID) {
        val payload = LobbyDeletedPayload()
        simpMessagingOps.convertAndSend("/topic/lobbies/$lobbyId/deleted", payload)
    }

    fun sendGameStartedMessage(lobbyId: UUID, gameId: UUID) {
        val payload = GameStartedPayload(gameId)
        simpMessagingOps.convertAndSend("/topic/lobbies/$lobbyId/game-started", payload)
    }

    internal data class PlayerJoinedLobbyPayload(val playerId: UUID)

    internal data class PlayerLeftLobbyPayload(val playerId: UUID)

    internal class LobbyDeletedPayload

    internal data class GameStartedPayload(val gameId: UUID)
}