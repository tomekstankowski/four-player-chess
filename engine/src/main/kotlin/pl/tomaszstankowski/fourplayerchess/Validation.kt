package pl.tomaszstankowski.fourplayerchess

import pl.tomaszstankowski.fourplayerchess.Color.*
import pl.tomaszstankowski.fourplayerchess.PieceType.Pawn

sealed class Move {
    abstract val from: Position
    abstract val to: Position

    data class ToEmptySquare(override val from: Position, override val to: Position) : Move()
    data class TwoSquaresForwardPawnMove(override val from: Position, override val to: Position) : Move()
    data class Capture(override val from: Position, override val to: Position) : Move()
    data class CaptureByEnPassant(override val from: Position, override val to: Position, val capturedPawnPosition: Position) : Move()
}

typealias MoveList = List<Move>

fun getValidMoves(state: State): MoveList =
        Position.allPositions
                .filter { (file, rank) ->
                    (state.squares[rank][file] as? Square.Occupied)?.piece?.color == state.nextMoveColor
                }
                .map { pos -> getValidMoves(pos, state) }
                .flatten()

private fun getValidMoves(position: Position, state: State): List<Move> {
    val square = state.squares[position.rank][position.file] as Square.Occupied
    return when (square.piece.type) {
        Pawn -> getValidPawnMoves(position, state)
        else -> emptyList()
    }
}

private fun getValidPawnMoves(position: Position, state: State): List<Move> {
    val oneSquareForward = position.offsetOrNull(state.nextMoveColor.pawnForwardVector)
            ?.takeIf { pos -> state.squares.getSquareByPosition(pos) == Square.Empty }
            ?.let { Move.ToEmptySquare(from = position, to = it) }
    val twoSquaresForward = position
            .offsetOrNull(state.nextMoveColor.pawnForwardVector)
            ?.offsetOrNull(state.nextMoveColor.pawnForwardVector)
            ?.takeIf { oneSquareForward != null }
            ?.takeIf { pos -> state.squares.getSquareByPosition(pos) == Square.Empty }
            ?.takeIf { position.isPawnStartingPositionForColor(state.nextMoveColor) }
            ?.let { Move.TwoSquaresForwardPawnMove(from = position, to = it) }
    val captureRightPos = position.offsetOrNull(state.nextMoveColor.pawnCaptureRightVector)
    val captureLeftPos = position.offsetOrNull(state.nextMoveColor.pawnCaptureLeftVector)
    val capturingMoves = listOfNotNull(captureLeftPos, captureRightPos)
            .map { capturePosition ->
                val capture = capturePosition.takeIf { pos ->
                    (state.squares.getSquareByPosition(pos) as? Square.Occupied)?.piece?.color
                            ?.let { color -> color != state.nextMoveColor }
                            ?: false
                }?.let { Move.Capture(from = position, to = it) }
                val captureByEnPassant = capturePosition.takeIf { capture == null }
                        ?.takeIf { state.enPassantSquares.containsValue(it) }
                        ?.let { pos ->
                            val capturedColor = state.enPassantSquares.entries.first { (_, p) -> pos == p }.key
                            Move.CaptureByEnPassant(
                                    from = position,
                                    to = pos,
                                    capturedPawnPosition = pos.offsetOrNull(capturedColor.pawnForwardVector)!!
                            )
                        }
                listOfNotNull(capture, captureByEnPassant)
            }
            .flatten()
    return listOfNotNull(oneSquareForward, twoSquaresForward) + capturingMoves
}

private val Color.pawnForwardVector: Pair<Int, Int>
    get() = when (this) {
        Red -> 0 to 1
        Green -> -1 to 0
        Blue -> 1 to 0
        Yellow -> 0 to -1
    }

private val Color.pawnCaptureRightVector: Pair<Int, Int>
    get() = when (this) {
        Red -> 1 to 1
        Green -> -1 to 1
        Blue -> 1 to -1
        Yellow -> -1 to -1
    }

private val Color.pawnCaptureLeftVector: Pair<Int, Int>
    get() = when (this) {
        Red -> -1 to 1
        Green -> -1 to -1
        Blue -> 1 to 1
        Yellow -> 1 to -1
    }

private fun Position.isPawnStartingPositionForColor(color: Color) =
        when (color) {
            Red -> rank == 1
            Green -> rank == BOARD_SIZE - 2
            Blue -> file == 1
            Yellow -> file == BOARD_SIZE - 2
        }