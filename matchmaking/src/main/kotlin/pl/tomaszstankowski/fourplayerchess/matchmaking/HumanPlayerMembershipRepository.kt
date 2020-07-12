package pl.tomaszstankowski.fourplayerchess.matchmaking

import pl.tomaszstankowski.fourplayerchess.matchmaking.LobbyMembership.HumanPlayerMembership
import java.util.*

internal interface HumanPlayerMembershipRepository {

    fun insert(membership: HumanPlayerMembership)

    fun findByLobbyId(lobbyId: UUID): List<HumanPlayerMembership>

    fun deleteByLobbyIdAndPlayerId(lobbyId: UUID, playerId: UUID)
}