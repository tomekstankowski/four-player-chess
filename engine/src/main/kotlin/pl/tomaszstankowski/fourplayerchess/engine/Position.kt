package pl.tomaszstankowski.fourplayerchess.engine

import gnu.trove.list.array.TIntArrayList
import gnu.trove.list.array.TShortArrayList
import gnu.trove.list.linked.TIntLinkedList
import pl.tomaszstankowski.fourplayerchess.engine.Castling.KingSide
import pl.tomaszstankowski.fourplayerchess.engine.Castling.QueenSide
import pl.tomaszstankowski.fourplayerchess.engine.PieceType.*
import java.util.*
import kotlin.math.min
import kotlin.random.Random

internal class Position(private val previousStates: LinkedList<State>,
                        private var state: State,
                        private val board: Array<Square?>) {
    private val pieceLists: Array<Array<PieceList>> = Array(allColors.size) {
        Array(allPieceTypes.size) {
            TIntLinkedList()
        }
    }

    // for each color squares attacked by this color
    private val attackedSquares: Array<BooleanArray> = Array(allColors.size) {
        BooleanArray(BOARD_SIZE * BOARD_SIZE) { false }
    }

    // for each color squares that checked king of this color cannot move into but other kings can if only these squares are not attacked
    private val scannedSquaresBehindKing: Array<TIntArrayList> = Array(allColors.size) {
        TIntArrayList()
    }

    // for each color pins against this color
    private val pins: Array<TShortArrayList> = Array(allColors.size) {
        TShortArrayList()
    }

    // for each color checks against this color
    val checks: Array<TShortArrayList> = Array(allColors.size) {
        TShortArrayList()
    }
    private val legalMoves: TIntArrayList = TIntArrayList()

    init {
        for (i in 0 until BOARD_SIZE * BOARD_SIZE) {
            val square = this.board[i]
            if (square is Square.Occupied) {
                val piece = square.piece
                pieceLists[piece.color.ordinal][piece.type.ordinal].add(i)
            }
        }
        checkAttackVectors()
        generateLegalMoves()
    }

    companion object {
        private val zobrist = ZobristSignatures(Random(2137))

        private fun hashState(fenState: FenState): Long {
            var hash = 0L
            fenState.board.forEachIndexed { rank, row ->
                row.forEachIndexed { file, square ->
                    if (square is Square.Occupied) {
                        val piece = square.piece
                        val coords = Coordinates.ofFileAndRank(file, rank)
                        hash = hash xor zobrist.getPieceSquareVal(piece, coords.squareIndex)
                    }
                }
            }
            hash = hash xor zobrist.getNextMoveColorVal(fenState.nextMoveColor)
            allColors.forEach { color ->
                hash = hash xor zobrist.getCastlingOptionsValue(color, fenState.castlingOptions[color]
                        ?: castlingOptionsNone)
            }
            allColors.forEach { color ->
                val enPassantSqrCoords = fenState.enPassantSquares[color]
                if (enPassantSqrCoords != null) {
                    hash = hash xor zobrist.getEnPassantVal(color, enPassantSqrCoords.squareIndex)
                }
            }
            return hash
        }

        fun fromFenState(fenState: FenState): Position {
            val stateHash = hashState(fenState)
            val state = State.of(fenState, stateHash)
            val board = arrayOfNulls<Square?>(BOARD_SIZE * BOARD_SIZE)
            for (i in 0 until BOARD_SIZE)
                for (j in 0 until BOARD_SIZE)
                    board[i * BOARD_SIZE + j] = fenState.board[i][j]
            return Position(
                    previousStates = LinkedList(),
                    state = state,
                    board = board
            )
        }
    }

    fun getLegalMoves(): IntArray = legalMoves.toArray()

    val hash: Long
        get() = state.hash

    val isFiftyMoveRule: Boolean
        get() = state.plyCount > 99

    val isThreeFoldRepetition: Boolean
        get() {
            var pliesRemaining = min(state.plyCount, previousStates.size)
            val playingColorsCount = allColors.size - state.eliminatedColors.eliminatedColorsCount
            if (pliesRemaining < playingColorsCount * 2 - 1) {
                return false
            }
            val iterator = previousStates.descendingIterator()
            var repCount = 0
            while (pliesRemaining > 0) {
                val prevState = iterator.next()
                if (prevState.hash == state.hash) {
                    repCount++
                    if (repCount == 2) {
                        return true
                    }
                }
                pliesRemaining--
            }
            return false
        }

    private val isStaleMate: Boolean
        get() = allColors.size - state.eliminatedColors.eliminatedColorsCount == 2 && legalMoves.isEmpty()

    val isDrawByClaimPossible: Boolean
        get() = isFiftyMoveRule || isThreeFoldRepetition

    val isDraw: Boolean
        get() = isMaterialInsufficient || isStaleMate

    fun isEliminated(color: Color) = state.eliminatedColors.isEliminated(color)

    val nextMoveColor: Color
        get() = state.nextMoveColor

    val winner: Color?
        get() =
            if (state.eliminatedColors.eliminatedColorsCount == allColors.size - 1)
                allColors.firstOrNull { c -> !state.eliminatedColors.isEliminated(c) }
            else
                null

    val isMaterialInsufficient: Boolean
        get() {
            if (isEachKingAlone())
                return true
            val isOneVsOne = state.eliminatedColors.eliminatedColorsCount == allColors.size - 2
            if (isOneVsOne) {
                val firstColor = allColors.first { !state.eliminatedColors.isEliminated(it) }
                val secondColor = allColors.first { it != firstColor && !state.eliminatedColors.isEliminated(it) }
                return isKingVsKingAndBishop(firstColor, secondColor)
                        || isKingVsKingAndBishop(secondColor, firstColor)
                        || isKingVsKingAndKnight(firstColor, secondColor)
                        || isKingVsKingAndKnight(secondColor, firstColor)
                        || isKingAndBishopVsKingAndBishopOfSameType(firstColor, secondColor)
            }
            return false
        }

    private fun isEachKingAlone(): Boolean {
        for (i in allColors.indices) {
            if (!state.eliminatedColors.isEliminated(allColors[i])) {
                for (j in allPieceTypes.indices) {
                    if (j != King.ordinal
                            && !pieceLists[i][j].isEmpty) {
                        return false
                    }
                }
            }
        }
        return true
    }

    fun countPiecesBy(color: Color, pieceType: PieceType) =
            pieceLists[color.ordinal][pieceType.ordinal].size()

    private fun isKingVsKingAndBishop(firstColor: Color, secondColor: Color) =
            countPiecesBy(firstColor, Pawn) == 0
                    && countPiecesBy(firstColor, Knight) == 0
                    && countPiecesBy(firstColor, Bishop) == 0
                    && countPiecesBy(firstColor, Rook) == 0
                    && countPiecesBy(firstColor, Queen) == 0
                    && countPiecesBy(secondColor, Pawn) == 0
                    && countPiecesBy(secondColor, Knight) == 0
                    && countPiecesBy(secondColor, Bishop) == 1
                    && countPiecesBy(secondColor, Rook) == 0
                    && countPiecesBy(secondColor, Queen) == 0

    private fun isKingVsKingAndKnight(firstColor: Color, secondColor: Color) =
            countPiecesBy(firstColor, Pawn) == 0
                    && countPiecesBy(firstColor, Knight) == 0
                    && countPiecesBy(firstColor, Bishop) == 0
                    && countPiecesBy(firstColor, Rook) == 0
                    && countPiecesBy(firstColor, Queen) == 0
                    && countPiecesBy(secondColor, Pawn) == 0
                    && countPiecesBy(secondColor, Knight) == 1
                    && countPiecesBy(secondColor, Bishop) == 0
                    && countPiecesBy(secondColor, Rook) == 0
                    && countPiecesBy(secondColor, Queen) == 0

    private fun isKingAndBishopVsKingAndBishopOfSameType(firstColor: Color, secondColor: Color): Boolean {
        val isKBvsKB = countPiecesBy(firstColor, Pawn) == 0
                && countPiecesBy(firstColor, Knight) == 0
                && countPiecesBy(firstColor, Bishop) == 1
                && countPiecesBy(firstColor, Rook) == 0
                && countPiecesBy(firstColor, Queen) == 0
                && countPiecesBy(secondColor, Pawn) == 0
                && countPiecesBy(secondColor, Knight) == 0
                && countPiecesBy(secondColor, Bishop) == 1
                && countPiecesBy(secondColor, Rook) == 0
                && countPiecesBy(secondColor, Queen) == 0
        if (!isKBvsKB) {
            return false
        }
        val firstBishopPos = pieceLists[firstColor.ordinal][Bishop.ordinal][0]
        val secondBishopPos = pieceLists[secondColor.ordinal][Bishop.ordinal][0]
        return isLightSquare(firstBishopPos) == isLightSquare(secondBishopPos)
    }

    fun copy() = Position(
            previousStates = LinkedList(this.previousStates),
            board = this.board.copyOf(),
            state = this.state
    )

    fun toFenState() =
            FenState(
                    board = Array(BOARD_SIZE) { i ->
                        Array(BOARD_SIZE) { j ->
                            this.board[i * BOARD_SIZE + j]
                        }
                    },
                    eliminatedColors = allColors.filter { color ->
                        this.state.eliminatedColors.isEliminated(color)
                    }.toSet(),
                    enPassantSquares = allColors.mapNotNull { color ->
                        this.state.enPassantSquares.getEnPassantSquareByColor(color)
                                .takeIf { squareIndex -> squareIndex != NULL_SQUARE }
                                ?.let { squareIndex -> color to Coordinates(squareFile(squareIndex), squareRank(squareIndex)) }
                    }.toMap(),
                    nextMoveColor = this.state.nextMoveColor,
                    castlingOptions = allColors.map { color ->
                        color to this.state.castlingOptions[color]
                    }.toMap(),
                    plyCount = this.state.plyCount
            )

    fun makeMove(move: MoveBits) {
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
        val move = state.lastMove
        if (move == NULL_MOVE) {
            return false
        }
        val from = move.from
        val to = move.to
        val promotionPieceType = move.promotionPieceType
        val prevState = previousStates.removeLast()
        val prevColor = prevState.nextMoveColor
        val capturedPiece = state.capturedPiece
        val toSquare = board[to]
        val movedPiece = if (promotionPieceType != null)
            Square.Occupied.by(prevColor, Pawn).piece
        else
            (toSquare as Square.Occupied).piece
        board[from] = Square.Occupied.by(movedPiece)
        pieceLists[prevColor.ordinal][movedPiece.type.ordinal].apply {
            add(from)
            remove(to)
        }
        if (promotionPieceType != null) {
            val pieceType = promotionPieceType.toPieceType()
            pieceLists[prevColor.ordinal][pieceType.ordinal].remove(to)
        }
        if (capturedPiece != null) {
            val isCaptureByEnPassant = prevState.enPassantSquares.getColorByEnPassantSquare(to) != null
                    && capturedPiece.type == Pawn
            if (isCaptureByEnPassant) {
                val capturedPawnSquareIndex = offsetSquareBy(to, capturedPiece.color.pawnForwardOffset)
                board[capturedPawnSquareIndex] = Square.Occupied.by(capturedPiece)
                board[to] = Square.Empty
                pieceLists[capturedPiece.color.ordinal][capturedPiece.type.ordinal].add(capturedPawnSquareIndex)
            } else {
                board[to] = Square.Occupied.by(capturedPiece)
                pieceLists[capturedPiece.color.ordinal][capturedPiece.type.ordinal].add(to)
            }
        } else {
            board[to] = Square.Empty
        }
        if (movedPiece.type == King) {
            val castling = when (to) {
                offsetSquareBy(from, 2 * prevColor.kingSideOffset) -> KingSide
                offsetSquareBy(from, 2 * prevColor.queenSideOffset) -> QueenSide
                else -> null
            }
            if (castling != null) {
                val squareIndexBeforeCastling = rookSquareBeforeCastling[prevColor.ordinal][castling.ordinal]
                val squareIndexAfterCastling = rookSquareAfterCastling[prevColor.ordinal][castling.ordinal]
                board[squareIndexAfterCastling] = Square.Empty
                board[squareIndexBeforeCastling] = Square.Occupied.by(prevColor, Rook)
                pieceLists[prevColor.ordinal][Rook.ordinal].apply {
                    remove(squareIndexAfterCastling)
                    add(squareIndexBeforeCastling)
                }
            }
        }
        state = prevState
        checkAttackVectors()
        generateLegalMoves()
        return true
    }

    private fun assertState() {
        try {
            allColors.forEach { color ->
                assert(countPiecesBy(color, Pawn) <= 8) { "Too many pawns for color $color" }
                assert(countPiecesBy(color, King) <= 1) { "Too many kings for color $color" }
            }
            allColors.forEach { color ->
                allPieceTypes.forEach { pieceType ->
                    pieceLists[color.ordinal][pieceType.ordinal].forEach {
                        val isOccupied = board[it] is Square.Occupied
                        assert(isOccupied) { "$color $pieceType not on the board, square index: $it" }
                        true
                    }
                }
            }
        } catch (e: Throwable) {
            val fen = toFenState().toFen()
            throw IllegalStateException("Illegal internal state detected, last move: ${state.lastMove}, FEN:\n$fen", e)
        }
    }

    fun makeResignation(color: Color) {
        val pseudoState = state.copy(
                eliminatedColors = state.eliminatedColors.withColorEliminated(color),
                nextMoveColor = if (state.nextMoveColor == color) getNewNextMoveColor() else state.nextMoveColor,
                enPassantSquares = state.enPassantSquares.dropEnPassantSquareForColor(color),
                hash = getNewHash(eliminatedColor = color),
                lastMove = NULL_MOVE,
                capturedPiece = null
        )
        val prevState = state
        state = pseudoState
        findLegalState()
        previousStates.add(prevState)
    }

    private tailrec fun findLegalState() {
        checkAttackVectors()
        generateLegalMoves()
        if (legalMoves.isEmpty) {
            val color = state.nextMoveColor
            val isCheck = checks[color.ordinal].size() > 0
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

    private fun updateBoardAndPieceLists(move: MoveBits) {
        val color = state.nextMoveColor
        val from = move.from
        val to = move.to
        val promotionPieceType = move.promotionPieceType

        // query before board gets updated
        val fromSquare = board[from]
        val toSquare = board[to]
        val movedPiece = (fromSquare as Square.Occupied).piece
        val castling = getCastling(move)
        val isCaptureByEnPassant = isCaptureByEnPassant(move)

        board[from] = Square.Empty
        board[to] = fromSquare
        pieceLists[color.ordinal][movedPiece.type.ordinal].apply {
            remove(from)
            add(to)
        }
        if (toSquare is Square.Occupied) {
            val capturedPiece = toSquare.piece
            pieceLists[capturedPiece.color.ordinal][capturedPiece.type.ordinal].apply {
                remove(to)
            }
        }
        if (castling != null) {
            val rookSquareIndexBeforeCastling = rookSquareBeforeCastling[color.ordinal][castling.ordinal]
            val rookSquareIndexAfterCastling = rookSquareAfterCastling[color.ordinal][castling.ordinal]
            board[rookSquareIndexBeforeCastling] = Square.Empty
            board[rookSquareIndexAfterCastling] = Square.Occupied.by(color, Rook)
            pieceLists[color.ordinal][Rook.ordinal].apply {
                remove(rookSquareIndexBeforeCastling)
                add(rookSquareIndexAfterCastling)
            }
        }
        if (isCaptureByEnPassant) {
            val capturedPawnColor = state.enPassantSquares.getColorByEnPassantSquare(to)!!
            val capturedPawnSquareIndex = offsetSquareBy(to, capturedPawnColor.pawnForwardOffset)
            board[capturedPawnSquareIndex] = Square.Empty
            pieceLists[capturedPawnColor.ordinal][Pawn.ordinal].remove(capturedPawnSquareIndex)
        }
        if (promotionPieceType != null) {
            val newPieceType = promotionPieceType.toPieceType()
            val newSquare = Square.Occupied.by(color, newPieceType)
            board[to] = newSquare
            pieceLists[color.ordinal][Pawn.ordinal].remove(to)
            pieceLists[color.ordinal][newPieceType.ordinal].add(to)
        }
    }

    private fun getCastling(move: MoveBits): Castling? {
        val from = move.from
        val to = move.to
        val square = board[from]
        val movedPieceType = (square as Square.Occupied).piece.type
        val movedPieceColor = state.nextMoveColor
        if (movedPieceType == King && from == movedPieceColor.defaultKingSquare) {
            if (to == kingSquareAfterCastling[movedPieceColor.ordinal][KingSide.ordinal]) {
                return KingSide
            }
            if (to == kingSquareAfterCastling[movedPieceColor.ordinal][QueenSide.ordinal]) {
                return QueenSide
            }
        }
        return null
    }

    private fun isCaptureByEnPassant(move: MoveBits): Boolean {
        val srcSquare = board[move.from]
        val movedPieceType = (srcSquare as Square.Occupied).piece.type
        return movedPieceType == Pawn && state.enPassantSquares.getColorByEnPassantSquare(move.to) != null
    }

    private fun getCapturedPiece(move: MoveBits): Piece? {
        val from = move.from
        val to = move.to
        val toSquare = board[to]
        if (toSquare is Square.Occupied) {
            return toSquare.piece
        }
        val fromSquare = board[from]
        val movedPieceType = (fromSquare as Square.Occupied).piece.type
        if (movedPieceType == Pawn) {
            val enPassantSquareColor = state.enPassantSquares.getColorByEnPassantSquare(to)
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
        val newColorIndex = (currentColor.ordinal + 1) % allColors.size
        val newColor = allColors[newColorIndex]
        if (!state.eliminatedColors.isEliminated(newColor)) {
            return newColor
        }
        return newNextMoveColor(currentColor = newColor)
    }

    private fun getNewEnPassantSquares(move: MoveBits): EnPassantSquaresBits {
        val from = move.from
        val to = move.to
        return allColors.fold(initialEnPassantSquares()) { newSqrs, color ->
            if (state.nextMoveColor == color) {
                val square = board[from] as Square.Occupied
                if (square.piece.type == Pawn && offsetSquareBy(from, 2 * color.pawnForwardOffset) == to) {
                    val enPassantSquareIndex = offsetSquareBy(from, color.pawnForwardOffset)
                    newSqrs.withEnPassantSquareForColor(color, enPassantSquareIndex)
                } else {
                    newSqrs
                }
            } else {
                val squareIndex = state.enPassantSquares.getEnPassantSquareByColor(color)
                if (squareIndex != NULL_SQUARE
                        && to != squareIndex
                        && offsetSquareBy(squareIndex, color.pawnForwardOffset) != to) {
                    newSqrs.withEnPassantSquareForColor(color, squareIndex)
                } else {
                    newSqrs
                }
            }
        }
    }

    private fun getNewCastlingOptions(move: MoveBits): CastlingOptionsBits {
        val from = move.from
        val to = move.to
        val fromSquare = board[from] as Square.Occupied
        val toSquare = board[to]
        val color = state.nextMoveColor
        var castlingRights = state.castlingOptions
        if (fromSquare.piece.type == King) {
            castlingRights = castlingRights
                    .dropCastlingForColor(color, KingSide)
                    .dropCastlingForColor(color, QueenSide)
        } else if (fromSquare.piece.type == Rook) {
            if (from == rookSquareBeforeCastling[color.ordinal][KingSide.ordinal]) {
                castlingRights = castlingRights.dropCastlingForColor(color, KingSide)
            } else if (from == rookSquareBeforeCastling[color.ordinal][QueenSide.ordinal]) {
                castlingRights = castlingRights.dropCastlingForColor(color, QueenSide)
            }
        }
        if (toSquare is Square.Occupied && toSquare.piece.type == Rook) {
            val capturedColor = toSquare.piece.color
            if (to == rookSquareBeforeCastling[capturedColor.ordinal][KingSide.ordinal]) {
                castlingRights = castlingRights.dropCastlingForColor(capturedColor, KingSide)
            } else if (to == rookSquareBeforeCastling[capturedColor.ordinal][QueenSide.ordinal]) {
                castlingRights = castlingRights.dropCastlingForColor(capturedColor, QueenSide)
            }
        }
        return castlingRights
    }

    private fun getNewPlyCount(move: MoveBits): Int {
        if (isRegularCapture(move) || isPawnAdvance(move)) {
            return 0
        }
        return state.plyCount + 1
    }

    private fun isRegularCapture(move: MoveBits): Boolean {
        val targetSquare = board[move.to]
        return targetSquare is Square.Occupied
                && targetSquare.piece.color != state.nextMoveColor
    }

    private fun isPawnAdvance(move: MoveBits): Boolean {
        val srcSquare = board[move.from]
        val movedPieceType = (srcSquare as Square.Occupied).piece.type
        return movedPieceType == Pawn
    }

    private fun getNewHash(move: MoveBits): Long {
        val from = move.from
        val to = move.to
        val promotionPieceType = move.promotionPieceType
        var hash = state.hash
        val moveColor = state.nextMoveColor
        val fromSquare = board[from]
        val movedPiece = (fromSquare as Square.Occupied).piece
        // piece-square
        hash = hash xor zobrist.getPieceSquareVal(movedPiece, from)
        val toSquare = board[to]
        if (toSquare is Square.Occupied) {
            val capturedPiece = toSquare.piece
            hash = hash xor zobrist.getPieceSquareVal(capturedPiece, to)
        }
        hash = if (promotionPieceType != null) {
            val pieceType = promotionPieceType.toPieceType()
            val pieceAfterPromotion = Square.Occupied.by(moveColor, pieceType).piece
            hash xor zobrist.getPieceSquareVal(pieceAfterPromotion, to)
        } else {
            hash xor zobrist.getPieceSquareVal(movedPiece, to)
        }
        if (isCaptureByEnPassant(move)) {
            val capturedPawnColor = state.enPassantSquares.getColorByEnPassantSquare(to)!!
            val capturedPawnSquareIndex = offsetSquareBy(to, capturedPawnColor.pawnForwardOffset)
            val piece = Square.Occupied.by(capturedPawnColor, Pawn).piece
            hash = hash xor zobrist.getPieceSquareVal(piece, capturedPawnSquareIndex)
        }
        val castling = getCastling(move)
        if (castling != null) {
            val oldRookSquareIndex = rookSquareBeforeCastling[moveColor.ordinal][castling.ordinal]
            val newRookCoords = rookSquareAfterCastling[moveColor.ordinal][castling.ordinal]
            val piece = Square.Occupied.by(moveColor, Rook).piece
            hash = hash xor zobrist.getPieceSquareVal(piece, oldRookSquareIndex)
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
            movedPiece.type == Rook && from == rookSquareBeforeCastling[moveColor.ordinal][KingSide.ordinal] ->
                state.castlingOptions.dropCastlingForColor(moveColor, KingSide)[moveColor]
            movedPiece.type == Rook && from == rookSquareBeforeCastling[moveColor.ordinal][QueenSide.ordinal] ->
                state.castlingOptions.dropCastlingForColor(moveColor, QueenSide)[moveColor]
            else -> oldCastlingOptions
        }
        if (oldCastlingOptions != newCastlingOptions) {
            hash = hash xor zobrist.getCastlingOptionsValue(moveColor, oldCastlingOptions)
            hash = hash xor zobrist.getCastlingOptionsValue(moveColor, newCastlingOptions)
        }
        // en passant squares
        allColors.forEach { color ->
            if (moveColor == color) {
                val enPassantSquareIndex = state.enPassantSquares.getEnPassantSquareByColor(color)
                if (enPassantSquareIndex != NULL_SQUARE) {
                    hash = hash xor zobrist.getEnPassantVal(color, enPassantSquareIndex)
                }
                if (movedPiece.type == Pawn && offsetSquareBy(from, 2 * color.pawnForwardOffset) == to) {
                    val newEnPassantSquareIndex = offsetSquareBy(from, color.pawnForwardOffset)
                    hash = hash xor zobrist.getEnPassantVal(color, newEnPassantSquareIndex)
                }
            } else {
                val enPassantSquareIndex = state.enPassantSquares.getEnPassantSquareByColor(color)
                if (enPassantSquareIndex != NULL_SQUARE) {
                    val pawnSquare = offsetSquareBy(enPassantSquareIndex, color.pawnForwardOffset)
                    if (to == enPassantSquareIndex || pawnSquare == to) {
                        hash = hash xor zobrist.getEnPassantVal(color, enPassantSquareIndex)
                    }
                }
            }
        }
        return hash
    }

    private fun getNewHash(eliminatedColor: Color): Long {
        var hash = state.hash

        hash = hash xor zobrist.getEliminatedColorValue(eliminatedColor)

        val enPassantSquareIndex = state.enPassantSquares.getEnPassantSquareByColor(eliminatedColor)
        if (enPassantSquareIndex != NULL_SQUARE) {
            hash = hash xor zobrist.getEnPassantVal(eliminatedColor, enPassantSquareIndex)
        }

        val newColor = newNextMoveColor(state.nextMoveColor)
        if (newColor != state.nextMoveColor) {
            hash = hash xor zobrist.getNextMoveColorVal(state.nextMoveColor)
            hash = hash xor zobrist.getNextMoveColorVal(newColor)
        }
        return hash
    }

    private fun checkAttackVectors() {
        for (i in allColors.indices) {
            for (j in 0 until BOARD_SIZE * BOARD_SIZE) {
                attackedSquares[i][j] = false
            }
            scannedSquaresBehindKing[i].clear()
            pins[i].clear()
            checks[i].clear()
        }

        allColors.forEach { color ->
            if (!state.eliminatedColors.isEliminated(color)) {

                pieceLists[color.ordinal][Pawn.ordinal].forEachDo { squareIndex ->
                    pawnAttackOffsets[color.ordinal].forEach { offset ->
                        checkAttackVector(squareIndex, color, offset)
                    }
                }

                pieceLists[color.ordinal][Knight.ordinal].forEachDo { squareIndex ->
                    knightMoveOffsets.forEach { offset ->
                        checkAttackVector(squareIndex, color, offset)
                    }
                }

                pieceLists[color.ordinal][King.ordinal].forEachDo { squareIndex ->
                    allDirectionsOffsets.forEach { offset ->
                        checkAttackVector(squareIndex, color, offset)
                    }
                }

                pieceLists[color.ordinal][Bishop.ordinal].forEachDo { squareIndex ->
                    bishopMoveOffsets.forEach { offset ->
                        checkSlidingAttackVector(squareIndex, color, offset)
                    }
                }

                pieceLists[color.ordinal][Rook.ordinal].forEachDo { squareIndex ->
                    rookMoveOffsets.forEach { offset ->
                        checkSlidingAttackVector(squareIndex, color, offset)
                    }
                }

                pieceLists[color.ordinal][Queen.ordinal].forEachDo { squareIndex ->
                    allDirectionsOffsets.forEach { offset ->
                        checkSlidingAttackVector(squareIndex, color, offset)
                    }
                }
            }
        }
    }

    private fun checkAttackVector(attackingSquareIndex: Int,
                                  color: Color,
                                  attackOffset: Int) {
        val attackedSquareIndex = offsetSquareBy(attackingSquareIndex, attackOffset)
        if (attackedSquareIndex != NULL_SQUARE) {
            attackedSquares[color.ordinal][attackedSquareIndex] = true
            val square = board[attackedSquareIndex]
            if (square is Square.Occupied
                    && square.piece.type == King
                    && square.piece.color != color
                    && !state.eliminatedColors.isEliminated(square.piece.color)) {
                val check = checkOf(
                        checkingPieceSquareIndex = attackingSquareIndex,
                        checkedPieceSquareIndex = attackedSquareIndex

                )
                checks[square.piece.color.ordinal].add(check)
            }
        }
    }

    private fun checkSlidingAttackVector(attackingSquareIndex: Int, color: Color, attackOffset: Int) {
        var squareIndex = attackingSquareIndex
        while (true) {
            squareIndex = offsetSquareBy(squareIndex, attackOffset)
            if (squareIndex == NULL_SQUARE)
                break
            attackedSquares[color.ordinal][squareIndex] = true
            val attackedSquare = board[squareIndex]
            if (attackedSquare is Square.Occupied) {
                if (attackedSquare.piece.color != color && !state.eliminatedColors.isEliminated(attackedSquare.piece.color)) {
                    if (attackedSquare.piece.type == King) {
                        val check = checkOf(
                                checkingPieceSquareIndex = attackingSquareIndex,
                                checkedPieceSquareIndex = squareIndex
                        )
                        checks[attackedSquare.piece.color.ordinal].add(check)
                        while (true) {
                            squareIndex = offsetSquareBy(squareIndex, attackOffset)
                            if (squareIndex == NULL_SQUARE) {
                                break
                            }
                            scannedSquaresBehindKing[attackedSquare.piece.color.ordinal].add(squareIndex)
                            val scannedSquare = board[squareIndex]
                            if (scannedSquare is Square.Occupied) {
                                break
                            }
                        }
                    } else {
                        val attackedSquareIndex = squareIndex
                        while (true) {
                            squareIndex = offsetSquareBy(squareIndex, attackOffset)
                            if (squareIndex == NULL_SQUARE) {
                                break
                            }
                            val scannedSquare = board[squareIndex]
                            if (scannedSquare is Square.Occupied) {
                                if (scannedSquare.piece.type == King && scannedSquare.piece.color == attackedSquare.piece.color) {
                                    val pin = pinOf(
                                            pinningPieceSquareIndex = attackingSquareIndex,
                                            pinnedPieceSquareIndex = attackedSquareIndex
                                    )
                                    pins[attackedSquare.piece.color.ordinal].add(pin)
                                }
                                break
                            }
                        }
                    }
                }
                break
            }
        }
    }

    private fun generateLegalMoves() {
        legalMoves.clear()

        pieceLists[state.nextMoveColor.ordinal][Pawn.ordinal].forEachDo { squareIndex ->
            generatePawnMoves(squareIndex)
        }

        pieceLists[state.nextMoveColor.ordinal][Knight.ordinal].forEachDo { squareIndex ->
            generateKnightMoves(squareIndex)
        }

        pieceLists[state.nextMoveColor.ordinal][Bishop.ordinal].forEachDo { squareIndex ->
            generateBishopMoves(squareIndex)
        }

        pieceLists[state.nextMoveColor.ordinal][Rook.ordinal].forEachDo { squareIndex ->
            generateRookMoves(squareIndex)
        }

        pieceLists[state.nextMoveColor.ordinal][Queen.ordinal].forEachDo { squareIndex ->
            generateQueenMoves(squareIndex)
        }

        pieceLists[state.nextMoveColor.ordinal][King.ordinal].forEachDo { squareIndex ->
            generateKingMoves(squareIndex)
        }
    }

    private fun generateMove(from: Int, to: Int) {
        val toSquare = board[to]
        if (toSquare is Square.Empty
                || (toSquare is Square.Occupied && toSquare.piece.color != state.nextMoveColor)) {
            addMoveIfLegal(from, to)
        }
    }

    private fun addMoveIfLegal(from: Int, to: Int) {
        if (isLegalMove(from, to)) {
            val move = moveOf(from, to)
            legalMoves.add(move)
        }
    }

    private fun generatePawnMoves(squareIndex: Int) {
        val pawnColor = state.nextMoveColor
        val forwardOffset = pawnColor.pawnForwardOffset
        val oneSquareForwardIndex = offsetSquareBy(squareIndex, forwardOffset)
        if (oneSquareForwardIndex != NULL_SQUARE) {
            val oneSquareForward = board[oneSquareForwardIndex]
            if (oneSquareForward is Square.Empty) {
                generatePawnMoves(from = squareIndex, to = oneSquareForwardIndex)
                if (isSquareOnStartingPawnRowForColor(squareIndex, pawnColor)) {
                    val twoSquaresForwardIndex = offsetSquareBy(oneSquareForwardIndex, forwardOffset)
                    if (twoSquaresForwardIndex != NULL_SQUARE) {
                        val twoSquaresForward = board[twoSquaresForwardIndex]
                        if (twoSquaresForward is Square.Empty) {
                            addMoveIfLegal(
                                    from = squareIndex,
                                    to = twoSquaresForwardIndex
                            )
                        }
                    }
                }
            }
        }
        pawnAttackOffsets[pawnColor.ordinal].forEach { offset ->
            val attackedSquareIndex = offsetSquareBy(squareIndex, offset)
            if (attackedSquareIndex != NULL_SQUARE) {
                val attackedSquare = board[attackedSquareIndex]
                if (attackedSquare is Square.Occupied && attackedSquare.piece.color != pawnColor) {
                    generatePawnMoves(from = squareIndex, to = attackedSquareIndex)
                } else {
                    val isCaptureByEnPassant = state.enPassantSquares.getColorByEnPassantSquare(attackedSquareIndex)
                            ?.let { it != pawnColor }
                            ?: false
                    if (isCaptureByEnPassant) {
                        generatePawnMoves(from = squareIndex, to = attackedSquareIndex)
                    }
                }
            }
        }
    }

    private fun generatePawnMoves(from: Int, to: Int) {
        if (isLegalMove(from, to)) {
            val isPromotion = isPromotionSquareForColor(to, state.nextMoveColor)
            if (isPromotion) {
                PromotionPieceType.values().forEach { promotionPieceType ->
                    val move = moveOf(from, to, promotionPieceType)
                    legalMoves.add(move)
                }
            } else {
                val move = moveOf(from, to)
                legalMoves.add(move)
            }
        }
    }

    private fun generateKnightMoves(squareIndex: Int) {
        knightMoveOffsets.forEach { offset ->
            val destinationSquareIndex = offsetSquareBy(squareIndex, offset)
            if (destinationSquareIndex != NULL_SQUARE) {
                generateMove(from = squareIndex, to = destinationSquareIndex)
            }
        }
    }

    private fun generateBishopMoves(squareIndex: Int) {
        bishopMoveOffsets.forEach { offset ->
            generateSlidingMoves(squareIndex, offset)
        }
    }

    private fun generateRookMoves(squareIndex: Int) {
        rookMoveOffsets.forEach { offset ->
            generateSlidingMoves(squareIndex, offset)
        }
    }

    private fun generateQueenMoves(squareIndex: Int) {
        allDirectionsOffsets.forEach { offset ->
            generateSlidingMoves(squareIndex, offset)
        }
    }

    private fun generateSlidingMoves(squareIndex: Int, offset: Int) {
        var currSquareIndex = squareIndex
        while (true) {
            currSquareIndex = offsetSquareBy(currSquareIndex, offset)
            if (currSquareIndex == NULL_SQUARE) {
                break
            }
            val square = board[currSquareIndex]
            if (square is Square.Empty) {
                addMoveIfLegal(from = squareIndex, to = currSquareIndex)
            }
            if (square is Square.Occupied) {
                if (square.piece.color != state.nextMoveColor) {
                    addMoveIfLegal(from = squareIndex, to = currSquareIndex)
                }
                break
            }
        }
    }

    private fun addKingMoveIfLegal(from: Int,
                                   to: Int,
                                   castling: Castling? = null) {
        if (isLegalKingMove(from, to, castling)) {
            val move = moveOf(from, to)
            legalMoves.add(move)
        }
    }

    private fun isLegalKingMove(from: Int,
                                to: Int,
                                castling: Castling?): Boolean {
        val color = state.nextMoveColor
        val isCheck = checks[color.ordinal].size() > 0
        val isDestinationAttacked = isSquareAttackedByOtherColors(color, to) || (isCheck && this.scannedSquaresBehindKing[color.ordinal].contains(to))
        if (isDestinationAttacked) {
            return false
        }
        if (castling != null) {
            if (isCheck) {
                return false
            }
            if (castling == KingSide) {
                val newRookSquare = rookSquareAfterCastling[color.ordinal][KingSide.ordinal]
                val isNewRookSquareAttacked = isSquareAttackedByOtherColors(color, newRookSquare)
                if (isNewRookSquareAttacked) {
                    return false
                }
            }
            if (castling == QueenSide) {
                val newRookSquare = rookSquareAfterCastling[color.ordinal][QueenSide.ordinal]
                val isNewRookSquareAttacked = isSquareAttackedByOtherColors(color, newRookSquare)
                if (isNewRookSquareAttacked) {
                    return false
                }
            }
        }
        return true
    }

    private fun isSquareAttackedByOtherColors(color: Color, index: Int) =
            allColors.any { it != color && attackedSquares[it.ordinal][index] }

    private fun isLegalMove(from: Int, to: Int): Boolean {
        val color = state.nextMoveColor

        val pinsAgainstColor = pins[color.ordinal]
        val isMovePlacingKingInCheck = pinsAgainstColor.any { pin ->
            val pinnedSquareIndex = pin.pinnedPieceSquareIndex
            val pinningSquareIndex = pin.pinningPieceSquareIndex
            from == pinnedSquareIndex
                    && !isSquareOnLineBetween(to, pinnedSquareIndex, pinningSquareIndex)
                    && to != pinningSquareIndex
        }
        if (isMovePlacingKingInCheck) {
            return false
        }

        val checksAgainstColor = checks[color.ordinal]
        if (checksAgainstColor.size() > 1) {
            return false
        }
        if (checksAgainstColor.size() > 0) {
            val check = checksAgainstColor[0]
            val checkingSquareIndex = check.checkingPieceSquareIndex
            val checkedSquareIndex = check.checkedPieceSquareIndex
            val isPossibleToBlock = when {
                (board[checkedSquareIndex] as Square.Occupied).piece.type == Knight -> false
                areSquaresAdjacent(checkingSquareIndex, checkedSquareIndex) -> false
                else -> true
            }
            val isBlockingMove = isPossibleToBlock && isSquareOnLineBetween(to, checkedSquareIndex, checkingSquareIndex)
            val isCapturingCheckingPieceMove = to == checkingSquareIndex
            if (!isCapturingCheckingPieceMove && !isBlockingMove) {
                return false
            }
        }

        val toSquare = board[to]
        val isOccupiedByNotEliminatedKing = toSquare is Square.Occupied
                && toSquare.piece.type == King
                && !state.eliminatedColors.isEliminated(toSquare.piece.color)
        // King must not be captured via discovered attack, each player should be allowed to respond to check
        if (isOccupiedByNotEliminatedKing) {
            return false
        }

        return true
    }

    private fun generateKingMoves(squareIndex: Int) {
        val color = state.nextMoveColor
        val castlingOptions = state.castlingOptions[color]
        allCastlings.forEach { castling ->
            if (castling in castlingOptions) {
                val offset = castlingSideOffsets[color.ordinal][castling.ordinal]
                val newRookSquareIndex = offsetSquareBy(squareIndex, offset)
                val newKingSquareIndex = offsetSquareBy(newRookSquareIndex, offset)
                if (board[newRookSquareIndex] == Square.Empty && board[newKingSquareIndex] == Square.Empty) {
                    addKingMoveIfLegal(from = squareIndex, to = newKingSquareIndex, castling = castling)
                }
            }
        }
        allDirectionsOffsets.forEach { offset ->
            val targetSquareIndex = offsetSquareBy(squareIndex, offset)
            if (targetSquareIndex != NULL_SQUARE) {
                val square = board[targetSquareIndex]
                if (square == Square.Empty
                        || (square is Square.Occupied && square.piece.color != state.nextMoveColor)) {
                    addKingMoveIfLegal(from = squareIndex, to = targetSquareIndex)
                }
            }
        }
    }
}