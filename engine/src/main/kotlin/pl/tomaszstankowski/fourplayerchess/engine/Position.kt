package pl.tomaszstankowski.fourplayerchess.engine

import pl.tomaszstankowski.fourplayerchess.engine.Castling.KingSide
import pl.tomaszstankowski.fourplayerchess.engine.Castling.QueenSide
import pl.tomaszstankowski.fourplayerchess.engine.PieceType.*
import java.util.*
import kotlin.collections.HashSet
import kotlin.math.min
import kotlin.random.Random

internal class Position(fenState: FenState) {
    private val previousStates: LinkedList<State> = LinkedList()
    private var state: State
    private val board: Board
    private val pieceLists: Array<Array<PieceList>> = Array(Color.values().size) {
        Array(PieceType.values().size) {
            LinkedList<Coordinates>()
        }
    }
    private val attackedSquares: Array<MutableSet<Coordinates>> = Array(Color.values().size) {
        HashSet<Coordinates>()
    }
    private val pins: Array<MutableList<Pin>> = Array(Color.values().size) {
        mutableListOf<Pin>()
    }
    val checks: Array<MutableList<Check>> = Array(Color.values().size) {
        mutableListOf<Check>()
    }
    val legalMoves: MutableList<Move> = mutableListOf()

    private val zobrist = ZobristSignatures(Random(2137))

    init {
        val stateHash = hashState(fenState)
        this.state = State.of(fenState, stateHash)
        board = fenState.board.copyOf()
        Coordinates.allCoordinates.forEach { coords ->
            val square = board.byCoordinates(coords)
            if (square is Square.Occupied) {
                val piece = square.piece
                pieceLists[piece.color.ordinal][piece.type.ordinal].add(coords)
            }
        }
        computeStateFeatures()
        genLegalMoves()
    }


    val isFiftyMoveRule: Boolean
        get() = state.plyCount > 99

    val isThreeFoldRepetition: Boolean
        get() {
            var i = 0
            val end = min(state.plyCount, previousStates.size)
            val playingColorsCount = Color.values().size - state.eliminatedColors.eliminatedColorsCount
            if (end < playingColorsCount * 2 - 1) {
                return false
            }
            val iterator = previousStates.descendingIterator()
            var repCount = 0
            while (i <= end - playingColorsCount) {
                var prevState: State
                do {
                    prevState = iterator.next()
                    i++
                } while (prevState.nextMoveColor != state.nextMoveColor)
                if (prevState.hash == state.hash) {
                    repCount++
                    if (repCount == 2) {
                        return true
                    }
                }
            }
            return false
        }

    private val isStaleMate: Boolean
        get() = Color.values().size - state.eliminatedColors.eliminatedColorsCount == 2 && legalMoves.isEmpty()

    val isDrawByClaimPossible: Boolean
        get() = isFiftyMoveRule || isThreeFoldRepetition

    val isDraw: Boolean
        get() = isMaterialInsufficient || isStaleMate

    fun isEliminated(color: Color) = state.eliminatedColors.isEliminated(color)

    val nextMoveColor: Color
        get() = state.nextMoveColor

    val winner: Color?
        get() =
            if (state.eliminatedColors.eliminatedColorsCount == Color.values().size - 1)
                Color.values().firstOrNull { c -> !state.eliminatedColors.isEliminated(c) }
            else
                null

    val isMaterialInsufficient: Boolean
        get() {
            if (isEachKingAlone())
                return true
            val isOneVsOne = state.eliminatedColors.eliminatedColorsCount == Color.values().size - 2
            if (isOneVsOne) {
                val firstColor = Color.values().first { !state.eliminatedColors.isEliminated(it) }
                val secondColor = Color.values().first { it != firstColor && !state.eliminatedColors.isEliminated(it) }
                return isKingVsKingAndBishop(firstColor, secondColor)
                        || isKingVsKingAndBishop(secondColor, firstColor)
                        || isKingVsKingAndKnight(firstColor, secondColor)
                        || isKingVsKingAndKnight(secondColor, firstColor)
                        || isKingAndBishopVsKingAndBishopOfSameType(firstColor, secondColor)
            }
            return false
        }

    private fun isEachKingAlone(): Boolean {
        for (i in Color.values().indices) {
            for (j in PieceType.values().indices) {
                if (!state.eliminatedColors.isEliminated(Color.values()[i])
                        && j != King.ordinal
                        && pieceLists[i][j].isNotEmpty()) {
                    return false
                }
            }
        }
        return true
    }

    private fun countByColorAndPieceType(color: Color, pieceType: PieceType) =
            pieceLists[color.ordinal][pieceType.ordinal].size

    private fun isKingVsKingAndBishop(firstColor: Color, secondColor: Color) =
            countByColorAndPieceType(firstColor, Pawn) == 0
                    && countByColorAndPieceType(firstColor, Knight) == 0
                    && countByColorAndPieceType(firstColor, Bishop) == 0
                    && countByColorAndPieceType(firstColor, Rook) == 0
                    && countByColorAndPieceType(firstColor, Queen) == 0
                    && countByColorAndPieceType(secondColor, Pawn) == 0
                    && countByColorAndPieceType(secondColor, Knight) == 0
                    && countByColorAndPieceType(secondColor, Bishop) == 1
                    && countByColorAndPieceType(secondColor, Rook) == 0
                    && countByColorAndPieceType(secondColor, Queen) == 0

