package pl.tomaszstankowski.fourplayerchess.auth

import io.jsonwebtoken.JwtException
import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import pl.tomaszstankowski.fourplayerchess.common.utils.toUUID
import java.time.Clock
import java.time.Instant
import java.time.temporal.TemporalUnit
import java.util.*
import javax.crypto.SecretKey

internal class JwtIssuer(private val clock: Clock,
                         secretKeyBase64Encoded: String,
                         private val expirationTime: Long,
                         private val expirationTimeUnit: TemporalUnit) {
    private val secretKey: SecretKey
    private val jwtParser: JwtParser

    init {
        val secretKeyBytes = Base64.getDecoder().decode(secretKeyBase64Encoded)
        this.secretKey = Keys.hmacShaKeyFor(secretKeyBytes)
        this.jwtParser = Jwts.parserBuilder()
                .setClock { Date.from(clock.instant()) }
                .setSigningKey(secretKey)
                .build()
    }

    fun generateToken(userId: UUID): String {
        val expirationDate: Date = getExpirationDate()
        val subject = userId.toString()

        return Jwts.builder()
                .setSubject(subject)
                .setExpiration(expirationDate)
                .signWith(secretKey)
                .compact()
    }

    fun parseUserIdFromValidToken(token: String): UUID? =
            try {
                jwtParser
                        .parseClaimsJws(token)
                        .body
                        .subject
                        .toUUID()
            } catch (e: JwtException) {
                null
            }

    private fun getExpirationDate(): Date {
        val now = Instant.now(clock)
        val expireAt = now.plus(expirationTime, expirationTimeUnit)
        return Date.from(expireAt)
    }
}