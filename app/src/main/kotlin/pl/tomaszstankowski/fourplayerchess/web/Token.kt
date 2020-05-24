package pl.tomaszstankowski.fourplayerchess.web

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import pl.tomaszstankowski.fourplayerchess.auth.AuthenticationService

@RestController
class TokenController(private val authenticationService: AuthenticationService) {

    @PostMapping("/token")
    fun createTokenForAnonymousUser(): TokenResponse {
        val token = authenticationService.authenticateAnonymously().jwt
        return TokenResponse(token)
    }
}

data class TokenResponse(val token: String)