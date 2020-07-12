package pl.tomaszstankowski.fourplayerchess.matchmaking

import org.springframework.util.IdGenerator
import pl.tomaszstankowski.fourplayerchess.matchmaking.LobbyMembership.RandomBotMembership
import java.time.Clock
import java.time.Instant
import java.util.*

internal class RandomBotFactory(private val clock: Clock, private val idGenerator: IdGenerator) {

    fun createBotMembership(lobbyId: UUID): RandomBotMembership =
            RandomBotMembership(
                    lobbyId = lobbyId,
                    joinedAt = Instant.now(clock),
                    botId = idGenerator.generateId()
            )
}