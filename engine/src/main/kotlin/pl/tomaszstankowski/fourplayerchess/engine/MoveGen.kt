package pl.tomaszstankowski.fourplayerchess.engine

import pl.tomaszstankowski.fourplayerchess.engine.PieceType.*

data class Move(val from: Position, val to: Position)

data class StateFeatures(val legalMoves: List<Move>, val isCheck: Boolean)

internal fun getStateFeatures(state: State): StateFeatures {
    val pseudoMoves = genPseudoMoves(state)
    val opponentsActivity = checkActivityOfOpponents(state)
    removeIllegalMoves(state, pseudoMoves, opponentsActivity)
    return StateFeatures(
            legalMoves = pseudoMoves,
            isCheck = opponentsActivity.checks.isNotEmpty()
    )
}

private data class OpponentsActivity(val checks: MutableList<Check> = ArrayList(),
                                     val pins: MutableList<Pin> = ArrayList(),
                                     val controlledPositions: MutableSet<Position> = HashSet())

private data class Check(val checkingPiecePosition: Position,
                         val checkedKingPosition: Position)

private data class Pin(val pinningPiecePosition: Position, val pinnedPiecePosition: Position)

private fun checkActivityOfOpponents(state: State): OpponentsActivity {
    val opponentsActivity = OpponentsActivity()
    Position.allPositions
            .forEach { pos ->
                val square = state.squares.byPosition(pos)
                if (square is Square.Occupied
                        && square.piece.color != state.nextMoveColor
                        && !state.eliminatedColors.contains(square.piece.color)) {
                    when (square.piece.type) {
                        Pawn -> checkPawnActivity(pos, state, opponentsActivity)
                        Bishop -> checkBishopActivity(pos, state, opponentsActivity)
                        Knight -> checkKnightActivity(pos, state, opponentsActivity)
                        Rook -> checkRootActivity(pos, state, opponentsActivity)
                        Queen -> checkQueenActivity(pos, state, opponentsActivity)
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
    val kingSquare = Square.Occupied.by(state.nextMoveColor, King)
    pseudoMoves.removeIf { move ->
        val fromSquare = state.squares.byPosition(move.from)
        if (fromSquare == kingSquare) {
            if (opponentsActivity.controlledPositions.contains(move.to)) {
                return@removeIf true
            }
            val isKingSideCastling = move.isKingSideCastling(state)
            val isQueenSideCastling = move.isQueenSideCastling(state)
            if (isKingSideCastling || isQueenSideCastling) {
                if (opponentsActivity.checks.count() > 0) {
                    return@removeIf true
                }
                if (isKingSideCastling) {
                    val newRookPos = move.from.offset(state.nextMoveColor.kingSideVector)
                    if (opponentsActivity.controlledPositions.contains(newRookPos)) {
                        return@removeIf true
                    }
                }
                if (isQueenSideCastling) {
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

private fun toMoveOrNull(position: Position, vector: Vector, state: State): Move? {
    val newPos = position.offsetOrNull(vector) ?: return null
    return when (val square = state.squares.byPosition(newPos)) {
        is Square.Occupied ->
            if (square.piece.color == state.nextMoveColor) null
            else Move(from = position, to = newPos)
        is Square.Empty -> Move(from = position, to = newPos)
    }
}

private fun checkOpponentMove(position: Position,
                              moveVector: Vector,
                              state: State,
                              opponentsActivity: OpponentsActivity) {
    val newPos = position.offsetOrNull(moveVector)
    if (newPos != null) {
        opponentsActivity.controlledPositions += newPos
        val square = state.squares.byPosition(newPos)
        if (square == Square.Occupied.by(state.nextMoveColor, King)) {
            opponentsActivity.checks += Check(
                    checkingPiecePosition = position,
                    checkedKingPosition = newPos
            )
        }
    }
}

private fun checkPawnActivity(position: Position, state: State, opponentsActivity: OpponentsActivity) {
    val pawnColor = (state.squares.byPosition(position) as Square.Occupied).piece.color
    pawnColor.pawnCapturingVectors
            .forEach { vector ->
                checkOpponentMove(position, vector, state, opponentsActivity)
            }
}

private fun genPawnPseudoMoves(position: Position, state: State, moves: MutableList<Move>) {
    position.offsetOrNull(state.nextMoveColor.pawnForwardVector)
            ?.takeIf { pos -> state.squares.byPosition(pos) == Square.Empty }
            ?.let { Move(from = position, to = it) }
            ?.let { moves += it }
    position.takeIf { position.isPawnStartingPositionForColor(state.nextMoveColor) }
            ?.offsetOrNull(state.nextMoveColor.pawnForwardVector)
            ?.takeIf { pos -> state.squares.byPosition(pos) == Square.Empty }
            ?.offsetOrNull(state.nextMoveColor.pawnForwardVector)
            ?.takeIf { pos -> state.squares.byPosition(pos) == Square.Empty }
            ?.let { newPos -> Move(from = position, to = newPos) }
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
                        ?.let { Move(from = position, to = it) }
                        ?.let { moves += it }

                capturePos.takeIf { state.enPassantSquares.containsValue(it) }
                        ?.let { pos -> Move(from = position, to = pos) }
                        ?.let { moves += it }
            }
}

private fun scan(startingPosition: Position,
                 vector: Vector,
                 state: State,
                 opponentsActivity: OpponentsActivity) {
    val lastPositionInSight = traverseSquares(
            startingPosition = startingPosition,
            vector = vector,
            forEachDo = { pos -> opponentsActivity.controlledPositions += pos },
            stopPredicate = { pos -> state.squares.byPosition(pos) is Square.Occupied }
    )
    if (lastPositionInSight == startingPosition) {
        return
    }
    val lastSquareInSight = state.squares.byPosition(lastPositionInSight)
    if (lastSquareInSight == Square.Occupied.by(state.nextMoveColor, King)) {
        opponentsActivity.checks += Check(
                checkingPiecePosition = startingPosition,
                checkedKingPosition = lastPositionInSight
        )
        // squares behind the king are controlled as well - the king can't move there
        traverseSquares(
                startingPosition = lastPositionInSight,
                vector = vector,
                forEachDo = { pos -> opponentsActivity.controlledPositions += pos },
                stopPredicate = { pos -> state.squares.byPosition(pos) is Square.Occupied }
        )
    } else if ((lastSquareInSight as? Square.Occupied)?.piece?.color == state.nextMoveColor) {
        val lastXrayedPosition = traverseSquares(
                startingPosition = lastPositionInSight,
                vector = vector,
                stopPredicate = { pos -> state.squares.byPosition(pos) is Square.Occupied }
        )
        val lastXrayedSquare = state.squares.byPosition(lastXrayedPosition)
        if (lastXrayedSquare == Square.Occupied.by(state.nextMoveColor, King)) {
            opponentsActivity.pins += Pin(
                    pinningPiecePosition = startingPosition,
                    pinnedPiecePosition = lastPositionInSight
            )
        }
    }
}

private inline fun traverseSquares(
        startingPosition: Position,
        vector: Vector,
        forEachDo: (Position) -> Unit = {},
        stopPredicate: (Position) -> Boolean): Position {
    var lastPosition = startingPosition
    while (true) {
        val newPos = lastPosition.offsetOrNull(vector) ?: break
        lastPosition = newPos
        forEachDo(newPos)
        if (stopPredicate(newPos))
            break
    }
    return lastPosition
}

private fun checkBishopActivity(position: Position, state: State, opponentsActivity: OpponentsActivity) {
    bishopMoveVectors
            .forEach { vector ->
                scan(position, vector, state, opponentsActivity)
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
                    moves += Move(from = position, to = newPos)
                }
            }
            is Square.Empty -> {
                moves += Move(from = position, to = newPos)
            }
        }
        lastPos = newPos
    }
}

private fun genBishopPseudoMoves(position: Position, state: State, moves: MutableList<Move>) {
    bishopMoveVectors.forEach { vector -> genPseudoMovesByVector(position, vector, state, moves) }
}

private fun checkKnightActivity(position: Position, state: State, opponentsActivity: OpponentsActivity) {
    knightMoveVectors.forEach { vector -> checkOpponentMove(position, vector, state, opponentsActivity) }
}

private fun genKnightPseudoMoves(position: Position, state: State, moves: MutableList<Move>) {
    knightMoveVectors.forEach { vector ->
        toMoveOrNull(position, vector, state)
                ?.let { moves += it }
    }
}

private fun checkRootActivity(position: Position, state: State, opponentsActivity: OpponentsActivity) {
    rookMoveVectors.forEach { vector ->
        scan(position, vector, state, opponentsActivity)
    }
}

private fun genRookPseudoMoves(position: Position, state: State, moves: MutableList<Move>) {
    rookMoveVectors.forEach { vector -> genPseudoMovesByVector(position, vector, state, moves) }
}

private fun checkQueenActivity(position: Position, state: State, opponentsActivity: OpponentsActivity) {
    allDirectionsVectors.forEach { vector ->
        scan(position, vector, state, opponentsActivity)
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
            ?.takeIf { state.castlingOptions[color].contains(Castling.KingSide) }
            ?.takeIf { newRookPosition -> state.squares.byPosition(newRookPosition) == Square.Empty }
            ?.offsetOrNull(kingSideVector)
            ?.takeIf { newKingPosition -> state.squares.byPosition(newKingPosition) == Square.Empty }
            ?.let { newPos -> Move(from = position, to = newPos) }
            ?.let { moves += it }
    val queenSideVector = color.queenSideVector
    position.offsetOrNull(queenSideVector)
            ?.takeIf { state.castlingOptions[color].contains(Castling.QueenSide) }
            ?.takeIf { newRookPosition -> state.squares.byPosition(newRookPosition) == Square.Empty }
            ?.offsetOrNull(queenSideVector)
            ?.takeIf { newKingPosition -> state.squares.byPosition(newKingPosition) == Square.Empty }
            ?.let { newPos -> Move(from = position, to = newPos) }
            ?.let { moves += it }
    allDirectionsVectors
            .forEach { vector ->
                toMoveOrNull(position, vector, state)
                        ?.let { moves += it }
            }
}

