package pl.tomaszstankowski.fourplayerchess.tester

import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import java.time.Duration

private val Experiment.cmdArgValue: String
    get() = when (this) {
        Experiment.NoLimits -> "no_limits"
        Experiment.FixedDepth -> "fixed_depth"
        Experiment.Random -> "random"
    }

private fun String.toExperiment(): Experiment? =
        Experiment.values()
                .firstOrNull { it.cmdArgValue == this }

private val DEFAULT_EXPERIMENT = Experiment.NoLimits.cmdArgValue
private const val DEFAULT_NUMBER_OF_GAMES = 5
private const val DEFAULT_TIME_PER_MOVE_S = 5
private const val DEFAULT_DEPTH = 4

private val experimentOption: Option = Option.builder("e")
        .longOpt("experiment")
        .desc("Experiment type, default is '${DEFAULT_EXPERIMENT}'")
        .hasArg()
        .build()
private val numberOfGamesOption: Option = Option.builder("n")
        .longOpt("numberOfGames")
        .desc("Number of games per algorithm assignment, default is $DEFAULT_NUMBER_OF_GAMES.")
        .hasArg()
        .build()
private val secondsPerMoveOption: Option = Option.builder("t")
        .longOpt("timePerMove")
        .desc("Seconds per move, default is $DEFAULT_TIME_PER_MOVE_S. " +
                "Used when experiment is '${Experiment.NoLimits.cmdArgValue}' or '${Experiment.Random.cmdArgValue}'.")
        .hasArg()
        .build()
private val depthOption: Option = Option.builder("d")
        .longOpt("depth")
        .desc("Target search depth, default is $DEFAULT_DEPTH. " +
                "Used when experiment is '${Experiment.FixedDepth.cmdArgValue}'.")
        .hasArg()
        .build()
private val cmdOptions: Options = Options()
        .addOption(experimentOption)
        .addOption(numberOfGamesOption)
        .addOption(secondsPerMoveOption)
        .addOption(depthOption)

data class ExperimentArgs(
        val experiment: Experiment,
        val numberOfGamesPerAssignment: Int,
        val searchDuration: Duration,
        val searchDepth: Int
)

fun parseArguments(args: Array<String>): ExperimentArgs {
    val cmdParser = DefaultParser()
    val cmd = cmdParser.parse(cmdOptions, args)
    val experiment = cmd
            .getOptionValue(experimentOption.opt, Experiment.NoLimits.cmdArgValue)
            .toExperiment()
            ?: throw ParseException("Invalid ${experimentOption.longOpt} value")
    val numberOfGamesPerAssignment = cmd
            .getOptionValue(numberOfGamesOption.opt, DEFAULT_NUMBER_OF_GAMES.toString())
            .toIntOrNull()
            ?.takeIf { it > 0 }
            ?: throw ParseException("Invalid ${numberOfGamesOption.longOpt} value")
    val secondsPerMove = cmd
            .getOptionValue(secondsPerMoveOption.opt, DEFAULT_TIME_PER_MOVE_S.toString())
            .toIntOrNull()
            ?.takeIf { it > 0 }
            ?: throw  ParseException("Invalid ${secondsPerMoveOption.longOpt} value")
    val searchDepth = cmd
            .getOptionValue(secondsPerMoveOption.opt, DEFAULT_DEPTH.toString())
            .toIntOrNull()
            ?.takeIf { it in 1..29 }
            ?: throw ParseException("Invalid ${secondsPerMoveOption.opt} value")
    return ExperimentArgs(
            experiment,
            numberOfGamesPerAssignment,
            Duration.ofSeconds(secondsPerMove.toLong()),
            searchDepth
    )
}