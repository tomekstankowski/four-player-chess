package pl.tomaszstankowski.fourplayerchess.matchmaking

import org.springframework.messaging.simp.SimpMessageSendingOperations
import java.util.*

internal class LobbyMessageBroker(private val simpMessagingOps: SimpMessageSendingOperations) {

    fun sendPlayerJoinedLobbyMessage(lobbyId: UUID, membership: LobbyMembershipDto) {
        simpMessagingOps.convertAndSend("/topic/lobbies/$lobbyId/joining-players", membership)
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

    fun sendRandomBotAddedToLobbyMessage(lobbyId: UUID, membership: LobbyMembershipDto) {
        simpMessagingOps.convertAndSend("/topic/lobbies/$lobbyId/added-bots", membership)
    }

    fun sendRandomBotRemovedFromLobby(lobbyId: UUID, botId: UUID) {
        val payload = RandomBotRemovedFromLobbyPayload(botId)
        simpMessagingOps.convertAndSend("/topic/lobbies/$lobbyId/removed-bots", payload)
    }

    internal data class PlayerLeftLobbyPayload(val playerId: UUID)

    internal class LobbyDeletedPayload

    internal data class GameStartedPayload(val gameId: UUID)

    internal data class RandomBotRemovedFromLobbyPayload(val botId: UUID)
}