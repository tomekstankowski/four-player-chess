package pl.tomaszstankowski.fourplayerchess.matchmaking.data

import pl.tomaszstankowski.fourplayerchess.matchmaking.LobbyMembership.RandomBotMembership
import pl.tomaszstankowski.fourplayerchess.matchmaking.RandomBotMembershipRepository
import java.util.*

internal class InMemoryRandomBotMembershipRepository(private val dataSource: InMemoryDataSource) : RandomBotMembershipRepository {

    override fun insert(membership: RandomBotMembership) {
        val isUnique = dataSource.randomBotsMemberships.none {
            it.lobbyId == membership.lobbyId && it.botId == membership.botId
        }
        check(isUnique)
        dataSource.randomBotsMemberships += membership
    }

    override fun findByLobbyId(lobbyId: UUID): List<RandomBotMembership> =
            dataSource.randomBotsMemberships.filter { it.lobbyId == lobbyId }

    override fun deleteByLobbyIdAndBotId(lobbyId: UUID, botId: UUID) {
        dataSource.randomBotsMemberships.removeIf { it.botId == botId && it.lobbyId == lobbyId }
    }
}