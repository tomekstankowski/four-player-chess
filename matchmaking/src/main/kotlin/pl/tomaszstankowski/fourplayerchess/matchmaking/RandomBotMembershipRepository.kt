package pl.tomaszstankowski.fourplayerchess.matchmaking

import pl.tomaszstankowski.fourplayerchess.matchmaking.LobbyMembership.RandomBotMembership
import java.util.*

internal interface RandomBotMembershipRepository {

    fun insert(membership: RandomBotMembership)

    fun findByLobbyId(lobbyId: UUID): List<RandomBotMembership>

    fun deleteByLobbyIdAndBotId(lobbyId: UUID, botId: UUID)
}