    private fun isKingVsKingAndKnight(firstColor: Color, secondColor: Color) =
            countByColorAndPieceType(firstColor, Pawn) == 0
                    && countByColorAndPieceType(firstColor, Knight) == 0
                    && countByColorAndPieceType(firstColor, Bishop) == 0
                    && countByColorAndPieceType(firstColor, Rook) == 0
                    && countByColorAndPieceType(firstColor, Queen) == 0
                    && countByColorAndPieceType(secondColor, Pawn) == 0
                    && countByColorAndPieceType(secondColor, Knight) == 1
                    && countByColorAndPieceType(secondColor, Bishop) == 0
                    && countByColorAndPieceType(secondColor, Rook) == 0
                    && countByColorAndPieceType(secondColor, Queen) == 0

    private fun isKingAndBishopVsKingAndBishopOfSameType(firstColor: Color, secondColor: Color): Boolean {
        val isKBvsKB = countByColorAndPieceType(firstColor, Pawn) == 0
                && countByColorAndPieceType(firstColor, Knight) == 0
                && countByColorAndPieceType(firstColor, Bishop) == 1
                && countByColorAndPieceType(firstColor, Rook) == 0
                && countByColorAndPieceType(firstColor, Queen) == 0
                && countByColorAndPieceType(secondColor, Pawn) == 0
                && countByColorAndPieceType(secondColor, Knight) == 0
                && countByColorAndPieceType(secondColor, Bishop) == 1
                && countByColorAndPieceType(secondColor, Rook) == 0
                && countByColorAndPieceType(secondColor, Queen) == 0
        if (!isKBvsKB) {
            return false
        }
        val firstBishopCoords = pieceLists[firstColor.ordinal][Bishop.ordinal].first
        val secondBishopCoords = pieceLists[secondColor.ordinal][Bishop.ordinal].first
        return firstBishopCoords.isLightSquare == secondBishopCoords.isLightSquare
    }

    fun toState() =
            FenState(
                    board = this.board.copyOf(),
                    eliminatedColors = Color.values().filter { color ->
                        this.state.eliminatedColors.isEliminated(color)
                    }.toSet(),
                    enPassantSquares = Color.values().mapNotNull { color ->
                        this.state.enPassantSquares.getEnPassantSquareByColor(color)
                                ?.let { coords -> color to coords }
                    }.toMap(),
                    nextMoveColor = this.state.nextMoveColor,
                    castlingOptions = Color.values().map { color ->
                        color to this.state.castlingOptions[color]
                    }.toMap(),
                    plyCount = this.state.plyCount
            )

    fun makeMove(move: Move) {
        val pseudoState = state.copy(
                nextMoveColor = getNewNextMoveColor(),
                enPassantSquares = getNewEnPassantSquares(move),
                castlingOptions = getNewCastlingOptions(move),
                plyCount = getNewPlyCount(move),
                lastMove = move,
                capturedPiece = getCapturedPiece(move),
                hash = getNewHash(move)
        )
        updateBoardAndPieceLists(move)
        val prevState = state
        state = pseudoState
        findLegalState()
        previousStates.add(prevState)
    }

    fun unmakeMove(): Boolean {
        val move = state.lastMove ?: return false
        val prevState = previousStates.pop()
        val prevColor = prevState.nextMoveColor
        val capturedPiece = state.capturedPiece
        val toSquare = board.byCoordinates(move.to)
        val movedPiece = if (move is Promotion)
            Square.Occupied.by(prevColor, Pawn).piece
        else
            (toSquare as Square.Occupied).piece
        board.set(move.from, Square.Occupied.by(movedPiece))
        pieceLists[prevColor.ordinal][movedPiece.type.ordinal].apply {
            add(move.from)
            remove(move.to)
        }
        if (move is Promotion) {
            val promotionPieceType: PromotionPieceType = move.pieceType
            val pieceType = promotionPieceType.toPieceType()
            pieceLists[prevColor.ordinal][pieceType.ordinal].remove(move.to)
        }
        if (capturedPiece != null) {
            val isCaptureByEnPassant = prevState.enPassantSquares.getColorByEnPassantSquare(move.to) != null
                    && capturedPiece.type == Pawn
            if (isCaptureByEnPassant) {
                val capturedPawnCoords = move.to.offset(capturedPiece.color.pawnForwardVector)
                board.apply {
                    set(capturedPawnCoords, Square.Occupied.by(capturedPiece))
                    set(move.to, Square.Empty)
                }
                pieceLists[capturedPiece.color.ordinal][capturedPiece.type.ordinal].add(capturedPawnCoords)
            } else {
                board.set(move.to, Square.Occupied.by(capturedPiece))
                pieceLists[capturedPiece.color.ordinal][capturedPiece.type.ordinal].add(move.to)
            }
        } else {
            board.set(move.to, Square.Empty)
        }
        if (movedPiece.type == King) {
            val castling = when (move.to) {
                move.from.offset(prevColor.kingSideVector, 2) -> KingSide
                move.from.offset(prevColor.queenSideVector, 2) -> QueenSide
                else -> null
            }
            if (castling != null) {
                val coordsBeforeCastling = getRookCoordinatesBeforeCastling(prevColor, castling)
                val coordsAfterCastling = getRookCoordinatesAfterCastling(prevColor, castling)
                board.apply {
                    set(coordsAfterCastling, Square.Empty)
                    set(coordsBeforeCastling, Square.Occupied.by(prevColor, Rook))
                }
                pieceLists[prevColor.ordinal][Rook.ordinal].apply {
                    remove(coordsAfterCastling)
                    add(coordsBeforeCastling)
                }
            }
        }
        state = prevState
        computeStateFeatures()
        genLegalMoves()
        return true
    }

