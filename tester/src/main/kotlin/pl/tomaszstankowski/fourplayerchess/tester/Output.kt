package pl.tomaszstankowski.fourplayerchess.tester

import pl.tomaszstankowski.fourplayerchess.engine.Color
import pl.tomaszstankowski.fourplayerchess.engine.SearchResult
import pl.tomaszstankowski.fourplayerchess.engine.SearchTask
import pl.tomaszstankowski.fourplayerchess.engine.UIState
import java.io.BufferedWriter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

enum class OutputFileType {
    Result,
    Data
}

private fun getExperimentDir(experiment: Experiment): Path {
    val mainDir = Paths.get("resultsdebug")
    if (!Files.exists(mainDir)) {
        Files.createDirectory(mainDir)
    }
    val experimentDirName = when (experiment) {
        Experiment.NoLimits -> "no_limits"
        Experiment.FixedDepth -> "fixed_depth"
        Experiment.Random -> "random"
    }
    val experimentDir = mainDir.resolve(experimentDirName)
    if (!Files.exists(experimentDir)) {
        Files.createDirectory(experimentDir)
    }
    return experimentDir
}

fun getErrorOutputWriter(experiment: Experiment): BufferedWriter {
    val experimentDir = getExperimentDir(experiment)
    val file = experimentDir.resolve("errors.txt")
    if (!Files.exists(file)) {
        Files.createFile(file)
    }
    return Files.newBufferedWriter(file, StandardOpenOption.APPEND)
}

fun createOutputFileIfNotExists(experiment: Experiment, assignment: AlgorithmAssignment, gameNr: Int, fileType: OutputFileType): BufferedWriter? {
    val typePrefix = when (fileType) {
        OutputFileType.Result -> "result"
        OutputFileType.Data -> "data"
    }
    val filenameBuilder = StringBuilder()
    assignment.forEach { algorithm ->
        filenameBuilder.append(algorithm.name.first().toLowerCase())
    }
    filenameBuilder.append("_")
    filenameBuilder.append(gameNr)
    filenameBuilder.append("_")
    filenameBuilder.append(typePrefix)
    filenameBuilder.append(".csv")
    val filename = filenameBuilder.toString()

    val experimentDir = getExperimentDir(experiment)

    val file = experimentDir.resolve(filename)
    if (Files.exists(file)) {
        return null
    }
    Files.createFile(file)
    return Files.newBufferedWriter(file)
}

private const val FIELD_SEP = ","
private const val LINE_SEP = "\n"

fun BufferedWriter.writeSearchResult(result: SearchResult, color: Color) {
    write(color.ordinal.toString())
    write(FIELD_SEP)
    write(result.principalVariation.first().moveText)
    write(FIELD_SEP)
    write(result.evaluation.toString())
    write(FIELD_SEP)
    write(result.depth.toString())
    write(FIELD_SEP)
    write(result.nodeCount.toString())
    write(FIELD_SEP)
    write(result.leafCount.toString())
    write(FIELD_SEP)
    write(result.duration.toMillis().toString())
    write(LINE_SEP)
    flush()
}

fun BufferedWriter.writeGameResult(state: UIState, eliminatedColors: LinkedHashSet<Color>): BufferedWriter {
    val winner = state.winningColor
    if (winner != null) {
        write(winner.ordinal.toString())
    } else {
        write("-")
    }
    write(FIELD_SEP)
    val eliminatedColorsText = eliminatedColors.joinToString(separator = "-", transform = { color -> color.ordinal.toString() })
    write(eliminatedColorsText)
    return this
}

object PrintOnDepthReached : SearchTask.OnDepthReachedListener {

    override fun onDepthReached(result: SearchResult) {
        result.print()
        println("--------------------------------")
    }

}
