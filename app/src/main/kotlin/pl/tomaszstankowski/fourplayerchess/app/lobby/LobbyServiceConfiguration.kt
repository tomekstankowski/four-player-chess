package pl.tomaszstankowski.fourplayerchess.app.lobby

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.tomaszstankowski.fourplayerchess.common.data.IdGenerator
import pl.tomaszstankowski.fourplayerchess.common.data.TransactionExecutor
import pl.tomaszstankowski.fourplayerchess.lobby.LobbyRepository
import pl.tomaszstankowski.fourplayerchess.lobby.LobbyService
import java.time.Clock

@Configuration
class LobbyServiceConfiguration {

    @Bean
    fun lobbyService(
        clock: Clock,
        idGenerator: IdGenerator,
        lobbyRepository: LobbyRepository,
        transactionExecutor: TransactionExecutor
    ) = LobbyService.create(
        clock = clock,
        idGenerator = idGenerator,
        lobbyRepository = lobbyRepository,
        transactionExecutor = transactionExecutor
    )
}