package pl.tomaszstankowski.fourplayerchess.matchmaking.data

import pl.tomaszstankowski.fourplayerchess.matchmaking.HumanPlayerMembershipRepository
import pl.tomaszstankowski.fourplayerchess.matchmaking.LobbyMembership.HumanPlayerMembership
import java.util.*

internal class InMemoryHumanPlayerMembershipRepository(private val dataSource: InMemoryDataSource) : HumanPlayerMembershipRepository {

    override fun insert(membership: HumanPlayerMembership) {
        val isUnique = dataSource.humanPlayerMemberships
                .none { it.userId == membership.userId && it.lobbyId == membership.userId }
        check(isUnique)
        dataSource.humanPlayerMemberships += membership
    }

    override fun findByLobbyId(lobbyId: UUID): List<HumanPlayerMembership> =
            dataSource.humanPlayerMemberships
                    .filter { it.lobbyId == lobbyId }
                    .sortedByDescending { it.joinedAt }

    override fun deleteByLobbyIdAndPlayerId(lobbyId: UUID, playerId: UUID) {
        dataSource.humanPlayerMemberships.removeIf { it.lobbyId == lobbyId && it.userId == playerId }
    }
}