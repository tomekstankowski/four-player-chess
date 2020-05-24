package pl.tomaszstankowski.fourplayerchess.auth

import org.amshove.kluent.shouldBeEqualTo
import org.spekframework.spek2.Spek
import org.springframework.util.SimpleIdGenerator
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit.HOURS
import java.util.*

class AuthenticationServiceTest : Spek({

    fun createServiceForTesting(
            clock: Clock = Clock.fixed(Instant.parse("2020-05-24T12:03:26.117834Z"), ZoneId.of("UTC")),
            secretKey: String = "BDKdSNVjQX60q8nRK2RGVI2IIIXuq1j+/quqX0InBbI="
    ) =
            AuthenticationService.create(
                    idGenerator = SimpleIdGenerator(),
                    clock = clock,
                    secretKeyBase64Encoded = secretKey,
                    expirationTime = 8,
                    expirationTimeUnit = HOURS
            )

    test("can authenticate with token") {
        val service = createServiceForTesting()

        val token = service.authenticateAnonymously().jwt
        val result = service.authenticateWithJwt(token)

        result shouldBeEqualTo JwtAuthenticationResult.Authenticated(
                UUID.fromString("00000000-0000-0000-0000-000000000001")
        )
    }

    test("cannot authenticate with invalid token") {
        val service = createServiceForTesting()

        val result = service.authenticateWithJwt("not a jwt")

        result shouldBeEqualTo JwtAuthenticationResult.InvalidToken
    }

    test("cannot authenticate with token signed with different key") {
        val serviceWithOldKey = createServiceForTesting()
        val serviceWithNewKey = createServiceForTesting(secretKey = "yYqI65DVLMOMPnt2HDLrltMfABbIiTBZAhMuLCPpYQIkaQZ23a73J6JTttwhVxSu")
        val token = serviceWithOldKey.authenticateAnonymously().jwt

        val result = serviceWithNewKey.authenticateWithJwt(token)

        result shouldBeEqualTo JwtAuthenticationResult.InvalidToken
    }

    test("cannot authenticate with expired token") {
        val service = createServiceForTesting()
        val serviceWithClockOffset = createServiceForTesting(
                clock = Clock.fixed(Instant.parse("2020-05-24T20:03:26.117834Z"), ZoneId.of("UTC"))
        )
        val token = service.authenticateAnonymously().jwt

        val result = serviceWithClockOffset.authenticateWithJwt(token)

        result shouldBeEqualTo JwtAuthenticationResult.InvalidToken
    }
})