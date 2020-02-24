package pl.tomaszstankowski.fourplayerchess

import org.amshove.kluent.shouldBeEqualTo
import org.spekframework.spek2.Spek
import pl.tomaszstankowski.fourplayerchess.PieceType.Pawn

class MoveValidationTest : Spek({

    group("pawn moves") {

        test("starting position") {
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

        test("starting position and one square forward occupied by same color piece") {
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

        test("starting position and two squares forward occupied by same color piece") {
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

        test("starting position and one square forward occupied by other color piece") {
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

        test("starting position and two squares forward occupied by other color piece") {
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

        test("touched position") {
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

        test("touched position and one square forward occupied by same color piece") {
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

        test("touched position and one square forward occupied by other color piece") {
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

        test("capture right") {
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

        test("capture left") {
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

        test("capture left and right") {
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

        test("capture right by en passant") {
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

        test("capture left by en passant") {
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

})

private fun parseStateFromFenOrThrow(input: String): State =
        (State.parseFromFen(input) as ParseStateFromFenResult.Parsed).state

private fun MoveList.filterByMovedPieceType(state: State, pieceType: PieceType): MoveList =
        filter { move ->
            (state.squares.getSquareByPosition(move.from) as? Square.Occupied)?.piece?.type == pieceType
        }