    fun makeResignation(color: Color) {
        val pseudoState = state.copy(
                eliminatedColors = state.eliminatedColors.withColorEliminated(color),
                nextMoveColor = if (state.nextMoveColor == color) getNewNextMoveColor() else state.nextMoveColor,
                enPassantSquares = state.enPassantSquares.dropEnPassantSquareForColor(color),
                hash = getNewHash(eliminatedColor = color),
                lastMove = null,
                capturedPiece = null
        )
        val prevState = state
        state = pseudoState
        findLegalState()
        previousStates.add(prevState)
    }

    private tailrec fun findLegalState() {
        computeStateFeatures()
        genLegalMoves()
        if (legalMoves.isEmpty()) {
            val color = state.nextMoveColor
            val isCheck = checks[color.ordinal].isNotEmpty()
            val isEliminated = isCheck || state.eliminatedColors.eliminatedColorsCount < 2
            if (isEliminated) {
                state = state.copy(
                        eliminatedColors = state.eliminatedColors.withColorEliminated(color),
                        enPassantSquares = state.enPassantSquares.dropEnPassantSquareForColor(color),
                        nextMoveColor = newNextMoveColor(color),
                        hash = getNewHash(eliminatedColor = color)
                )
                findLegalState()
            }
        }
    }

    private fun updateBoardAndPieceLists(move: Move) {
        val color = state.nextMoveColor

        // query before board gets updated
        val fromSquare = board.byCoordinates(move.from)
        val toSquare = board.byCoordinates(move.to)
        val movedPiece = (fromSquare as Square.Occupied).piece
        val castling = getCastling(move)
        val isCaptureByEnPassant = isCaptureByEnPassant(move)

        board.set(move.from, Square.Empty)
        board.set(move.to, fromSquare)
        pieceLists[color.ordinal][movedPiece.type.ordinal].apply {
            remove(move.from)
            add(move.to)
        }
        if (toSquare is Square.Occupied) {
            val capturedPiece = toSquare.piece
            pieceLists[capturedPiece.color.ordinal][capturedPiece.type.ordinal].apply {
                remove(move.to)
            }
        }
        if (castling != null) {
            val oldRookCoords = getRookCoordinatesBeforeCastling(color, castling)
            val newRookCoords = getRookCoordinatesAfterCastling(color, castling)
            board.set(oldRookCoords, Square.Empty)
            board.set(newRookCoords, Square.Occupied.by(color, Rook))
            pieceLists[color.ordinal][Rook.ordinal].apply {
                remove(oldRookCoords)
                add(newRookCoords)
            }
        }
        if (isCaptureByEnPassant) {
            val capturedPawnColor = state.enPassantSquares.getColorByEnPassantSquare(move.to)!!
            val capturedPawnCoords = move.to.offset(capturedPawnColor.pawnForwardVector)
            board.set(capturedPawnCoords, Square.Empty)
            pieceLists[capturedPawnColor.ordinal][Pawn.ordinal].remove(capturedPawnCoords)
        }
        if (move is Promotion) {
            val promotionPieceType: PromotionPieceType = move.pieceType
            val newPieceType = promotionPieceType.toPieceType()
            val newSquare = Square.Occupied.by(color, newPieceType)
            board.set(move.to, newSquare)
            pieceLists[color.ordinal][Pawn.ordinal].remove(move.to)
            pieceLists[color.ordinal][newPieceType.ordinal].add(move.to)
        }
    }

    private fun getCastling(move: Move): Castling? {
        val square = board.byCoordinates(move.from)
        val movedPieceType = (square as Square.Occupied).piece.type
        if (movedPieceType == King) {
            if (move.from.offsetOrNull(state.nextMoveColor.kingSideVector, 2) == move.to) {
                return KingSide
            }
            if (move.from.offsetOrNull(state.nextMoveColor.queenSideVector, 2) == move.to) {
                return QueenSide
            }
        }
        return null
    }

    private fun isCaptureByEnPassant(move: Move): Boolean {
        val srcSquare = board.byCoordinates(move.from)
        val movedPieceType = (srcSquare as Square.Occupied).piece.type
        return movedPieceType == Pawn && state.enPassantSquares.getColorByEnPassantSquare(move.to) != null
    }

    private fun getCapturedPiece(move: Move): Piece? {
        val toSquare = board.byCoordinates(move.to)
        if (toSquare is Square.Occupied) {
            return toSquare.piece
        }
        val fromSquare = board.byCoordinates(move.from)
        val movedPieceType = (fromSquare as Square.Occupied).piece.type
        if (movedPieceType == Pawn) {
            val enPassantSquareColor = state.enPassantSquares.getColorByEnPassantSquare(move.to)
            if (enPassantSquareColor != null) {
                return Square.Occupied.by(enPassantSquareColor, Pawn).piece
            }
        }
        return null
    }

