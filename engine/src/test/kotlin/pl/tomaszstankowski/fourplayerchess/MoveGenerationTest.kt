package pl.tomaszstankowski.fourplayerchess

import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldContain
import org.amshove.kluent.shouldNotContain
import org.spekframework.spek2.Spek
import pl.tomaszstankowski.fourplayerchess.engine.Coordinates
import pl.tomaszstankowski.fourplayerchess.engine.Engine
import pl.tomaszstankowski.fourplayerchess.engine.PieceType.*
import pl.tomaszstankowski.fourplayerchess.engine.RegularMove

object MoveGenerationTest : Spek({

    group("pawn") {

        test("can go one or two squares forward on its first move") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,0,0-0,0,0,0-0,0,0,0-0,0,0,0-2-
            9,yK,4/
            7,yP,yP,yP,yP,3/
            3,yB,4,yN,5/
            1,bP,3,yP,yP,7/
            bK,bP,10,gP,gN/
            bB,13/
            12,gB,1/
            12,gP,gK/
            1,bP,bN,8,gN,1,gB/
            1,bP,11,gR/
            14/
            14/
            6,rP,7/
            6,rK,7
        """.trimIndent())
            val engine = Engine(state)

            val validMoves = engine.legalMoves

            validMoves.filterByMovedPieceType(state, Pawn) shouldBeEqualTo setOf(
                    RegularMove(from = Coordinates.parse("g2"), to = Coordinates.parse("g3")),
                    RegularMove(from = Coordinates.parse("g2"), to = Coordinates.parse("g4"))
            )
        }

        test("cannot go forward on its first move when one square forward is occupied by piece of its color") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,0,0-0,0,0,0-0,0,0,0-0,0,0,0-2-
            9,yK,4/
            7,yP,yP,yP,yP,3/
            3,yB,4,yN,5/
            1,bP,3,yP,yP,7/
            bK,bP,10,gP,gN/
            bB,13/
            12,gB,1/
            12,gP,gK/
            1,bP,bN,8,gN,1,gB/
            1,bP,11,gR/
            14/
            6,rN,7/
            6,rP,7/
            6,rK,7
        """.trimIndent())
            val engine = Engine(state)

            val validMoves = engine.legalMoves

            validMoves.filterByMovedPieceType(state, Pawn) shouldBeEqualTo setOf()
        }

        test("cannot go two squares forward on its first move when the square is occupied by piece of its color") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,0,0-0,0,0,0-0,0,0,0-0,0,0,0-2-
            9,yK,4/
            7,yP,yP,yP,yP,3/
            3,yB,4,yN,5/
            1,bP,3,yP,yP,7/
            bK,bP,10,gP,gN/
            bB,13/
            12,gB,1/
            12,gP,gK/
            1,bP,bN,8,gN,1,gB/
            1,bP,11,gR/
            6,rN,7/
            14/
            6,rP,7/
            6,rK,7
        """.trimIndent())
            val engine = Engine(state)

            val validMoves = engine.legalMoves

            validMoves.filterByMovedPieceType(state, Pawn) shouldBeEqualTo setOf(
                    RegularMove(from = Coordinates.parse("g2"), to = Coordinates.parse("g3"))
            )
        }

        test("cannot go forward on its first move when one square forward is occupied by piece of other color") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,0,0-0,0,0,0-0,0,0,0-0,0,0,0-2-
            9,yK,4/
            7,yP,yP,yP,yP,3/
            3,yB,4,yN,5/
            1,bP,3,yP,yP,7/
            bK,bP,10,gP,gN/
            bB,13/
            12,gB,1/
            12,gP,gK/
            1,bP,bN,8,gN,1,gB/
            1,bP,11,gR/
            14/
            6,bN,7/
            6,rP,7/
            6,rK,7
        """.trimIndent())
            val engine = Engine(state)

            val validMoves = engine.legalMoves

            validMoves.filterByMovedPieceType(state, Pawn) shouldBeEqualTo setOf()
        }

        test("cannot go two squares forward on its first move when the square is occupied by piece of other color") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,0,0-0,0,0,0-0,0,0,0-0,0,0,0-2-
            9,yK,4/
            7,yP,yP,yP,yP,3/
            3,yB,4,yN,5/
            1,bP,3,yP,yP,7/
            bK,bP,10,gP,gN/
            bB,13/
            12,gB,1/
            12,gP,gK/
            1,bP,bN,8,gN,1,gB/
            1,bP,11,gR/
            6,bN,7/
            14/
            6,rP,7/
            6,rK,7
        """.trimIndent())
            val engine = Engine(state)

            val validMoves = engine.legalMoves

            validMoves.filterByMovedPieceType(state, Pawn) shouldBeEqualTo setOf(
                    RegularMove(from = Coordinates.parse("g2"), to = Coordinates.parse("g3"))
            )
        }

        test("can move only one square forward when has already been touched before") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,0,0-0,0,0,0-0,0,0,0-0,0,0,0-2-
            9,yK,4/
            7,yP,yP,yP,yP,3/
            3,yB,4,yN,5/
            1,bP,3,yP,yP,7/
            bK,bP,10,gP,gN/
            bB,13/
            12,gB,1/
            12,gP,gK/
            1,bP,bN,8,gN,1,gB/
            1,bP,11,gR/
            14/
            6,rP,7/
            14/
            6,rK,7
        """.trimIndent())
            val engine = Engine(state)

            val validMoves = engine.legalMoves

            validMoves.filterByMovedPieceType(state, Pawn) shouldBeEqualTo setOf(
                    RegularMove(from = Coordinates.parse("g3"), to = Coordinates.parse("g4"))
            )
        }

        test("cannot move forward when has been touched before and the square forward is occupied by piece of its color") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,0,0-0,0,0,0-0,0,0,0-0,0,0,0-2-
            9,yK,4/
            7,yP,yP,yP,yP,3/
            3,yB,4,yN,5/
            1,bP,3,yP,yP,7/
            bK,bP,10,gP,gN/
            bB,13/
            12,gB,1/
            12,gP,gK/
            1,bP,bN,8,gN,1,gB/
            1,bP,11,gR/
            6,rN,7/
            6,rP,7/
            14/
            6,rK,7
        """.trimIndent())
            val engine = Engine(state)

            val validMoves = engine.legalMoves

            validMoves.filterByMovedPieceType(state, Pawn) shouldBeEqualTo setOf()
        }

        test("cannot move forward when has been touched before and the square forward is occupied by piece of other color") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,0,0-0,0,0,0-0,0,0,0-0,0,0,0-2-
            9,yK,4/
            7,yP,yP,yP,yP,3/
            3,yB,4,yN,5/
            1,bP,3,yP,yP,7/
            bK,bP,10,gP,gN/
            bB,13/
            12,gB,1/
            12,gP,gK/
            1,bP,bN,8,gN,1,gB/
            1,bP,11,gR/
            6,bN,7/
            6,rP,7/
            14/
            6,rK,7
        """.trimIndent())
            val engine = Engine(state)

            val validMoves = engine.legalMoves

            validMoves.filterByMovedPieceType(state, Pawn) shouldBeEqualTo setOf()
        }

        test("can capture right") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,0,0-0,0,0,0-0,0,0,0-0,0,0,0-2-
            9,yK,4/
            7,yP,yP,yP,yP,3/
            3,yB,4,yN,5/
            1,bP,3,yP,yP,7/
            bK,bP,10,gP,gN/
            bB,13/
            12,gB,1/
            12,gP,gK/
            1,bP,bN,8,gN,1,gB/
            1,bP,11,gR/
            7,bN,6/
            6,rP,7/
            14/
            6,rK,7
        """.trimIndent())
            val engine = Engine(state)

            val validMoves = engine.legalMoves

            validMoves.filterByMovedPieceType(state, Pawn) shouldBeEqualTo setOf(
                    RegularMove(from = Coordinates.parse("g3"), to = Coordinates.parse("g4")),
                    RegularMove(from = Coordinates.parse("g3"), to = Coordinates.parse("h4"))
            )
        }

        test("can capture left") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,0,0-0,0,0,0-0,0,0,0-0,0,0,0-2-
            9,yK,4/
            7,yP,yP,yP,yP,3/
            3,yB,4,yN,5/
            1,bP,3,yP,yP,7/
            bK,bP,10,gP,gN/
            bB,13/
            12,gB,1/
            12,gP,gK/
            1,bP,bN,8,gN,1,gB/
            1,bP,11,gR/
            5,bN,8/
            6,rP,7/
            14/
            6,rK,7
        """.trimIndent())
            val engine = Engine(state)

            val validMoves = engine.legalMoves

            validMoves.filterByMovedPieceType(state, Pawn) shouldBeEqualTo setOf(
                    RegularMove(from = Coordinates.parse("g3"), to = Coordinates.parse("g4")),
                    RegularMove(from = Coordinates.parse("g3"), to = Coordinates.parse("f4"))
            )
        }

        test("can capture left and right") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,0,0-0,0,0,0-0,0,0,0-0,0,0,0-2-
            9,yK,4/
            7,yP,yP,yP,yP,3/
            3,yB,10/
            1,bP,3,yP,yP,7/
            bK,bP,10,gP,gN/
            bB,13/
            12,gB,1/
            12,gP,gK/
            1,bP,bN,8,gN,1,gB/
            1,bP,11,gR/
            5,bN,1,yN,6/
            6,rP,7/
            14/
            6,rK,7
        """.trimIndent())
            val engine = Engine(state)

            val validMoves = engine.legalMoves

            validMoves.filterByMovedPieceType(state, Pawn) shouldBeEqualTo setOf(
                    RegularMove(from = Coordinates.parse("g3"), to = Coordinates.parse("g4")),
                    RegularMove(from = Coordinates.parse("g3"), to = Coordinates.parse("f4")),
                    RegularMove(from = Coordinates.parse("g3"), to = Coordinates.parse("h4"))
            )
        }

        test("can capture right by en passant") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,0,0-0,0,0,0-0,0,0,0-0,0,0,l4-0-
            9,yK,4/
            7,yP,yP,yP,yP,3/
            3,yB,4,yN,5/
            1,bP,5,yP,6/
            bK,bP,10,gP,gN/
            bB,13/
            12,gB,1/
            12,gP,gK/
            1,bP,bN,8,gN,1,gB/
            1,bP,11,gR/
            10,gP,3/
            10,rP,3/
            6,rN,7/
            6,rK,7
        """.trimIndent())
            val engine = Engine(state)

            val validMoves = engine.legalMoves

            validMoves.filterByMovedPieceType(state, Pawn) shouldBeEqualTo setOf(
                    RegularMove(from = Coordinates.parse("k3"), to = Coordinates.parse("l4"))
            )
        }

        test("can capture left by en passant") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,0,0-0,0,0,0-0,0,0,0-0,c4,0,0-1-
            9,yK,4/
            7,yP,yP,yP,yP,3/
            3,yB,4,yN,5/
            1,bP,5,yP,6/
            bK,bP,10,gP,gN/
            bB,13/
            12,gB,1/
            12,gP,gK/
            1,bP,bN,8,gN,1,gB/
            13,gR/
            3,bP,8,gP,1/
            3,rP,10/
            6,rN,7/
            6,rK,7
        """.trimIndent())
            val engine = Engine(state)

            val validMoves = engine.legalMoves

            validMoves.filterByMovedPieceType(state, Pawn) shouldBeEqualTo setOf(
                    RegularMove(from = Coordinates.parse("d3"), to = Coordinates.parse("c4"))
            )
        }

        test("cannot move from pin line") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,1,1-0,0,0,0-0,0,0,0-0,0,0,0-0-
            14/
            14/
            14/
            14/
            14/
            3,bK,2,bR,7/
            14/
            14/
            14/
            7,bR,6/
            6,rP,7/
            6,rK,7/
            14/
            14
        """.trimIndent())
            val engine = Engine(state)

            val validMoves = engine.legalMoves

            validMoves.filterByMovedPieceType(state, Pawn) shouldBeEqualTo setOf(
                    RegularMove(from = Coordinates.parse("g4"), to = Coordinates.parse("g5"))
            )
        }

        test("must stop check") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,1,1-0,0,0,0-0,0,0,0-0,0,0,0-0-
            14/
            14/
            14/
            14/
            14/
            3,bK,10/
            14/
            14/
            14/
            7,bN,6/
            6,rP,7/
            6,rK,7/
            14/
            14
        """.trimIndent())
            val engine = Engine(state)

            val validMoves = engine.legalMoves

            validMoves.filterByMovedPieceType(state, Pawn) shouldBeEqualTo setOf(
                    RegularMove(from = Coordinates.parse("g4"), to = Coordinates.parse("h5"))
            )
        }

        test("cannot stop two or more checks") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,1,1-0,0,0,0-0,0,0,0-0,0,0,0-0-
            14/
            14/
            14/
            14/
            14/
            3,bK,10/
            14/
            14/
            14/
            7,bN,6/
            6,rP,7/
            4,bR,1,rK,7/
            14/
            14
        """.trimIndent())
            val engine = Engine(state)

            val validMoves = engine.legalMoves

            validMoves.filterByMovedPieceType(state, Pawn) shouldBeEqualTo setOf()
        }
    }

    group("bishop") {

        test("can move on diagonals") {
            val state = parseStateFromFenOrThrow("""
                R-0,0,0,0-1,1,0,1-1,1,0,1-0,0,0,0-0-
                3,yR,1,yB,yK,yQ,yB,1,yR,3/
                3,yP,yP,1,yN,yP,yP,yP,yP,3/
                5,yP,yP,1,yN,5/
                bR,bP,10,gP,gR/
                bN,bP,9,gP,2/
                bK,bP,9,gN,gP,gB/
                2,bP,8,gP,1,gQ/
                bQ,bP,10,gP,gK/
                bB,bP,bN,9,gP,gB/
                1,bP,10,gP,1/
                bR,1,bP,3,rP,rP,rB,2,gN,gP,gR/
                14/
                3,rP,rP,rP,2,rP,rP,rP,3/
                3,rR,rN,1,rQ,rK,1,rN,rR,3
            """.trimIndent())
            val engine = Engine(state)

            val validMoves = engine.legalMoves

            validMoves.filterByMovedPieceType(state, Bishop) shouldBeEqualTo setOf(
                    RegularMove(from = Coordinates.parse("i4"), to = Coordinates.parse("j5")),
                    RegularMove(from = Coordinates.parse("i4"), to = Coordinates.parse("k6")),
                    RegularMove(from = Coordinates.parse("i4"), to = Coordinates.parse("l7")),
                    RegularMove(from = Coordinates.parse("i4"), to = Coordinates.parse("m8")),
                    RegularMove(from = Coordinates.parse("i4"), to = Coordinates.parse("n9")),
                    RegularMove(from = Coordinates.parse("i4"), to = Coordinates.parse("h5")),
                    RegularMove(from = Coordinates.parse("i4"), to = Coordinates.parse("g6")),
                    RegularMove(from = Coordinates.parse("i4"), to = Coordinates.parse("f7")),
                    RegularMove(from = Coordinates.parse("i4"), to = Coordinates.parse("e8")),
                    RegularMove(from = Coordinates.parse("i4"), to = Coordinates.parse("d9")),
                    RegularMove(from = Coordinates.parse("i4"), to = Coordinates.parse("c10")),
                    RegularMove(from = Coordinates.parse("i4"), to = Coordinates.parse("b11")),
                    RegularMove(from = Coordinates.parse("i4"), to = Coordinates.parse("h3")),
                    RegularMove(from = Coordinates.parse("i4"), to = Coordinates.parse("g2")),
                    RegularMove(from = Coordinates.parse("i4"), to = Coordinates.parse("f1")),
                    RegularMove(from = Coordinates.parse("i4"), to = Coordinates.parse("j3"))
            )
        }

        test("cannot move from pin line") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,1,1-0,0,0,0-0,0,0,0-0,0,0,0-0-
            14/
            14/
            14/
            14/
            14/
            3,bK,10/
            14/
            14/
            14/
            7,bN,6/
            1,bR,4,rB,rK,6/
            14/
            14/
            14
        """.trimIndent())
            val engine = Engine(state)

            val validMoves = engine.legalMoves

            validMoves.filterByMovedPieceType(state, Bishop) shouldBeEqualTo setOf()
        }

        test("can capture pinning piece") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,1,1-0,0,0,0-0,0,0,0-0,0,0,0-0-
            14/
            14/
            14/
            14/
            14/
            3,bK,10/
            14/
            3,bB,3,bN,6/
            14/
            14/
            6,rB,7/
            7,rK,6/
            14/
            14
        """.trimIndent())
            val engine = Engine(state)

            val validMoves = engine.legalMoves

            validMoves.filterByMovedPieceType(state, Bishop) shouldBeEqualTo setOf(
                    RegularMove(from = Coordinates.parse("g4"), to = Coordinates.parse("f5")),
                    RegularMove(from = Coordinates.parse("g4"), to = Coordinates.parse("e6")),
                    RegularMove(from = Coordinates.parse("g4"), to = Coordinates.parse("d7"))
            )
        }

        test("must stop check") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,1,1-0,0,0,0-0,0,0,0-0,0,0,0-0-
            14/
            14/
            14/
            14/
            14/
            3,bK,10/
            14/
            14/
            1,bR,12/
            7,bN,6/
            6,rB,7/
            6,rK,7/
            14/
            14
        """.trimIndent())
            val engine = Engine(state)

            val validMoves = engine.legalMoves

            validMoves.filterByMovedPieceType(state, Bishop) shouldBeEqualTo setOf(
                    RegularMove(from = Coordinates.parse("g4"), to = Coordinates.parse("h5"))
            )
        }

        test("cannot stop two or more checks") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,1,1-0,0,0,0-0,0,0,0-0,0,0,0-0-
            14/
            14/
            14/
            14/
            14/
            3,bK,10/
            14/
            14/
            14/
            7,bN,6/
            6,rB,7/
            3,bR,2,rK,7/
            14/
            14
        """.trimIndent())
            val engine = Engine(state)

            val validMoves = engine.legalMoves

            validMoves.filterByMovedPieceType(state, Bishop) shouldBeEqualTo setOf()
        }
    }

    group("knight") {

        test("can jump over other pieces") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,0,0-1,1,1,1-1,1,1,1-0,0,0,0-0-
            3,yR,yN,yB,yK,yQ,yB,1,yR,3/
            3,yP,2,yP,yP,yP,yP,yP,3/
            4,yP,yP,2,yN,5/
            bR,bP,10,gP,gR/
            bN,bP,9,gP,gB,gN/
            1,bP,10,gP,1/
            bK,bB,1,bP,8,gP,gQ/
            bQ,bP,10,gP,gK/
            bB,bP,bN,9,gP,gB/
            1,bP,10,gP,gN/
            bR,bP,10,gR,1/
            5,rN,8/
            3,rP,rP,rP,rP,rP,rP,rP,rP,3/
            3,rR,1,rB,rQ,rK,rB,1,rR,3
        """.trimIndent())
            val engine = Engine(state)

            val moves = engine.legalMoves

            moves.filterByMovedPieceType(state, Knight) shouldBeEqualTo setOf(
                    RegularMove(from = Coordinates.parse("f3"), to = Coordinates.parse("g5")),
                    RegularMove(from = Coordinates.parse("f3"), to = Coordinates.parse("h4")),
                    RegularMove(from = Coordinates.parse("f3"), to = Coordinates.parse("e1")),
                    RegularMove(from = Coordinates.parse("f3"), to = Coordinates.parse("d4")),
                    RegularMove(from = Coordinates.parse("f3"), to = Coordinates.parse("e5"))
            )
        }

        test("cannot move from pin line") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,1,1-0,0,0,0-0,0,0,0-0,0,0,0-0-
            14/
            14/
            14/
            14/
            6,bR,7/
            3,bK,10/
            14/
            14/
            14/
            8,bB,5/
            6,rN,7/
            6,rK,7/
            14/
            14
        """.trimIndent())
            val engine = Engine(state)

            val validMoves = engine.legalMoves

            validMoves.filterByMovedPieceType(state, Knight) shouldBeEqualTo setOf()
        }

        test("must stop check") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,1,1-0,0,0,0-0,0,0,0-0,0,0,0-0-
            14/
            14/
            14/
            14/
            5,bR,8/
            3,bK,10/
            14/
            14/
            14/
            8,bB,5/
            6,rN,7/
            6,rK,7/
            14/
            14
        """.trimIndent())
            val engine = Engine(state)

            val validMoves = engine.legalMoves

            validMoves.filterByMovedPieceType(state, Knight) shouldBeEqualTo setOf(
                    RegularMove(from = Coordinates.parse("g4"), to = Coordinates.parse("i5"))
            )
        }

        test("cannot stop two or more checks") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,1,1-0,0,0,0-0,0,0,0-0,0,0,0-0-
            14/
            14/
            14/
            14/
            14/
            3,bK,10/
            14/
            14/
            14/
            8,bB,5/
            6,rN,7/
            4,bR,1,rK,7/
            14/
            14
        """.trimIndent())
            val engine = Engine(state)

            val validMoves = engine.legalMoves

            validMoves.filterByMovedPieceType(state, Knight) shouldBeEqualTo setOf()
        }
    }

    group("rook") {

        test("can move along file or rank") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,0,0-1,1,1,1-1,1,1,1-0,0,0,0-0-
            3,yR,1,yB,yK,yQ,yB,1,yR,3/
            3,yP,1,yP,yP,yP,yP,yP,yP,3/
            4,yP,yN,2,yN,5/
            bR,bP,10,gP,gR/
            bN,bP,10,gP,1/
            bB,bP,9,gN,gP,gB/
            bK,bP,10,gP,gQ/
            bQ,bP,9,gP,1,gK/
            bB,bP,10,gP,gB/
            2,bP,9,gP,gN/
            bR,bP,1,rR,7,gP,1,gR/
            8,rN,5/
            4,rP,rP,rP,rP,rP,rP,rP,3/
            4,rN,rB,rQ,rK,rB,1,rR,3
        """.trimIndent())
            val engine = Engine(state)

            val moves = engine.legalMoves

            moves.filterByMovedPieceType(state, Rook) shouldBeEqualTo setOf(
                    RegularMove(from = Coordinates.parse("d4"), to = Coordinates.parse("d5")),
                    RegularMove(from = Coordinates.parse("d4"), to = Coordinates.parse("d6")),
                    RegularMove(from = Coordinates.parse("d4"), to = Coordinates.parse("d7")),
                    RegularMove(from = Coordinates.parse("d4"), to = Coordinates.parse("d8")),
                    RegularMove(from = Coordinates.parse("d4"), to = Coordinates.parse("d9")),
                    RegularMove(from = Coordinates.parse("d4"), to = Coordinates.parse("d10")),
                    RegularMove(from = Coordinates.parse("d4"), to = Coordinates.parse("d11")),
                    RegularMove(from = Coordinates.parse("d4"), to = Coordinates.parse("d12")),
                    RegularMove(from = Coordinates.parse("d4"), to = Coordinates.parse("d13")),
                    RegularMove(from = Coordinates.parse("d4"), to = Coordinates.parse("e4")),
                    RegularMove(from = Coordinates.parse("d4"), to = Coordinates.parse("f4")),
                    RegularMove(from = Coordinates.parse("d4"), to = Coordinates.parse("g4")),
                    RegularMove(from = Coordinates.parse("d4"), to = Coordinates.parse("h4")),
                    RegularMove(from = Coordinates.parse("d4"), to = Coordinates.parse("i4")),
                    RegularMove(from = Coordinates.parse("d4"), to = Coordinates.parse("j4")),
                    RegularMove(from = Coordinates.parse("d4"), to = Coordinates.parse("k4")),
                    RegularMove(from = Coordinates.parse("d4"), to = Coordinates.parse("l4")),
                    RegularMove(from = Coordinates.parse("d4"), to = Coordinates.parse("d3")),
                    RegularMove(from = Coordinates.parse("d4"), to = Coordinates.parse("d2")),
                    RegularMove(from = Coordinates.parse("d4"), to = Coordinates.parse("d1")),
                    RegularMove(from = Coordinates.parse("d4"), to = Coordinates.parse("c4")),
                    RegularMove(from = Coordinates.parse("d4"), to = Coordinates.parse("b4")),

                    RegularMove(from = Coordinates.parse("k1"), to = Coordinates.parse("j1"))

            )
        }

        test("cannot move from pin line") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,1,1-0,0,0,0-0,0,0,0-0,0,0,0-0-
            14/
            14/
            14/
            14/
            14/
            3,bK,2,bR,7/
            14/
            14/
            4,bB,9/
            14/
            6,rR,7/
            7,rK,6/
            14/
            14
        """.trimIndent())
            val engine = Engine(state)

            val validMoves = engine.legalMoves

            validMoves.filterByMovedPieceType(state, Rook) shouldBeEqualTo setOf()
        }

        test("can capture pinning piece") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,1,1-0,0,0,0-0,0,0,0-0,0,0,0-0-
            14/
            14/
            14/
            14/
            14/
            3,bK,10/
            14/
            14/
            14/
            11,bB,2/
            14/
            3,bR,2,rR,rK,6/
            14/
            14
        """.trimIndent())
            val engine = Engine(state)

            val validMoves = engine.legalMoves

            validMoves.filterByMovedPieceType(state, Rook) shouldBeEqualTo setOf(
                    RegularMove(from = Coordinates.parse("g3"), to = Coordinates.parse("f3")),
                    RegularMove(from = Coordinates.parse("g3"), to = Coordinates.parse("e3")),
                    RegularMove(from = Coordinates.parse("g3"), to = Coordinates.parse("d3"))
            )
        }

        test("must stop check") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,1,1-0,0,0,0-0,0,0,0-0,0,0,0-0-
            14/
            14/
            14/
            14/
            14/
            3,bK,2,bR,7/
            14/
            14/
            14/
            14/
            6,rR,1,bB,5/
            7,rK,6/
            14/
            14
        """.trimIndent())
            val engine = Engine(state)

            val validMoves = engine.legalMoves

            validMoves.filterByMovedPieceType(state, Rook) shouldBeEqualTo setOf(
                    RegularMove(from = Coordinates.parse("g4"), to = Coordinates.parse("i4"))
            )
        }

        test("cannot stop two or more checks") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,1,1-0,0,0,0-0,0,0,0-0,0,0,0-0-
            14/
            14/
            14/
            14/
            14/
            3,bK,10/
            14/
            14/
            14/
            14/
            6,rR,1,bB,5/
            4,bR,2,rK,6/
            14/
            14
        """.trimIndent())
            val engine = Engine(state)

            val validMoves = engine.legalMoves

            validMoves.filterByMovedPieceType(state, Rook) shouldBeEqualTo setOf()
        }
    }

    group("queen") {

        test("can move on diagonals and along file or rank") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,0,0-1,1,1,1-1,1,1,1-0,0,0,0-0-
            3,yR,1,yB,yK,yQ,yB,1,yR,3/
            3,yP,yP,yP,yP,yP,yP,yP,4/
            5,yN,2,yN,1,yP,3/
            bR,bP,10,gP,gR/
            1,bP,10,gP,1/
            bB,bP,bN,8,gN,gP,gB/
            bK,1,bP,9,gP,gQ/
            bQ,1,bP,9,gP,gK/
            bB,bP,7,rQ,1,gN,gP,gB/
            bN,bP,10,gP,1/
            bR,bP,4,rP,4,gP,1,gR/
            14/
            3,rP,rP,rP,1,rP,rP,rP,rP,3/
            3,rR,rN,rB,1,rK,rB,rN,rR,3
        """.trimIndent())
            val engine = Engine(state)

            val moves = engine.legalMoves

            moves.filterByMovedPieceType(state, Queen) shouldBeEqualTo setOf(
                    RegularMove(from = Coordinates.parse("j6"), to = Coordinates.parse("j7")),
                    RegularMove(from = Coordinates.parse("j6"), to = Coordinates.parse("j8")),
                    RegularMove(from = Coordinates.parse("j6"), to = Coordinates.parse("j9")),
                    RegularMove(from = Coordinates.parse("j6"), to = Coordinates.parse("j10")),
                    RegularMove(from = Coordinates.parse("j6"), to = Coordinates.parse("j11")),
                    RegularMove(from = Coordinates.parse("j6"), to = Coordinates.parse("j12")),
                    RegularMove(from = Coordinates.parse("j6"), to = Coordinates.parse("j13")),

                    RegularMove(from = Coordinates.parse("j6"), to = Coordinates.parse("k7")),
                    RegularMove(from = Coordinates.parse("j6"), to = Coordinates.parse("l8")),
                    RegularMove(from = Coordinates.parse("j6"), to = Coordinates.parse("m9")),

                    RegularMove(from = Coordinates.parse("j6"), to = Coordinates.parse("k6")),
                    RegularMove(from = Coordinates.parse("j6"), to = Coordinates.parse("l6")),

                    RegularMove(from = Coordinates.parse("j6"), to = Coordinates.parse("k5")),
                    RegularMove(from = Coordinates.parse("j6"), to = Coordinates.parse("l4")),

                    RegularMove(from = Coordinates.parse("j6"), to = Coordinates.parse("j5")),
                    RegularMove(from = Coordinates.parse("j6"), to = Coordinates.parse("j4")),
                    RegularMove(from = Coordinates.parse("j6"), to = Coordinates.parse("j3")),

                    RegularMove(from = Coordinates.parse("j6"), to = Coordinates.parse("i5")),
                    RegularMove(from = Coordinates.parse("j6"), to = Coordinates.parse("h4")),
                    RegularMove(from = Coordinates.parse("j6"), to = Coordinates.parse("g3")),

                    RegularMove(from = Coordinates.parse("j6"), to = Coordinates.parse("i6")),
                    RegularMove(from = Coordinates.parse("j6"), to = Coordinates.parse("h6")),
                    RegularMove(from = Coordinates.parse("j6"), to = Coordinates.parse("g6")),
                    RegularMove(from = Coordinates.parse("j6"), to = Coordinates.parse("f6")),
                    RegularMove(from = Coordinates.parse("j6"), to = Coordinates.parse("e6")),
                    RegularMove(from = Coordinates.parse("j6"), to = Coordinates.parse("d6")),
                    RegularMove(from = Coordinates.parse("j6"), to = Coordinates.parse("c6")),
                    RegularMove(from = Coordinates.parse("j6"), to = Coordinates.parse("b6")),

                    RegularMove(from = Coordinates.parse("j6"), to = Coordinates.parse("i7")),
                    RegularMove(from = Coordinates.parse("j6"), to = Coordinates.parse("h8")),
                    RegularMove(from = Coordinates.parse("j6"), to = Coordinates.parse("g9")),
                    RegularMove(from = Coordinates.parse("j6"), to = Coordinates.parse("f10")),
                    RegularMove(from = Coordinates.parse("j6"), to = Coordinates.parse("e11")),
                    RegularMove(from = Coordinates.parse("j6"), to = Coordinates.parse("d12"))
            )
        }

        test("cannot move from pin line") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,1,1-0,0,0,0-0,0,0,0-0,0,0,0-0-
            14/
            14/
            14/
            14/
            5,bK,bB,7/
            6,bR,7/
            14/
            14/
            8,bN,5/
            14/
            6,rQ,7/
            6,rK,7/
            14/
            14
        """.trimIndent())
            val engine = Engine(state)

            val validMoves = engine.legalMoves

            validMoves.filterByMovedPieceType(state, Queen) shouldBeEqualTo setOf(
                    RegularMove(from = Coordinates.parse("g4"), to = Coordinates.parse("g5")),
                    RegularMove(from = Coordinates.parse("g4"), to = Coordinates.parse("g6")),
                    RegularMove(from = Coordinates.parse("g4"), to = Coordinates.parse("g7")),
                    RegularMove(from = Coordinates.parse("g4"), to = Coordinates.parse("g8")),
                    RegularMove(from = Coordinates.parse("g4"), to = Coordinates.parse("g9"))
            )
        }

        test("must stop check") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,1,1-0,0,0,0-0,0,0,0-0,0,0,0-0-
            14/
            14/
            14/
            14/
            6,bB,7/
            5,bK,8/
            8,bR,5/
            14/
            14/
            14/
            6,rQ,1,bN,5/
            6,rK,7/
            14/
            14
        """.trimIndent())
            val engine = Engine(state)

            val validMoves = engine.legalMoves

            validMoves.filterByMovedPieceType(state, Queen) shouldBeEqualTo setOf(
                    RegularMove(from = Coordinates.parse("g4"), to = Coordinates.parse("i4"))
            )
        }

        test("cannot stop two or more checks") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,1,1-0,0,0,0-0,0,0,0-0,0,0,0-0-
            14/
            14/
            14/
            14/
            6,bB,7/
            5,bK,8/
            14/
            14/
            14/
            14/
            6,rQ,1,bN,5/
            5,bR,rK,7/
            14/
            14
        """.trimIndent())
            val engine = Engine(state)

            val validMoves = engine.legalMoves

            validMoves.filterByMovedPieceType(state, Queen) shouldBeEqualTo setOf()
        }
    }

    group("king") {

        test("can go to every adjacent square if not controlled by piece of other color") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,0,0-0,0,0,0-0,0,0,0-0,0,0,0-0-
            9,yK,4/
            7,yP,yP,yP,yP,3/
            3,yB,4,yN,5/
            1,bP,3,yP,yP,2,gB,4/
            bK,bP,10,gP,gN/
            bB,13/
            14/
            12,gP,gK/
            1,bP,bN,8,gN,1,gB/
            1,bP,11,gR/
            5,rP,8/
            14/
            4,rK,9/
            14
        """.trimIndent())
            val engine = Engine(state)

            val validMoves = engine.legalMoves

            validMoves.filterByMovedPieceType(state, King) shouldBeEqualTo setOf(
                    RegularMove(from = Coordinates.parse("e2"), to = Coordinates.parse("e3")),
                    RegularMove(from = Coordinates.parse("e2"), to = Coordinates.parse("f3")),
                    RegularMove(from = Coordinates.parse("e2"), to = Coordinates.parse("f2")),
                    RegularMove(from = Coordinates.parse("e2"), to = Coordinates.parse("f1")),
                    RegularMove(from = Coordinates.parse("e2"), to = Coordinates.parse("e1")),
                    RegularMove(from = Coordinates.parse("e2"), to = Coordinates.parse("d1")),
                    RegularMove(from = Coordinates.parse("e2"), to = Coordinates.parse("d2")),
                    RegularMove(from = Coordinates.parse("e2"), to = Coordinates.parse("d3"))
            )
        }

        test("can capture other piece") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,0,0-0,0,0,0-0,0,0,0-0,0,0,0-0-
            9,yK,4/
            7,yP,yP,yP,yP,3/
            3,yB,4,yN,5/
            1,bP,3,yP,yP,6,gB/
            bK,bP,10,gP,gN/
            bB,13/
            14/
            12,gP,gK/
            1,bP,9,gN,1,gB/
            1,bP,11,gR/
            5,rP,8/
            3,bN,10/
            4,rK,9/
            14
        """.trimIndent())
            val engine = Engine(state)

            val validMoves = engine.legalMoves

            validMoves.filterByMovedPieceType(state, King) shouldBeEqualTo setOf(
                    RegularMove(from = Coordinates.parse("e2"), to = Coordinates.parse("e3")),
                    RegularMove(from = Coordinates.parse("e2"), to = Coordinates.parse("f3")),
                    RegularMove(from = Coordinates.parse("e2"), to = Coordinates.parse("f1")),
                    RegularMove(from = Coordinates.parse("e2"), to = Coordinates.parse("d1")),
                    RegularMove(from = Coordinates.parse("e2"), to = Coordinates.parse("d2")),
                    RegularMove(from = Coordinates.parse("e2"), to = Coordinates.parse("d3"))
            )
        }

        test("cannot castle when destination square is occupied") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,0,0-1,1,1,1-1,1,1,1-0,0,0,0-0-
            3,yR,1,yB,yK,yQ,yB,1,yR,3/
            3,yP,yP,2,yP,yP,yP,yP,3/
            3,yN,1,yP,yP,1,yN,5/
            bR,bP,10,gP,gR/
            1,bP,9,gP,2/
            bB,bP,bN,8,gN,gP,gB/
            bK,bP,9,gP,1,gQ/
            bQ,1,bP,9,gP,gK/
            bB,1,bP,8,gN,gP,gB/
            1,bP,10,gP,1/
            bR,bP,bN,2,rB,6,gP,gR/
            5,rN,1,rP,6/
            3,rP,rP,rP,rP,rQ,rP,rP,rP,3/
            3,rR,1,rB,1,rK,1,rN,rR,3
        """.trimIndent())
            val engine = Engine(state)

            val validMoves = engine.legalMoves

            validMoves.filterByMovedPieceType(state, King) shouldBeEqualTo setOf(
                    RegularMove(from = Coordinates.parse("h1"), to = Coordinates.parse("i1")),
                    RegularMove(from = Coordinates.parse("h1"), to = Coordinates.parse("g1"))
            )
        }

        test("cannot castle king side when rook destination square is controlled by piece of other color") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,0,0-1,1,1,1-1,1,1,1-0,0,0,0-0-
            3,yR,1,yB,yK,yQ,yB,1,yR,3/
            4,yP,yP,yP,yP,yP,yP,4/
            3,yP,1,yN,2,yN,1,yP,3/
            bR,1,bP,8,gP,1,gR/
            bN,bP,10,gP,1/
            bB,1,bP,8,gN,gP,gB/
            bK,1,bP,9,gP,gQ/
            bQ,bP,10,gP,gK/
            bB,bP,9,gN,gP,gB/
            1,bP,10,gP,1/
            bR,bP,bN,8,gP,1,gR/
            7,rP,rN,rP,4/
            3,rP,rP,rP,rP,1,rP,rB,rP,3/
            3,rR,rN,rB,rQ,rK,2,rR,3
        """.trimIndent())
            val engine = Engine(state)

            val validMoves = engine.legalMoves

            validMoves.filterByMovedPieceType(state, King) shouldBeEqualTo setOf()
        }

        test("cannot castle king side when king destination square is controlled by piece of other color") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,0,0-1,1,1,1-1,1,1,1-0,0,0,0-0-
            3,yR,1,yB,yK,yQ,yB,1,yR,3/
            4,yP,yP,yP,yP,yP,yP,4/
            3,yP,1,yN,2,yN,1,gP,3/
            bR,1,bP,10,gR/
            bN,bP,10,gP,1/
            bB,bP,9,gN,gP,gB/
            bK,bP,bB,9,gP,gQ/
            bQ,1,bP,9,gP,gK/
            1,bP,9,gN,gP,gB/
            bN,bP,10,gP,1/
            bR,1,bP,4,rP,4,gP,gR/
            6,rB,1,rP,5/
            3,rP,rP,rP,rP,rN,1,rP,rP,3/
            3,rR,rN,rB,rQ,rK,2,rR,3
        """.trimIndent())
            val engine = Engine(state)

            val validMoves = engine.legalMoves

            validMoves.filterByMovedPieceType(state, King) shouldBeEqualTo setOf(
                    RegularMove(from = Coordinates.parse("h1"), to = Coordinates.parse("i1"))
            )
        }

        test("cannot castle queen side when rook destination square is controlled by piece of other color") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,0,0-1,1,1,1-1,1,1,1-0,0,0,0-0-
            3,yR,1,yB,yK,yQ,yB,1,yR,3/
            5,yP,yP,yP,yP,yP,4/
            4,yP,yN,2,yN,1,gP,3/
            bR,2,bP,9,gR/
            11,gP,2/
            bB,bP,bN,8,gN,gP,gB/
            bK,bP,10,gP,gQ/
            bQ,bP,9,gP,1,gK/
            bB,1,bP,8,gN,gP,gB/
            1,bP,10,gP,1/
            bR,bP,bN,9,gP,gR/
            3,rN,1,rP,rP,rP,6/
            3,rP,rP,1,rB,rQ,rP,rP,rP,3/
            3,rR,3,rK,rB,rN,rR,3
        """.trimIndent())
            val engine = Engine(state)

            val validMoves = engine.legalMoves

            validMoves.filterByMovedPieceType(state, King) shouldBeEqualTo setOf()
        }

        test("cannot castle queen side when king destination square is controlled by piece of other color") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,0,0-1,1,1,1-1,1,1,1-0,0,0,0-0-
            3,yR,1,yB,yK,yQ,yB,1,yR,3/
            4,yP,yP,yP,yP,yP,gP,4/
            5,yN,2,yN,5/
            bR,1,yP,10,gR/
            2,bP,9,gP,1/
            bB,bP,9,gN,gP,gB/
            bK,bP,bN,9,gP,gQ/
            bQ,bP,10,gP,gK/
            bB,bP,bN,8,gN,gP,gB/
            2,bP,9,gP,1/
            bR,bP,4,rP,5,gP,gR/
            4,rP,rN,rQ,7/
            3,rP,1,rP,rB,rP,rP,rP,rP,3/
            3,rR,3,rK,rB,rN,rR,3
        """.trimIndent())
            val engine = Engine(state)

            val validMoves = engine.legalMoves

            validMoves.filterByMovedPieceType(state, King) shouldBeEqualTo setOf(
                    RegularMove(from = Coordinates.parse("h1"), to = Coordinates.parse("g1"))
            )
        }

        test("can castle queen side when knight square is controlled by piece of other color") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,0,0-1,1,1,1-1,1,1,1-0,0,0,0-0-
            3,yR,1,yB,yK,yQ,yB,1,yR,3/
            4,yP,yP,yP,yP,yP,yP,yP,3/
            3,yN,4,yN,5/
            bR,10,gP,1,gR/
            2,bP,9,gP,gN/
            bB,bP,bN,9,gP,gB/
            bK,bP,9,gB,gP,gQ/
            bQ,bP,9,gP,1,gK/
            bB,bP,bN,8,gN,gP,1/
            1,bP,10,gP,1/
            bR,bP,4,rP,5,gP,gR/
            3,rN,1,rP,1,rB,6/
            3,rP,rP,1,rQ,rP,rP,rP,rP,3/
            3,rR,3,rK,rB,rN,rR,3
        """.trimIndent())
            val engine = Engine(state)

            val validMoves = engine.legalMoves

            validMoves.filterByMovedPieceType(state, King) shouldBeEqualTo setOf(
                    RegularMove(from = Coordinates.parse("h1"), to = Coordinates.parse("f1")),
                    RegularMove(from = Coordinates.parse("h1"), to = Coordinates.parse("g1"))
            )
        }

        test("cannot castle when any square between king and rook is occupied") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,0,0-1,1,1,1-1,1,1,1-0,0,0,0-0-
            3,yR,yN,yB,yK,1,yB,yN,yR,3/
            3,yP,yP,yP,1,yP,yP,yP,yP,3/
            5,yQ,yP,7/
            bR,bP,10,gP,gR/
            bN,bP,10,gP,gN/
            bB,bP,10,gP,gB/
            bK,bP,10,gP,gQ/
            bQ,bP,10,gP,gK/
            bB,bP,10,gP,gB/
            bN,bP,10,gP,gN/
            bR,bP,10,gP,gR/
            3,rN,rP,2,rP,6/
            3,rP,rP,1,rP,rQ,rP,rP,rP,3/
            3,rR,3,rK,rB,rN,rR,3
        """.trimIndent())
            val engine = Engine(state)

            val validMoves = engine.legalMoves

            validMoves.filterByMovedPieceType(state, King) shouldBeEqualTo setOf(
                    RegularMove(from = Coordinates.parse("h1"), to = Coordinates.parse("g1"))
            )
        }

        test("cannot castle when king or rook moved before") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,0,0-0,1,1,1-0,1,1,1-0,0,0,0-0-
            3,yR,1,yB,yK,yQ,yB,1,yR,3/
            5,yP,1,yP,yP,yP,4/
            5,yN,2,yN,1,yP,3/
            bR,1,yP,1,yP,1,yP,4,gP,1,gR/
            1,bP,8,gP,3/
            bB,bP,bN,9,gP,gB/
            bK,1,bP,bN,8,gP,gQ/
            bQ,bP,8,gN,1,gP,gK/
            bB,1,bP,8,gN,gP,1/
            2,bP,8,gP,2/
            bR,2,bP,7,gP,1,gR/
            4,rP,rN,rP,1,rN,rP,4/
            3,rP,rB,rP,rQ,rP,rP,1,rP,3/
            3,rR,3,rK,2,rR,3
        """.trimIndent())
            val engine = Engine(state)

            val validMoves = engine.legalMoves

            validMoves.filterByMovedPieceType(state, King) shouldBeEqualTo setOf(
                    RegularMove(from = Coordinates.parse("h1"), to = Coordinates.parse("i1")),
                    RegularMove(from = Coordinates.parse("h1"), to = Coordinates.parse("g1"))
            )
        }

        test("cannot castle while checked") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,0,0-1,1,1,1-1,1,1,1-0,0,0,0-0-
            3,yR,1,yB,yK,yQ,yB,1,yR,3/
            4,yP,yP,yP,yP,yP,yP,yP,3/
            5,yN,2,yN,5/
            bR,1,yP,9,gP,gR/
            1,bP,10,gP,gN/
            bB,bP,bN,9,gP,gB/
            bK,bP,8,gP,3/
            bQ,bP,10,gP,gK/
            bB,bP,bN,9,gP,gB/
            1,bP,9,gQ,gP,gN/
            bR,1,bP,8,gP,1,gR/
            6,rB,rP,rP,5/
            3,rP,rP,rP,rP,rN,1,rP,rP,3/
            3,rR,rN,rB,rQ,rK,2,rR,3
        """.trimIndent())
            val engine = Engine(state)

            val validMoves = engine.legalMoves

            validMoves.filterByMovedPieceType(state, King) shouldBeEqualTo setOf(
                    RegularMove(from = Coordinates.parse("h1"), to = Coordinates.parse("i1"))
            )
        }

        test("can castle king side") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,0,0-1,1,1,1-1,1,1,1-0,0,0,0-0-
            3,yR,1,yB,yK,yQ,yB,1,yR,3/
            4,yP,yP,yP,yP,yP,yP,yP,3/
            3,yP,1,yN,2,yN,5/
            bR,bP,9,gP,1,gR/
            1,bP,10,gP,1/
            bB,bP,bN,8,gN,gP,gB/
            bK,bP,10,gP,gQ/
            bQ,bP,10,gP,gK/
            bB,bP,bN,8,gN,gP,gB/
            1,bP,10,gP,1/
            bR,1,bP,9,gP,gR/
            7,rP,rN,5/
            3,rP,rP,rP,rP,rB,rP,rP,rP,3/
            3,rR,rN,rB,rQ,rK,2,rR,3
        """.trimIndent())
            val engine = Engine(state)

            val validMoves = engine.legalMoves

            validMoves.filterByMovedPieceType(state, King) shouldBeEqualTo setOf(
                    RegularMove(from = Coordinates.parse("h1"), to = Coordinates.parse("j1")),
                    RegularMove(from = Coordinates.parse("h1"), to = Coordinates.parse("i1"))
            )
        }

        test("can castle queen side") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,0,0-1,1,1,1-1,1,1,1-0,0,0,0-0-
            3,yR,1,yB,yK,yQ,yB,1,yR,3/
            3,yP,1,yP,1,yP,yP,1,yP,3/
            4,yP,yN,yP,1,yN,yP,4/
            bR,bP,9,gP,1,gR/
            1,bP,10,gP,1/
            bB,bP,bN,8,gN,gP,gB/
            bK,1,bP,9,gP,gQ/
            bQ,1,bP,8,gP,gN,gK/
            bB,bP,bN,9,gP,gB/
            2,bP,9,gP,1/
            bR,bP,9,gP,1,gR/
            4,rP,rN,1,rP,rQ,5/
            3,rP,rB,rP,rP,1,rP,rP,rP,3/
            3,rR,3,rK,rB,rN,rR,3
        """.trimIndent())
            val engine = Engine(state)

            val validMoves = engine.legalMoves

            validMoves.filterByMovedPieceType(state, King) shouldBeEqualTo setOf(
                    RegularMove(from = Coordinates.parse("h1"), to = Coordinates.parse("f1")),
                    RegularMove(from = Coordinates.parse("h1"), to = Coordinates.parse("h2")),
                    RegularMove(from = Coordinates.parse("h1"), to = Coordinates.parse("g1"))
            )
        }

        test("can castle both king and queen side") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,0,0-1,1,1,1-1,1,1,1-0,0,0,0-0-
            3,yR,1,yB,yK,yQ,yB,1,yR,3/
            5,yP,2,yP,1,yP,3/
            3,yP,yP,yN,yP,yP,yN,yP,4/
            bR,bP,9,gP,1,gR/
            1,bP,8,gP,3/
            bB,bP,bN,8,gN,gP,gB/
            bK,1,bP,bN,8,gP,gQ/
            bQ,1,bP,8,gP,gN,gK/
            bB,bP,10,gP,gB/
            2,bP,9,gP,1/
            bR,1,bP,8,gP,1,gR/
            4,rP,rN,rB,rP,rQ,5/
            3,rP,rB,rP,rP,rN,rP,rP,rP,3/
            3,rR,3,rK,2,rR,3
        """.trimIndent())
            val engine = Engine(state)

            val validMoves = engine.legalMoves

            validMoves.filterByMovedPieceType(state, King) shouldBeEqualTo setOf(
                    RegularMove(from = Coordinates.parse("h1"), to = Coordinates.parse("j1")),
                    RegularMove(from = Coordinates.parse("h1"), to = Coordinates.parse("f1")),
                    RegularMove(from = Coordinates.parse("h1"), to = Coordinates.parse("i1")),
                    RegularMove(from = Coordinates.parse("h1"), to = Coordinates.parse("g1"))

            )
        }

        test("can go to squares controlled by eliminated color") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,0,1-0,0,0,0-0,0,0,0-0,0,0,0-0-
            5,yB,8/
            5,yP,yK,yB,6/
            6,yP,7/
            8,yP,4,gR/
            5,bR,6,gP,1/
            3,bK,bP,7,gP,gB/
            4,bP,6,gB,2/
            8,rP,rP,rB,gP,1,gK/
            8,rN,3,gP,gN/
            10,rK,1,gP,1/
            14/
            14/
            14/
            14
        """.trimIndent())
            val engine = Engine(state)

            val validMoves = engine.legalMoves

            validMoves.filterByMovedPieceType(state, King) shouldBeEqualTo setOf(
                    RegularMove(from = Coordinates.parse("k5"), to = Coordinates.parse("k6")),
                    RegularMove(from = Coordinates.parse("k5"), to = Coordinates.parse("l6")),
                    RegularMove(from = Coordinates.parse("k5"), to = Coordinates.parse("l5")),
                    RegularMove(from = Coordinates.parse("k5"), to = Coordinates.parse("l4")),
                    RegularMove(from = Coordinates.parse("k5"), to = Coordinates.parse("k4")),
                    RegularMove(from = Coordinates.parse("k5"), to = Coordinates.parse("j4")),
                    RegularMove(from = Coordinates.parse("k5"), to = Coordinates.parse("j5")),
                    RegularMove(from = Coordinates.parse("k5"), to = Coordinates.parse("j6"))
            )
        }

        test("cannot be captured via discovered attack") {
            val state = parseStateFromFenOrThrow("""
            Y-0,0,0,0-1,1,1,1-1,1,1,1-0,0,0,0-0-
            3,yR,yN,yB,yK,1,yB,yN,yR,3/
            4,yP,yP,1,yP,yP,yP,4/
            3,yP,2,yP,3,yP,3/
            bR,bP,9,gP,1,gR/
            bN,bP,5,yQ,2,gP,2,gN/
            bB,bP,9,gP,1,gB/
            bK,1,bP,9,gP,1/
            bQ,bP,9,gP,1,gK/
            bB,bP,10,gP,gB/
            1,bP,7,bN,2,gP,gN/
            bR,bP,10,gP,gR/
            3,rP,rP,rN,2,rB,1,rN,3/
            5,rP,rP,1,rP,rP,rP,3/
            3,rR,1,rB,rQ,rK,2,rR,3
        """.trimIndent())
            val engine = Engine(state)

            val validMoves = engine.legalMoves

            validMoves.filterByMovedPieceType(state, Queen) shouldNotContain RegularMove(
                    from = Coordinates.parse("h10"), to = Coordinates.parse("h1")
            )
            validMoves.filterByMovedPieceType(state, Queen) shouldContain RegularMove(
                    from = Coordinates.parse("h10"), to = Coordinates.parse("h2")
            )
        }
    }
})