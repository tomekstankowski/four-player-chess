package pl.tomaszstankowski.fourplayerchess

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

fun getLegalMoves(state: State): List<Move> {
    val pseudoMoves = genPseudoMoves(state)
    val opponentsActivity = checkActivityOfOpponents(state)
    removeIllegalMoves(state, pseudoMoves, opponentsActivity)
    return pseudoMoves
}

private data class OpponentsActivity(val checks: MutableList<Check> = ArrayList(),
                                     val pins: MutableList<Pin> = ArrayList(),
                                     val controlledPositions: MutableSet<Position> = HashSet())

private data class Check(val checkingPiecePosition: Position,
                         val checkedKingPosition: Position)

private data class Pin(val pinningPiecePosition: Position, val pinnedPiecePosition: Position)

private fun checkActivityOfOpponents(state: State): OpponentsActivity {
    val opponentsActivity = OpponentsActivity()
    val kingPosition = Position.allPositions.first { pos ->
        val square = state.squares.byPosition(pos)
        square is Square.Occupied && square.piece.color == state.nextMoveColor && square.piece.type == King
    }
    Position.allPositions
            .forEach { pos ->
                val square = state.squares.byPosition(pos)
                if (square is Square.Occupied && square.piece.color != state.nextMoveColor) {
                    when (square.piece.type) {
                        Pawn -> checkPawnActivity(square.piece.color, pos, kingPosition, opponentsActivity)
                        Bishop -> checkBishopActivity(pos, state, kingPosition, opponentsActivity)
                        Knight -> checkKnightActivity(pos, kingPosition, opponentsActivity)
                        Rook -> checkRootActivity(pos, state, kingPosition, opponentsActivity)
                        Queen -> checkQueenActivity(pos, state, kingPosition, opponentsActivity)
                        King -> checkKingActivity(pos, opponentsActivity)
                    }
                }
            }
    return opponentsActivity
}

private fun genPseudoMoves(state: State): MutableList<Move> {
    val moves = ArrayList<Move>()
    Position.allPositions
            .forEach { pos ->
                when (val square = state.squares.byPosition(pos)) {
                    is Square.Occupied ->
                        if (square.piece.color == state.nextMoveColor) {
                            when (square.piece.type) {
                                Pawn -> genPawnPseudoMoves(pos, state, moves)
                                Bishop -> genBishopPseudoMoves(pos, state, moves)
                                Knight -> genKnightPseudoMoves(pos, state, moves)
                                Rook -> genRookPseudoMoves(pos, state, moves)
                                Queen -> genQueenPseudoMoves(pos, state, moves)
                                King -> genKingPseudoMoves(pos, state, moves)
                            }
                        }
                }
            }
    return moves
}

private fun removeIllegalMoves(state: State, pseudoMoves: MutableList<Move>, opponentsActivity: OpponentsActivity) {
    pseudoMoves.removeIf { move ->
        val movingPieceType = (state.squares.byPosition(move.from) as Square.Occupied).piece.type
        if (movingPieceType == King) {
            if (opponentsActivity.controlledPositions.contains(move.to)) {
                return@removeIf true
            }
            if (move is Move.Castling) {
                if (opponentsActivity.checks.count() > 0) {
                    return@removeIf true
                }
                if (move is Move.Castling.KingSide) {
                    val newRookPos = move.from.offset(state.nextMoveColor.kingSideVector)
                    if (opponentsActivity.controlledPositions.contains(newRookPos)) {
                        return@removeIf true
                    }
                }
                if (move is Move.Castling.QueenSide) {
                    val newRookPos = move.from.offset(state.nextMoveColor.queenSideVector)
                    if (opponentsActivity.controlledPositions.contains(newRookPos)) {
                        return@removeIf true
                    }
                }
            }
        }
        return@removeIf false
    }
    if (opponentsActivity.pins.isNotEmpty()) {
        pseudoMoves.removeIf { move ->
            opponentsActivity.pins.any { pin ->
                val pinnedPos = pin.pinnedPiecePosition
                val pinningPos = pin.pinningPiecePosition
                pinnedPos == move.from && !move.to.isOnLineBetween(pinnedPos, pinningPos)
                        && move.to != pinningPos
            }
        }
    }
    if (opponentsActivity.checks.isNotEmpty()) {
        pseudoMoves.removeIf { move ->
            val movingPieceType = (state.squares.byPosition(move.from) as Square.Occupied).piece.type
            if (movingPieceType == King) {
                false
            } else {
                if (opponentsActivity.checks.count() > 1) {
                    true
                } else {
                    val check = opponentsActivity.checks.first()
                    val checkingPos = check.checkingPiecePosition
                    val checkedPos = check.checkedKingPosition
                    val isPossibleToBlock = when {
                        (state.squares.byPosition(checkingPos) as Square.Occupied).piece.type == Knight -> false
                        checkingPos.isAdjacentTo(checkedPos) -> false
                        else -> true
                    }
                    val isBlockingMove = isPossibleToBlock && move.to.isOnLineBetween(checkedPos, checkingPos)
                    val isCapturingCheckingPieceMove = move.to == checkingPos
                    !isBlockingMove && !isCapturingCheckingPieceMove
                }
            }
        }
    }
}

