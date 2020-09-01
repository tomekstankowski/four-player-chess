package pl.tomaszstankowski.fourplayerchess.engine.paranoid

import pl.tomaszstankowski.fourplayerchess.engine.Color
import pl.tomaszstankowski.fourplayerchess.engine.PieceType.*
import pl.tomaszstankowski.fourplayerchess.engine.Position
import pl.tomaszstankowski.fourplayerchess.engine.allColors
import pl.tomaszstankowski.fourplayerchess.engine.materialValue

internal fun evaluateParanoidPosition(position: Position, evaluatedColor: Color): Int {
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
        position.countPiecesBy(color, Pawn) * Pawn.materialValue +
                position.countPiecesBy(color, Knight) * Knight.materialValue +
                position.countPiecesBy(color, Bishop) * Bishop.materialValue +
                position.countPiecesBy(color, Rook) * Rook.materialValue +
                position.countPiecesBy(color, Queen) * Queen.materialValue +
                // low value for king to prevent sacrificing pieces in order to capture lonely king (when not 1v1)
                // but not 0 to encourage eliminating lonely kings
                200