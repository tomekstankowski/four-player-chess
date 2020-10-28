package pl.tomaszstankowski.fourplayerchess.engine.hypermax

import pl.tomaszstankowski.fourplayerchess.engine.*

internal fun evaluateTerminalPosition(position: Position): FloatArray =
        if (position.winner == null) {
            FloatArray(allColors.size) { i ->
                if (position.isEliminated(allColors[i]))
                    WIN_VALUE * -1
                else
                    0f
            }
        } else {
            FloatArray(allColors.size) { i ->
                if (position.winner == allColors[i])
                    WIN_VALUE
                else
                    WIN_VALUE * -1
            }
        }

internal fun evaluateIntermediatePosition(position: Position): FloatArray {
    val scores = FloatArray(allColors.size) { i ->
        if (position.isEliminated(allColors[i])) {
            WIN_VALUE * -1
        } else {
            0f
        }
    }
    evaluateMaterial(position, scores)
    return scores
}

private fun evaluateMaterial(position: Position, scores: FloatArray) {
    val playersInGameCount = allColors.count { color -> !position.isEliminated(color) }
    allColors.forEach { evaluatedColor ->
        if (!position.isEliminated(evaluatedColor)) {
            val materialOfColor = evaluateMaterialOfColor(position, evaluatedColor)
            allColors.forEach { scoredColor ->
                if (scoredColor == evaluatedColor) {
                    scores[scoredColor.ordinal] += materialOfColor.toFloat()
                } else if (!position.isEliminated(scoredColor)) {
                    scores[scoredColor.ordinal] += materialOfColor * -1f / (playersInGameCount - 1)
                }
            }
        }
    }
}

private fun evaluateMaterialOfColor(position: Position, color: Color): Int =
        position.countPiecesBy(color, PieceType.Pawn) * PieceType.Pawn.materialValue +
                position.countPiecesBy(color, PieceType.Knight) * PieceType.Knight.materialValue +
                position.countPiecesBy(color, PieceType.Bishop) * PieceType.Bishop.materialValue +
                position.countPiecesBy(color, PieceType.Rook) * PieceType.Rook.materialValue +
                position.countPiecesBy(color, PieceType.Queen) * PieceType.Queen.materialValue +
                position.countPiecesBy(color, PieceType.King) * PieceType.King.materialValue

private const val WIN_VALUE = 1000000f