private fun Position.isAdjacentTo(other: Position) =
        allDirectionsVectors.any { vector -> this.offsetOrNull(vector) == other }

private fun Position.isOnLineBetween(a: Position, b: Position): Boolean {
    if (a.file == b.file)
        return file == a.file && rank > min(a.rank, b.rank) && rank < max(a.rank, b.rank)
    if (a.rank == b.rank)
        return rank == a.rank && file > min(a.file, b.file) && file < max(a.file, b.file)
    return (rank - a.rank) * (b.file - a.file) == (b.rank - a.rank) * (file - a.file)
            && rank > min(a.rank, b.rank) && rank < max(a.rank, b.rank)
            && file > min(a.file, b.file) && file < max(a.file, b.file)
}

private fun Position.isPawnStartingPositionForColor(color: Color) =
        when (color) {
            Red -> rank == 1
            Green -> rank == BOARD_SIZE - 2
            Blue -> file == 1
            Yellow -> file == BOARD_SIZE - 2
        }

private fun Position.asCaptureOrMoveToEmptySquareOrNull(state: State, startingPosition: Position): Move? =
        when (val square = state.squares.byPosition(this)) {
            is Square.Occupied ->
                if (square.piece.color == state.nextMoveColor) null
                else Move.Capture(from = startingPosition, to = this)
            is Square.Empty -> Move.ToEmptySquare(from = startingPosition, to = this)
        }

private fun checkOpponentMove(position: Position,
                              moveVector: Vector,
                              kingPosition: Position,
                              opponentsActivity: OpponentsActivity) {
    val newPos = position.offsetOrNull(moveVector)
    if (newPos != null) {
        opponentsActivity.controlledPositions += newPos
        if (newPos == kingPosition) {
            opponentsActivity.checks += Check(
                    checkingPiecePosition = position,
                    checkedKingPosition = kingPosition
            )
        }
    }
}

private fun checkPawnActivity(color: Color, position: Position, kingPosition: Position, opponentsActivity: OpponentsActivity) {
    color.pawnCapturingVectors
            .forEach { vector ->
                checkOpponentMove(position, vector, kingPosition, opponentsActivity)
            }
}

private fun genPawnPseudoMoves(position: Position, state: State, moves: MutableList<Move>) {
    position.offsetOrNull(state.nextMoveColor.pawnForwardVector)
            ?.takeIf { pos -> state.squares.byPosition(pos) == Square.Empty }
            ?.let { Move.ToEmptySquare(from = position, to = it) }
            ?.let { moves += it }
    position.takeIf { position.isPawnStartingPositionForColor(state.nextMoveColor) }
            ?.offsetOrNull(state.nextMoveColor.pawnForwardVector)
            ?.takeIf { pos -> state.squares.byPosition(pos) == Square.Empty }
            ?.offsetOrNull(state.nextMoveColor.pawnForwardVector)
            ?.takeIf { pos -> state.squares.byPosition(pos) == Square.Empty }
            ?.let { newPos -> Move.TwoSquaresForwardPawnMove(from = position, to = newPos) }
            ?.let { moves += it }
    state.nextMoveColor.pawnCapturingVectors
            .forEach { vector ->
                val capturePos = position.offsetOrNull(vector) ?: return@forEach
                capturePos.takeIf { pos ->
                    when (val square = state.squares.byPosition(pos)) {
                        is Square.Occupied -> square.piece.color != state.nextMoveColor
                        is Square.Empty -> false
                    }
                }
                        ?.let { Move.Capture(from = position, to = it) }
                        ?.let { moves += it }

                capturePos.takeIf { state.enPassantSquares.containsValue(it) }
                        ?.let { pos ->
                            val capturedColor = state.enPassantSquares.entries.first { (_, p) -> pos == p }.key
                            Move.CaptureByEnPassant(
                                    from = position,
                                    to = pos,
                                    capturedPawnPosition = pos.offset(capturedColor.pawnForwardVector)
                            )
                        }
                        ?.let { moves += it }
            }
}

