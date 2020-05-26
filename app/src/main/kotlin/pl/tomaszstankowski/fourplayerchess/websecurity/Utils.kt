package pl.tomaszstankowski.fourplayerchess.websecurity

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import pl.tomaszstankowski.fourplayerchess.common.utils.toUUID
import java.util.*

fun getAuthenticatedUserId(): UUID =
        (SecurityContextHolder.getContext().authentication.principal as User).username.toUUID()