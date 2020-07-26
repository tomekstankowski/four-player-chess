package pl.tomaszstankowski.fourplayerchess.engine

internal fun getStateFeatures(state: State, helperState: HelperState): StateFeatures {
    val attackedPositions = Color.values().map { color -> color to HashSet<Position>() }.toMap()
    val pins = Color.values().map { color -> color to ArrayList<Pin>() }.toMap()
    val checks = Color.values().map { color -> color to ArrayList<Check>() }.toMap()

    helperState.pieceList.forEach { (piece, position) ->
        if (state.eliminatedColors.contains(piece.color)) {
            return@forEach
        }
        val attackedPositionsForColor = attackedPositions.getValue(piece.color)
        findChecks(
                checkingPiecePosition = position,
                checkingPiece = piece,
                state = state,
                forEach = { check ->
                    val checkedKingColor =
                            (state.squares.byPosition(check.checkedKingPosition) as Square.Occupied).piece.color
                    checks.getValue(checkedKingColor) += check
                }
        )
        getAttackedPositions(
                piecePosition = position,
                piece = piece,
                state = state,
                forEach = { pos -> attackedPositionsForColor += pos }
        )
        findPins(
                pinningPiecePosition = position,
                pinningPiece = piece,
                state = state,
                forEach = { pin ->
                    val pinnedPieceColor =
                            (state.squares.byPosition(pin.pinnedPiecePosition) as Square.Occupied).piece.color
                    pins.getValue(pinnedPieceColor) += pin
                }
        )
    }

    return StateFeatures(
            attackedPositions = attackedPositions,
            pins = pins,
            checks = checks
    )
}

private inline fun findChecks(checkingPiecePosition: Position,
                              checkingPiece: Piece,
                              state: State,
                              forEach: (Check) -> Unit) {
    val color = checkingPiece.color
    when (checkingPiece.type) {
        PieceType.Pawn -> findChecks(checkingPiecePosition, color.pawnCapturingVectors, color, state, forEach)
        PieceType.Knight -> findChecks(checkingPiecePosition, knightMoveVectors, color, state, forEach)
        PieceType.Bishop -> findChecksAlongLine(checkingPiecePosition, bishopMoveVectors, color, state, forEach)
        PieceType.Rook -> findChecksAlongLine(checkingPiecePosition, rookMoveVectors, color, state, forEach)
        PieceType.Queen -> findChecksAlongLine(checkingPiecePosition, allDirectionsVectors, color, state, forEach)
        PieceType.King -> {
        }
    }
}

private inline fun findChecks(checkingPiecePosition: Position,
                              attackVectors: List<Vector>,
                              checkingPieceColor: Color,
                              state: State,
                              forEach: (Check) -> Unit) {
    attackVectors.forEach { vector ->
        val attackedPos = checkingPiecePosition.offsetOrNull(vector)
        if (attackedPos != null) {
            getCheckOrNull(
                    piecePosition = checkingPiecePosition,
                    pieceColor = checkingPieceColor,
                    attackedPosition = attackedPos,
                    state = state
            )
                    ?.let { forEach(it) }
        }
    }
}

private inline fun findChecksAlongLine(checkingPiecePosition: Position,
                                       attackUnitVectors: List<Vector>,
                                       checkingPieceColor: Color,
                                       state: State,
                                       forEach: (Check) -> Unit) {
    attackUnitVectors.forEach { vector ->
        val lastPosition = traverseSquares(
                startingPosition = checkingPiecePosition,
                vector = vector,
                isEndReached = { pos -> state.squares.byPosition(pos) is Square.Occupied }
        )
        getCheckOrNull(
                piecePosition = checkingPiecePosition,
                pieceColor = checkingPieceColor,
                attackedPosition = lastPosition,
                state = state
        )
                ?.let { forEach(it) }
    }
}

private fun getCheckOrNull(piecePosition: Position,
                           pieceColor: Color,
                           attackedPosition: Position,
                           state: State): Check? {
    val attackedSquare = state.squares.byPosition(attackedPosition)
    if (attackedSquare is Square.Occupied
            && attackedSquare.piece.type == PieceType.King
            && attackedSquare.piece.color != pieceColor
            && !state.eliminatedColors.contains(attackedSquare.piece.color)) {
        return Check(
                checkingPiecePosition = piecePosition,
                checkedKingPosition = attackedPosition
        )
    }
    return null
}