    private fun getNewNextMoveColor(): Color {
        return newNextMoveColor(state.nextMoveColor)
    }

    private tailrec fun newNextMoveColor(currentColor: Color): Color {
        val newColorIndex = (currentColor.ordinal + 1) % Color.values().size
        val newColor = Color.values()[newColorIndex]
        if (!state.eliminatedColors.isEliminated(newColor)) {
            return newColor
        }
        return newNextMoveColor(currentColor = newColor)
    }

    private fun getNewEnPassantSquares(move: Move): EnPassantSquaresBits {
        return Color.values().fold(initialEnPassantSquares()) { newSqrs, color ->
            if (state.nextMoveColor == color) {
                val square = board.byCoordinates(move.from) as Square.Occupied
                if (square.piece.type == Pawn && move.from.offset(color.pawnForwardVector, 2) == move.to) {
                    val enPassantCoords = move.from.offset(color.pawnForwardVector)
                    newSqrs.withEnPassantSquareForColor(color, enPassantCoords)
                } else {
                    newSqrs
                }
            } else {
                state.enPassantSquares.getEnPassantSquareByColor(color)
                        ?.takeIf { move.to != it }
                        ?.takeIf {
                            val pawnCoords = it.offset(color.pawnForwardVector)
                            pawnCoords != move.to
                        }
                        ?.let { newSqrs.withEnPassantSquareForColor(color, it) }
                        ?: newSqrs
            }
        }
    }

    private fun getNewCastlingOptions(move: Move): CastlingOptionsBits =
            Color.values().fold(state.castlingOptions) { acc, color ->
                if (color == state.nextMoveColor) {
                    val square = board.byCoordinates(move.from) as Square.Occupied
                    when (square.piece.type) {
                        King -> acc
                                .dropCastlingForColor(color, KingSide)
                                .dropCastlingForColor(color, QueenSide)
                        Rook -> {
                            when (move.from) {
                                getRookCoordinatesBeforeCastling(color, KingSide) ->
                                    acc.dropCastlingForColor(color, KingSide)
                                getRookCoordinatesBeforeCastling(color, QueenSide) ->
                                    acc.dropCastlingForColor(color, QueenSide)
                                else ->
                                    acc
                            }
                        }
                        else -> acc
                    }
                } else {
                    acc
                }
            }

    private fun getNewPlyCount(move: Move): Int {
        if (isRegularCapture(move) || isPawnAdvance(move)) {
            return 0
        }
        return state.plyCount + 1
    }

    private fun isRegularCapture(move: Move): Boolean {
        val targetSquare = board.byCoordinates(move.to)
        return targetSquare is Square.Occupied
                && targetSquare.piece.color != state.nextMoveColor
    }

    private fun isPawnAdvance(move: Move): Boolean {
        val srcSquare = board.byCoordinates(move.from)
        val movedPieceType = (srcSquare as Square.Occupied).piece.type
        return movedPieceType == Pawn
    }

    private fun getNewHash(move: Move): Long {
        var hash = state.hash
        val moveColor = state.nextMoveColor
        val fromSquare = board.byCoordinates(move.from)
        val movedPiece = (fromSquare as Square.Occupied).piece
        // piece-square
        hash = hash xor zobrist.getPieceSquareVal(movedPiece, move.from)
        val toSquare = board.byCoordinates(move.to)
        if (toSquare is Square.Occupied) {
            val capturedPiece = toSquare.piece
            hash = hash xor zobrist.getPieceSquareVal(capturedPiece, move.to)
        }
        hash = if (move is Promotion) {
            val promotionPieceType: PromotionPieceType = move.pieceType
            val pieceType = promotionPieceType.toPieceType()
            val pieceAfterPromotion = Square.Occupied.by(moveColor, pieceType).piece
            hash xor zobrist.getPieceSquareVal(pieceAfterPromotion, move.to)
        } else {
            hash xor zobrist.getPieceSquareVal(movedPiece, move.to)
        }
        if (isCaptureByEnPassant(move)) {
            val capturedPawnColor = state.enPassantSquares.getColorByEnPassantSquare(move.to)!!
            val capturedPawnCoords = move.to.offset(capturedPawnColor.pawnForwardVector)
            val piece = Square.Occupied.by(capturedPawnColor, Pawn).piece
            hash = hash xor zobrist.getPieceSquareVal(piece, capturedPawnCoords)
        }
        val castling = getCastling(move)
        if (castling != null) {
            val oldRookCoords = getRookCoordinatesBeforeCastling(moveColor, castling)
            val newRookCoords = getRookCoordinatesAfterCastling(moveColor, castling)
            val piece = Square.Occupied.by(moveColor, Rook).piece
            hash = hash xor zobrist.getPieceSquareVal(piece, oldRookCoords)
            hash = hash xor zobrist.getPieceSquareVal(piece, newRookCoords)
        }
        // next move color
        hash = hash xor zobrist.getNextMoveColorVal(moveColor)
        hash = hash xor zobrist.getNextMoveColorVal(newNextMoveColor(moveColor))
        // castling
        val oldCastlingOptions: Set<Castling> = state.castlingOptions[moveColor]
        val newCastlingOptions: Set<Castling> = when {
            castling != null || movedPiece.type == King ->
                castlingOptionsNone
            movedPiece.type == Rook && move.from == getRookCoordinatesBeforeCastling(moveColor, KingSide) ->
                state.castlingOptions.dropCastlingForColor(moveColor, KingSide)[moveColor]
            movedPiece.type == Rook && move.from == getRookCoordinatesBeforeCastling(moveColor, QueenSide) ->
                state.castlingOptions.dropCastlingForColor(moveColor, QueenSide)[moveColor]
            else -> oldCastlingOptions
        }
        if (oldCastlingOptions != newCastlingOptions) {
            hash = hash xor zobrist.getCastlingOptionsValue(moveColor, oldCastlingOptions)
            hash = hash xor zobrist.getCastlingOptionsValue(moveColor, newCastlingOptions)
        }
        // en passant squares
        Color.values().forEach { color ->
            if (moveColor == color) {
                val enPassantSquare = state.enPassantSquares.getEnPassantSquareByColor(color)
                if (enPassantSquare != null) {
                    hash = hash xor zobrist.getEnPassantVal(color, enPassantSquare)
                }
                if (movedPiece.type == Pawn && move.from.offset(color.pawnForwardVector, 2) == move.to) {
                    val newEnPassantSquare = move.from.offset(color.pawnForwardVector)
                    hash = hash xor zobrist.getEnPassantVal(color, newEnPassantSquare)
                }
            } else {
                val enPassantSquare = state.enPassantSquares.getEnPassantSquareByColor(color)
                if (enPassantSquare != null) {
                    val pawnSquare = enPassantSquare.offset(color.pawnForwardVector)
                    if (move.to == enPassantSquare || pawnSquare == move.to) {
                        hash = hash xor zobrist.getEnPassantVal(color, enPassantSquare)
                    }
                }
            }
        }
        return hash
    }

