package pl.tomaszstankowski.fourplayerchess.auth

import org.springframework.util.IdGenerator
import org.springframework.util.JdkIdGenerator
import java.time.Clock
import java.time.temporal.TemporalUnit

class AuthenticationService private constructor(private val idGenerator: IdGenerator,
                                                private val jwtIssuer: JwtIssuer) {

    companion object {
        fun create(idGenerator: IdGenerator = JdkIdGenerator(),
                   clock: Clock,
                   secretKeyBase64Encoded: String,
                   expirationTime: Long,
                   expirationTimeUnit: TemporalUnit) =
                AuthenticationService(
                        idGenerator = idGenerator,
                        jwtIssuer = JwtIssuer(
                                clock = clock,
                                secretKeyBase64Encoded = secretKeyBase64Encoded,
                                expirationTime = expirationTime,
                                expirationTimeUnit = expirationTimeUnit
                        )
                )
    }

    fun authenticateAnonymously(): AnonymousAuthenticationResult {
        val userId = idGenerator.generateId()
        val jwt = jwtIssuer.generateToken(userId)
        return AnonymousAuthenticationResult(jwt)
    }

    fun authenticateWithJwt(jwt: String): JwtAuthenticationResult {
        val userId = jwtIssuer.parseUserIdFromValidToken(jwt)
                ?: return JwtAuthenticationResult.InvalidToken
        return JwtAuthenticationResult.Authenticated(userId)
    }
}