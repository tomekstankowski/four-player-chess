package pl.tomaszstankowski.fourplayerchess.matchmaking

import java.util.*

internal interface LobbyMembershipRepository {

    fun insert(lobbyMembership: LobbyMembership)

    fun findByLobbyIdOrderByCreatedAtDesc(lobbyId: UUID): List<LobbyMembership>

    fun findByPlayerId(playerId: UUID): LobbyMembership?

    fun deleteByLobbyIdAndPlayerId(lobbyId: UUID, playerId: UUID)

    fun deleteByLobbyId(lobbyId: UUID)
}