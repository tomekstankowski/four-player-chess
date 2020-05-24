package pl.tomaszstankowski.fourplayerchess.auth

import java.util.*

sealed class JwtAuthenticationResult {
    data class Authenticated(val userId: UUID) : JwtAuthenticationResult()
    object InvalidToken : JwtAuthenticationResult()
}