    private fun getNewHash(eliminatedColor: Color): Long {
        var hash = state.hash

        hash = hash xor zobrist.getEliminatedColorValue(eliminatedColor)

        val enPassantSquare = state.enPassantSquares.getEnPassantSquareByColor(eliminatedColor)
        if (enPassantSquare != null) {
            hash = hash xor zobrist.getEnPassantVal(eliminatedColor, enPassantSquare)
        }

        val newColor = newNextMoveColor(state.nextMoveColor)
        if (newColor != state.nextMoveColor) {
            hash = hash xor zobrist.getNextMoveColorVal(state.nextMoveColor)
            hash = hash xor zobrist.getNextMoveColorVal(newColor)
        }
        return hash
    }

    private fun hashState(fenState: FenState): Long {
        var hash = 0L
        fenState.board.forEachIndexed { rank, row ->
            row.forEachIndexed { file, square ->
                if (square is Square.Occupied) {
                    val piece = square.piece
                    val coords = Coordinates.ofFileAndRank(file, rank)
                    hash = hash xor zobrist.getPieceSquareVal(piece, coords)
                }
            }
        }
        hash = hash xor zobrist.getNextMoveColorVal(fenState.nextMoveColor)
        Color.values().forEach { color ->
            hash = hash xor zobrist.getCastlingOptionsValue(color, fenState.castlingOptions[color]
                    ?: castlingOptionsNone)
        }
        Color.values().forEach { color ->
            val enPassantSqrCoords = fenState.enPassantSquares[color]
            if (enPassantSqrCoords != null) {
                hash = hash xor zobrist.getEnPassantVal(color, enPassantSqrCoords)
            }
        }
        return hash
    }

    private fun computeStateFeatures() {
        for (i in Color.values().indices) {
            attackedSquares[i].clear()
            pins[i].clear()
            checks[i].clear()
        }

        Color.values().forEach { color ->
            if (!state.eliminatedColors.isEliminated(color)) {
                PieceType.values().forEach { pieceType ->
                    pieceLists[color.ordinal][pieceType.ordinal].forEach { coords ->
                        findChecks(
                                checkingPieceCoords = coords,
                                checkingPieceColor = color,
                                checkingPieceType = pieceType
                        )
                        getAttackedCoordinates(
                                pieceCoords = coords,
                                pieceColor = color,
                                pieceType = pieceType
                        )
                        findPins(
                                pinningPieceCoords = coords,
                                pinningPieceType = pieceType,
                                pinningPieceColor = color
                        )
                    }
                }
            }
        }
    }

    private fun findChecks(checkingPieceCoords: Coordinates,
                           checkingPieceColor: Color,
                           checkingPieceType: PieceType) {
        when (checkingPieceType) {
            Pawn -> findChecks(checkingPieceCoords, checkingPieceColor.pawnCapturingVectors, checkingPieceColor)
            Knight -> findChecks(checkingPieceCoords, knightMoveVectors, checkingPieceColor)
            Bishop -> findChecksAlongLine(checkingPieceCoords, bishopMoveVectors, checkingPieceColor)
            Rook -> findChecksAlongLine(checkingPieceCoords, rookMoveVectors, checkingPieceColor)
            Queen -> findChecksAlongLine(checkingPieceCoords, allDirectionsVectors, checkingPieceColor)
            King -> {
            }
        }
    }

    private fun findChecks(checkingPieceCoords: Coordinates,
                           attackVectors: List<Vector>,
                           checkingPieceColor: Color) {
        attackVectors.forEach { vector ->
            val attackedCoords = checkingPieceCoords.offsetOrNull(vector)
            if (attackedCoords != null) {
                addCheckIfPresent(
                        pieceCoords = checkingPieceCoords,
                        pieceColor = checkingPieceColor,
                        attackedCoordinates = attackedCoords
                )
            }
        }
    }

