package pl.tomaszstankowski.fourplayerchess.app.data

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.tomaszstankowski.fourplayerchess.common.data.IdGenerator
import pl.tomaszstankowski.fourplayerchess.common.data.SimpleIdGenerator

@Configuration
class IdGeneratorConfig {

    @Bean
    fun idGenerator(): IdGenerator = SimpleIdGenerator()
}