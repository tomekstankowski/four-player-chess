package pl.tomaszstankowski.fourplayerchess.websecurity

import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import pl.tomaszstankowski.fourplayerchess.auth.AuthenticationService
import pl.tomaszstankowski.fourplayerchess.auth.JwtAuthenticationResult
import java.util.*
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JwtAuthenticationFilter(private val authenticationService: AuthenticationService,
                              authenticationManager: AuthenticationManager)
    : BasicAuthenticationFilter(authenticationManager) {

    companion object {
        private const val HEADER_NAME = "Authorization"
        private const val TOKEN_PREFIX = "Bearer"
    }

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        val header = request.getHeader(HEADER_NAME)
        if (header != null && header.startsWith(TOKEN_PREFIX)) {
            val token = header.replace(TOKEN_PREFIX, "")
            val authResult = authenticationService.authenticateWithJwt(token)
            if (authResult is JwtAuthenticationResult.Authenticated) {
                setAuthenticatedUser(authResult.userId)
            }
        }
        chain.doFilter(request, response)
    }

    private fun setAuthenticatedUser(userId: UUID) {
        val authorities = emptySet<GrantedAuthority>()
        val principal = User(
                userId.toString(),
                "",
                authorities
        )
        SecurityContextHolder.getContext().authentication =
                UsernamePasswordAuthenticationToken(principal, "", authorities)
    }
}