    private fun findChecksAlongLine(checkingPieceCoords: Coordinates,
                                    attackUnitVectors: List<Vector>,
                                    checkingPieceColor: Color) {
        attackUnitVectors.forEach { vector ->
            val lastCoords = traverseSquares(
                    startingCoordinates = checkingPieceCoords,
                    vector = vector,
                    isEndReached = { coords -> board.byCoordinates(coords) is Square.Occupied }
            )
            addCheckIfPresent(
                    pieceCoords = checkingPieceCoords,
                    pieceColor = checkingPieceColor,
                    attackedCoordinates = lastCoords
            )
        }
    }

    private fun addCheckIfPresent(pieceCoords: Coordinates,
                                  pieceColor: Color,
                                  attackedCoordinates: Coordinates) {
        val attackedSquare = board.byCoordinates(attackedCoordinates)
        if (attackedSquare is Square.Occupied
                && attackedSquare.piece.type == King
                && attackedSquare.piece.color != pieceColor
                && !state.eliminatedColors.isEliminated(attackedSquare.piece.color)) {
            val check = Check(
                    checkingPieceCoordinates = pieceCoords,
                    checkedKingCoordinates = attackedCoordinates
            )
            checks[attackedSquare.piece.color.ordinal].add(check)
        }
    }

    private fun getAttackedCoordinates(pieceCoords: Coordinates,
                                       pieceColor: Color,
                                       pieceType: PieceType) {
        when (pieceType) {
            Pawn -> getAttackedCoordinates(pieceCoords, pieceColor, pieceColor.pawnCapturingVectors)
            Knight -> getAttackedCoordinates(pieceCoords, pieceColor, knightMoveVectors)
            Bishop -> getAttackedCoordinatesAlongLine(pieceCoords, pieceColor, bishopMoveVectors)
            Rook -> getAttackedCoordinatesAlongLine(pieceCoords, pieceColor, rookMoveVectors)
            Queen -> getAttackedCoordinatesAlongLine(pieceCoords, pieceColor, allDirectionsVectors)
            King -> getAttackedCoordinates(pieceCoords, pieceColor, allDirectionsVectors)
        }
    }

    private fun getAttackedCoordinates(pieceCoords: Coordinates,
                                       pieceColor: Color,
                                       attackVectors: List<Vector>) {
        attackVectors.forEach { vector ->
            val attackedCoords = pieceCoords.offsetOrNull(vector)
            if (attackedCoords != null) {
                attackedSquares[pieceColor.ordinal].add(attackedCoords)
            }
        }
    }


    private fun getAttackedCoordinatesAlongLine(pieceCoords: Coordinates,
                                                pieceColor: Color,
                                                attackingUnitVectors: List<Vector>) {
        attackingUnitVectors.forEach { vector ->
            traverseSquares(
                    startingCoordinates = pieceCoords,
                    vector = vector,
                    forEachDo = { coords ->
                        attackedSquares[pieceColor.ordinal].add(coords)
                    },
                    isEndReached = { coords ->
                        when (val square = board.byCoordinates(coords)) {
                            is Square.Empty -> false
                            is Square.Occupied -> square.piece.type != King || square.piece.color == pieceColor
                        }
                    }
            )
        }

    }

    private fun findPins(pinningPieceCoords: Coordinates,
                         pinningPieceType: PieceType,
                         pinningPieceColor: Color) {
        when (pinningPieceType) {
            Bishop -> findPins(pinningPieceCoords, bishopMoveVectors, pinningPieceColor)
            Rook -> findPins(pinningPieceCoords, rookMoveVectors, pinningPieceColor)
            Queen -> findPins(pinningPieceCoords, allDirectionsVectors, pinningPieceColor)
            else -> {
            }
        }
    }

    private fun findPins(pinningPieceCoords: Coordinates,
                         attackUnitVectors: List<Vector>,
                         pinningPieceColor: Color) {
        attackUnitVectors.forEach { vector ->
            val lastAttackedCoords = traverseSquares(
                    startingCoordinates = pinningPieceCoords,
                    vector = vector,
                    isEndReached = { coords -> board.byCoordinates(coords) is Square.Occupied }
            )
            val lastAttackedSquare = board.byCoordinates(lastAttackedCoords)
            if (lastAttackedSquare is Square.Occupied
                    && lastAttackedSquare.piece.color != pinningPieceColor
                    && lastAttackedSquare.piece.type != King
                    && !state.eliminatedColors.isEliminated(lastAttackedSquare.piece.color)) {
                val lastScannedCoords = traverseSquares(
                        startingCoordinates = lastAttackedCoords,
                        vector = vector,
                        isEndReached = { coords -> board.byCoordinates(coords) is Square.Occupied }
                )
                val lastScannedSquare = board.byCoordinates(lastScannedCoords)
                if (lastScannedSquare is Square.Occupied
                        && lastScannedSquare.piece.type == King
                        && lastScannedSquare.piece.color == lastAttackedSquare.piece.color) {
                    val pin = Pin(
                            pinningPieceCoordinates = pinningPieceCoords,
                            pinnedPieceCoordinates = lastAttackedCoords
                    )
                    pins[lastAttackedSquare.piece.color.ordinal].add(pin)
                }
            }
        }
    }

