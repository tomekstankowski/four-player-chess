package pl.tomaszstankowski.fourplayerchess

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.transaction.PlatformTransactionManager
import pl.tomaszstankowski.fourplayerchess.auth.AuthenticationService
import pl.tomaszstankowski.fourplayerchess.game.GameControlService
import pl.tomaszstankowski.fourplayerchess.infr.CreateGameUserCaseAdapter
import pl.tomaszstankowski.fourplayerchess.matchmaking.LobbySearchService
import pl.tomaszstankowski.fourplayerchess.matchmaking.MatchmakingService
import java.time.Clock
import java.time.temporal.ChronoUnit.HOURS
import javax.sql.DataSource

@SpringBootApplication
class FourPlayerChessApplication {

    @Bean
    fun clock(): Clock = Clock.systemUTC()

    @Bean
    fun matchmakingService(clock: Clock,
                           dataSource: DataSource,
                           transactionManager: PlatformTransactionManager,
                           gameControlService: GameControlService,
                           simpMessagingTemplate: SimpMessagingTemplate): MatchmakingService {
        return MatchmakingService.create(
                clock = clock,
                dataSource = dataSource,
                transactionManager = transactionManager,
                createGameUseCase = CreateGameUserCaseAdapter(gameControlService),
                simpMessageSendingOperations = simpMessagingTemplate
        )
    }

    @Bean
    fun lobbySearchService(dataSource: DataSource) = LobbySearchService.create(dataSource)

    @Bean
    fun authenticationService(clock: Clock,
                              @Value("\${jwt.secret-key}") secretKey: String,
                              @Value("\${jwt.expiration-time-hours}") expirationTimeHours: Long) =
            AuthenticationService.create(
                    clock = clock,
                    secretKeyBase64Encoded = secretKey,
                    expirationTime = expirationTimeHours,
                    expirationTimeUnit = HOURS
            )

    @Bean
    fun gameControlService(clock: Clock,
                           dataSource: DataSource,
                           transactionManager: PlatformTransactionManager,
                           messagingTemplate: SimpMessagingTemplate) =
            GameControlService.create(
                    clock = clock,
                    dataSource = dataSource,
                    transactionManager = transactionManager,
                    messageSendingOperations = messagingTemplate
            )
}

fun main(args: Array<String>) {
    runApplication<FourPlayerChessApplication>(*args)
}
