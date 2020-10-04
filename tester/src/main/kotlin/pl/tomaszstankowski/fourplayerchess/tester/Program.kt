package pl.tomaszstankowski.fourplayerchess.tester

import org.apache.commons.cli.ParseException

fun main(args: Array<String>) {
    try {
        val experimentArgs = parseArguments(args)
        doExperiment(
                experimentArgs.experiment,
                experimentArgs.numberOfGamesPerAssignment,
                experimentArgs.searchDuration,
                experimentArgs.searchDepth
        )
    } catch (e: ParseException) {
        println("Parse error: ")
        println(e.message)
    }

}