    private inline fun traverseSquares(
            startingCoordinates: Coordinates,
            vector: Vector,
            forEachDo: (Coordinates) -> Unit = {},
            isEndReached: (Coordinates) -> Boolean): Coordinates {
        var lastCoords = startingCoordinates
        while (true) {
            val newCoords = lastCoords.offsetOrNull(vector) ?: break
            lastCoords = newCoords
            forEachDo(newCoords)
            if (isEndReached(newCoords))
                break
        }
        return lastCoords
    }

    private fun genLegalMoves() {
        legalMoves.clear()
        PieceType.values().forEach { pieceType ->
            pieceLists[state.nextMoveColor.ordinal][pieceType.ordinal].forEach { coords ->
                when (pieceType) {
                    Pawn -> genPawnLegalMoves(coords)
                    Knight -> genKnightMoves(coords)
                    Bishop -> genBishopMoves(coords)
                    Rook -> genRookMoves(coords)
                    Queen -> genQueenMoves(coords)
                    King -> genKingPseudoMoves(coords)
                }
            }
        }
    }

    private fun addMoveIfLegal(from: Coordinates, to: Coordinates) {
        if (isLegalMove(from, to)) {
            val move = RegularMove(from, to)
            legalMoves.add(move)
        }
    }

    private fun addPawnMoveIfLegal(from: Coordinates, to: Coordinates, promotionPieceType: PromotionPieceType?) {
        if (isLegalMove(from, to)) {
            val move = when (promotionPieceType) {
                null -> RegularMove(from, to)
                else -> Promotion(from, to, promotionPieceType)
            }
            legalMoves.add(move)
        }
    }

    private fun addKingMoveIfLegal(from: Coordinates,
                                   to: Coordinates,
                                   isKingSideCastling: Boolean = false,
                                   isQueenSideCastling: Boolean = false) {
        if (isLegalKingMove(from, to, isKingSideCastling, isQueenSideCastling)) {
            val move = RegularMove(from, to)
            legalMoves.add(move)
        }
    }

    private fun isLegalKingMove(from: Coordinates,
                                to: Coordinates,
                                isKingSideCastling: Boolean,
                                isQueenSideCastling: Boolean): Boolean {
        val color = state.nextMoveColor
        val isDestinationAttacked = isSquareAttackedByOtherColors(color, to)
        if (isDestinationAttacked) {
            return false
        }
        if (isKingSideCastling || isQueenSideCastling) {
            val isChecked = checks[color.ordinal].isNotEmpty()
            if (isChecked) {
                return false
            }
            if (isKingSideCastling) {
                val newRookSquare = from.offset(color.kingSideVector)
                val isNewRookSquareAttacked = isSquareAttackedByOtherColors(color, newRookSquare)
                if (isNewRookSquareAttacked) {
                    return false
                }
            }
            if (isQueenSideCastling) {
                val newRookSquare = from.offset(color.queenSideVector)
                val isNewRookSquareAttacked = isSquareAttackedByOtherColors(color, newRookSquare)
                if (isNewRookSquareAttacked) {
                    return false
                }
            }
        }
        return true
    }

    private fun isSquareAttackedByOtherColors(color: Color, coords: Coordinates) =
            Color.values().any { it != color && coords in attackedSquares[it.ordinal] }

    private fun isLegalMove(from: Coordinates, to: Coordinates): Boolean {
        val color = state.nextMoveColor

        val pinsAgainstColor = pins[color.ordinal]
        val isMovePlacingKingInCheck = pinsAgainstColor.any { pin ->
            from == pin.pinnedPieceCoordinates
                    && !to.isOnLineBetween(pin.pinnedPieceCoordinates, pin.pinningPieceCoordinates)
                    && to != pin.pinningPieceCoordinates
        }
        if (isMovePlacingKingInCheck) {
            return false
        }

        val checksAgainstColor = checks[color.ordinal]
        if (checksAgainstColor.count() > 1) {
            return false
        }
        if (checksAgainstColor.isNotEmpty()) {
            val check = checksAgainstColor[0]
            val checkingCoords = check.checkingPieceCoordinates
            val checkedCoords = check.checkedKingCoordinates
            val isPossibleToBlock = when {
                (board.byCoordinates(checkingCoords) as Square.Occupied).piece.type == Knight -> false
                checkingCoords.isAdjacentTo(checkedCoords) -> false
                else -> true
            }
            val isBlockingMove = isPossibleToBlock && to.isOnLineBetween(checkedCoords, checkingCoords)
            val isCapturingCheckingPieceMove = to == checkingCoords
            if (!isCapturingCheckingPieceMove && !isBlockingMove) {
                return false
            }
        }

        val toSquare = board.byCoordinates(to)
        val isOccupiedByNotEliminatedKing = toSquare is Square.Occupied
                && toSquare.piece.type == King
                && !state.eliminatedColors.isEliminated(toSquare.piece.color)
        // King must not be captured via discovered attack, each player should be allowed to respond to check
        if (isOccupiedByNotEliminatedKing) {
            return false
        }

        return true
    }


