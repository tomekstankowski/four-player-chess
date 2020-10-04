package pl.tomaszstankowski.fourplayerchess.engine.hypermax

import pl.tomaszstankowski.fourplayerchess.engine.*

internal fun evaluateHyperMaxPosition(position: Position): FloatArray {
    val scores = FloatArray(allColors.size)

    if (position.isDraw || position.isDrawByClaimPossible) {
        val eliminatedPlayersCount = allColors.count { color -> position.isEliminated(color) }

        allColors.forEach { color ->
            if (position.isEliminated(color)) {
                scores[color.ordinal] = WIN_VALUE * -1f / eliminatedPlayersCount
            } else {
                scores[color.ordinal] = WIN_VALUE / (allColors.size - eliminatedPlayersCount)
            }
        }
    } else if (position.winner != null) {
        allColors.forEach { color ->
            if (color == position.winner) {
                scores[color.ordinal] = WIN_VALUE
            } else {
                scores[color.ordinal] = WIN_VALUE * -1f / (allColors.size - 1)
            }
        }
    } else {
        evaluateMaterial(position, scores)
    }

    return scores
}

private fun evaluateMaterial(position: Position, scores: FloatArray) {
    allColors.forEach { color ->
        if (!position.isEliminated(color)) {
            val materialOfColor = evaluateMaterialOfColor(position, color)
            allColors.forEach { scoredColor ->
                if (scoredColor == color) {
                    scores[scoredColor.ordinal] += materialOfColor.toFloat()
                } else {
                    scores[scoredColor.ordinal] += materialOfColor * -1f / (allColors.size - 1)
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