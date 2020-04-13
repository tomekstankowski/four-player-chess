package pl.tomaszstankowski.fourplayerchess

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.lexer.TokenMatch
import com.github.h0tk3y.betterParse.parser.ErrorResult
import com.github.h0tk3y.betterParse.parser.ParseResult
import com.github.h0tk3y.betterParse.parser.Parsed
import com.github.h0tk3y.betterParse.parser.Parser
import pl.tomaszstankowski.fourplayerchess.PieceType.King


data class IllegalRowLength(val length: Int) : ErrorResult()

data class IllegalKingCount(val color: Color, val count: Int) : ErrorResult()

data class PiecesWithoutKing(val color: Color) : ErrorResult()

private sealed class RowToken {
    data class EmptySquaresToken(val count: Int) : RowToken()
    data class OccupiedSquareToken(val square: Square.Occupied) : RowToken()
}

internal object FenGrammar : Grammar<State>() {
    private val upperB by token("B")
    private val upperG by token("G")
    private val upperK by token("K")
    private val upperN by token("N")
    private val upperP by token("P")
    private val upperQ by token("Q")
    private val upperR by token("R")
    private val upperY by token("Y")
    private val a by token("a")
    private val b by token("b")
    private val c by token("c")
    private val d by token("d")
    private val e by token("e")
    private val f by token("f")
    private val g by token("g")
    private val h by token("h")
    private val i by token("i")
    private val j by token("j")
    private val k by token("k")
    private val l by token("l")
    private val m by token("m")
    private val n by token("n")
    private val r by token("r")
    private val y by token("y")
    private val nine by token("9")
    private val eight by token("8")
    private val seven by token("7")
    private val six by token("6")
    private val five by token("5")
    private val four by token("4")
    private val three by token("3")
    private val two by token("2")
    private val one by token("1")
    private val zero by token("0")
    private val comma by token(",")
    private val slash by token("/")
    private val hyphen by token("-")
    private val zeroOrMoreWhiteSpace by token("\\s*")

    private val skipWhiteSpaceOpt = skip(optional(zeroOrMoreWhiteSpace))

    private val pDigitExceptZero = nine or eight or seven or six or five or four or three or two or one
    private val pDigit = pDigitExceptZero or zero
    private val pNumber = zero asJust 0 or (oneOrMore(pDigit) map { tokenMatches ->
        tokenMatches.joinToString(separator = "") { it.text }.toInt()
    })
    private val pRed = upperR asJust Color.Red
    private val pGreen = upperG asJust Color.Green
    private val pBlue = upperB asJust Color.Blue
    private val pYellow = upperY asJust Color.Yellow
    private val pColor = pRed or pGreen or pBlue or pYellow

    private val pFile = a or b or c or d or e or f or g or h or i or j or k or l or m or n map { it.text.toCharArray()[0] - 'a' }

    private val pRank = ((one and four) or
            (one and three) or
            (one and two) or
            (one and one) or
            (one and zero) map { (it.t1.text + it.t2.text).toInt() }) or (
            pDigitExceptZero map { it.text.toInt() }
            ) map { it.dec() }
    private val pPosition = pFile and pRank map { (file, rank) -> Position.ofFileAndRank(file, rank) }
    private val pEnPassantSquare: Parser<Position?> = pPosition or (zero asJust null)
    private val pEnPassantSquares: Parser<EnPassantSquares> = pEnPassantSquare and skip(comma) and
            pEnPassantSquare and skip(comma) and
            pEnPassantSquare and skip(comma) and
            pEnPassantSquare map { (redSquareOpt, greenSquareOpt, blueSquareOpt, yellowSquareOpt) ->
        listOfNotNull(
                redSquareOpt?.let { Color.Red to it },
                greenSquareOpt?.let { Color.Green to it },
                blueSquareOpt?.let { Color.Blue to it },
                yellowSquareOpt?.let { Color.Yellow to it }

        ).toMap()
    }

    private val pTrue = one asJust true
    private val pFalse = zero asJust false
    private val pFlag = pTrue or pFalse
    private val pComma = skip(comma)
    private val pFlags = pFlag and pComma and pFlag and pComma and pFlag and pComma and pFlag map { (isRed, isGreen, isBlue, isYellow) ->
        mapOf(
                Color.Red to isRed,
                Color.Green to isGreen,
                Color.Blue to isBlue,
                Color.Yellow to isYellow
        )
    }

    private val pPlyCount = pNumber map { num -> PlyCount.of(num) }

    private val pPawn = upperP asJust PieceType.Pawn
    private val pKnight = upperN asJust PieceType.Knight
    private val pBishop = upperB asJust PieceType.Bishop
    private val pRook = upperR asJust PieceType.Rook
    private val pQueen = upperQ asJust PieceType.Queen
    private val pKing = upperK asJust King
    private val pPieceType = pPawn or pKnight or pBishop or pRook or pQueen or pKing

