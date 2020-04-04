package pl.tomaszstankowski.fourplayerchess

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.plus
import pl.tomaszstankowski.fourplayerchess.Color.*
import pl.tomaszstankowski.fourplayerchess.PieceType.*
import kotlin.math.max
import kotlin.math.min

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

fun getValidMoves(state: State): List<Move> =
        Position.allPositions
                .mapNotNull { pos ->
                    when (val square = state.squares.getSquareByPosition(pos)) {
                        is Square.Occupied ->
                            if (square.piece.color == state.nextMoveColor) getValidMoves(pos, state)
                            else null
                        is Square.Empty -> null
                    }
                }
                .flatten()

private data class ValidationContext(val checks: MutableList<Check> = ArrayList(),
                                     val pins: MutableList<Pin> = ArrayList(),
                                     val controlledPositions: MutableList<Position> = ArrayList(),
                                     val kingPosition: Position,
                                     val kingColor: Color)

sealed class Check {
    abstract val checkingPiecePosition: Position
    abstract val checkedKingPosition: Position
}

data class BlockableCheck(
        override val checkingPiecePosition: Position,
        override val checkedKingPosition: Position) : Check()

data class UnblockableCheck(
        override val checkingPiecePosition: Position,
        override val checkedKingPosition: Position) : Check()


data class Pin(val pinningPiecePosition: Position, val pinnedPiecePosition: Position)

private typealias ControlledPositions = List<Position>

private fun getControlledPositions(color: Color, state: State): ControlledPositions =
        Position.allPositions
                .mapNotNull { pos ->
                    when (val square = state.squares.getSquareByPosition(pos)) {
                        is Square.Occupied ->
                            if (square.piece.color == color) getControlledPositions(color, pos, state)
                            else null
                        is Square.Empty -> null
                    }
                }
                .flatten()

private fun getControlledPositions(color: Color, position: Position, state: State): List<Position> {
    val square = state.squares[position.rank][position.file] as Square.Occupied
    return when (square.piece.type) {
        Pawn -> getPawnControlledPositions(color, position)
        Bishop -> getControlledBishopPositions(position, state)
        Knight -> getKnightControlledPositions(position)
        Rook -> getRookControlledPositions(position, state)
        Queen -> getQueenControlledPositions(position, state)
        King -> getKingControlledPositions(position)
    }
}

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

private fun Position.isOnLineBetween(a: Position, b: Position): Boolean {
    if (a.file == b.file)
        return file == a.file && rank > min(a.rank, b.rank) && rank < max(a.rank, b.rank)
    if (a.rank == b.rank)
        return rank == a.rank && file > min(a.file, b.file) && file < max(a.file, b.file)
    return (rank - a.rank) * (b.file - a.file) == (b.rank - a.rank) * (file - a.file)
            && rank > min(a.rank, b.rank) && rank < max(a.rank, b.rank)
            && file > min(a.file, b.file) && file < max(a.file, b.file)
}

