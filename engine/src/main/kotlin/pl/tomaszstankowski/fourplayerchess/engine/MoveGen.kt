package pl.tomaszstankowski.fourplayerchess.engine

import pl.tomaszstankowski.fourplayerchess.engine.PieceType.*

internal fun genLegalMoves(state: State, stateFeatures: StateFeatures): List<Move> {
    val moves = ArrayList<Move>()
    val addMoveIfLegal = { move: Move ->
        val isLegalMove = isLegalMove(move, state, stateFeatures)
        if (isLegalMove) {
            moves += move
        }
    }
    Position.allPositions.forEach { pos ->
        val square = state.squares.byPosition(pos)
        if (square is Square.Occupied) {
            val piece = square.piece
            if (piece.color == state.nextMoveColor) {
                when (square.piece.type) {
                    Pawn -> genPawnPseudoMoves(pos, state, forEach = addMoveIfLegal)
                    Knight -> genKnightPseudoMoves(pos, state, forEach = addMoveIfLegal)
                    Bishop -> genBishopPseudoMoves(pos, state, forEach = addMoveIfLegal)
                    Rook -> genRookPseudoMoves(pos, state, forEach = addMoveIfLegal)
                    Queen -> genQueenPseudoMoves(pos, state, forEach = addMoveIfLegal)
                    King -> genKingPseudoMoves(pos, state, forEach = addMoveIfLegal)
                }
            }
        }
    }
    return moves
}

private fun isLegalMove(pseudoMove: Move, state: State, stateFeatures: StateFeatures): Boolean {
    val color = state.nextMoveColor
    val fromSquare = state.squares.byPosition(pseudoMove.from) as Square.Occupied

    if (fromSquare == Square.Occupied.by(color, King)) {
        val attackedPositions = stateFeatures.attackedPositions
                .filter { (c, _) -> c != color && !state.eliminatedColors.contains(c) }
                .values
        val isDestinationAttacked = attackedPositions
                .any { positions -> positions.contains(pseudoMove.to) }
        if (isDestinationAttacked) {
            return false
        }
        val isKingSideCastling = pseudoMove.isKingSideCastling(state)
        val isQueenSideCastling = pseudoMove.isQueenSideCastling(state)
        if (isKingSideCastling || isQueenSideCastling) {
            val isChecked = stateFeatures.checks.getValue(color).isNotEmpty()
            if (isChecked) {
                return false
            }
            if (isKingSideCastling) {
                val newRookPos = pseudoMove.from.offset(color.kingSideVector)
                val isNewRookPosAttacked = attackedPositions
                        .any { positions -> positions.contains(newRookPos) }
                if (isNewRookPosAttacked) {
                    return false
                }
            }
            if (isQueenSideCastling) {
                val newRookPos = pseudoMove.from.offset(color.queenSideVector)
                val isNewRookPosAttacked = attackedPositions
                        .any { positions -> positions.contains(newRookPos) }
                if (isNewRookPosAttacked) {
                    return false
                }
            }
        }
        return true
    }

    val pins = stateFeatures.pins.getValue(color)
    val isMovePlacingKingInCheck = pins.any { pin ->
        pseudoMove.from == pin.pinnedPiecePosition
                && !pseudoMove.to.isOnLineBetween(pin.pinnedPiecePosition, pin.pinningPiecePosition)
                && pseudoMove.to != pin.pinningPiecePosition
    }
    if (isMovePlacingKingInCheck) {
        return false
    }

    val checks = stateFeatures.checks.getValue(color)
    if (checks.count() > 1) {
        return false
    }
    if (checks.isNotEmpty()) {
        val check = checks[0]
        val checkingPos = check.checkingPiecePosition
        val checkedPos = check.checkedKingPosition
        val isPossibleToBlock = when {
            (state.squares.byPosition(checkingPos) as Square.Occupied).piece.type == Knight -> false
            checkingPos.isAdjacentTo(checkedPos) -> false
            else -> true
        }
        val isBlockingMove = isPossibleToBlock && pseudoMove.to.isOnLineBetween(checkedPos, checkingPos)
        val isCapturingCheckingPieceMove = pseudoMove.to == checkingPos
        if (!isCapturingCheckingPieceMove && !isBlockingMove) {
            return false
        }
    }

    val toSquare = state.squares.byPosition(pseudoMove.to)
    val isOccupiedByNotEliminatedKing = toSquare is Square.Occupied
            && toSquare.piece.type == King
            && !state.eliminatedColors.contains(toSquare.piece.color)
    // King must not be captured via discovered attack, each player should be allowed to respond to check
    if (isOccupiedByNotEliminatedKing) {
        return false
    }

    return true
}


