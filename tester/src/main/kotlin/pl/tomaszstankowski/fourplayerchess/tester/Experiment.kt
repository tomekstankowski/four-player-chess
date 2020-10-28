package pl.tomaszstankowski.fourplayerchess.tester

import pl.tomaszstankowski.fourplayerchess.engine.Color
import pl.tomaszstankowski.fourplayerchess.engine.Engine
import pl.tomaszstankowski.fourplayerchess.engine.FenState
import pl.tomaszstankowski.fourplayerchess.engine.TranspositionTableOptions
import java.io.PrintWriter
import java.time.Duration

enum class Experiment {
    NoLimits,
    FixedDepth,
    Random
}

enum class Algorithm {
    Paranoid, Hypermax, Random
}

data class AlgorithmAssignment(val red: Algorithm,
                               val blue: Algorithm,
                               val yellow: Algorithm,
                               val green: Algorithm) : Iterable<Algorithm> {

    override fun iterator(): Iterator<Algorithm> =
            listOf(red, blue, yellow, green).listIterator()

    override fun toString(): String {
        return "($red,$blue,$yellow,$green)"
    }

}

private val Experiment.algorithmAssignments: List<AlgorithmAssignment>
    get() = when (this) {
        Experiment.NoLimits, Experiment.FixedDepth ->
            listOf(
                    AlgorithmAssignment(Algorithm.Paranoid, Algorithm.Paranoid, Algorithm.Paranoid, Algorithm.Hypermax),
                    AlgorithmAssignment(Algorithm.Paranoid, Algorithm.Paranoid, Algorithm.Hypermax, Algorithm.Paranoid),
                    AlgorithmAssignment(Algorithm.Paranoid, Algorithm.Hypermax, Algorithm.Paranoid, Algorithm.Paranoid),
                    AlgorithmAssignment(Algorithm.Hypermax, Algorithm.Paranoid, Algorithm.Paranoid, Algorithm.Paranoid),

                    AlgorithmAssignment(Algorithm.Paranoid, Algorithm.Paranoid, Algorithm.Hypermax, Algorithm.Hypermax),
                    AlgorithmAssignment(Algorithm.Paranoid, Algorithm.Hypermax, Algorithm.Paranoid, Algorithm.Hypermax),
                    AlgorithmAssignment(Algorithm.Paranoid, Algorithm.Hypermax, Algorithm.Hypermax, Algorithm.Paranoid),

                    AlgorithmAssignment(Algorithm.Hypermax, Algorithm.Hypermax, Algorithm.Paranoid, Algorithm.Paranoid),
                    AlgorithmAssignment(Algorithm.Hypermax, Algorithm.Paranoid, Algorithm.Hypermax, Algorithm.Paranoid),
                    AlgorithmAssignment(Algorithm.Hypermax, Algorithm.Paranoid, Algorithm.Paranoid, Algorithm.Hypermax),

                    AlgorithmAssignment(Algorithm.Hypermax, Algorithm.Hypermax, Algorithm.Hypermax, Algorithm.Paranoid),
                    AlgorithmAssignment(Algorithm.Hypermax, Algorithm.Hypermax, Algorithm.Paranoid, Algorithm.Hypermax),
                    AlgorithmAssignment(Algorithm.Hypermax, Algorithm.Paranoid, Algorithm.Hypermax, Algorithm.Hypermax),
                    AlgorithmAssignment(Algorithm.Paranoid, Algorithm.Hypermax, Algorithm.Hypermax, Algorithm.Hypermax)
            )
        Experiment.Random ->
            listOf(
                    AlgorithmAssignment(Algorithm.Hypermax, Algorithm.Random, Algorithm.Random, Algorithm.Random),
                    AlgorithmAssignment(Algorithm.Random, Algorithm.Hypermax, Algorithm.Random, Algorithm.Random),
                    AlgorithmAssignment(Algorithm.Random, Algorithm.Random, Algorithm.Hypermax, Algorithm.Random),
                    AlgorithmAssignment(Algorithm.Random, Algorithm.Random, Algorithm.Random, Algorithm.Hypermax),

                    AlgorithmAssignment(Algorithm.Paranoid, Algorithm.Random, Algorithm.Random, Algorithm.Random),
                    AlgorithmAssignment(Algorithm.Random, Algorithm.Paranoid, Algorithm.Random, Algorithm.Random),
                    AlgorithmAssignment(Algorithm.Random, Algorithm.Random, Algorithm.Paranoid, Algorithm.Random),
                    AlgorithmAssignment(Algorithm.Random, Algorithm.Random, Algorithm.Random, Algorithm.Paranoid)
            )
    }

