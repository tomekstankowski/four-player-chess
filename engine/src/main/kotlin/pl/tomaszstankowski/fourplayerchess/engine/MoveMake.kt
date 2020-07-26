package pl.tomaszstankowski.fourplayerchess.engine

import pl.tomaszstankowski.fourplayerchess.engine.Castling.KingSide
import pl.tomaszstankowski.fourplayerchess.engine.Castling.QueenSide
import pl.tomaszstankowski.fourplayerchess.engine.MoveClaim.PromotionMoveClaim
import pl.tomaszstankowski.fourplayerchess.engine.PieceType.*

internal data class MakeMoveOutcome(val state: State, val helperState: HelperState, val stateFeatures: StateFeatures, val legalMoves: List<Move>)

internal fun makeMove(moveClaim: MoveClaim, state: State, helperState: HelperState): MakeMoveOutcome {
    val (newSquares, newPieceList) = getNewBoardAndPieceList(moveClaim, state, helperState)
    val pseudoState = state.copy(
            squares = newSquares,
            nextMoveColor = getNewNextMoveColor(state),
            enPassantSquares = getNewEnPassantSquares(moveClaim, state),
            castlingOptions = getNewColorToCastlingOptions(moveClaim, state),
            plyCount = getNewPlyCount(moveClaim, state)
    )
    val newHelperState = helperState.copy(pieceList = newPieceList)
    return getLegalStateAndStateFeatures(pseudoState, newHelperState)
}

internal fun makeResignation(color: Color, state: State, helperState: HelperState): MakeMoveOutcome {
    val pseudoState = state.copy(
            eliminatedColors = state.eliminatedColors + color,
            nextMoveColor = if (state.nextMoveColor == color) getNewNextMoveColor(state) else state.nextMoveColor
    )
    return getLegalStateAndStateFeatures(pseudoState, helperState)
}

private tailrec fun getLegalStateAndStateFeatures(state: State, helperState: HelperState): MakeMoveOutcome {
    val stateFeatures = getStateFeatures(state, helperState)
    val legalMoves = genLegalMoves(state, helperState, stateFeatures)
    if (legalMoves.isEmpty()) {
        val isCheck = stateFeatures.checks.getValue(state.nextMoveColor).isNotEmpty()
        val isEliminated = isCheck || state.eliminatedColors.size < 2
        if (isEliminated)
            return getLegalStateAndStateFeatures(
                    state.copy(
                            eliminatedColors = state.eliminatedColors + state.nextMoveColor,
                            enPassantSquares = state.enPassantSquares - state.nextMoveColor,
                            nextMoveColor = getNewNextMoveColor(state)
                    ),
                    helperState
            )
    }
    return MakeMoveOutcome(state, helperState, stateFeatures, legalMoves)
}

private fun getNewBoardAndPieceList(moveClaim: MoveClaim,
                                    state: State,
                                    helperState: HelperState): Pair<Board, PieceList> {
    val move = moveClaim.move
    val color = state.nextMoveColor
    return (state.squares to helperState.pieceList)
            .let { (squares, pieceList) ->
                val newSquares = squares.withPieceMoved(move.from, move.to)
                val newPieceList = pieceList.withPieceMoved2(move.from, move.to)
                newSquares to newPieceList
            }
            .let { (squares, pieceList) ->
                if (move.isKingSideCastling(state)) {
                    val oldRookPos = color.defaultKingPosition.offset(color.kingSideVector, 3)
                    val newRookPos = color.defaultKingPosition.offset(color.kingSideVector, 1)
                    val newSquares = squares.withPieceMoved(oldRookPos, newRookPos)
                    val newPieceList = pieceList.withPieceMoved2(oldRookPos, newRookPos)
                    newSquares to newPieceList
                } else {
                    squares to pieceList
                }
            }
            .let { (squares, pieceList) ->
                if (move.isQueenSideCastling(state)) {
                    val oldRookPos = color.defaultKingPosition.offset(color.queenSideVector, 4)
                    val newRookPos = color.defaultKingPosition.offset(color.queenSideVector, 1)
                    val newSquares = squares.withPieceMoved(oldRookPos, newRookPos)
                    val newPieceList = pieceList.withPieceMoved2(oldRookPos, newRookPos)
                    newSquares to newPieceList
                } else {
                    squares to pieceList
                }
            }
            .let { (squares, pieceList) ->
                if (move.isCaptureByEnPassant(state)) {
                    val capturedPawnColor = state.enPassantSquares
                            .toList()
                            .first { (_, pos) -> pos == move.to }
                            .first
                    val capturedPawnPosition = move.to.offset(capturedPawnColor.pawnForwardVector)
                    val newSquares = squares.replaceSquareOnPosition(capturedPawnPosition, Square.Empty)
                    val newPieceList = pieceList.withPieceRemoved(at = capturedPawnPosition)
                    newSquares to newPieceList
                } else {
                    squares to pieceList
                }
            }
            .let { (squares, pieceList) ->
                if (moveClaim is PromotionMoveClaim) {
                    val newSquare = Square.Occupied.by(color, moveClaim.pieceType)
                    val newSquares = squares.replaceSquareOnPosition(move.to, newSquare)
                    val newPieceList = pieceList.withPieceReplaced(at = move.to, newPiece = newSquare.piece)
                    newSquares to newPieceList
                } else {
                    squares to pieceList
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

// ends with 2 to avoid name clash
private fun PieceList.withPieceMoved2(from: Position, to: Position): PieceList =
        mapNotNull { (piece, pos) ->
            when (pos) {
                from -> piece to to
                to -> null
                else -> piece to pos
            }
        }

private fun PieceList.withPieceReplaced(at: Position, newPiece: Piece): PieceList =
        map { (piece, pos) ->
            if (pos == at)
                newPiece to pos
            else
                piece to pos
        }

private fun PieceList.withPieceRemoved(at: Position): PieceList =
        filter { (_, pos) -> pos != at }

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
    if (move.isRegularCapture(state) || move.isPawnAdvance(state)) {
        return PlyCount(0)
    }
    return PlyCount(state.plyCount.count + 1)
}