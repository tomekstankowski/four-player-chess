package pl.tomaszstankowski.fourplayerchess

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.util.JdkIdGenerator
import pl.tomaszstankowski.fourplayerchess.auth.AuthenticationService
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
                           transactionManager: PlatformTransactionManager) =
            MatchmakingService.create(clock, dataSource, transactionManager)

    @Bean
    fun authenticationService(clock: Clock,
                              @Value("\${jwt.secret-key}") secretKey: String,
                              @Value("\${jwt.expiration-time-hours}") expirationTimeHours: Long) =
            AuthenticationService.create(
                    idGenerator = JdkIdGenerator(),
                    clock = clock,
                    secretKeyBase64Encoded = secretKey,
                    expirationTime = expirationTimeHours,
                    expirationTimeUnit = HOURS
            )
}

fun main(args: Array<String>) {
    runApplication<FourPlayerChessApplication>(*args)
}