    private val pLowerRed = r asJust Color.Red
    private val pLowerGreen = g asJust Color.Green
    private val pLowerBlue = b asJust Color.Blue
    private val pLowerYellow = y asJust Color.Yellow
    private val pLowerColor = pLowerRed or pLowerGreen or pLowerBlue or pLowerYellow
    private val pOccupiedSquareToken = pLowerColor and pPieceType map { (color, pieceType) ->
        RowToken.OccupiedSquareToken(square = Square.Occupied.by(color, pieceType))
    }
    private val pEmptySquaresToken = pRank map { RowToken.EmptySquaresToken(it.inc()) }
    private val pRowToken = pOccupiedSquareToken or pEmptySquaresToken
    private val pRowTokens = separatedTerms(
            term = pRowToken,
            separator = comma
    )
    private val pRowTokensAndCheckIfRowLengthIsValid = object : Parser<List<RowToken>> {

        override fun tryParse(tokens: Sequence<TokenMatch>): ParseResult<List<RowToken>> {
            when (val result = pRowTokens.tryParse(tokens)) {
                is Parsed -> {
                    val rowLength = result.value.sumBy { token ->
                        when (token) {
                            is RowToken.EmptySquaresToken -> token.count
                            is RowToken.OccupiedSquareToken -> 1
                        }
                    }
                    return if (rowLength == BOARD_SIZE) {
                        Parsed(result.value, result.remainder)
                    } else {
                        IllegalRowLength(rowLength)
                    }
                }
                is ErrorResult -> return result
            }
        }

    }
    private val pRow = pRowTokensAndCheckIfRowLengthIsValid map { tokens ->
        tokens.map { token ->
            when (token) {
                is RowToken.EmptySquaresToken -> List(size = token.count) { Square.Empty }
                is RowToken.OccupiedSquareToken -> listOf(element = token.square)
            }
        }
                .flatten()
    }
    private val pRowsUnchecked = BOARD_SIZE - 1 times (pRow and skip(slash) and skipWhiteSpaceOpt) and (
            pRow and skipWhiteSpaceOpt) map { (rows, row) -> rows.plus<Row>(row).reversed() }
    private val pBoard = object : Parser<Board> {
        override fun tryParse(tokens: Sequence<TokenMatch>): ParseResult<Board> {
            when (val result = pRowsUnchecked.tryParse(tokens)) {
                is Parsed -> {
                    val allSquares = result.value.flatten()
                    val kingCountsByColor = Color.values()
                            .map { color ->
                                val count = allSquares
                                        .filter { square -> square == Square.Occupied.by(color, King) }
                                        .size
                                color to count
                            }
                    val colorOfMoreThanOneKing = kingCountsByColor.find { (_, kingCount) -> kingCount > 1 }
                    if (colorOfMoreThanOneKing != null) {
                        return IllegalKingCount(
                                color = colorOfMoreThanOneKing.first,
                                count = colorOfMoreThanOneKing.second)
                    }
                    val colorOfPiecesWithoutKing = kingCountsByColor
                            .filter { (_, kingCount) -> kingCount == 0 }
                            .map { (color, _) -> color }
                            .find { color -> allSquares.count { square -> square is Square.Occupied && square.piece.color == color } > 0 }
                    if (colorOfPiecesWithoutKing != null) {
                        return PiecesWithoutKing(colorOfPiecesWithoutKing)
                    }
                    return Parsed(result.value, result.remainder)
                }
                is ErrorResult -> return result
            }
        }
    }
    private val pState = pColor and skip(hyphen) and
            pFlags and skip(hyphen) and
            pFlags and skip(hyphen) and
            pFlags and skip(hyphen) and
            pEnPassantSquares and skip(hyphen) and
            pPlyCount and skip(hyphen) and skipWhiteSpaceOpt and
            pBoard map { (nextMoveColor, eliminatedColorsFlags, kingSideCastlingFlags, queenSideCastlingFlags, enPassantSquares, plyCount, board) ->
        State(
                squares = board,
                nextMoveColor = nextMoveColor,
                plyCount = plyCount,
                colorToCastlingOptions = CastlingOptions(
                        Color.values().map { color ->
                            val kingSide = kingSideCastlingFlags[color]
                                    ?.takeIf { canCastle -> canCastle }
                                    ?.let { setOf(Castling.KingSide) }
                                    ?: emptySet()
                            val queenSide = queenSideCastlingFlags[color]
                                    ?.takeIf { canCastle -> canCastle }
                                    ?.let { setOf(Castling.QueenSide) }
                                    ?: emptySet()
                            color to (kingSide + queenSide)
                        }.toMap()
                ),
                enPassantSquares = enPassantSquares
        )
    }

    override val rootParser: Parser<State>
        get() = pState
}