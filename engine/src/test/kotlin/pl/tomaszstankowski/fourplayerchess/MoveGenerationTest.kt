package pl.tomaszstankowski.fourplayerchess

import org.amshove.kluent.shouldBeEqualTo
import org.spekframework.spek2.Spek
import pl.tomaszstankowski.fourplayerchess.engine.*
import pl.tomaszstankowski.fourplayerchess.engine.PieceType.*

class MoveGenerationTest : Spek({

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

            val validMoves = engine.stateFeatures.legalMoves

            validMoves.filterByMovedPieceType(state, Pawn) shouldBeEqualTo listOf(
                    Move(from = Position.parse("g2"), to = Position.parse("g3")),
                    Move(from = Position.parse("g2"), to = Position.parse("g4"))
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

            val validMoves = engine.stateFeatures.legalMoves

            validMoves.filterByMovedPieceType(state, Pawn) shouldBeEqualTo listOf()
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

            val validMoves = engine.stateFeatures.legalMoves

            validMoves.filterByMovedPieceType(state, Pawn) shouldBeEqualTo listOf(
                    Move(from = Position.parse("g2"), to = Position.parse("g3"))
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

            val validMoves = engine.stateFeatures.legalMoves

            validMoves.filterByMovedPieceType(state, Pawn) shouldBeEqualTo listOf()
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

            val validMoves = engine.stateFeatures.legalMoves

            validMoves.filterByMovedPieceType(state, Pawn) shouldBeEqualTo listOf(
                    Move(from = Position.parse("g2"), to = Position.parse("g3"))
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

            val validMoves = engine.stateFeatures.legalMoves

            validMoves.filterByMovedPieceType(state, Pawn) shouldBeEqualTo listOf(
                    Move(from = Position.parse("g3"), to = Position.parse("g4"))
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

            val validMoves = engine.stateFeatures.legalMoves

            validMoves.filterByMovedPieceType(state, Pawn) shouldBeEqualTo listOf()
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

            val validMoves = engine.stateFeatures.legalMoves

            validMoves.filterByMovedPieceType(state, Pawn) shouldBeEqualTo listOf()
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

            val validMoves = engine.stateFeatures.legalMoves

            validMoves.filterByMovedPieceType(state, Pawn) shouldBeEqualTo listOf(
                    Move(from = Position.parse("g3"), to = Position.parse("g4")),
                    Move(from = Position.parse("g3"), to = Position.parse("h4"))
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

            val validMoves = engine.stateFeatures.legalMoves

            validMoves.filterByMovedPieceType(state, Pawn) shouldBeEqualTo listOf(
                    Move(from = Position.parse("g3"), to = Position.parse("g4")),
                    Move(from = Position.parse("g3"), to = Position.parse("f4"))
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

            val validMoves = engine.stateFeatures.legalMoves

            validMoves.filterByMovedPieceType(state, Pawn) shouldBeEqualTo listOf(
                    Move(from = Position.parse("g3"), to = Position.parse("g4")),
                    Move(from = Position.parse("g3"), to = Position.parse("f4")),
                    Move(from = Position.parse("g3"), to = Position.parse("h4"))
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

            val validMoves = engine.stateFeatures.legalMoves

            validMoves.filterByMovedPieceType(state, Pawn) shouldBeEqualTo listOf(
                    Move(from = Position.parse("k3"), to = Position.parse("l4"))
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

            val validMoves = engine.stateFeatures.legalMoves

            validMoves.filterByMovedPieceType(state, Pawn) shouldBeEqualTo listOf(
                    Move(from = Position.parse("d3"), to = Position.parse("c4"))
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

            val validMoves = engine.stateFeatures.legalMoves

            validMoves.filterByMovedPieceType(state, Pawn) shouldBeEqualTo listOf(
                    Move(from = Position.parse("g4"), to = Position.parse("g5"))
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

            val validMoves = engine.stateFeatures.legalMoves

            validMoves.filterByMovedPieceType(state, Pawn) shouldBeEqualTo listOf(
                    Move(from = Position.parse("g4"), to = Position.parse("h5"))
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

            val validMoves = engine.stateFeatures.legalMoves

            validMoves.filterByMovedPieceType(state, Pawn) shouldBeEqualTo listOf()
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

            val validMoves = engine.stateFeatures.legalMoves

            validMoves.filterByMovedPieceType(state, Bishop) shouldBeEqualTo listOf(
                    Move(from = Position.parse("i4"), to = Position.parse("j5")),
                    Move(from = Position.parse("i4"), to = Position.parse("k6")),
                    Move(from = Position.parse("i4"), to = Position.parse("l7")),
                    Move(from = Position.parse("i4"), to = Position.parse("m8")),
                    Move(from = Position.parse("i4"), to = Position.parse("n9")),
                    Move(from = Position.parse("i4"), to = Position.parse("h5")),
                    Move(from = Position.parse("i4"), to = Position.parse("g6")),
                    Move(from = Position.parse("i4"), to = Position.parse("f7")),
                    Move(from = Position.parse("i4"), to = Position.parse("e8")),
                    Move(from = Position.parse("i4"), to = Position.parse("d9")),
                    Move(from = Position.parse("i4"), to = Position.parse("c10")),
                    Move(from = Position.parse("i4"), to = Position.parse("b11")),
                    Move(from = Position.parse("i4"), to = Position.parse("h3")),
                    Move(from = Position.parse("i4"), to = Position.parse("g2")),
                    Move(from = Position.parse("i4"), to = Position.parse("f1")),
                    Move(from = Position.parse("i4"), to = Position.parse("j3"))
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

            val validMoves = engine.stateFeatures.legalMoves

            validMoves.filterByMovedPieceType(state, Bishop) shouldBeEqualTo listOf()
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

            val validMoves = engine.stateFeatures.legalMoves

            validMoves.filterByMovedPieceType(state, Bishop) shouldBeEqualTo listOf(
                    Move(from = Position.parse("g4"), to = Position.parse("f5")),
                    Move(from = Position.parse("g4"), to = Position.parse("e6")),
                    Move(from = Position.parse("g4"), to = Position.parse("d7"))
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

            val validMoves = engine.stateFeatures.legalMoves

            validMoves.filterByMovedPieceType(state, Bishop) shouldBeEqualTo listOf(
                    Move(from = Position.parse("g4"), to = Position.parse("h5"))
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

            val validMoves = engine.stateFeatures.legalMoves

            validMoves.filterByMovedPieceType(state, Bishop) shouldBeEqualTo listOf()
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

            val moves = engine.stateFeatures.legalMoves

            moves.filterByMovedPieceType(state, Knight) shouldBeEqualTo listOf(
                    Move(from = Position.parse("f3"), to = Position.parse("g5")),
                    Move(from = Position.parse("f3"), to = Position.parse("h4")),
                    Move(from = Position.parse("f3"), to = Position.parse("e1")),
                    Move(from = Position.parse("f3"), to = Position.parse("d4")),
                    Move(from = Position.parse("f3"), to = Position.parse("e5"))
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

            val validMoves = engine.stateFeatures.legalMoves

            validMoves.filterByMovedPieceType(state, Knight) shouldBeEqualTo listOf()
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

            val validMoves = engine.stateFeatures.legalMoves

            validMoves.filterByMovedPieceType(state, Knight) shouldBeEqualTo listOf(
                    Move(from = Position.parse("g4"), to = Position.parse("i5"))
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

            val validMoves = engine.stateFeatures.legalMoves

            validMoves.filterByMovedPieceType(state, Knight) shouldBeEqualTo listOf()
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

            val moves = engine.stateFeatures.legalMoves

            moves.filterByMovedPieceType(state, Rook) shouldBeEqualTo listOf(
                    Move(from = Position.parse("d4"), to = Position.parse("d5")),
                    Move(from = Position.parse("d4"), to = Position.parse("d6")),
                    Move(from = Position.parse("d4"), to = Position.parse("d7")),
                    Move(from = Position.parse("d4"), to = Position.parse("d8")),
                    Move(from = Position.parse("d4"), to = Position.parse("d9")),
                    Move(from = Position.parse("d4"), to = Position.parse("d10")),
                    Move(from = Position.parse("d4"), to = Position.parse("d11")),
                    Move(from = Position.parse("d4"), to = Position.parse("d12")),
                    Move(from = Position.parse("d4"), to = Position.parse("d13")),
                    Move(from = Position.parse("d4"), to = Position.parse("e4")),
                    Move(from = Position.parse("d4"), to = Position.parse("f4")),
                    Move(from = Position.parse("d4"), to = Position.parse("g4")),
                    Move(from = Position.parse("d4"), to = Position.parse("h4")),
                    Move(from = Position.parse("d4"), to = Position.parse("i4")),
                    Move(from = Position.parse("d4"), to = Position.parse("j4")),
                    Move(from = Position.parse("d4"), to = Position.parse("k4")),
                    Move(from = Position.parse("d4"), to = Position.parse("l4")),
                    Move(from = Position.parse("d4"), to = Position.parse("d3")),
                    Move(from = Position.parse("d4"), to = Position.parse("d2")),
                    Move(from = Position.parse("d4"), to = Position.parse("d1")),
                    Move(from = Position.parse("d4"), to = Position.parse("c4")),
                    Move(from = Position.parse("d4"), to = Position.parse("b4")),

                    Move(from = Position.parse("k1"), to = Position.parse("j1"))

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

            val validMoves = engine.stateFeatures.legalMoves

            validMoves.filterByMovedPieceType(state, Rook) shouldBeEqualTo listOf()
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

            val validMoves = engine.stateFeatures.legalMoves

            validMoves.filterByMovedPieceType(state, Rook) shouldBeEqualTo listOf(
                    Move(from = Position.parse("g3"), to = Position.parse("f3")),
                    Move(from = Position.parse("g3"), to = Position.parse("e3")),
                    Move(from = Position.parse("g3"), to = Position.parse("d3"))
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

            val validMoves = engine.stateFeatures.legalMoves

            validMoves.filterByMovedPieceType(state, Rook) shouldBeEqualTo listOf(
                    Move(from = Position.parse("g4"), to = Position.parse("i4"))
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

            val validMoves = engine.stateFeatures.legalMoves

            validMoves.filterByMovedPieceType(state, Rook) shouldBeEqualTo listOf()
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

            val moves = engine.stateFeatures.legalMoves

            moves.filterByMovedPieceType(state, Queen) shouldBeEqualTo listOf(
                    Move(from = Position.parse("j6"), to = Position.parse("j7")),
                    Move(from = Position.parse("j6"), to = Position.parse("j8")),
                    Move(from = Position.parse("j6"), to = Position.parse("j9")),
                    Move(from = Position.parse("j6"), to = Position.parse("j10")),
                    Move(from = Position.parse("j6"), to = Position.parse("j11")),
                    Move(from = Position.parse("j6"), to = Position.parse("j12")),
                    Move(from = Position.parse("j6"), to = Position.parse("j13")),

                    Move(from = Position.parse("j6"), to = Position.parse("k7")),
                    Move(from = Position.parse("j6"), to = Position.parse("l8")),
                    Move(from = Position.parse("j6"), to = Position.parse("m9")),

                    Move(from = Position.parse("j6"), to = Position.parse("k6")),
                    Move(from = Position.parse("j6"), to = Position.parse("l6")),

                    Move(from = Position.parse("j6"), to = Position.parse("k5")),
                    Move(from = Position.parse("j6"), to = Position.parse("l4")),

                    Move(from = Position.parse("j6"), to = Position.parse("j5")),
                    Move(from = Position.parse("j6"), to = Position.parse("j4")),
                    Move(from = Position.parse("j6"), to = Position.parse("j3")),

                    Move(from = Position.parse("j6"), to = Position.parse("i5")),
                    Move(from = Position.parse("j6"), to = Position.parse("h4")),
                    Move(from = Position.parse("j6"), to = Position.parse("g3")),

                    Move(from = Position.parse("j6"), to = Position.parse("i6")),
                    Move(from = Position.parse("j6"), to = Position.parse("h6")),
                    Move(from = Position.parse("j6"), to = Position.parse("g6")),
                    Move(from = Position.parse("j6"), to = Position.parse("f6")),
                    Move(from = Position.parse("j6"), to = Position.parse("e6")),
                    Move(from = Position.parse("j6"), to = Position.parse("d6")),
                    Move(from = Position.parse("j6"), to = Position.parse("c6")),
                    Move(from = Position.parse("j6"), to = Position.parse("b6")),

                    Move(from = Position.parse("j6"), to = Position.parse("i7")),
                    Move(from = Position.parse("j6"), to = Position.parse("h8")),
                    Move(from = Position.parse("j6"), to = Position.parse("g9")),
                    Move(from = Position.parse("j6"), to = Position.parse("f10")),
                    Move(from = Position.parse("j6"), to = Position.parse("e11")),
                    Move(from = Position.parse("j6"), to = Position.parse("d12"))
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

            val validMoves = engine.stateFeatures.legalMoves

            validMoves.filterByMovedPieceType(state, Queen) shouldBeEqualTo listOf(
                    Move(from = Position.parse("g4"), to = Position.parse("g5")),
                    Move(from = Position.parse("g4"), to = Position.parse("g6")),
                    Move(from = Position.parse("g4"), to = Position.parse("g7")),
                    Move(from = Position.parse("g4"), to = Position.parse("g8")),
                    Move(from = Position.parse("g4"), to = Position.parse("g9"))
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

            val validMoves = engine.stateFeatures.legalMoves

            validMoves.filterByMovedPieceType(state, Queen) shouldBeEqualTo listOf(
                    Move(from = Position.parse("g4"), to = Position.parse("i4"))
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

            val validMoves = engine.stateFeatures.legalMoves

            validMoves.filterByMovedPieceType(state, Queen) shouldBeEqualTo listOf()
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

            val validMoves = engine.stateFeatures.legalMoves

            validMoves.filterByMovedPieceType(state, King) shouldBeEqualTo listOf(
                    Move(from = Position.parse("e2"), to = Position.parse("e3")),
                    Move(from = Position.parse("e2"), to = Position.parse("f3")),
                    Move(from = Position.parse("e2"), to = Position.parse("f2")),
                    Move(from = Position.parse("e2"), to = Position.parse("f1")),
                    Move(from = Position.parse("e2"), to = Position.parse("e1")),
                    Move(from = Position.parse("e2"), to = Position.parse("d1")),
                    Move(from = Position.parse("e2"), to = Position.parse("d2")),
                    Move(from = Position.parse("e2"), to = Position.parse("d3"))
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

            val validMoves = engine.stateFeatures.legalMoves

            validMoves.filterByMovedPieceType(state, King) shouldBeEqualTo listOf(
                    Move(from = Position.parse("e2"), to = Position.parse("e3")),
                    Move(from = Position.parse("e2"), to = Position.parse("f3")),
                    Move(from = Position.parse("e2"), to = Position.parse("f1")),
                    Move(from = Position.parse("e2"), to = Position.parse("d1")),
                    Move(from = Position.parse("e2"), to = Position.parse("d2")),
                    Move(from = Position.parse("e2"), to = Position.parse("d3"))
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

            val validMoves = engine.stateFeatures.legalMoves

            validMoves.filterByMovedPieceType(state, King) shouldBeEqualTo listOf(
                    Move(from = Position.parse("h1"), to = Position.parse("i1")),
                    Move(from = Position.parse("h1"), to = Position.parse("g1"))
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

            val validMoves = engine.stateFeatures.legalMoves

            validMoves.filterByMovedPieceType(state, King) shouldBeEqualTo listOf()
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

            val validMoves = engine.stateFeatures.legalMoves

            validMoves.filterByMovedPieceType(state, King) shouldBeEqualTo listOf(
                    Move(from = Position.parse("h1"), to = Position.parse("i1"))
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

            val validMoves = engine.stateFeatures.legalMoves

            validMoves.filterByMovedPieceType(state, King) shouldBeEqualTo listOf()
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

            val validMoves = engine.stateFeatures.legalMoves

            validMoves.filterByMovedPieceType(state, King) shouldBeEqualTo listOf(
                    Move(from = Position.parse("h1"), to = Position.parse("g1"))
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

            val validMoves = engine.stateFeatures.legalMoves

            validMoves.filterByMovedPieceType(state, King) shouldBeEqualTo listOf(
                    Move(from = Position.parse("h1"), to = Position.parse("f1")),
                    Move(from = Position.parse("h1"), to = Position.parse("g1"))
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

            val validMoves = engine.stateFeatures.legalMoves

            validMoves.filterByMovedPieceType(state, King) shouldBeEqualTo listOf(
                    Move(from = Position.parse("h1"), to = Position.parse("g1"))
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

            val validMoves = engine.stateFeatures.legalMoves

            validMoves.filterByMovedPieceType(state, King) shouldBeEqualTo listOf(
                    Move(from = Position.parse("h1"), to = Position.parse("i1")),
                    Move(from = Position.parse("h1"), to = Position.parse("g1"))
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

            val validMoves = engine.stateFeatures.legalMoves

            validMoves.filterByMovedPieceType(state, King) shouldBeEqualTo listOf(
                    Move(from = Position.parse("h1"), to = Position.parse("i1"))
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

            val validMoves = engine.stateFeatures.legalMoves

            validMoves.filterByMovedPieceType(state, King) shouldBeEqualTo listOf(
                    Move(from = Position.parse("h1"), to = Position.parse("j1")),
                    Move(from = Position.parse("h1"), to = Position.parse("i1"))
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

            val validMoves = engine.stateFeatures.legalMoves

            validMoves.filterByMovedPieceType(state, King) shouldBeEqualTo listOf(
                    Move(from = Position.parse("h1"), to = Position.parse("f1")),
                    Move(from = Position.parse("h1"), to = Position.parse("h2")),
                    Move(from = Position.parse("h1"), to = Position.parse("g1"))
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

            val validMoves = engine.stateFeatures.legalMoves

            validMoves.filterByMovedPieceType(state, King) shouldBeEqualTo listOf(
                    Move(from = Position.parse("h1"), to = Position.parse("j1")),
                    Move(from = Position.parse("h1"), to = Position.parse("f1")),
                    Move(from = Position.parse("h1"), to = Position.parse("i1")),
                    Move(from = Position.parse("h1"), to = Position.parse("g1"))

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

            val validMoves = engine.stateFeatures.legalMoves

            validMoves.filterByMovedPieceType(state, King) shouldBeEqualTo listOf(
                    Move(from = Position.parse("k5"), to = Position.parse("k6")),
                    Move(from = Position.parse("k5"), to = Position.parse("l6")),
                    Move(from = Position.parse("k5"), to = Position.parse("l5")),
                    Move(from = Position.parse("k5"), to = Position.parse("l4")),
                    Move(from = Position.parse("k5"), to = Position.parse("k4")),
                    Move(from = Position.parse("k5"), to = Position.parse("j4")),
                    Move(from = Position.parse("k5"), to = Position.parse("j5")),
                    Move(from = Position.parse("k5"), to = Position.parse("j6"))
            )
        }
    }
})

private fun List<Move>.filterByMovedPieceType(state: State, pieceType: PieceType): List<Move> =
        filter { move ->
            (state.squares.byPosition(move.from) as? Square.Occupied)?.piece?.type == pieceType
        }