private inline fun getAttackedPositions(piecePosition: Position,
                                        piece: Piece,
                                        state: State,
                                        forEach: (Position) -> Unit) {
    val color = piece.color
    when (piece.type) {
        PieceType.Pawn -> getAttackedPositions(piecePosition, color.pawnCapturingVectors, forEach)
        PieceType.Knight -> getAttackedPositions(piecePosition, knightMoveVectors, forEach)
        PieceType.Bishop -> getAttackedPositionsAlongLine(piecePosition, bishopMoveVectors, color, state, forEach)
        PieceType.Rook -> getAttackedPositionsAlongLine(piecePosition, rookMoveVectors, color, state, forEach)
        PieceType.Queen -> getAttackedPositionsAlongLine(piecePosition, allDirectionsVectors, color, state, forEach)
        PieceType.King -> getAttackedPositions(piecePosition, allDirectionsVectors, forEach)
    }
}

private inline fun getAttackedPositions(piecePosition: Position,
                                        attackVectors: List<Vector>,
                                        forEach: (Position) -> Unit) {
    attackVectors.forEach { vector ->
        val attackedPosition = piecePosition.offsetOrNull(vector)
        if (attackedPosition != null) {
            forEach(attackedPosition)
        }
    }
}


private inline fun getAttackedPositionsAlongLine(piecePosition: Position,
                                                 attackingUnitVectors: List<Vector>,
                                                 attackingColor: Color,
                                                 state: State,
                                                 forEach: (Position) -> Unit) {
    attackingUnitVectors.forEach { vector ->
        traverseSquares(
                startingPosition = piecePosition,
                vector = vector,
                forEachDo = forEach,
                isEndReached = { pos ->
                    when (val square = state.squares.byPosition(pos)) {
                        is Square.Empty -> false
                        is Square.Occupied -> square.piece.type != PieceType.King || square.piece.color == attackingColor
                    }
                }
        )
    }

}

private inline fun findPins(pinningPiecePosition: Position, pinningPiece: Piece, state: State, forEach: (Pin) -> Unit) {
    when (pinningPiece.type) {
        PieceType.Bishop -> findPins(pinningPiecePosition, bishopMoveVectors, pinningPiece.color, state, forEach)
        PieceType.Rook -> findPins(pinningPiecePosition, rookMoveVectors, pinningPiece.color, state, forEach)
        PieceType.Queen -> findPins(pinningPiecePosition, allDirectionsVectors, pinningPiece.color, state, forEach)
        else -> {
        }
    }
}

private inline fun findPins(pinningPiecePosition: Position,
                            attackUnitVectors: List<Vector>,
                            pinningPieceColor: Color,
                            state: State,
                            forEach: (Pin) -> Unit) {
    attackUnitVectors.forEach { vector ->
        val lastAttackedPos = traverseSquares(
                startingPosition = pinningPiecePosition,
                vector = vector,
                isEndReached = { pos -> state.squares.byPosition(pos) is Square.Occupied }
        )
        val lastAttackedSquare = state.squares.byPosition(lastAttackedPos)
        if (lastAttackedSquare is Square.Occupied
                && lastAttackedSquare.piece.color != pinningPieceColor
                && lastAttackedSquare.piece.type != PieceType.King
                && !state.eliminatedColors.contains(lastAttackedSquare.piece.color)) {
            val lastScannedPosition = traverseSquares(
                    startingPosition = lastAttackedPos,
                    vector = vector,
                    isEndReached = { pos -> state.squares.byPosition(pos) is Square.Occupied }
            )
            val lastScannedSquare = state.squares.byPosition(lastScannedPosition)
            if (lastScannedSquare is Square.Occupied
                    && lastScannedSquare.piece.type == PieceType.King
                    && lastScannedSquare.piece.color == lastAttackedSquare.piece.color) {
                val pin = Pin(
                        pinningPiecePosition = pinningPiecePosition,
                        pinnedPiecePosition = lastAttackedPos
                )
                forEach(pin)
            }
        }
    }
}

private inline fun traverseSquares(
        startingPosition: Position,
        vector: Vector,
        forEachDo: (Position) -> Unit = {},
        isEndReached: (Position) -> Boolean): Position {
    var lastPosition = startingPosition
    while (true) {
        val newPos = lastPosition.offsetOrNull(vector) ?: break
        lastPosition = newPos
        forEachDo(newPos)
        if (isEndReached(newPos))
            break
    }
    return lastPosition
}