private typealias Vector = Pair<Int, Int>

private val topV = 0 to 1
private val topRightV = 1 to 1
private val topLeftV = -1 to 1
private val rightV = 1 to 0
private val leftV = -1 to 0
private val bottomV = 0 to -1
private val bottomRightV = 1 to -1
private val bottomLeftV = -1 to -1

private val allDirectionsVectors = listOf(
        topV,
        topRightV,
        rightV,
        bottomRightV,
        bottomV,
        bottomLeftV,
        leftV,
        topLeftV
)

private val Color.pawnForwardVector: Vector
    get() = when (this) {
        Red -> topV
        Green -> leftV
        Blue -> rightV
        Yellow -> bottomV
    }

private val redPawnCapturingVectors = listOf(topLeftV, topRightV)
private val greenPawnCapturingVectors = listOf(bottomLeftV, topLeftV)
private val bluePawnCapturingVectors = listOf(topRightV, bottomRightV)
private val yellowPawnCapturingVectors = listOf(bottomRightV, bottomLeftV)

private val Color.pawnCapturingVectors: List<Vector>
    get() = when (this) {
        Red -> redPawnCapturingVectors
        Green -> greenPawnCapturingVectors
        Blue -> bluePawnCapturingVectors
        Yellow -> yellowPawnCapturingVectors
    }

private val bishopMoveVectors = listOf(topRightV, topLeftV, bottomLeftV, bottomRightV)

private val knightMoveVectors = listOf(
        1 to 2,
        2 to 1,
        2 to -1,
        1 to -2,
        -1 to -2,
        -2 to -1,
        -2 to 1,
        -1 to 2)

private val rookMoveVectors = listOf(topV, rightV, bottomV, leftV)

private val Color.kingSideVector: Vector
    get() = when (this) {
        Red -> rightV
        Green -> bottomV
        Blue -> topV
        Yellow -> leftV
    }

private val Color.queenSideVector: Vector
    get() = when (this) {
        Red -> leftV
        Green -> topV
        Blue -> bottomV
        Yellow -> rightV
    }

private fun scan(startingPosition: Position,
                 vector: Vector,
                 kingPosition: Position,
                 state: State,
                 opponentsActivity: OpponentsActivity) {
    var lastPos = startingPosition
    var endReached = false
    while (!endReached) {
        val newPos = lastPos.offsetOrNull(vector) ?: break
        lastPos = newPos
        opponentsActivity.controlledPositions += newPos
        endReached = when (state.squares.byPosition(newPos)) {
            is Square.Occupied -> true
            is Square.Empty -> false
        }
    }
    val lastControlledPosition = lastPos
    if (lastControlledPosition == startingPosition) {
        return
    }
    if (lastControlledPosition == kingPosition) {
        opponentsActivity.checks += Check(
                checkingPiecePosition = startingPosition,
                checkedKingPosition = kingPosition
        )
        return
    }
    val lastSquare = state.squares.byPosition(lastControlledPosition)
    if ((lastSquare as? Square.Occupied)?.piece?.color == state.nextMoveColor) {
        while (true) {
            val newPos = lastPos.offsetOrNull(vector) ?: return
            lastPos = newPos
            val square = state.squares.byPosition(newPos)
            if (square is Square.Occupied) {
                if (newPos == kingPosition) {
                    opponentsActivity.pins += Pin(
                            pinningPiecePosition = startingPosition,
                            pinnedPiecePosition = lastControlledPosition)
                }
                return
            }
        }
    }
}

private fun checkBishopActivity(position: Position, state: State, kingPosition: Position, opponentsActivity: OpponentsActivity) {
    bishopMoveVectors
            .forEach { vector ->
                scan(position, vector, kingPosition, state, opponentsActivity)
            }
}

