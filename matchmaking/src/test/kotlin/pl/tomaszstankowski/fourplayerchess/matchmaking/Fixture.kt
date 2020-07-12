package pl.tomaszstankowski.fourplayerchess.matchmaking

import java.time.Instant
import java.util.*

object Fixture {
    val NOW: Instant = Instant.parse("2020-05-23T18:39:00.240455Z")
    val CREATE_LOBBY_DTO = CreateLobbyDto(
            lobbyEditableDetails = LobbyEditableDetails(name = "Pokój testowy"),
            requestingPlayerId = UUID.fromString("3e8d0140-105c-4d1b-95e6-6b655aad53f0")
    )
    val UPDATE_LOBBY_DTO = UpdateLobbyDto(
            lobbyId = UUID.fromString("00000000-0000-0000-0000-000000000001"),
            lobbyEditableDetails = LobbyEditableDetails(name = "Nowy pokój testowy"),
            requestingPlayerId = UUID.fromString("3e8d0140-105c-4d1b-95e6-6b655aad53f0")
    )
    val DELETE_LOBBY_DTO = DeleteLobbyDto(
            lobbyId = UUID.fromString("00000000-0000-0000-0000-000000000001"),
            requestingPlayerId = UUID.fromString("3e8d0140-105c-4d1b-95e6-6b655aad53f0")
    )
    val JOIN_LOBBY_DTO = JoinLobbyDto(
            lobbyId = UUID.fromString("00000000-0000-0000-0000-000000000001"),
            requestingPlayerId = UUID.fromString("ae312d19-71f0-4078-999a-a3d559c3fb2b")
    )
    val LEAVE_LOBBY_DTO = LeaveLobbyDto(
            lobbyId = UUID.fromString("00000000-0000-0000-0000-000000000001"),
            requestingPlayerId = UUID.fromString("ae312d19-71f0-4078-999a-a3d559c3fb2b")
    )
    val ADD_RANDOM_BOT_DTO = AddRandomBotDto(
            lobbyId = UUID.fromString("00000000-0000-0000-0000-000000000001"),
            requestingPlayerId = UUID.fromString("3e8d0140-105c-4d1b-95e6-6b655aad53f0")
    )
    val REMOVE_RANDOM_BOT_DTO = RemoveRandomBotDto(
            lobbyId = UUID.fromString("00000000-0000-0000-0000-000000000001"),
            requestingPlayerId = UUID.fromString("3e8d0140-105c-4d1b-95e6-6b655aad53f0"),
            botId = UUID.fromString("00000000-0000-0000-0000-000000000001")
    )
    val START_GAME_DTO = StartGameDto(
            lobbyId = UUID.fromString("00000000-0000-0000-0000-000000000001"),
            requestingPlayerId = UUID.fromString("3e8d0140-105c-4d1b-95e6-6b655aad53f0")
    )
    val NEW_GAME_ID: UUID = UUID.fromString("0d3ada42-37f3-4bbf-bd97-90c5633e1b37")
}