private inline fun genPawnPseudoMoves(position: Position, state: State, forEach: (Move) -> Unit) {
    position.offsetOrNull(state.nextMoveColor.pawnForwardVector)
            ?.takeIf { pos -> state.squares.byPosition(pos) == Square.Empty }
            ?.let { Move(from = position, to = it) }
            ?.let { forEach(it) }
    position.takeIf { position.isPawnStartingPositionForColor(state.nextMoveColor) }
            ?.offsetOrNull(state.nextMoveColor.pawnForwardVector)
            ?.takeIf { pos -> state.squares.byPosition(pos) == Square.Empty }
            ?.offsetOrNull(state.nextMoveColor.pawnForwardVector)
            ?.takeIf { pos -> state.squares.byPosition(pos) == Square.Empty }
            ?.let { newPos -> Move(from = position, to = newPos) }
            ?.let { forEach(it) }
    state.nextMoveColor.pawnCapturingVectors
            .forEach { vector ->
                val capturePos = position.offsetOrNull(vector) ?: return@forEach
                capturePos.takeIf { pos ->
                    when (val square = state.squares.byPosition(pos)) {
                        is Square.Occupied -> square.piece.color != state.nextMoveColor
                        is Square.Empty -> false
                    }
                }
                        ?.let { Move(from = position, to = it) }
                        ?.let { forEach(it) }

                capturePos.takeIf {
                    state.enPassantSquares.containsValue(it)
                            && state.enPassantSquares[state.nextMoveColor] != it
                }
                        ?.let { pos -> Move(from = position, to = pos) }
                        ?.let { forEach(it) }
            }
}


private inline fun genPseudoMovesByVector(position: Position, vector: Vector, state: State, forEach: (Move) -> Unit) {
    var lastPos = position
    var endReached = false
    while (!endReached) {
        val newPos = lastPos.offsetOrNull(vector) ?: break
        when (val square = state.squares.byPosition(newPos)) {
            is Square.Occupied -> {
                endReached = true
                if (square.piece.color != state.nextMoveColor) {
                    val move = Move(from = position, to = newPos)
                    forEach(move)
                }
            }
            is Square.Empty -> {
                val move = Move(from = position, to = newPos)
                forEach(move)
            }
        }
        lastPos = newPos
    }
}

private inline fun genBishopPseudoMoves(position: Position, state: State, forEach: (Move) -> Unit) {
    bishopMoveVectors.forEach { vector -> genPseudoMovesByVector(position, vector, state, forEach) }
}

private inline fun genKnightPseudoMoves(position: Position, state: State, forEach: (Move) -> Unit) {
    knightMoveVectors.forEach { vector ->
        toMoveOrNull(position, vector, state)
                ?.let { forEach(it) }
    }
}

private inline fun genRookPseudoMoves(position: Position, state: State, forEach: (Move) -> Unit) {
    rookMoveVectors.forEach { vector -> genPseudoMovesByVector(position, vector, state, forEach) }
}

private inline fun genQueenPseudoMoves(position: Position, state: State, forEach: (Move) -> Unit) {
    allDirectionsVectors.forEach { vector ->
        genPseudoMovesByVector(position, vector, state, forEach)
    }
}

private inline fun genKingPseudoMoves(position: Position, state: State, forEach: (Move) -> Unit) {
    val color = state.nextMoveColor
    val kingSideVector = color.kingSideVector
    position.offsetOrNull(kingSideVector)
            ?.takeIf { state.castlingOptions[color].contains(Castling.KingSide) }
            ?.takeIf { newRookPosition -> state.squares.byPosition(newRookPosition) == Square.Empty }
            ?.offsetOrNull(kingSideVector)
            ?.takeIf { newKingPosition -> state.squares.byPosition(newKingPosition) == Square.Empty }
            ?.let { newPos -> Move(from = position, to = newPos) }
            ?.let { forEach(it) }
    val queenSideVector = color.queenSideVector
    position.offsetOrNull(queenSideVector)
            ?.takeIf { state.castlingOptions[color].contains(Castling.QueenSide) }
            ?.takeIf { newRookPosition -> state.squares.byPosition(newRookPosition) == Square.Empty }
            ?.offsetOrNull(queenSideVector)
            ?.takeIf { newKingPosition -> state.squares.byPosition(newKingPosition) == Square.Empty }
            ?.let { newPos -> Move(from = position, to = newPos) }
            ?.let { forEach(it) }
    allDirectionsVectors
            .forEach { vector ->
                toMoveOrNull(position, vector, state)
                        ?.let { forEach(it) }
            }
}

private fun toMoveOrNull(position: Position, vector: Vector, state: State): Move? {
    val newPos = position.offsetOrNull(vector) ?: return null
    return when (val square = state.squares.byPosition(newPos)) {
        is Square.Occupied ->
            if (square.piece.color == state.nextMoveColor) null
            else Move(from = position, to = newPos)
        is Square.Empty -> Move(from = position, to = newPos)
    }
}