private fun genPseudoMovesByVector(position: Position, vector: Vector, state: State, moves: MutableList<Move>) {
    var lastPos = position
    var endReached = false
    while (!endReached) {
        val newPos = lastPos.offsetOrNull(vector) ?: break
        when (val square = state.squares.byPosition(newPos)) {
            is Square.Occupied -> {
                endReached = true
                if (square.piece.color != state.nextMoveColor) {
                    moves += Move.Capture(from = position, to = newPos)
                }
            }
            is Square.Empty -> {
                moves += Move.ToEmptySquare(from = position, to = newPos)
            }
        }
        lastPos = newPos
    }
}

private fun genBishopPseudoMoves(position: Position, state: State, moves: MutableList<Move>) {
    bishopMoveVectors.forEach { vector -> genPseudoMovesByVector(position, vector, state, moves) }
}

private fun checkKnightActivity(position: Position, kingPosition: Position, opponentsActivity: OpponentsActivity) {
    knightMoveVectors.forEach { vector -> checkOpponentMove(position, vector, kingPosition, opponentsActivity) }
}

private fun genKnightPseudoMoves(position: Position, state: State, moves: MutableList<Move>) {
    knightMoveVectors.forEach { vector ->
        val newPos = position.offsetOrNull(vector)
        if (newPos != null) {
            newPos.asCaptureOrMoveToEmptySquareOrNull(state, position)
                    ?.let { moves += it }
        }
    }
}


private fun checkRootActivity(position: Position,
                              state: State,
                              kingPosition: Position,
                              opponentsActivity: OpponentsActivity) {
    rookMoveVectors.forEach { vector ->
        scan(position, vector, kingPosition, state, opponentsActivity)
    }
}

private fun genRookPseudoMoves(position: Position, state: State, moves: MutableList<Move>) {
    rookMoveVectors.forEach { vector -> genPseudoMovesByVector(position, vector, state, moves) }
}

private fun checkQueenActivity(position: Position, state: State, kingPosition: Position,
                               opponentsActivity: OpponentsActivity) {
    allDirectionsVectors.forEach { vector ->
        scan(position, vector, kingPosition, state, opponentsActivity)
    }
}

private fun genQueenPseudoMoves(position: Position, state: State, moves: MutableList<Move>) {
    allDirectionsVectors.forEach { vector ->
        genPseudoMovesByVector(position, vector, state, moves)
    }
}

private fun checkKingActivity(position: Position, opponentsActivity: OpponentsActivity) {
    allDirectionsVectors.forEach { vector ->
        val newPos = position.offsetOrNull(vector)
        if (newPos != null) {
            opponentsActivity.controlledPositions += newPos
        }
    }
}

private fun genKingPseudoMoves(position: Position, state: State, moves: MutableList<Move>) {
    val color = state.nextMoveColor
    val kingSideVector = color.kingSideVector
    position.offsetOrNull(kingSideVector)
            ?.takeIf { state.colorToCastlingOptions[color].contains(Castling.KingSide) }
            ?.takeIf { newRookPosition -> state.squares.byPosition(newRookPosition) == Square.Empty }
            ?.offsetOrNull(kingSideVector)
            ?.takeIf { newKingPosition -> state.squares.byPosition(newKingPosition) == Square.Empty }
            ?.let { newPos -> Move.Castling.KingSide(from = position, to = newPos) }
            ?.let { moves += it }
    val queenSideVector = color.queenSideVector
    position.offsetOrNull(queenSideVector)
            ?.takeIf { state.colorToCastlingOptions[color].contains(Castling.QueenSide) }
            ?.takeIf { newRookPosition -> state.squares.byPosition(newRookPosition) == Square.Empty }
            ?.offsetOrNull(queenSideVector)
            ?.takeIf { newKingPosition -> state.squares.byPosition(newKingPosition) == Square.Empty }
            ?.let { newPos -> Move.Castling.QueenSide(from = position, to = newPos) }
            ?.let { moves += it }
    allDirectionsVectors
            .forEach { vector ->
                val newPos = position.offsetOrNull(vector)
                if (newPos != null) {
                    newPos.asCaptureOrMoveToEmptySquareOrNull(state = state, startingPosition = position)
                            ?.let { moves += it }
                }
            }
}

