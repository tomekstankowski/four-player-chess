package pl.tomaszstankowski.fourplayerchess.engine

import pl.tomaszstankowski.fourplayerchess.engine.PieceType.*

internal fun evaluatePosition(position: Position, evaluatedColor: Color): Int {
    val material = evaluateMaterial(position, evaluatedColor)
    return material
}

private fun evaluateMaterial(position: Position, evaluatedColor: Color): Int =
        evaluateMaterialForColor(position, evaluatedColor) - allColors.sumBy { color ->
            if (position.isEliminated(color) || color == evaluatedColor)
                0
            else
                evaluateMaterialForColor(position, color)
        }

private fun evaluateMaterialForColor(position: Position, color: Color) =
        position.countPiecesBy(color, Pawn) * 100 +
                position.countPiecesBy(color, Knight) * 300 +
                position.countPiecesBy(color, Bishop) * 500 +
                position.countPiecesBy(color, Rook) * 500 +
                position.countPiecesBy(color, Queen) * 900 +
                position.countPiecesBy(color, King) * 2000