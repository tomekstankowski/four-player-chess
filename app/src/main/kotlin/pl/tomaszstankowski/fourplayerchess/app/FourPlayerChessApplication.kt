package pl.tomaszstankowski.fourplayerchess.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class FourPlayerChessApplication

fun main(args: Array<String>) {
    runApplication<FourPlayerChessApplication>(*args)
}
