package pl.tomaszstankowski.fourplayerchess

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.plus
import pl.tomaszstankowski.fourplayerchess.Color.*
import pl.tomaszstankowski.fourplayerchess.PieceType.*

sealed class Move {
    abstract val from: Position
    abstract val to: Position

    data class ToEmptySquare(override val from: Position, override val to: Position) : Move()
    data class TwoSquaresForwardPawnMove(override val from: Position, override val to: Position) : Move()
    data class Capture(override val from: Position, override val to: Position) : Move()
    data class CaptureByEnPassant(override val from: Position, override val to: Position, val capturedPawnPosition: Position) : Move()
    sealed class Castling : Move() {
        data class KingSide(override val from: Position, override val to: Position) : Castling()
        data class QueenSide(override val from: Position, override val to: Position) : Castling()
    }
}

typealias MoveList = List<Move>

fun getValidMoves(state: State): MoveList =
        Position.allPositions
                .mapNotNull { pos ->
                    when (val square = state.squares.getSquareByPosition(pos)) {
                        is Square.Occupied ->
                            if (square.piece.color == state.nextMoveColor) getValidMoves(pos, state)
                            else null
                        else -> null
                    }
                }
                .flatten()

private fun getValidMoves(position: Position, state: State): List<Move> {
    val square = state.squares[position.rank][position.file] as Square.Occupied
    return when (square.piece.type) {
        Pawn -> getValidPawnMoves(position, state)
        Bishop -> getValidBishopMoves(position, state)
        Knight -> getValidKnightMoves(position, state)
        Rook -> getValidRookMoves(position, state)
        Queen -> getValidQueenMoves(position, state)
        King -> getValidKingMoves(position, state)
    }
}

