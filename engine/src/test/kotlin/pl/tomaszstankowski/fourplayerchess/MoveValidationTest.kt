package pl.tomaszstankowski.fourplayerchess

import org.amshove.kluent.shouldBeEqualTo
import org.spekframework.spek2.Spek
import pl.tomaszstankowski.fourplayerchess.PieceType.*

class MoveValidationTest : Spek({

    group("pawn") {

        test("can go one or two squares forward on its first move") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,0,0-0,0,0,0-0,0,0,0-0,0,0,0-41-
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

            val validMoves = getValidMoves(state)

            validMoves.filterByMovedPieceType(state, Pawn) shouldBeEqualTo listOf(
                    Move.ToEmptySquare(from = Position.parse("g2"), to = Position.parse("g3")),
                    Move.TwoSquaresForwardPawnMove(from = Position.parse("g2"), to = Position.parse("g4"))
            )
        }

        test("cannot go forward on its first move when one square forward is occupied by piece of its color") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,0,0-0,0,0,0-0,0,0,0-0,0,0,0-41-
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

            val validMoves = getValidMoves(state)

            validMoves.filterByMovedPieceType(state, Pawn) shouldBeEqualTo listOf()
        }

        test("cannot go two squares forward on its first move when the square is occupied by piece of its color") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,0,0-0,0,0,0-0,0,0,0-0,0,0,0-41-
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

            val validMoves = getValidMoves(state)

            validMoves.filterByMovedPieceType(state, Pawn) shouldBeEqualTo listOf(
                    Move.ToEmptySquare(from = Position.parse("g2"), to = Position.parse("g3"))
            )
        }

        test("cannot go forward on its first move when one square forward is occupied by piece of other color") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,0,0-0,0,0,0-0,0,0,0-0,0,0,0-41-
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

            val validMoves = getValidMoves(state)

            validMoves.filterByMovedPieceType(state, Pawn) shouldBeEqualTo listOf()
        }

        test("cannot go two squares forward on its first move when the square is occupied by piece of other color") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,0,0-0,0,0,0-0,0,0,0-0,0,0,0-41-
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

            val validMoves = getValidMoves(state)

            validMoves.filterByMovedPieceType(state, Pawn) shouldBeEqualTo listOf(
                    Move.ToEmptySquare(from = Position.parse("g2"), to = Position.parse("g3"))
            )
        }

        test("can move only one square forward when has already been touched before") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,0,0-0,0,0,0-0,0,0,0-0,0,0,0-41-
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

            val validMoves = getValidMoves(state)

            validMoves.filterByMovedPieceType(state, Pawn) shouldBeEqualTo listOf(
                    Move.ToEmptySquare(from = Position.parse("g3"), to = Position.parse("g4"))
            )
        }

        test("cannot move forward when has been touched before and the square forward is occupied by piece of its color") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,0,0-0,0,0,0-0,0,0,0-0,0,0,0-41-
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

            val validMoves = getValidMoves(state)

            validMoves.filterByMovedPieceType(state, Pawn) shouldBeEqualTo listOf()
        }

        test("cannot move forward when has been touched before and the square forward is occupied by piece of other color") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,0,0-0,0,0,0-0,0,0,0-0,0,0,0-41-
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

            val validMoves = getValidMoves(state)

            validMoves.filterByMovedPieceType(state, Pawn) shouldBeEqualTo listOf()
        }

        test("can capture right") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,0,0-0,0,0,0-0,0,0,0-0,0,0,0-41-
            9,yK,4/
            7,yP,yP,yP,yP,3/
            3,yB,4,yN,5/
            1,bP,4,yP,7/
            bK,bP,10,gP,gN/
            bB,13/
            12,gB,1/
            12,gP,gK/
            1,bP,bN,8,gN,1,gB/
            1,bP,11,gR/
            7,yP,6/
            6,rP,7/
            14/
            6,rK,7
        """.trimIndent())

            val validMoves = getValidMoves(state)

            validMoves.filterByMovedPieceType(state, Pawn) shouldBeEqualTo listOf(
                    Move.ToEmptySquare(from = Position.parse("g3"), to = Position.parse("g4")),
                    Move.Capture(from = Position.parse("g3"), to = Position.parse("h4"))
            )
        }

        test("can capture left") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,0,0-0,0,0,0-0,0,0,0-0,0,0,0-41-
            9,yK,4/
            7,yP,yP,yP,yP,3/
            3,yB,4,yN,5/
            1,bP,4,yP,7/
            bK,bP,10,gP,gN/
            bB,13/
            12,gB,1/
            12,gP,gK/
            1,bP,bN,8,gN,1,gB/
            1,bP,11,gR/
            5,yP,8/
            6,rP,7/
            14/
            6,rK,7
        """.trimIndent())

            val validMoves = getValidMoves(state)

            validMoves.filterByMovedPieceType(state, Pawn) shouldBeEqualTo listOf(
                    Move.ToEmptySquare(from = Position.parse("g3"), to = Position.parse("g4")),
                    Move.Capture(from = Position.parse("g3"), to = Position.parse("f4"))
            )
        }

        test("can capture left and right") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,0,0-0,0,0,0-0,0,0,0-0,0,0,0-41-
            9,yK,4/
            7,yP,yP,yP,yP,3/
            3,yB,4,yN,5/
            1,bP,4,yP,7/
            bK,bP,10,gP,gN/
            bB,13/
            12,gB,1/
            12,gP,gK/
            1,bP,bN,8,gN,1,gB/
            1,bP,11,gR/
            5,yP,1,yP,6/
            6,rP,7/
            14/
            6,rK,7
        """.trimIndent())

            val validMoves = getValidMoves(state)

            validMoves.filterByMovedPieceType(state, Pawn) shouldBeEqualTo listOf(
                    Move.ToEmptySquare(from = Position.parse("g3"), to = Position.parse("g4")),
                    Move.Capture(from = Position.parse("g3"), to = Position.parse("f4")),
                    Move.Capture(from = Position.parse("g3"), to = Position.parse("h4"))
            )
        }

        test("can capture right by en passant") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,0,0-0,0,0,0-0,0,0,0-0,0,0,h12-41-
            9,yK,4/
            7,yP,yP,yP,yP,3/
            3,yB,4,yN,5/
            1,bP,4,rP,yP,6/
            bK,bP,10,gP,gN/
            bB,13/
            12,gB,1/
            12,gP,gK/
            1,bP,bN,8,gN,1,gB/
            1,bP,11,gR/
            14/
            14/
            14/
            6,rK,7
        """.trimIndent())

            val validMoves = getValidMoves(state)

            validMoves.filterByMovedPieceType(state, Pawn) shouldBeEqualTo listOf(
                    Move.ToEmptySquare(from = Position.parse("g11"), to = Position.parse("g12")),
                    Move.CaptureByEnPassant(
                            from = Position.parse("g11"),
                            to = Position.parse("h12"),
                            capturedPawnPosition = Position.parse("h11")
                    )
            )
        }

        test("can capture left by en passant") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,0,0-0,0,0,0-0,0,0,0-0,0,0,f12-41-
            9,yK,4/
            7,yP,yP,yP,yP,3/
            3,yB,4,yN,5/
            1,bP,3,yP,rP,7/
            bK,bP,10,gP,gN/
            bB,13/
            12,gB,1/
            12,gP,gK/
            1,bP,bN,8,gN,1,gB/
            1,bP,11,gR/
            14/
            14/
            14/
            6,rK,7
        """.trimIndent())

            val validMoves = getValidMoves(state)

            validMoves.filterByMovedPieceType(state, Pawn) shouldBeEqualTo listOf(
                    Move.ToEmptySquare(from = Position.parse("g11"), to = Position.parse("g12")),
                    Move.CaptureByEnPassant(
                            from = Position.parse("g11"),
                            to = Position.parse("f12"),
                            capturedPawnPosition = Position.parse("f11")
                    )
            )
        }
    }

    group("bishop") {

        test("can move on diagonals") {
            val state = parseStateFromFenOrThrow("""
                R-0,0,0,0-1,1,0,1-1,1,0,1-0,0,0,0-16-
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

            val validMoves = getValidMoves(state)

            validMoves.filterByMovedPieceType(state, Bishop) shouldBeEqualTo listOf(
                    Move.ToEmptySquare(from = Position.parse("i4"), to = Position.parse("j5")),
                    Move.ToEmptySquare(from = Position.parse("i4"), to = Position.parse("k6")),
                    Move.ToEmptySquare(from = Position.parse("i4"), to = Position.parse("l7")),
                    Move.ToEmptySquare(from = Position.parse("i4"), to = Position.parse("m8")),
                    Move.Capture(from = Position.parse("i4"), to = Position.parse("n9")),
                    Move.ToEmptySquare(from = Position.parse("i4"), to = Position.parse("h5")),
                    Move.ToEmptySquare(from = Position.parse("i4"), to = Position.parse("g6")),
                    Move.ToEmptySquare(from = Position.parse("i4"), to = Position.parse("f7")),
                    Move.ToEmptySquare(from = Position.parse("i4"), to = Position.parse("e8")),
                    Move.ToEmptySquare(from = Position.parse("i4"), to = Position.parse("d9")),
                    Move.ToEmptySquare(from = Position.parse("i4"), to = Position.parse("c10")),
                    Move.Capture(from = Position.parse("i4"), to = Position.parse("b11")),
                    Move.ToEmptySquare(from = Position.parse("i4"), to = Position.parse("h3")),
                    Move.ToEmptySquare(from = Position.parse("i4"), to = Position.parse("g2")),
                    Move.ToEmptySquare(from = Position.parse("i4"), to = Position.parse("f1")),
                    Move.ToEmptySquare(from = Position.parse("i4"), to = Position.parse("j3"))
            )
        }
    }

    group("knight") {

        test("can jump over other pieces") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,0,0-1,1,1,1-1,1,1,1-0,0,0,0-12-
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

            val moves = getValidMoves(state)

            moves.filterByMovedPieceType(state, Knight) shouldBeEqualTo listOf(
                    Move.ToEmptySquare(from = Position.parse("f3"), to = Position.parse("g5")),
                    Move.ToEmptySquare(from = Position.parse("f3"), to = Position.parse("h4")),
                    Move.ToEmptySquare(from = Position.parse("f3"), to = Position.parse("e1")),
                    Move.ToEmptySquare(from = Position.parse("f3"), to = Position.parse("d4")),
                    Move.ToEmptySquare(from = Position.parse("f3"), to = Position.parse("e5"))
            )
        }
    }

    group("rook") {

        test("can move along file or rank") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,0,0-1,1,1,1-1,1,1,1-0,0,0,0-12-
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

            val moves = getValidMoves(state)

            moves.filterByMovedPieceType(state, Rook) shouldBeEqualTo listOf(
                    Move.ToEmptySquare(from = Position.parse("d4"), to = Position.parse("d5")),
                    Move.ToEmptySquare(from = Position.parse("d4"), to = Position.parse("d6")),
                    Move.ToEmptySquare(from = Position.parse("d4"), to = Position.parse("d7")),
                    Move.ToEmptySquare(from = Position.parse("d4"), to = Position.parse("d8")),
                    Move.ToEmptySquare(from = Position.parse("d4"), to = Position.parse("d9")),
                    Move.ToEmptySquare(from = Position.parse("d4"), to = Position.parse("d10")),
                    Move.ToEmptySquare(from = Position.parse("d4"), to = Position.parse("d11")),
                    Move.ToEmptySquare(from = Position.parse("d4"), to = Position.parse("d12")),
                    Move.Capture(from = Position.parse("d4"), to = Position.parse("d13")),
                    Move.ToEmptySquare(from = Position.parse("d4"), to = Position.parse("e4")),
                    Move.ToEmptySquare(from = Position.parse("d4"), to = Position.parse("f4")),
                    Move.ToEmptySquare(from = Position.parse("d4"), to = Position.parse("g4")),
                    Move.ToEmptySquare(from = Position.parse("d4"), to = Position.parse("h4")),
                    Move.ToEmptySquare(from = Position.parse("d4"), to = Position.parse("i4")),
                    Move.ToEmptySquare(from = Position.parse("d4"), to = Position.parse("j4")),
                    Move.ToEmptySquare(from = Position.parse("d4"), to = Position.parse("k4")),
                    Move.Capture(from = Position.parse("d4"), to = Position.parse("l4")),
                    Move.ToEmptySquare(from = Position.parse("d4"), to = Position.parse("d3")),
                    Move.ToEmptySquare(from = Position.parse("d4"), to = Position.parse("d2")),
                    Move.ToEmptySquare(from = Position.parse("d4"), to = Position.parse("d1")),
                    Move.ToEmptySquare(from = Position.parse("d4"), to = Position.parse("c4")),
                    Move.Capture(from = Position.parse("d4"), to = Position.parse("b4")),

                    Move.ToEmptySquare(from = Position.parse("k1"), to = Position.parse("j1"))

            )
        }
    }

    group("queen") {

        test("can move on diagonals and along file or rank") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,0,0-1,1,1,1-1,1,1,1-0,0,0,0-12-
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

            val moves = getValidMoves(state)

            moves.filterByMovedPieceType(state, Queen) shouldBeEqualTo listOf(
                    Move.ToEmptySquare(from = Position.parse("j6"), to = Position.parse("j7")),
                    Move.ToEmptySquare(from = Position.parse("j6"), to = Position.parse("j8")),
                    Move.ToEmptySquare(from = Position.parse("j6"), to = Position.parse("j9")),
                    Move.ToEmptySquare(from = Position.parse("j6"), to = Position.parse("j10")),
                    Move.ToEmptySquare(from = Position.parse("j6"), to = Position.parse("j11")),
                    Move.ToEmptySquare(from = Position.parse("j6"), to = Position.parse("j12")),
                    Move.Capture(from = Position.parse("j6"), to = Position.parse("j13")),

                    Move.ToEmptySquare(from = Position.parse("j6"), to = Position.parse("k7")),
                    Move.ToEmptySquare(from = Position.parse("j6"), to = Position.parse("l8")),
                    Move.Capture(from = Position.parse("j6"), to = Position.parse("m9")),

                    Move.ToEmptySquare(from = Position.parse("j6"), to = Position.parse("k6")),
                    Move.Capture(from = Position.parse("j6"), to = Position.parse("l6")),

                    Move.ToEmptySquare(from = Position.parse("j6"), to = Position.parse("k5")),
                    Move.Capture(from = Position.parse("j6"), to = Position.parse("l4")),

                    Move.ToEmptySquare(from = Position.parse("j6"), to = Position.parse("j5")),
                    Move.ToEmptySquare(from = Position.parse("j6"), to = Position.parse("j4")),
                    Move.ToEmptySquare(from = Position.parse("j6"), to = Position.parse("j3")),

                    Move.ToEmptySquare(from = Position.parse("j6"), to = Position.parse("i5")),
                    Move.ToEmptySquare(from = Position.parse("j6"), to = Position.parse("h4")),
                    Move.ToEmptySquare(from = Position.parse("j6"), to = Position.parse("g3")),

                    Move.ToEmptySquare(from = Position.parse("j6"), to = Position.parse("i6")),
                    Move.ToEmptySquare(from = Position.parse("j6"), to = Position.parse("h6")),
                    Move.ToEmptySquare(from = Position.parse("j6"), to = Position.parse("g6")),
                    Move.ToEmptySquare(from = Position.parse("j6"), to = Position.parse("f6")),
                    Move.ToEmptySquare(from = Position.parse("j6"), to = Position.parse("e6")),
                    Move.ToEmptySquare(from = Position.parse("j6"), to = Position.parse("d6")),
                    Move.ToEmptySquare(from = Position.parse("j6"), to = Position.parse("c6")),
                    Move.Capture(from = Position.parse("j6"), to = Position.parse("b6")),

                    Move.ToEmptySquare(from = Position.parse("j6"), to = Position.parse("i7")),
                    Move.ToEmptySquare(from = Position.parse("j6"), to = Position.parse("h8")),
                    Move.ToEmptySquare(from = Position.parse("j6"), to = Position.parse("g9")),
                    Move.ToEmptySquare(from = Position.parse("j6"), to = Position.parse("f10")),
                    Move.ToEmptySquare(from = Position.parse("j6"), to = Position.parse("e11")),
                    Move.ToEmptySquare(from = Position.parse("j6"), to = Position.parse("d12"))
            )
        }
    }

    group("king") {

        test("can go to every adjacent square") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,0,0-0,0,0,0-0,0,0,0-0,0,0,0-41-
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

            val validMoves = getValidMoves(state)

            validMoves.filterByMovedPieceType(state, King) shouldBeEqualTo listOf(
                    Move.ToEmptySquare(from = Position.parse("e2"), to = Position.parse("e3")),
                    Move.ToEmptySquare(from = Position.parse("e2"), to = Position.parse("f3")),
                    Move.ToEmptySquare(from = Position.parse("e2"), to = Position.parse("f2")),
                    Move.ToEmptySquare(from = Position.parse("e2"), to = Position.parse("f1")),
                    Move.ToEmptySquare(from = Position.parse("e2"), to = Position.parse("e1")),
                    Move.ToEmptySquare(from = Position.parse("e2"), to = Position.parse("d1")),
                    Move.ToEmptySquare(from = Position.parse("e2"), to = Position.parse("d2")),
                    Move.ToEmptySquare(from = Position.parse("e2"), to = Position.parse("d3"))
            )
        }

        test("can capture other piece") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,0,0-0,0,0,0-0,0,0,0-0,0,0,0-41-
            9,yK,4/
            7,yP,yP,yP,yP,3/
            3,yB,4,yN,5/
            1,bP,3,yP,yP,2,gB,4/
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

            val validMoves = getValidMoves(state)

            validMoves.filterByMovedPieceType(state, King) shouldBeEqualTo listOf(
                    Move.ToEmptySquare(from = Position.parse("e2"), to = Position.parse("e3")),
                    Move.ToEmptySquare(from = Position.parse("e2"), to = Position.parse("f3")),
                    Move.ToEmptySquare(from = Position.parse("e2"), to = Position.parse("f2")),
                    Move.ToEmptySquare(from = Position.parse("e2"), to = Position.parse("f1")),
                    Move.ToEmptySquare(from = Position.parse("e2"), to = Position.parse("e1")),
                    Move.ToEmptySquare(from = Position.parse("e2"), to = Position.parse("d1")),
                    Move.ToEmptySquare(from = Position.parse("e2"), to = Position.parse("d2")),
                    Move.Capture(from = Position.parse("e2"), to = Position.parse("d3"))
            )
        }

        test("cannot castle when destination square is occupied") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,0,0-1,1,1,1-1,1,1,1-0,0,0,0-16-
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

            val validMoves = getValidMoves(state)

            validMoves.filterByMovedPieceType(state, King) shouldBeEqualTo listOf(
                    Move.ToEmptySquare(from = Position.parse("h1"), to = Position.parse("i1")),
                    Move.ToEmptySquare(from = Position.parse("h1"), to = Position.parse("g1"))
            )
        }

        test("cannot castle when any square between king and rook is occupied") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,0,0-1,1,1,1-1,1,1,1-0,0,0,0-20-
            3,yR,1,yB,yK,yQ,yB,1,yR,3/
            4,yP,yP,1,yP,yP,yP,yP,3/
            5,yN,yP,1,yN,5/
            bR,1,yP,8,gP,1,gR/
            1,bP,9,gP,2/
            bB,bP,bN,8,gN,gP,gB/
            bK,1,bP,9,gP,gQ/
            bQ,bP,10,gP,gK/
            bB,bP,bN,8,gN,gP,gB/
            1,bP,9,gP,2/
            bR,1,bP,9,gP,gR/
            4,rP,1,rP,1,rN,5/
            3,rP,rB,rP,rQ,rP,rP,rP,rP,3/
            3,rR,rN,2,rK,rB,1,rR,3
        """.trimIndent())

            val validMoves = getValidMoves(state)

            validMoves.filterByMovedPieceType(state, King) shouldBeEqualTo listOf(
                    Move.ToEmptySquare(from = Position.parse("h1"), to = Position.parse("g1"))
            )
        }

        test("cannot castle when king or rook moved before") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,0,0-0,1,1,1-0,1,1,1-0,0,0,0-36-
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

            val validMoves = getValidMoves(state)

            validMoves.filterByMovedPieceType(state, King) shouldBeEqualTo listOf(
                    Move.ToEmptySquare(from = Position.parse("h1"), to = Position.parse("i1")),
                    Move.ToEmptySquare(from = Position.parse("h1"), to = Position.parse("g1"))
            )
        }

        test("can castle king side") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,0,0-1,1,1,1-1,1,1,1-0,0,0,0-12-
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

            val validMoves = getValidMoves(state)

            validMoves.filterByMovedPieceType(state, King) shouldBeEqualTo listOf(
                    Move.ToEmptySquare(from = Position.parse("h1"), to = Position.parse("i1")),
                    Move.Castling.KingSide(from = Position.parse("h1"), to = Position.parse("j1"))
            )
        }

        test("can castle queen side") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,0,0-1,1,1,1-1,1,1,1-0,0,0,0-20-
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

            val validMoves = getValidMoves(state)

            validMoves.filterByMovedPieceType(state, King) shouldBeEqualTo listOf(
                    Move.ToEmptySquare(from = Position.parse("h1"), to = Position.parse("h2")),
                    Move.ToEmptySquare(from = Position.parse("h1"), to = Position.parse("g1")),
                    Move.Castling.QueenSide(from = Position.parse("h1"), to = Position.parse("f1"))
            )
        }

        test("can castle both king and queen side") {
            val state = parseStateFromFenOrThrow("""
            R-0,0,0,0-1,1,1,1-1,1,1,1-0,0,0,0-20-
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

            val validMoves = getValidMoves(state)

            validMoves.filterByMovedPieceType(state, King) shouldBeEqualTo listOf(
                    Move.ToEmptySquare(from = Position.parse("h1"), to = Position.parse("i1")),
                    Move.ToEmptySquare(from = Position.parse("h1"), to = Position.parse("g1")),
                    Move.Castling.KingSide(from = Position.parse("h1"), to = Position.parse("j1")),
                    Move.Castling.QueenSide(from = Position.parse("h1"), to = Position.parse("f1"))
            )
        }
    }
})

private fun parseStateFromFenOrThrow(input: String): State =
        (State.parseFromFen(input) as ParseStateFromFenResult.Parsed).state

private fun MoveList.filterByMovedPieceType(state: State, pieceType: PieceType): MoveList =
        filter { move ->
            (state.squares.getSquareByPosition(move.from) as? Square.Occupied)?.piece?.type == pieceType
        }