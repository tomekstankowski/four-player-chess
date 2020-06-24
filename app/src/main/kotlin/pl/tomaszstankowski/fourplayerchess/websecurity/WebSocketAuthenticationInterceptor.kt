package pl.tomaszstankowski.fourplayerchess.websecurity

import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor

class WebSocketAuthenticationInterceptor(private val authenticationHelper: AuthenticationHelper) : ChannelInterceptor {
    companion object {
        private const val AUTHORIZATION_HEADER = "Authorization"
    }

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {
        val accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java)!!
        if (accessor.command == StompCommand.CONNECT) {
            val header = accessor.getFirstNativeHeader(AUTHORIZATION_HEADER)
            if (header != null) {
                accessor.user = authenticationHelper.authenticateWithJwt(header)
            }
        }
        return message
    }
}