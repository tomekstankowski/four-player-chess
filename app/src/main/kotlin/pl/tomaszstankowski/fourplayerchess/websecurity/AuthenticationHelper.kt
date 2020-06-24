package pl.tomaszstankowski.fourplayerchess.websecurity

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User
import pl.tomaszstankowski.fourplayerchess.auth.AuthenticationService
import pl.tomaszstankowski.fourplayerchess.auth.JwtAuthenticationResult
import java.util.*

class AuthenticationHelper(private val authenticationService: AuthenticationService) {
    companion object {
        private const val TOKEN_PREFIX = "Bearer"
    }

    fun authenticateWithJwt(jwtHeader: String): Authentication? {
        if (jwtHeader.startsWith(TOKEN_PREFIX)) {
            val token = jwtHeader.replace(TOKEN_PREFIX, "")
            val authResult = authenticationService.authenticateWithJwt(token)
            if (authResult is JwtAuthenticationResult.Authenticated) {
                return getAuthentication(authResult.userId)
            }
        }
        return null
    }

    private fun getAuthentication(userId: UUID): Authentication {
        val authorities = emptySet<GrantedAuthority>()
        val principal = User(
                userId.toString(),
                "",
                authorities
        )
        return UsernamePasswordAuthenticationToken(principal, "", authorities)
    }
}