private fun getPawnControlledPositions(color: Color, position: Position): List<Position> =
        listOfNotNull(
                position.offsetOrNull(color.pawnCaptureRightVector),
                position.offsetOrNull(color.pawnCaptureLeftVector)
        )

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
    val capturingMoves = getPawnControlledPositions(
            color = state.nextMoveColor,
            position = position
    )
            .map { capturePosition ->
                val capture = capturePosition.takeIf { pos ->
                    when (val square = state.squares.getSquareByPosition(pos)) {
                        is Square.Occupied -> square.piece.color != state.nextMoveColor
                        is Square.Empty -> false
                    }
                }?.let { Move.Capture(from = position, to = it) }

                val captureByEnPassant = capturePosition.takeIf { capture == null }
                        ?.takeIf { state.enPassantSquares.containsValue(it) }
                        ?.let { pos ->
                            val capturedColor = state.enPassantSquares.entries.first { (_, p) -> pos == p }.key
                            Move.CaptureByEnPassant(
                                    from = position,
                                    to = pos,
                                    capturedPawnPosition = pos.offset(capturedColor.pawnForwardVector)
                            )
                        }

                return@map listOfNotNull(capture, captureByEnPassant)
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

private tailrec fun getControlledPositionsRec(position: Position,
                                              offsetVector: Vector,
                                              state: State,
                                              controlledPositions: PersistentList<Position>): PersistentList<Position> {
    val newPos = position.offsetOrNull(offsetVector) ?: return controlledPositions
    return when (state.squares.getSquareByPosition(newPos)) {
        is Square.Occupied -> controlledPositions + newPos
        is Square.Empty -> getControlledPositionsRec(
                position = newPos,
                offsetVector = offsetVector,
                state = state,
                controlledPositions = controlledPositions + newPos
        )
    }
}

private fun Position.asCaptureOrMoveToEmptySquareOrNull(state: State, startingPosition: Position): Move? =
        when (val square = state.squares.getSquareByPosition(this)) {
            is Square.Occupied ->
                if (square.piece.color == state.nextMoveColor) null
                else Move.Capture(from = startingPosition, to = this)
            is Square.Empty -> Move.ToEmptySquare(from = startingPosition, to = this)
        }

private fun scan(startingPosition: Position,
                 vector: Vector,
                 state: State,
                 validationContext: ValidationContext) {
    val controlled = getControlledPositionsRec(
            position = startingPosition,
            offsetVector = vector,
            state = state,
            controlledPositions = persistentListOf()
    )
    validationContext.controlledPositions += controlled
    val lastControlledPosition = controlled.last()
    if (lastControlledPosition == validationContext.kingPosition) {
        validationContext.checks += BlockableCheck(
                checkingPiecePosition = startingPosition,
                checkedKingPosition = validationContext.kingPosition
        )
    }
    val lastSquare = state.squares.getSquareByPosition(lastControlledPosition)
    if ((lastSquare as? Square.Occupied)?.piece?.color == validationContext.kingColor) {
        val scannedPositions = getControlledPositionsRec(
                position = lastControlledPosition,
                offsetVector = vector,
                state = state,
                controlledPositions = persistentListOf()
        )
        if (scannedPositions.lastOrNull() == validationContext.kingPosition) {
            validationContext.pins += Pin(
                    pinningPiecePosition = startingPosition,
                    pinnedPiecePosition = lastControlledPosition)
        }
    }
}

private val bishopMoveVectors = listOf(topRightV, topLeftV, bottomLeftV, bottomRightV)

private fun bishopScan(position: Position, state: State, validationContext: ValidationContext) {
    bishopMoveVectors
            .forEach { vector ->
                scan(
                        startingPosition = position,
                        vector = vector,
                        state = state,
                        validationContext = validationContext
                )
            }
}

private fun getControlledBishopPositions(position: Position, state: State): List<Position> =
        bishopMoveVectors
                .map { vector ->
                    getControlledPositionsRec(
                            position = position,
                            offsetVector = vector,
                            state = state,
                            controlledPositions = persistentListOf()
                    )
                }
                .flatten()

private fun getValidBishopMoves(position: Position, state: State): List<Move> =
        getControlledBishopPositions(position, state)
                .mapNotNull { pos -> pos.asCaptureOrMoveToEmptySquareOrNull(state = state, startingPosition = position) }

private val knightMoveVectors = listOf(
        1 to 2,
        2 to 1,
        2 to -1,
        1 to -2,
        -1 to -2,
        -2 to -1,
        -2 to 1,
        -1 to 2)

private fun getKnightControlledPositions(position: Position): List<Position> =
        knightMoveVectors
                .mapNotNull { vector -> position.offsetOrNull(vector) }

private fun getValidKnightMoves(position: Position, state: State): List<Move> =
        getKnightControlledPositions(position)
                .mapNotNull { pos -> pos.asCaptureOrMoveToEmptySquareOrNull(state = state, startingPosition = position) }

private fun getRookControlledPositions(position: Position, state: State): List<Position> =
        listOf(topV, rightV, bottomV, leftV)
                .map { vector ->
                    getControlledPositionsRec(
                            position = position,
                            offsetVector = vector,
                            state = state,
                            controlledPositions = persistentListOf()
                    )
                }
                .flatten()

private fun getValidRookMoves(position: Position, state: State): List<Move> =
        getRookControlledPositions(position, state)
                .mapNotNull { pos -> pos.asCaptureOrMoveToEmptySquareOrNull(state = state, startingPosition = position) }

private fun getQueenControlledPositions(position: Position, state: State): List<Position> =
        listOf(topV, topRightV, rightV, bottomRightV,
                bottomV, bottomLeftV, leftV, topLeftV)
                .map { vector ->
                    getControlledPositionsRec(
                            position = position,
                            offsetVector = vector,
                            state = state,
                            controlledPositions = persistentListOf()
                    )
                }
                .flatten()

private fun getValidQueenMoves(position: Position, state: State): List<Move> =
        getQueenControlledPositions(position, state)
                .mapNotNull { pos -> pos.asCaptureOrMoveToEmptySquareOrNull(state = state, startingPosition = position) }

private fun getKingControlledPositions(position: Position): List<Position> =
        listOf(topV, topRightV, rightV, bottomRightV,
                bottomV, bottomLeftV, leftV, topLeftV)
                .mapNotNull { vector -> position.offsetOrNull(vector) }

private fun getValidKingMoves(position: Position, state: State): List<Move> {
    val color = state.nextMoveColor
    val positionsControlledByOtherColors = Color.values()
            .filter { c -> c != color }
            .map { c -> getControlledPositions(c, state) }
            .flatten()
    val isChecked = positionsControlledByOtherColors.contains(position)
    val kingSideVector = color.kingSideBaseVector
    val castleKingSide = position.offsetOrNull(kingSideVector)
            ?.takeIf { !isChecked }
            ?.takeIf { state.colorToCastlingOptions[color].contains(Castling.KingSide) }
            ?.takeIf { newRookPosition ->
                state.squares.getSquareByPosition(newRookPosition) == Square.Empty
                        && !positionsControlledByOtherColors.contains(newRookPosition)
            }
            ?.offsetOrNull(kingSideVector)
            ?.takeIf { newKingPosition ->
                state.squares.getSquareByPosition(newKingPosition) == Square.Empty
                        && !positionsControlledByOtherColors.contains(newKingPosition)
            }
            ?.let { newPos -> Move.Castling.KingSide(from = position, to = newPos) }
    val queenSideVector = color.queenSideBaseVector
    val castleQueenSide = position.offsetOrNull(queenSideVector)
            ?.takeIf { !isChecked }
            ?.takeIf { state.colorToCastlingOptions[color].contains(Castling.QueenSide) }
            ?.takeIf { newRookPosition ->
                state.squares.getSquareByPosition(newRookPosition) == Square.Empty
                        && !positionsControlledByOtherColors.contains(newRookPosition)
            }
            ?.offsetOrNull(queenSideVector)
            ?.takeIf { newKingPosition ->
                state.squares.getSquareByPosition(newKingPosition) == Square.Empty
                        && !positionsControlledByOtherColors.contains(newKingPosition)
            }
            ?.takeIf { state.squares.getSquareByPosition(position.offset(queenSideVector * 3)) == Square.Empty }
            ?.let { newPos -> Move.Castling.QueenSide(from = position, to = newPos) }
    val basicMoves = getKingControlledPositions(position)
            .mapNotNull { pos -> pos.asCaptureOrMoveToEmptySquareOrNull(state = state, startingPosition = position) }

    return (basicMoves + listOfNotNull(castleKingSide, castleQueenSide))
            .filterNot { move -> positionsControlledByOtherColors.contains(move.to) }
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