package pl.tomaszstankowski.fourplayerchess.matchmaking

import java.util.*

internal class InMemoryLobbyMembershipRepository(private val dataSource: InMemoryDataSource) : LobbyMembershipRepository {

    override fun insert(lobbyMembership: LobbyMembership) {
        val isUnique = dataSource.lobbyMemberships
                .none { it.playerId == lobbyMembership.playerId && it.lobbyId == lobbyMembership.playerId }
        check(isUnique)
        dataSource.lobbyMemberships += lobbyMembership
    }

    override fun findByLobbyIdOrderByCreatedAtDesc(lobbyId: UUID): List<LobbyMembership> =
            dataSource.lobbyMemberships
                    .filter { it.lobbyId == lobbyId }
                    .sortedByDescending { it.joinedAt }

    override fun deleteByLobbyIdAndPlayerId(lobbyId: UUID, playerId: UUID) {
        dataSource.lobbyMemberships.removeIf { it.lobbyId == lobbyId && it.playerId == playerId }
    }

    override fun deleteByLobbyId(lobbyId: UUID) {
        dataSource.lobbyMemberships.removeIf { it.lobbyId == lobbyId }
    }
}