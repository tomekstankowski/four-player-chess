package pl.tomaszstankowski.fourplayerchess

import org.amshove.kluent.shouldBeEqualTo
import org.spekframework.spek2.Spek
import pl.tomaszstankowski.fourplayerchess.Color.Yellow

class MoveMakeTest : Spek({

    test("move to an empty square") {
        val engine = createEngineWithStateFromFen("""
            R-0,0,0,0-1,1,1,1-1,1,1,1-0,0,0,0-3-
            3,yR,yN,yB,yK,yQ,yB,1,yR,3/
            3,yP,yP,yP,yP,yP,yP,yP,yP,3/
            8,yN,5/
            bR,bP,10,gP,gR/
            1,bP,10,gP,1/
            bB,bP,bN,8,gN,gP,gB/
            bK,bP,10,gP,gQ/
            bQ,bP,10,gP,gK/
            bB,bP,10,gP,gB/
            bN,bP,10,gP,gN/
            bR,bP,10,gP,gR/
            7,rP,6/
            3,rP,rP,rP,rP,1,rP,rP,rP,3/
            3,rR,rN,rB,rQ,rK,rB,rN,rR,3
        """.trimIndent())

        val newState = engine.getStateAfterMove(from = Position.parse("i1"), to = Position.parse("f4"))

        newState shouldBeEqualTo parseStateFromFenOrThrow("""
            B-0,0,0,0-1,1,1,1-1,1,1,1-0,0,0,0-4-
            3,yR,yN,yB,yK,yQ,yB,1,yR,3/
            3,yP,yP,yP,yP,yP,yP,yP,yP,3/
            8,yN,5/
            bR,bP,10,gP,gR/
            1,bP,10,gP,1/
            bB,bP,bN,8,gN,gP,gB/
            bK,bP,10,gP,gQ/
            bQ,bP,10,gP,gK/
            bB,bP,10,gP,gB/
            bN,bP,10,gP,gN/
            bR,bP,3,rB,6,gP,gR/
            7,rP,6/
            3,rP,rP,rP,rP,1,rP,rP,rP,3/
            3,rR,rN,rB,rQ,rK,1,rN,rR,3
        """.trimIndent())
    }

    test("capture") {
        val engine = createEngineWithStateFromFen("""
            R-0,0,0,0-1,1,1,1-1,1,1,1-0,0,0,0-3-
            3,yR,yN,yB,yK,yQ,yB,1,yR,3/
            3,yP,yP,yP,yP,yP,yP,yP,yP,3/
            8,yN,5/
            bR,bP,10,gP,gR/
            1,bP,10,gP,1/
            bB,bP,bN,8,gN,gP,gB/
            bK,bP,10,gP,gQ/
            bQ,bP,10,gP,gK/
            bB,bP,10,gP,gB/
            bN,bP,10,gP,gN/
            bR,bP,10,gP,gR/
            7,rP,6/
            3,rP,rP,rP,rP,1,rP,rP,rP,3/
            3,rR,rN,rB,rQ,rK,rB,rN,rR,3
        """.trimIndent())

        val newState = engine.getStateAfterMove(from = Position.parse("i1"), to = Position.parse("b8"))

        newState shouldBeEqualTo parseStateFromFenOrThrow("""
            B-0,0,0,0-1,1,1,1-1,1,1,1-0,0,0,0-0-
            3,yR,yN,yB,yK,yQ,yB,1,yR,3/
            3,yP,yP,yP,yP,yP,yP,yP,yP,3/
            8,yN,5/
            bR,bP,10,gP,gR/
            1,bP,10,gP,1/
            bB,bP,bN,8,gN,gP,gB/
            bK,rB,10,gP,gQ/
            bQ,bP,10,gP,gK/
            bB,bP,10,gP,gB/
            bN,bP,10,gP,gN/
            bR,bP,10,gP,gR/
            7,rP,6/
            3,rP,rP,rP,rP,1,rP,rP,rP,3/
            3,rR,rN,rB,rQ,rK,1,rN,rR,3
        """.trimIndent())
    }

    test("move two squares forward as pawn") {
        val engine = createEngineWithStateFromFen("""
            R-0,0,0,0-1,1,1,1-1,1,1,1-0,0,0,0-3-
            3,yR,yN,yB,yK,yQ,yB,1,yR,3/
            3,yP,yP,yP,yP,yP,yP,yP,yP,3/
            8,yN,5/
            bR,bP,10,gP,gR/
            1,bP,10,gP,1/
            bB,bP,bN,8,gN,gP,gB/
            bK,bP,10,gP,gQ/
            bQ,bP,10,gP,gK/
            bB,bP,10,gP,gB/
            bN,bP,10,gP,gN/
            bR,bP,10,gP,gR/
            7,rP,6/
            3,rP,rP,rP,rP,1,rP,rP,rP,3/
            3,rR,rN,rB,rQ,rK,rB,rN,rR,3
        """.trimIndent())

        val newState = engine.getStateAfterMove(from = Position.parse("g2"), to = Position.parse("g4"))

        newState shouldBeEqualTo parseStateFromFenOrThrow("""
            B-0,0,0,0-1,1,1,1-1,1,1,1-g3,0,0,0-0-
            3,yR,yN,yB,yK,yQ,yB,1,yR,3/
            3,yP,yP,yP,yP,yP,yP,yP,yP,3/
            8,yN,5/
            bR,bP,10,gP,gR/
            1,bP,10,gP,1/
            bB,bP,bN,8,gN,gP,gB/
            bK,bP,10,gP,gQ/
            bQ,bP,10,gP,gK/
            bB,bP,10,gP,gB/
            bN,bP,10,gP,gN/
            bR,bP,4,rP,5,gP,gR/
            7,rP,6/
            3,rP,rP,rP,2,rP,rP,rP,3/
            3,rR,rN,rB,rQ,rK,rB,rN,rR,3
        """.trimIndent())
    }

    test("promote pawn") {
        val engine = createEngineWithStateFromFen("""
            R-0,0,0,1-0,0,0,0-0,0,0,0-0,0,0,0-0-
            14/
            5,yP,yK,yB,6/
            6,yP,7/
            6,bR,1,yP,1,yB,2,gR/
            12,gP,1/
            3,bK,bP,7,gP,gB/
            4,bP,6,gB,2/
            8,rP,rP,1,gP,1,gK/
            8,rN,rK,2,gP,gN/
            10,rB,1,gP,1/
            14/
            14/
            14/
            14
        """.trimIndent())

        val newState = engine.getStateAfterMove(from = Position.parse("i7"), to = Position.parse("i8"))

        newState shouldBeEqualTo parseStateFromFenOrThrow("""
            B-0,0,0,1-0,0,0,0-0,0,0,0-0,0,0,0-0-
            14/
            5,yP,yK,yB,6/
            6,yP,7/
            6,bR,1,yP,1,yB,2,gR/
            12,gP,1/
            3,bK,bP,7,gP,gB/
            4,bP,3,rQ,2,gB,2/
            9,rP,1,gP,1,gK/
            8,rN,rK,2,gP,gN/
            10,rB,1,gP,1/
            14/
            14/
            14/
            14
        """.trimIndent())
    }

    test("king side castle") {
        val engine = createEngineWithStateFromFen("""
            R-0,0,0,0-1,1,1,1-1,1,1,1-0,0,0,0-0-
            3,yR,1,yB,yK,yQ,yB,1,yR,3/
            4,yP,yP,yP,yP,yP,yP,yP,3/
            3,yP,1,yN,2,yN,5/
            bR,bP,9,gP,1,gR/
            1,bP,10,gP,1/
            bB,bP,bN,8,gN,gP,gB/
            bK,bP,10,gP,gQ/
            bQ,bP,10,gP,gK/
            bB,bP,bN,8,gP,1,gB/
            1,bP,10,gP,gN/
            bR,1,bP,9,gP,gR/
            6,rB,rP,rN,5/
            3,rP,rP,rP,rP,1,rP,rP,rP,3/
            3,rR,rN,rB,rQ,rK,2,rR,3
        """.trimIndent())

        val newState = engine.getStateAfterMove(from = Position.parse("h1"), to = Position.parse("j1"))

        newState shouldBeEqualTo parseStateFromFenOrThrow("""
            B-0,0,0,0-0,1,1,1-0,1,1,1-0,0,0,0-1-
            3,yR,1,yB,yK,yQ,yB,1,yR,3/
            4,yP,yP,yP,yP,yP,yP,yP,3/
            3,yP,1,yN,2,yN,5/
            bR,bP,9,gP,1,gR/
            1,bP,10,gP,1/
            bB,bP,bN,8,gN,gP,gB/
            bK,bP,10,gP,gQ/
            bQ,bP,10,gP,gK/
            bB,bP,bN,8,gP,1,gB/
            1,bP,10,gP,gN/
            bR,1,bP,9,gP,gR/
            6,rB,rP,rN,5/
            3,rP,rP,rP,rP,1,rP,rP,rP,3/
            3,rR,rN,rB,rQ,1,rR,rK,4
        """.trimIndent())
    }

    test("queen side castle") {
        val engine = createEngineWithStateFromFen("""
            R-0,0,0,0-1,1,1,1-1,1,1,1-0,0,0,0-0-
            3,yR,1,yB,yK,yQ,yB,1,yR,3/
            4,yP,yP,yP,yP,yP,yP,yP,3/
            5,yN,2,yN,5/
            bR,1,yP,8,gP,1,gR/
            1,bP,10,gP,1/
            bB,bP,bN,8,gN,gP,gB/
            bK,bP,9,gP,1,gQ/
            bQ,bP,10,gP,gK/
            bB,bP,bN,8,gN,gP,gB/
            1,bP,10,gP,1/
            bR,1,bP,9,gP,gR/
            5,rN,rP,rB,6/
            3,rP,rP,rP,rQ,rP,rP,rP,rP,3/
            3,rR,3,rK,rB,rN,rR,3
        """.trimIndent())

        val newState = engine.getStateAfterMove(from = Position.parse("h1"), to = Position.parse("f1"))

        newState shouldBeEqualTo parseStateFromFenOrThrow("""
            B-0,0,0,0-0,1,1,1-0,1,1,1-0,0,0,0-1-
            3,yR,1,yB,yK,yQ,yB,1,yR,3/
            4,yP,yP,yP,yP,yP,yP,yP,3/
            5,yN,2,yN,5/
            bR,1,yP,8,gP,1,gR/
            1,bP,10,gP,1/
            bB,bP,bN,8,gN,gP,gB/
            bK,bP,9,gP,1,gQ/
            bQ,bP,10,gP,gK/
            bB,bP,bN,8,gN,gP,gB/
            1,bP,10,gP,1/
            bR,1,bP,9,gP,gR/
            5,rN,rP,rB,6/
            3,rP,rP,rP,rQ,rP,rP,rP,rP,3/
            5,rK,rR,1,rB,rN,rR,3
        """.trimIndent())
    }

    test("move king before castling") {
        val engine = createEngineWithStateFromFen("""
            R-0,0,0,0-1,1,1,1-1,1,1,1-0,0,0,0-0-
            3,yR,1,yB,yK,yQ,yB,1,yR,3/
            4,yP,yP,yP,yP,yP,yP,yP,3/
            5,yN,2,yN,5/
            bR,1,yP,8,gP,1,gR/
            1,bP,10,gP,1/
            bB,bP,bN,8,gN,gP,gB/
            bK,bP,9,gP,1,gQ/
            bQ,bP,10,gP,gK/
            bB,bP,bN,8,gN,gP,gB/
            1,bP,10,gP,1/
            bR,1,bP,9,gP,gR/
            5,rN,rP,rB,6/
            3,rP,rP,rP,rQ,rP,rP,rP,rP,3/
            3,rR,3,rK,rB,rN,rR,3
        """.trimIndent())

        val newState = engine.getStateAfterMove(from = Position.parse("h1"), to = Position.parse("g1"))

        newState shouldBeEqualTo parseStateFromFenOrThrow("""
            B-0,0,0,0-0,1,1,1-0,1,1,1-0,0,0,0-1-
            3,yR,1,yB,yK,yQ,yB,1,yR,3/
            4,yP,yP,yP,yP,yP,yP,yP,3/
            5,yN,2,yN,5/
            bR,1,yP,8,gP,1,gR/
            1,bP,10,gP,1/
            bB,bP,bN,8,gN,gP,gB/
            bK,bP,9,gP,1,gQ/
            bQ,bP,10,gP,gK/
            bB,bP,bN,8,gN,gP,gB/
            1,bP,10,gP,1/
            bR,1,bP,9,gP,gR/
            5,rN,rP,rB,6/
            3,rP,rP,rP,rQ,rP,rP,rP,rP,3/
            3,rR,2,rK,1,rB,rN,rR,3
        """.trimIndent())
    }

    test("move rook before castling") {
        val engine = createEngineWithStateFromFen("""
            R-0,0,0,0-1,1,1,1-1,1,1,1-0,0,0,0-0-
            3,yR,1,yB,yK,yQ,yB,1,yR,3/
            4,yP,yP,yP,yP,yP,yP,yP,3/
            5,yN,2,yN,5/
            bR,1,yP,8,gP,1,gR/
            1,bP,10,gP,1/
            bB,bP,bN,8,gN,gP,gB/
            bK,bP,9,gP,1,gQ/
            bQ,bP,10,gP,gK/
            bB,bP,bN,8,gN,gP,gB/
            1,bP,10,gP,1/
            bR,1,bP,9,gP,gR/
            5,rN,rP,rB,6/
            3,rP,rP,rP,rQ,rP,rP,rP,rP,3/
            3,rR,3,rK,rB,rN,rR,3
        """.trimIndent())

        val newState = engine.getStateAfterMove(from = Position.parse("d1"), to = Position.parse("e1"))

        newState shouldBeEqualTo parseStateFromFenOrThrow("""
            B-0,0,0,0-1,1,1,1-0,1,1,1-0,0,0,0-1-
            3,yR,1,yB,yK,yQ,yB,1,yR,3/
            4,yP,yP,yP,yP,yP,yP,yP,3/
            5,yN,2,yN,5/
            bR,1,yP,8,gP,1,gR/
            1,bP,10,gP,1/
            bB,bP,bN,8,gN,gP,gB/
            bK,bP,9,gP,1,gQ/
            bQ,bP,10,gP,gK/
            bB,bP,bN,8,gN,gP,gB/
            1,bP,10,gP,1/
            bR,1,bP,9,gP,gR/
            5,rN,rP,rB,6/
            3,rP,rP,rP,rQ,rP,rP,rP,rP,3/
            4,rR,2,rK,rB,rN,rR,3
        """.trimIndent())
    }

    test("capture by en passant") {
        val engine = createEngineWithStateFromFen("""
            R-0,0,0,0-1,1,1,1-1,1,1,1-0,0,c5,0-2-
            3,yR,1,yB,yK,yQ,yB,yN,yR,3/
            3,yP,yP,yP,yP,yP,yP,yP,yP,3/
            5,yN,8/
            bR,bP,10,gP,gR/
            bN,bP,10,gP,1/
            bB,bP,9,gN,gP,gB/
            bK,bP,10,gP,gQ/
            bQ,bP,10,gP,gK/
            bB,bP,10,gP,gB/
            bN,2,bP,8,gP,gN/
            bR,bP,1,rP,8,gP,gR/
            14/
            4,rP,rP,rP,rP,rP,rP,rP,3/
            3,rR,rN,rB,rQ,rK,rB,rN,rR,3
        """.trimIndent())

        val newState = engine.getStateAfterMove(from = Position.parse("d4"), to = Position.parse("c5"))

        newState shouldBeEqualTo parseStateFromFenOrThrow("""
            B-0,0,0,0-1,1,1,1-1,1,1,1-0,0,0,0-0-
            3,yR,1,yB,yK,yQ,yB,yN,yR,3/
            3,yP,yP,yP,yP,yP,yP,yP,yP,3/
            5,yN,8/
            bR,bP,10,gP,gR/
            bN,bP,10,gP,1/
            bB,bP,9,gN,gP,gB/
            bK,bP,10,gP,gQ/
            bQ,bP,10,gP,gK/
            bB,bP,10,gP,gB/
            bN,1,rP,9,gP,gN/
            bR,bP,10,gP,gR/
            14/
            4,rP,rP,rP,rP,rP,rP,rP,3/
            3,rR,rN,rB,rQ,rK,rB,rN,rR,3
        """.trimIndent())
    }

    test("capture pawn with en passant square") {
        val engine = createEngineWithStateFromFen("""
            R-0,0,0,0-1,1,1,1-1,1,1,1-0,0,c4,0-2-
            3,yR,1,yB,yK,yQ,yB,yN,yR,3/
            3,yP,yP,yP,yP,yP,yP,yP,yP,3/
            5,yN,8/
            bR,bP,10,gP,gR/
            bN,bP,10,gP,1/
            bB,bP,9,gN,gP,gB/
            bK,bP,10,gP,gQ/
            bQ,bP,10,gP,gK/
            bB,bP,10,gP,gB/
            bN,bP,10,gP,gN/
            bR,2,bP,8,gP,gR/
            4,rP,9/
            3,rP,1,rP,rP,rP,rP,rP,rP,3/
            3,rR,rN,rB,rQ,rK,rB,rN,rR,3
        """.trimIndent())

        val newState = engine.getStateAfterMove(from = Position.parse("e3"), to = Position.parse("d4"))

        newState shouldBeEqualTo parseStateFromFenOrThrow("""
            B-0,0,0,0-1,1,1,1-1,1,1,1-0,0,0,0-0-
            3,yR,1,yB,yK,yQ,yB,yN,yR,3/
            3,yP,yP,yP,yP,yP,yP,yP,yP,3/
            5,yN,8/
            bR,bP,10,gP,gR/
            bN,bP,10,gP,1/
            bB,bP,9,gN,gP,gB/
            bK,bP,10,gP,gQ/
            bQ,bP,10,gP,gK/
            bB,bP,10,gP,gB/
            bN,bP,10,gP,gN/
            bR,2,rP,8,gP,gR/
            14/
            3,rP,1,rP,rP,rP,rP,rP,rP,3/
            3,rR,rN,rB,rQ,rK,rB,rN,rR,3
        """.trimIndent())
    }

    test("move onto en passant square") {
        val engine = createEngineWithStateFromFen("""
            R-0,0,0,0-1,1,1,1-1,1,1,1-0,0,c4,0-2-
            3,yR,1,yB,yK,yQ,yB,yN,yR,3/
            3,yP,yP,yP,yP,yP,yP,yP,yP,3/
            5,yN,8/
            bR,bP,10,gP,gR/
            bN,bP,10,gP,1/
            bB,bP,9,gN,gP,gB/
            bK,bP,10,gP,gQ/
            bQ,bP,10,gP,gK/
            bB,bP,10,gP,gB/
            bN,bP,10,gP,gN/
            bR,2,bP,8,gP,gR/
            4,rP,9/
            3,rP,1,rP,rP,rP,rP,rP,rP,3/
            3,rR,rN,rB,rQ,rK,rB,rN,rR,3
        """.trimIndent())

        val newState = engine.getStateAfterMove(from = Position.parse("f1"), to = Position.parse("c4"))

        newState shouldBeEqualTo parseStateFromFenOrThrow("""
            B-0,0,0,0-1,1,1,1-1,1,1,1-0,0,0,0-3-
            3,yR,1,yB,yK,yQ,yB,yN,yR,3/
            3,yP,yP,yP,yP,yP,yP,yP,yP,3/
            5,yN,8/
            bR,bP,10,gP,gR/
            bN,bP,10,gP,1/
            bB,bP,9,gN,gP,gB/
            bK,bP,10,gP,gQ/
            bQ,bP,10,gP,gK/
            bB,bP,10,gP,gB/
            bN,bP,10,gP,gN/
            bR,1,rB,bP,8,gP,gR/
            4,rP,9/
            3,rP,1,rP,rP,rP,rP,rP,rP,3/
            3,rR,rN,1,rQ,rK,rB,rN,rR,3
        """.trimIndent())
    }

    test("eliminate by check mate") {
        val engine = createEngineWithStateFromFen("""
            Y-0,0,0,1-0,0,0,0-0,0,0,0-0,0,0,0-0-
            14/
            5,yP,yK,yB,6/
            6,yP,7/
            6,bR,1,yP,1,yB,2,gR/
            12,gP,1/
            3,bK,bP,1,rK,5,gP,gB/
            4,bP,6,gB,2/
            8,rP,rP,1,gP,1,gK/
            8,rN,3,gP,gN/
            10,rB,1,gP,1/
            14/
            14/
            14/
            14
        """.trimIndent())

        val newState = engine.getStateAfterMove(from = Position.parse("h13"), to = Position.parse("j11"))

        newState shouldBeEqualTo parseStateFromFenOrThrow("""
            B-1,0,0,1-0,0,0,0-0,0,0,0-0,0,0,0-1-
            14/
            5,yP,yK,7/
            6,yP,7/
            6,bR,1,yP,yB,yB,2,gR/
            12,gP,1/
            3,bK,bP,1,rK,5,gP,gB/
            4,bP,6,gB,2/
            8,rP,rP,1,gP,1,gK/
            8,rN,3,gP,gN/
            10,rB,1,gP,1/
            14/
            14/
            14/
            14
        """.trimIndent())
    }

    test("eliminate by stale mate") {
        val engine = createEngineWithStateFromFen("""
            Y-0,0,0,1-0,0,0,0-0,0,0,0-0,0,0,0-4-
            14/
            5,yP,1,yB,6/
            6,yP,7/
            3,bK,2,bR,1,yP,4,gR/
            8,yR,3,gP,1/
            3,bP,bP,7,gP,gB/
            11,gB,2/
            11,gP,1,gK/
            12,gP,gN/
            12,gP,1/
            7,yK,6/
            14/
            14/
            7,rK,6
        """.trimIndent())

        val newState = engine.getStateAfterMove(from = Position.parse("h4"), to = Position.parse("h3"))

        newState shouldBeEqualTo parseStateFromFenOrThrow("""
            B-1,0,0,1-0,0,0,0-0,0,0,0-0,0,0,0-5-
            14/
            5,yP,1,yB,6/
            6,yP,7/
            3,bK,2,bR,1,yP,4,gR/
            8,yR,3,gP,1/
            3,bP,bP,7,gP,gB/
            11,gB,2/
            11,gP,1,gK/
            12,gP,gN/
            12,gP,1/
            14/
            7,yK,6/
            14/
            7,rK,6
        """.trimIndent())
    }

    test("draw by stale mate") {
        val engine = createEngineWithStateFromFen("""
            Y-0,1,0,1-0,0,0,0-0,0,0,0-0,0,0,0-1-
            14/
            14/
            14/
            14/
            14/
            14/
            14/
            14/
            14/
            14/
            6,yK,7/
            14/
            6,yP,7/
            6,rK,7
        """.trimIndent())

        val newState = engine.getStateAfterMove(from = Position.parse("g4"), to = Position.parse("g3"))
        engine.isDraw shouldBeEqualTo true
        engine.winningColor shouldBeEqualTo null

        newState shouldBeEqualTo parseStateFromFenOrThrow("""
            R-0,1,0,1-0,0,0,0-0,0,0,0-0,0,0,0-2-
            14/
            14/
            14/
            14/
            14/
            14/
            14/
            14/
            14/
            14/
            14/
            6,yK,7/
            6,yP,7/
            6,rK,7
        """.trimIndent())
    }

    test("game over by check mate") {
        val engine = createEngineWithStateFromFen("""
            Y-0,1,0,1-0,0,0,0-0,0,0,0-0,0,0,0-5-
            14/
            14/
            14/
            12,yR,1/
            14/
            14/
            14/
            14/
            14/
            11,yK,1,rK/
            14/
            14/
            14/
            14
        """.trimIndent())

        val newState = engine.getStateAfterMove(from = Position.parse("m11"), to = Position.parse("n11"))
        engine.isDraw shouldBeEqualTo false
        engine.winningColor shouldBeEqualTo Yellow

        newState shouldBeEqualTo parseStateFromFenOrThrow("""
            Y-1,1,0,1-0,0,0,0-0,0,0,0-0,0,0,0-6-
            14/
            14/
            14/
            13,yR/
            14/
            14/
            14/
            14/
            14/
            11,yK,1,rK/
            14/
            14/
            14/
            14
        """.trimIndent())
    }

    test("draw by threefold repetition") {
        val engine = createEngineWithStateFromFen("""
            Y-0,1,0,1-0,0,0,0-0,0,0,0-0,0,0,0-0-
            14/
            14/
            14/
            14/
            14/
            10,yQ,3/
            14/
            8,yK,5/
            14/
            12,rQ,1/
            13,rK/
            14/
            14/
            14
        """.trimIndent())

        engine.makeMove(from = Position.parse("k9"), to = Position.parse("k4"))
        engine.makeMove(from = Position.parse("m5"), to = Position.parse("m4"))
        engine.makeMove(from = Position.parse("k4"), to = Position.parse("n7"))
        engine.makeMove(from = Position.parse("m4"), to = Position.parse("n5"))
        engine.makeMove(from = Position.parse("n7"), to = Position.parse("k7"))
        engine.makeMove(from = Position.parse("n5"), to = Position.parse("m5"))
        engine.makeMove(from = Position.parse("k7"), to = Position.parse("k4"))
        engine.makeMove(from = Position.parse("m5"), to = Position.parse("m4"))
        engine.makeMove(from = Position.parse("k4"), to = Position.parse("k7"))
        engine.makeMove(from = Position.parse("m4"), to = Position.parse("m5"))
        engine.makeMove(from = Position.parse("k7"), to = Position.parse("k4"))
        engine.claimDraw()

        engine.isDraw shouldBeEqualTo true
        engine.winningColor shouldBeEqualTo null
    }

    test("draw by threefold repetition does not need to be claimed immediately") {
        val engine = createEngineWithStateFromFen("""
            Y-0,1,0,1-0,0,0,0-0,0,0,0-0,0,0,0-0-
            14/
            14/
            14/
            14/
            14/
            10,yQ,3/
            14/
            8,yK,5/
            14/
            12,rQ,1/
            13,rK/
            14/
            14/
            14
        """.trimIndent())

        engine.makeMove(from = Position.parse("k9"), to = Position.parse("k4"))
        engine.makeMove(from = Position.parse("m5"), to = Position.parse("m4"))
        engine.makeMove(from = Position.parse("k4"), to = Position.parse("n7"))
        engine.makeMove(from = Position.parse("m4"), to = Position.parse("n5"))
        engine.makeMove(from = Position.parse("n7"), to = Position.parse("k7"))
        engine.makeMove(from = Position.parse("n5"), to = Position.parse("m5"))
        engine.makeMove(from = Position.parse("k7"), to = Position.parse("k4"))
        engine.makeMove(from = Position.parse("m5"), to = Position.parse("m4"))
        engine.makeMove(from = Position.parse("k4"), to = Position.parse("k7"))
        engine.makeMove(from = Position.parse("m4"), to = Position.parse("m5"))
        engine.makeMove(from = Position.parse("k7"), to = Position.parse("k4"))
        engine.makeMove(from = Position.parse("n4"), to = Position.parse("n5"))
        engine.makeMove(from = Position.parse("k4"), to = Position.parse("n7"))
        engine.claimDraw()

        engine.isDraw shouldBeEqualTo true
        engine.winningColor shouldBeEqualTo null
    }

    test("draw by fifty move rule") {
        val engine = createEngineWithStateFromFen("""
            Y-0,1,0,1-0,0,0,0-0,0,0,0-0,0,0,0-49-
            14/
            14/
            14/
            9,yB,4/
            14/
            14/
            14/
            8,yK,5/
            9,yN,4/
            2,rR,11/
            13,rK/
            14/
            14/
            14
        """.trimIndent())

        engine.makeMove(from = Position.parse("j6"), to = Position.parse("k4"))
        engine.claimDraw()

        engine.isDraw shouldBeEqualTo true
        engine.winningColor shouldBeEqualTo null
    }

    test("draw by fifty move rule does not need by claimed immediately") {
        val engine = createEngineWithStateFromFen("""
            Y-0,1,0,1-0,0,0,0-0,0,0,0-0,0,0,0-49-
            14/
            14/
            14/
            9,yB,4/
            14/
            14/
            14/
            8,yK,5/
            9,yN,4/
            2,rR,11/
            13,rK/
            14/
            14/
            14
        """.trimIndent())

        engine.makeMove(from = Position.parse("j6"), to = Position.parse("k4"))
        engine.makeMove(from = Position.parse("n4"), to = Position.parse("n5"))
        engine.makeMove(from = Position.parse("k4"), to = Position.parse("l6"))
        engine.claimDraw()

        engine.isDraw shouldBeEqualTo true
        engine.winningColor shouldBeEqualTo null
    }

    test("draw not allowed unless threefold repetition or fifty move rule") {
        val engine = createEngineWithStateFromFen("""
            Y-0,1,0,1-0,0,0,0-0,0,0,0-0,0,0,0-39-
            14/
            14/
            14/
            14/
            14/
            10,yQ,3/
            14/
            8,yK,5/
            14/
            12,rQ,1/
            13,rK/
            14/
            14/
            14
        """.trimIndent())

        engine.makeMove(from = Position.parse("k9"), to = Position.parse("k4"))
        engine.makeMove(from = Position.parse("m5"), to = Position.parse("m4"))
        engine.makeMove(from = Position.parse("k4"), to = Position.parse("n7"))
        engine.makeMove(from = Position.parse("m4"), to = Position.parse("n5"))
        engine.makeMove(from = Position.parse("n7"), to = Position.parse("k7"))
        engine.makeMove(from = Position.parse("n5"), to = Position.parse("m5"))
        engine.makeMove(from = Position.parse("k7"), to = Position.parse("k4"))
        engine.makeMove(from = Position.parse("m5"), to = Position.parse("m4"))
        engine.makeMove(from = Position.parse("k4"), to = Position.parse("k7"))
        engine.makeMove(from = Position.parse("m4"), to = Position.parse("m5"))
        engine.claimDraw()

        engine.isDraw shouldBeEqualTo false
    }
})