package pl.tomaszstankowski.fourplayerchess

import org.amshove.kluent.shouldBeEqualTo
import org.spekframework.spek2.Spek
import pl.tomaszstankowski.fourplayerchess.engine.*
import pl.tomaszstankowski.fourplayerchess.engine.Castling.KingSide
import pl.tomaszstankowski.fourplayerchess.engine.Castling.QueenSide
import pl.tomaszstankowski.fourplayerchess.engine.Color.*
import pl.tomaszstankowski.fourplayerchess.engine.PieceType.*

class ParseStateFromFenTest : Spek({

    test("parses starting position") {
        val input = """
            R-0,0,0,0-1,1,1,1-1,1,1,1-0,0,0,0-0-
            3,yR,yN,yB,yK,yQ,yB,yN,yR,3/
            3,yP,yP,yP,yP,yP,yP,yP,yP,3/
            14/
            bR,bP,10,gP,gR/
            bN,bP,10,gP,gN/
            bB,bP,10,gP,gB/
            bK,bP,10,gP,gQ/
            bQ,bP,10,gP,gK/
            bB,bP,10,gP,gB/
            bN,bP,10,gP,gN/
            bR,bP,10,gP,gR/
            14/
            3,rP,rP,rP,rP,rP,rP,rP,rP,3/
            3,rR,rN,rB,rQ,rK,rB,rN,rR,3
        """.trimIndent()

        val result = State.parseFromFen(input)

        result shouldBeEqualTo ParseStateFromFenResult.Parsed(State.starting())
    }

    test("parses in game position") {
        val input = """
            B-0,0,0,0-0,1,1,1-0,1,1,1-0,0,0,g12-9-
            3,yR,yN,yB,yK,yQ,yB,1,yR,3/
            3,yP,yP,yP,1,yP,yP,yP,yP,3/
            8,yN,5/
            bR,bP,4,yP,5,gP,gR/
            bN,bP,9,gP,1,gN/
            bB,bP,10,gP,gB/
            bK,1,bP,9,gP,gQ/
            bQ,2,bP,8,gP,gK/
            bB,bP,9,gN,gP,gB/
            bN,bP,10,gP,1/
            bR,bP,10,gP,gR/
            7,rP,6/
            3,rP,rP,rP,rP,rB,rP,rP,rP,3/
            3,rR,rN,rB,rQ,1,rK,rN,rR,3
        """.trimIndent()

        val result = State.parseFromFen(input)

        result shouldBeEqualTo ParseStateFromFenResult.Parsed(
                State(
                        squares = listOf(
                                listOf(emptySquare(), emptySquare(), emptySquare(), squareOf(Red, Rook), squareOf(Red, Knight), squareOf(Red, Bishop), squareOf(Red, Queen), emptySquare(), squareOf(Red, King), squareOf(Red, Knight), squareOf(Red, Rook), emptySquare(), emptySquare(), emptySquare()),
                                listOf(emptySquare(), emptySquare(), emptySquare(), squareOf(Red, Pawn), squareOf(Red, Pawn), squareOf(Red, Pawn), squareOf(Red, Pawn), squareOf(Red, Bishop), squareOf(Red, Pawn), squareOf(Red, Pawn), squareOf(Red, Pawn), emptySquare(), emptySquare(), emptySquare()),
                                listOf(emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), squareOf(Red, Pawn), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare()),
                                listOf(squareOf(Blue, Rook), squareOf(Blue, Pawn), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), squareOf(Green, Pawn), squareOf(Green, Rook)),
                                listOf(squareOf(Blue, Knight), squareOf(Blue, Pawn), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), squareOf(Green, Pawn), emptySquare()),
                                listOf(squareOf(Blue, Bishop), squareOf(Blue, Pawn), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), squareOf(Green, Knight), squareOf(Green, Pawn), squareOf(Green, Bishop)),
                                listOf(squareOf(Blue, Queen), emptySquare(), emptySquare(), squareOf(Blue, Pawn), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), squareOf(Green, Pawn), squareOf(Green, King)),
                                listOf(squareOf(Blue, King), emptySquare(), squareOf(Blue, Pawn), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), squareOf(Green, Pawn), squareOf(Green, Queen)),
                                listOf(squareOf(Blue, Bishop), squareOf(Blue, Pawn), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), squareOf(Green, Pawn), squareOf(Green, Bishop)),
                                listOf(squareOf(Blue, Knight), squareOf(Blue, Pawn), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), squareOf(Green, Pawn), emptySquare(), squareOf(Green, Knight)),
                                listOf(squareOf(Blue, Rook), squareOf(Blue, Pawn), emptySquare(), emptySquare(), emptySquare(), emptySquare(), squareOf(Yellow, Pawn), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), squareOf(Green, Pawn), squareOf(Green, Rook)),
                                listOf(emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare(), squareOf(Yellow, Knight), emptySquare(), emptySquare(), emptySquare(), emptySquare(), emptySquare()),
                                listOf(emptySquare(), emptySquare(), emptySquare(), squareOf(Yellow, Pawn), squareOf(Yellow, Pawn), squareOf(Yellow, Pawn), emptySquare(), squareOf(Yellow, Pawn), squareOf(Yellow, Pawn), squareOf(Yellow, Pawn), squareOf(Yellow, Pawn), emptySquare(), emptySquare(), emptySquare()),
                                listOf(emptySquare(), emptySquare(), emptySquare(), squareOf(Yellow, Rook), squareOf(Yellow, Knight), squareOf(Yellow, Bishop), squareOf(Yellow, King), squareOf(Yellow, Queen), squareOf(Yellow, Bishop), emptySquare(), squareOf(Yellow, Rook), emptySquare(), emptySquare(), emptySquare())
                        ),
                        eliminatedColors = emptySet(),
                        nextMoveColor = Blue,
                        enPassantSquares = mapOf(
                                Yellow to Position.ofFileAndRank(6, 11)
                        ),
                        castlingOptions = CastlingOptions(
                                mapOf(
                                        Red to emptySet(),
                                        Green to setOf(KingSide, QueenSide),
                                        Blue to setOf(KingSide, QueenSide),
                                        Yellow to setOf(KingSide, QueenSide)
                                )
                        ),
                        plyCount = PlyCount.of(9)
                )
        )
    }

    test("returns error given row with too many squares") {
        val input = """
            R-0,0,0,0-1,1,1,1-1,1,1,1-0,0,0,0-0-
            3,yR,yN,yB,yK,yQ,yB,yN,yR,3/
            3,yP,yP,yP,yP,yP,yP,yP,yP,3/
            14/
            bR,bP,11,gP,gR/
            bN,bP,10,gP,gN/
            bB,bP,10,gP,gB/
            bK,bP,10,gP,gQ/
            bQ,bP,10,gP,gK/
            bB,bP,10,gP,gB/
            bN,bP,10,gP,gN/
            bR,bP,10,gP,gR/
            14/
            3,rP,rP,rP,rP,rP,rP,rP,rP,3/
            3,rR,rN,rB,rQ,rK,rB,rN,rR,3
        """.trimIndent()

        val result = State.parseFromFen(input)

        result shouldBeEqualTo ParseStateFromFenResult.IllegalState.IllegalRowLength(15)
    }

    test("returns error given more than one king of given color") {
        val input = """
            R-0,0,0,0-1,1,1,1-1,1,1,1-0,0,0,0-0-
            3,yR,yN,yB,yK,yQ,yB,yN,yR,3/
            3,yP,yP,yP,yP,yP,yP,yP,yP,3/
            14/
            bR,bP,yK,9,gP,gR/
            bN,bP,10,gP,gN/
            bB,bP,10,gP,gB/
            bK,bP,10,gP,gQ/
            bQ,bP,10,gP,gK/
            bB,bP,10,gP,gB/
            bN,bP,10,gP,gN/
            bR,bP,10,gP,gR/
            14/
            3,rP,rP,rP,rP,rP,rP,rP,rP,3/
            3,rR,rN,rB,rQ,rK,rB,rN,rR,3
        """.trimIndent()

        val result = State.parseFromFen(input)

        result shouldBeEqualTo ParseStateFromFenResult.IllegalState.IllegalKingCount(
                color = Yellow,
                count = 2
        )
    }

    test("returns error when pieces have no king") {
        val input = """
            R-0,0,0,0-1,1,1,1-1,1,1,1-0,0,0,0-0-
            3,yR,yN,yB,1,yQ,yB,yN,yR,3/
            3,yP,yP,yP,yP,yP,yP,yP,yP,3/
            14/
            bR,bP,10,gP,gR/
            bN,bP,10,gP,gN/
            bB,bP,10,gP,gB/
            bK,bP,10,gP,gQ/
            bQ,bP,10,gP,gK/
            bB,bP,10,gP,gB/
            bN,bP,10,gP,gN/
            bR,bP,10,gP,gR/
            14/
            3,rP,rP,rP,rP,rP,rP,rP,rP,3/
            3,rR,rN,rB,rQ,rK,rB,rN,rR,3
        """.trimIndent()

        val result = State.parseFromFen(input)

        result shouldBeEqualTo ParseStateFromFenResult.IllegalState.PiecesWithoutKing(Yellow)
    }

})