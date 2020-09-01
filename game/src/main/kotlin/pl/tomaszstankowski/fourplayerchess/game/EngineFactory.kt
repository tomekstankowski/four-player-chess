package pl.tomaszstankowski.fourplayerchess.game

import pl.tomaszstankowski.fourplayerchess.engine.Engine
import pl.tomaszstankowski.fourplayerchess.engine.FenState
import kotlin.random.Random

internal interface EngineFactory {

    fun create(): Engine
}

internal class ParanoidSearchEngineFactory : EngineFactory {

    override fun create(): Engine =
            Engine.withParanoidSearch(
                    state = FenState.starting()
            )

}

internal class RandomSearchEngineFactory(private val random: Random) : EngineFactory {

    override fun create(): Engine =
            Engine.withRandomSearch(FenState.starting(), random)

}

internal class HypermaxEngineFactory : EngineFactory {

    override fun create(): Engine =
            Engine.withHypermax(FenState.starting())

}