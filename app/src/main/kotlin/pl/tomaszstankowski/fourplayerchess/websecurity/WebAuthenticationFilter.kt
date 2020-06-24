package pl.tomaszstankowski.fourplayerchess.websecurity

import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class WebAuthenticationFilter(private val authenticationHelper: AuthenticationHelper,
                              authenticationManager: AuthenticationManager)
    : BasicAuthenticationFilter(authenticationManager) {

    companion object {
        private const val HEADER_NAME = "Authorization"
    }

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        val header = request.getHeader(HEADER_NAME)
        if (header != null) {
            SecurityContextHolder.getContext().authentication = authenticationHelper.authenticateWithJwt(header)
        }
        chain.doFilter(request, response)
    }
}