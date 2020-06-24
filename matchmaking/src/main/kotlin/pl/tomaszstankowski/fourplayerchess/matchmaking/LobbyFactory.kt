package pl.tomaszstankowski.fourplayerchess.matchmaking

import org.springframework.util.IdGenerator
import java.time.Clock
import java.time.Instant
import java.util.*

internal class LobbyFactory(private val clock: Clock,
                            private val idGenerator: IdGenerator) {

    fun create(lobbyEditableDetails: LobbyEditableDetails, ownerId: UUID) = Lobby(
            id = idGenerator.generateId(),
            name = lobbyEditableDetails.name,
            createdAt = Instant.now(clock),
            ownerId = ownerId,
            gameId = null,
            isDeleted = false,
            version = 1
    )
}