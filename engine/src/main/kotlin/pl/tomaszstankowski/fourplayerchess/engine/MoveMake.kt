package pl.tomaszstankowski.fourplayerchess.engine

import pl.tomaszstankowski.fourplayerchess.engine.Castling.KingSide
import pl.tomaszstankowski.fourplayerchess.engine.Castling.QueenSide
import pl.tomaszstankowski.fourplayerchess.engine.MoveClaim.PromotionMoveClaim
import pl.tomaszstankowski.fourplayerchess.engine.PieceType.*

internal data class MakeMoveOutcome(val state: State, val stateFeatures: StateFeatures, val legalMoves: List<Move>)

internal fun makeMove(moveClaim: MoveClaim, state: State): MakeMoveOutcome {
    val pseudoState = state.copy(
            squares = getNewBoard(moveClaim, state),
            nextMoveColor = getNewNextMoveColor(state),
            enPassantSquares = getNewEnPassantSquares(moveClaim, state),
            castlingOptions = getNewColorToCastlingOptions(moveClaim, state),
            plyCount = getNewPlyCount(moveClaim, state)
    )
    return getLegalStateAndStateFeatures(pseudoState)
}

internal fun makeResignation(color: Color, state: State): MakeMoveOutcome {
    val pseudoState = state.copy(
            eliminatedColors = state.eliminatedColors + color,
            nextMoveColor = if (state.nextMoveColor == color) getNewNextMoveColor(state) else state.nextMoveColor
    )
    return getLegalStateAndStateFeatures(pseudoState)
}

private tailrec fun getLegalStateAndStateFeatures(state: State): MakeMoveOutcome {
    val stateFeatures = getStateFeatures(state)
    val legalMoves = genLegalMoves(state, stateFeatures)
    if (legalMoves.isEmpty()) {
        val isCheck = stateFeatures.checks.getValue(state.nextMoveColor).isNotEmpty()
        val isEliminated = isCheck || state.eliminatedColors.size < 2
        if (isEliminated)
            return getLegalStateAndStateFeatures(
                    state.copy(
                            eliminatedColors = state.eliminatedColors + state.nextMoveColor,
                            enPassantSquares = state.enPassantSquares - state.nextMoveColor,
                            nextMoveColor = getNewNextMoveColor(state)
                    )
            )
    }
    return MakeMoveOutcome(state, stateFeatures, legalMoves)
}

private fun getNewBoard(moveClaim: MoveClaim, state: State): Board {
    val move = moveClaim.move
    val color = state.nextMoveColor
    return state.squares.withPieceMoved(move.from, move.to)
            .let { squares ->
                if (move.isKingSideCastling(state)) {
                    val oldRookPos = color.defaultKingPosition.offset(color.kingSideVector, 3)
                    val newRookPos = color.defaultKingPosition.offset(color.kingSideVector, 1)
                    squares.withPieceMoved(oldRookPos, newRookPos)
                } else {
                    squares
                }
            }
            .let { squares ->
                if (move.isQueenSideCastling(state)) {
                    val oldRookPos = color.defaultKingPosition.offset(color.queenSideVector, 4)
                    val newRookPos = color.defaultKingPosition.offset(color.queenSideVector, 1)
                    squares.withPieceMoved(oldRookPos, newRookPos)
                } else {
                    squares
                }
            }
            .let { squares ->
                if (move.isCaptureByEnPassant(state)) {
                    val capturedPawnColor = state.enPassantSquares
                            .toList()
                            .first { (_, pos) -> pos == move.to }
                            .first
                    val capturedPawnPosition = move.to.offset(capturedPawnColor.pawnForwardVector)
                    squares.replaceSquareOnPosition(capturedPawnPosition, Square.Empty)
                } else {
                    squares
                }
            }
            .let { squares ->
                if (moveClaim is PromotionMoveClaim) {
                    squares.replaceSquareOnPosition(move.to, Square.Occupied.by(color, moveClaim.pieceType))
                } else {
                    squares
                }
            }
}

private fun Board.withPieceMoved(from: Position, to: Position): Board =
        replaceSquareOnPosition(to, byPosition(from))
                .replaceSquareOnPosition(from, Square.Empty)

private fun Board.replaceSquareOnPosition(position: Position, newSquare: Square) =
        replace(position.rank, position.file) { newSquare }

private inline fun <E> List<E>.replace(index: Int, func: (E) -> E): List<E> =
        mapIndexed { i, e ->
            if (i == index) func(e) else e
        }

private inline fun <E> List<List<E>>.replace(i: Int, j: Int, func: (E) -> E): List<List<E>> =
        replace(i) { list ->
            list.replace(j) { e ->
                func(e)
            }
        }

private fun getNewNextMoveColor(state: State): Color {
    return getNewNextMoveColor(state.eliminatedColors, state.nextMoveColor)
}

private tailrec fun getNewNextMoveColor(eliminatedColors: Set<Color>, currentColor: Color): Color {
    val newColorIndex = (currentColor.ordinal + 1) % Color.values().size
    val newColor = Color.values()[newColorIndex]
    if (!eliminatedColors.contains(newColor)) {
        return newColor
    }
    return getNewNextMoveColor(eliminatedColors, currentColor = newColor)
}

private fun getNewEnPassantSquares(moveClaim: MoveClaim, state: State): EnPassantSquares {
    val move = moveClaim.move
    return Color.values().mapNotNull { color ->
        if (state.nextMoveColor == color) {
            val square = state.squares.byPosition(move.from) as Square.Occupied
            if (square.piece.type == Pawn && move.from.offset(color.pawnForwardVector, 2) == move.to) {
                val enPassantPosition = move.from.offset(color.pawnForwardVector)
                color to enPassantPosition
            } else {
                null
            }
        } else {
            state.enPassantSquares[color]
                    ?.takeIf { move.to != it }
                    ?.takeIf {
                        val pawnPosition = it.offset(color.pawnForwardVector)
                        pawnPosition != move.to
                    }
                    ?.let { color to it }
        }
    }
            .toMap()
}

private fun getNewColorToCastlingOptions(moveClaim: MoveClaim, state: State): CastlingOptions {
    val move = moveClaim.move
    return Color.values().map { color ->
        val currentCastlingOptions = state.castlingOptions[color]
        val newCastlingOptions = if (color == state.nextMoveColor) {
            val square = state.squares.byPosition(move.from) as Square.Occupied
            when (square.piece.type) {
                King -> emptySet()
                Rook -> {
                    val defaultKingPosition = color.defaultKingPosition
                    when (move.from) {
                        defaultKingPosition.offset(color.kingSideVector, 3) ->
                            currentCastlingOptions - KingSide
                        defaultKingPosition.offset(color.queenSideVector, 4) ->
                            currentCastlingOptions - QueenSide
                        else ->
                            currentCastlingOptions
                    }
                }
                else -> currentCastlingOptions
            }
        } else {
            currentCastlingOptions
        }
        color to newCastlingOptions
    }
            .toMap()
            .let { CastlingOptions(it) }
}

private val Color.defaultKingPosition: Position
    get() = when (this) {
        Color.Red -> Position.ofFileAndRank(7, 0)
        Color.Blue -> Position.ofFileAndRank(0, 7)
        Color.Yellow -> Position.ofFileAndRank(6, 13)
        Color.Green -> Position.ofFileAndRank(13, 6)
    }

private fun getNewPlyCount(moveClaim: MoveClaim, state: State): PlyCount {
    val move = moveClaim.move
    if (move.isCapture(state) || move.isPawnAdvance(state)) {
        return PlyCount(0)
    }
    return PlyCount(state.plyCount.count + 1)
}