private val startingState = FenState.starting()

fun doExperiment(experiment: Experiment,
                 numberOfGamesPerAssignment: Int,
                 searchDuration: Duration,
                 searchDepth: Int) {
    val errorWriter = getErrorOutputWriter(experiment)
    val errorPrinter = PrintWriter(errorWriter)
    experiment.algorithmAssignments.forEach { assignment ->
        for (i in 1..numberOfGamesPerAssignment) {
            try {
                playGame(
                        experiment,
                        assignment,
                        gameNr = i,
                        maxDepth = if (experiment == Experiment.FixedDepth) searchDepth else 20,
                        searchDuration = searchDuration,
                        ttOptions = when (experiment) {
                            Experiment.NoLimits, Experiment.Random -> TranspositionTableOptions(isPositionEvaluationFetchAllowed = true)
                            Experiment.FixedDepth -> TranspositionTableOptions(isPositionEvaluationFetchAllowed = false)
                        }
                )
            } catch (e: Throwable) {
                errorPrinter.println("Error occurred for assignment $assignment and game $i")
                e.printStackTrace(errorPrinter)
                errorPrinter.flush()
            }
        }
    }
    errorPrinter.close()
}

private fun playGame(experiment: Experiment,
                     assignment: AlgorithmAssignment,
                     gameNr: Int,
                     maxDepth: Int,
                     searchDuration: Duration,
                     ttOptions: TranspositionTableOptions) {
    val dataWriter = createOutputFileIfNotExists(experiment, assignment, gameNr, fileType = OutputFileType.Data)
    if (dataWriter == null) {
        println("Data file already exists for assignment $assignment and game $gameNr, skipping")
        return
    }
    val resultWriter = createOutputFileIfNotExists(experiment, assignment, gameNr, fileType = OutputFileType.Result)
    if (resultWriter == null) {
        println("Result file already exists for assignment $assignment and game $gameNr, skipping")
        return
    }
    val engines = assignment.map { algorithm ->
        when (algorithm) {
            Algorithm.Paranoid -> Engine.withParanoidSearch(startingState, transpositionTableOptions = ttOptions)
            Algorithm.Hypermax -> Engine.withHypermax(startingState, transpositionTableOptions = ttOptions)
            Algorithm.Random -> Engine.withRandomSearch(startingState)
        }
    }
    val eliminatedColors = linkedSetOf<Color>()
    var nextMoveColor = Color.Red

    while (true) {
        val engine = engines[nextMoveColor.ordinal]
        repeat(3) { println() }
        println("Searching for $nextMoveColor")
        println()
        val searchTask = engine.search(maxDepth) ?: break
        searchTask.setOnDepthReachedListener(PrintOnDepthReached)
        if (experiment == Experiment.NoLimits || experiment == Experiment.Random) {
            val isFinished = searchTask.await(searchDuration)
            if (!isFinished) engine.stopSearching()
        }
        searchTask.await()
        val searchResult = searchTask.depthSearchResults.lastOrNull()
                ?: throw IllegalStateException("No search result")
        val pvMove = searchResult.principalVariation.first()
        engines.forEach {
            val isMoveMade = it.makeMove(pvMove.move)
            if (!isMoveMade) {
                throw IllegalStateException("Move not made")
            }
        }
        dataWriter.writeSearchResult(searchResult, nextMoveColor)
        val state = engine.getUIState()
        eliminatedColors.addAll(state.fenState.eliminatedColors)
        if (state.isGameOver) {
            resultWriter.writeGameResult(state, eliminatedColors).close()
            break
        }
        nextMoveColor = state.fenState.nextMoveColor
    }
}