    private fun genPawnLegalMoves(pawnCoords: Coordinates) {
        pawnCoords.offsetOrNull(state.nextMoveColor.pawnForwardVector)
                ?.takeIf { coords -> board.byCoordinates(coords) == Square.Empty }
                ?.let { newCoords -> genPawnLegalMoves(from = pawnCoords, to = newCoords) }
        pawnCoords.takeIf { pawnCoords.isOnPawnStartingRowForColor(state.nextMoveColor) }
                ?.offsetOrNull(state.nextMoveColor.pawnForwardVector)
                ?.takeIf { coords -> board.byCoordinates(coords) == Square.Empty }
                ?.offsetOrNull(state.nextMoveColor.pawnForwardVector)
                ?.takeIf { coords -> board.byCoordinates(coords) == Square.Empty }
                ?.let { newCords -> addPawnMoveIfLegal(from = pawnCoords, to = newCords, promotionPieceType = null) }
        state.nextMoveColor.pawnCapturingVectors
                .forEach { vector ->
                    val captureCoords = pawnCoords.offsetOrNull(vector) ?: return@forEach
                    captureCoords.takeIf { coords ->
                        when (val square = board.byCoordinates(coords)) {
                            is Square.Occupied -> square.piece.color != state.nextMoveColor
                            is Square.Empty -> false
                        }
                    }
                            ?.let { coords -> genPawnLegalMoves(from = pawnCoords, to = coords) }

                    captureCoords.takeIf { coords ->
                        state.enPassantSquares.getColorByEnPassantSquare(coords)
                                ?.let { it != state.nextMoveColor }
                                ?: false
                    }
                            ?.let { coords -> genPawnLegalMoves(from = pawnCoords, to = coords) }
                }
    }

    private fun genPawnLegalMoves(from: Coordinates, to: Coordinates) {
        val isPromotion = to.isPromotionSquareForColor(state.nextMoveColor)
        if (isPromotion) {
            PromotionPieceType.values().forEach { promotionPieceType ->
                addPawnMoveIfLegal(from, to, promotionPieceType)
            }
        } else {
            addPawnMoveIfLegal(from, to, promotionPieceType = null)
        }
    }

    private fun genKnightMoves(coordinates: Coordinates) {
        knightMoveVectors.forEach { vector ->
            val newCoords = coordinates.offsetOrNull(vector) ?: return@forEach
            val square = board.byCoordinates(newCoords)
            if (square is Square.Empty || (square is Square.Occupied && square.piece.color != state.nextMoveColor)) {
                addMoveIfLegal(from = coordinates, to = newCoords)
            }
        }
    }

    private fun genBishopMoves(coordinates: Coordinates) {
        bishopMoveVectors.forEach { vector -> genMovesOnLine(coordinates, vector) }
    }

    private fun genRookMoves(coordinates: Coordinates) {
        rookMoveVectors.forEach { vector -> genMovesOnLine(coordinates, vector) }
    }

    private fun genQueenMoves(coordinates: Coordinates) {
        allDirectionsVectors.forEach { vector ->
            genMovesOnLine(coordinates, vector)
        }
    }

    private fun genKingPseudoMoves(coordinates: Coordinates) {
        val color = state.nextMoveColor
        val kingSideVector = color.kingSideVector
        coordinates.offsetOrNull(kingSideVector)
                ?.takeIf { KingSide in state.castlingOptions[color] }
                ?.takeIf { newRookCoords -> board.byCoordinates(newRookCoords) == Square.Empty }
                ?.offsetOrNull(kingSideVector)
                ?.takeIf { newKingCoords -> board.byCoordinates(newKingCoords) == Square.Empty }
                ?.let { newKingCoords ->
                    addKingMoveIfLegal(from = coordinates, to = newKingCoords, isKingSideCastling = true)
                }
        val queenSideVector = color.queenSideVector
        coordinates.offsetOrNull(queenSideVector)
                ?.takeIf { QueenSide in state.castlingOptions[color] }
                ?.takeIf { newRookCoords -> board.byCoordinates(newRookCoords) == Square.Empty }
                ?.offsetOrNull(queenSideVector)
                ?.takeIf { newKingCoords -> board.byCoordinates(newKingCoords) == Square.Empty }
                ?.let { newKingCoords ->
                    addKingMoveIfLegal(from = coordinates, to = newKingCoords, isQueenSideCastling = true)
                }
        allDirectionsVectors
                .forEach { vector ->
                    val newCoords = coordinates.offsetOrNull(vector) ?: return@forEach
                    val square = board.byCoordinates(newCoords)
                    if (square is Square.Empty || (square is Square.Occupied && square.piece.color != state.nextMoveColor)) {
                        addKingMoveIfLegal(from = coordinates, to = newCoords)
                    }
                }
    }

    private fun genMovesOnLine(coordinates: Coordinates, vector: Vector) {
        var lastCoords = coordinates
        var endReached = false
        while (!endReached) {
            val newCoords = lastCoords.offsetOrNull(vector) ?: break
            when (val square = board.byCoordinates(newCoords)) {
                is Square.Occupied -> {
                    endReached = true
                    if (square.piece.color != state.nextMoveColor) {
                        addMoveIfLegal(from = coordinates, to = newCoords)
                    }
                }
                is Square.Empty -> {
                    addMoveIfLegal(from = coordinates, to = newCoords)
                }
            }
            lastCoords = newCoords
        }
    }
}