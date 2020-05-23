package pl.tomaszstankowski.fourplayerchess.matchmaking

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.util.JdkIdGenerator
import pl.tomaszstankowski.fourplayerchess.matchmaking.data.JdbcLobbyRepository
import java.time.Clock
import javax.sql.DataSource

@Configuration
class MatchmakingServiceConfiguration {

    @Bean
    fun matchmakingService(clock: Clock,
                           dataSource: DataSource,
                           transactionManager: PlatformTransactionManager) =
            MatchmakingService.create(
                    clock = clock,
                    idGenerator = JdkIdGenerator(),
                    lobbyRepository = JdbcLobbyRepository(dataSource),
                    transactionOperations = TransactionTemplate(transactionManager)
            )
}