package pl.tomaszstankowski.fourplayerchess

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import pl.tomaszstankowski.fourplayerchess.game.GameControlService

@Component
class AppRunner(private val gameControlService: GameControlService) : ApplicationRunner {

    override fun run(args: ApplicationArguments?) {
        gameControlService.cancelAllActiveGames()
    }
}