private fun getValidPawnMoves(position: Position, state: State): List<Move> {
    val oneSquareForward = position.offsetOrNull(state.nextMoveColor.pawnForwardVector)
            ?.takeIf { pos -> state.squares.getSquareByPosition(pos) == Square.Empty }
            ?.let { Move.ToEmptySquare(from = position, to = it) }
    val twoSquaresForward = position
            .offsetOrNull(state.nextMoveColor.pawnForwardVector * 2)
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

private typealias Vector = Pair<Int, Int>

private operator fun Vector.times(n: Int): Vector =
        this.first * n to this.second * n

private val topV = 0 to 1
private val topRightV = 1 to 1
private val topLeftV = -1 to 1
private val rightV = 1 to 0
private val leftV = -1 to 0
private val bottomV = 0 to -1
private val bottomRightV = 1 to -1
private val bottomLeftV = -1 to -1

private val Color.pawnForwardVector: Vector
    get() = when (this) {
        Red -> topV
        Green -> leftV
        Blue -> rightV
        Yellow -> bottomV
    }

private val Color.pawnCaptureRightVector: Vector
    get() = when (this) {
        Red -> topRightV
        Green -> topLeftV
        Blue -> bottomRightV
        Yellow -> bottomLeftV
    }

private val Color.pawnCaptureLeftVector: Vector
    get() = when (this) {
        Red -> topLeftV
        Green -> bottomLeftV
        Blue -> topRightV
        Yellow -> bottomRightV
    }

private fun Position.isPawnStartingPositionForColor(color: Color) =
        when (color) {
            Red -> rank == 1
            Green -> rank == BOARD_SIZE - 2
            Blue -> file == 1
            Yellow -> file == BOARD_SIZE - 2
        }

private fun getValidBishopMoves(position: Position, state: State): List<Move> =
        listOf(topRightV, topLeftV, bottomLeftV, bottomRightV)
                .map { vector ->
                    getMovesRec(
                            startingPosition = position,
                            lastPosition = position,
                            offsetVector = vector,
                            state = state,
                            moves = persistentListOf()
                    )
                }
                .flatten()

private tailrec fun getMovesRec(startingPosition: Position,
                                lastPosition: Position,
                                offsetVector: Vector,
                                state: State,
                                moves: PersistentList<Move>): PersistentList<Move> {
    val newPos = lastPosition.offsetOrNull(offsetVector) ?: return moves
    return when (val square = state.squares.getSquareByPosition(newPos)) {
        is Square.Occupied -> when (square.piece.color) {
            state.nextMoveColor -> moves
            else -> moves + Move.Capture(from = startingPosition, to = newPos)
        }
        is Square.Empty -> getMovesRec(
                startingPosition = startingPosition,
                lastPosition = newPos,
                offsetVector = offsetVector,
                state = state,
                moves = moves + Move.ToEmptySquare(from = startingPosition, to = newPos)
        )
    }
}

private val knightMoveVectors = listOf(
        1 to 2,
        2 to 1,
        2 to -1,
        1 to -2,
        -1 to -2,
        -2 to -1,
        -2 to 1,
        -1 to 2)

private fun getValidKnightMoves(position: Position, state: State): List<Move> =
        knightMoveVectors
                .mapNotNull { vector ->
                    val newPos = position.offsetOrNull(vector) ?: return@mapNotNull null
                    return@mapNotNull when (val square = state.squares.getSquareByPosition(newPos)) {
                        is Square.Occupied ->
                            if (square.piece.color == state.nextMoveColor) null
                            else Move.Capture(from = position, to = newPos)
                        is Square.Empty -> Move.ToEmptySquare(from = position, to = newPos)
                    }
                }

private fun getValidRookMoves(position: Position, state: State): List<Move> =
        listOf(topV, rightV, bottomV, leftV)
                .map { vector ->
                    getMovesRec(
                            startingPosition = position,
                            lastPosition = position,
                            offsetVector = vector,
                            state = state,
                            moves = persistentListOf()
                    )
                }
                .flatten()

private fun getValidQueenMoves(position: Position, state: State): List<Move> =
        listOf(topV, topRightV, rightV, bottomRightV,
                bottomV, bottomLeftV, leftV, topLeftV)
                .map { vector ->
                    getMovesRec(
                            startingPosition = position,
                            lastPosition = position,
                            offsetVector = vector,
                            state = state,
                            moves = persistentListOf()
                    )
                }
                .flatten()

private fun getValidKingMoves(position: Position, state: State): List<Move> {
    val color = state.nextMoveColor
    val kingSideVector = color.kingSideBaseVector
    val castleKingSide = position.offsetOrNull(kingSideVector * 2)
            ?.takeIf { state.colorToCastlingOptions[color].contains(Castling.KingSide) }
            ?.takeIf { newPos -> state.squares.getSquareByPosition(newPos) == Square.Empty }
            ?.takeIf { state.squares.getSquareByPosition(position.offset(kingSideVector)) == Square.Empty }
            ?.let { newPos -> Move.Castling.KingSide(from = position, to = newPos) }
    val queenSideVector = color.queenSideBaseVector
    val castleQueenSide = position.offsetOrNull(queenSideVector * 2)
            ?.takeIf { state.colorToCastlingOptions[color].contains(Castling.QueenSide) }
            ?.takeIf { newPos -> state.squares.getSquareByPosition(newPos) == Square.Empty }
            ?.takeIf { state.squares.getSquareByPosition(position.offset(queenSideVector)) == Square.Empty }
            ?.takeIf { state.squares.getSquareByPosition(position.offset(queenSideVector * 3)) == Square.Empty }
            ?.let { newPos -> Move.Castling.QueenSide(from = position, to = newPos) }
    val basicMoves = listOf(topV, topRightV, rightV, bottomRightV,
            bottomV, bottomLeftV, leftV, topLeftV)
            .mapNotNull { vector ->
                val newPos = position.offsetOrNull(vector) ?: return@mapNotNull null
                return@mapNotNull when (val square = state.squares.getSquareByPosition(newPos)) {
                    is Square.Occupied ->
                        if (square.piece.color == color) null
                        else Move.Capture(from = position, to = newPos)
                    is Square.Empty -> Move.ToEmptySquare(from = position, to = newPos)
                }
            }
    return basicMoves + listOfNotNull(castleKingSide, castleQueenSide)
}


private val Color.kingSideBaseVector: Vector
    get() = when (this) {
        Red -> rightV
        Green -> bottomV
        Blue -> topV
        Yellow -> leftV
    }

private val Color.queenSideBaseVector: Vector
    get() = when (this) {
        Red -> leftV
        Green -> topV
        Blue -> bottomV
        Yellow -> rightV
    }