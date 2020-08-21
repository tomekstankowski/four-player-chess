package pl.tomaszstankowski.fourplayerchess

import org.amshove.kluent.shouldBeEqualTo
import org.spekframework.spek2.Spek
import pl.tomaszstankowski.fourplayerchess.engine.Color
import pl.tomaszstankowski.fourplayerchess.engine.Coordinates
import pl.tomaszstankowski.fourplayerchess.engine.Promotion
import pl.tomaszstankowski.fourplayerchess.engine.PromotionPieceType

object UnmakeMoveTest : Spek({

    test("unmakes pawn thrust") {
        val engine = createEngineWithStateFromFen("""
            B-0,0,0,0-1,1,1,1-1,1,1,1-0,0,0,0-0-
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
            7,rP,6/
            3,rP,rP,rP,rP,1,rP,rP,rP,3/
            3,rR,rN,rB,rQ,rK,rB,rN,rR,3
        """.trimIndent())

        val oldState = engine.getUIState()
        engine.makeMoveWithAssert(from = "b5", to = "d5")
        engine.unmakeMoveWithAssert()

        engine.getUIState() shouldBeEqualTo oldState
    }

    test("unmakes moving figure to empty square") {
        val engine = createEngineWithStateFromFen("""
            B-0,0,0,0-1,1,1,1-1,1,1,1-0,0,0,0-0-
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
            7,rP,6/
            3,rP,rP,rP,rP,1,rP,rP,rP,3/
            3,rR,rN,rB,rQ,rK,rB,rN,rR,3
        """.trimIndent())
        val oldState = engine.getUIState()

        engine.makeMoveWithAssert(from = "a5", to = "c4")
        engine.unmakeMoveWithAssert()

        engine.getUIState() shouldBeEqualTo oldState
    }

    test("unmakes capture") {
        val engine = createEngineWithStateFromFen("""
            R-0,0,0,0-1,1,1,1-1,1,1,1-0,0,0,0-0-
            3,yR,yN,yB,yK,yQ,yB,1,yR,3/
            3,yP,yP,yP,yP,yP,yP,yP,yP,3/
            8,yN,5/
            bR,bP,10,gP,gR/
            bN,bP,10,gP,gN/
            bB,bP,10,gP,gB/
            bK,bP,9,gP,1,gQ/
            bQ,1,bP,9,gP,gK/
            bB,bP,10,gP,gB/
            bN,bP,10,gP,gN/
            bR,bP,10,gP,gR/
            7,rP,6/
            3,rP,rP,rP,rP,1,rP,rP,rP,3/
            3,rR,rN,rB,rQ,rK,rB,rN,rR,3
        """.trimIndent())
        val oldState = engine.getUIState()

        engine.makeMoveWithAssert(from = "i1", to = "c7")
        engine.unmakeMoveWithAssert()

        engine.getUIState() shouldBeEqualTo oldState
    }

    test("unmakes capture by en passant") {
        val engine = createEngineWithStateFromFen("""
            R-0,0,0,0-1,1,1,1-1,1,1,1-0,c5,0,0-0-
            3,yR,yN,yB,yK,yQ,yB,1,yR,3/
            3,yP,yP,1,yP,yP,yP,yP,yP,3/
            5,yP,2,yN,5/
            bR,bP,10,gP,gR/
            bN,bP,10,gP,gN/
            bB,bP,9,gP,1,gB/
            bK,bP,9,gP,1,gQ/
            bQ,1,bP,9,gP,gK/
            bB,bP,10,gP,gB/
            bN,2,bP,8,gP,gN/
            bR,bP,1,rP,8,gP,gR/
            7,rP,6/
            4,rP,rP,rP,1,rP,rP,rP,3/
            3,rR,rN,rB,rQ,rK,rB,rN,rR,3
        """.trimIndent())
        val oldState = engine.getUIState()

        engine.makeMoveWithAssert(from = "d4", to = "c5")
        engine.unmakeMoveWithAssert()

        engine.getUIState() shouldBeEqualTo oldState
    }

    test("unmakes castling king side") {
        val engine = createEngineWithStateFromFen("""
            R-0,0,0,0-1,1,1,1-1,1,1,1-0,0,0,0-0-
            3,yR,yN,yB,yK,yQ,yB,1,yR,3/
            3,yP,yP,3,yP,yP,yP,3/
            5,yP,1,yP,yN,5/
            bR,bP,4,yP,5,gP,gR/
            bN,bP,10,gP,gN/
            bB,bP,9,gP,1,gB/
            bK,1,bP,8,gP,1,gQ/
            bQ,1,bP,9,gP,gK/
            bB,bP,9,gN,gP,gB/
            3,bP,8,gP,1/
            bR,bP,bN,rP,7,gP,1,gR/
            7,rP,rN,5/
            4,rP,rP,rP,rB,rP,rP,rP,3/
            3,rR,rN,rB,rQ,rK,2,rR,3
        """.trimIndent())
        val oldState = engine.getUIState()

        engine.makeMoveWithAssert(from = "h1", to = "j1")
        engine.unmakeMoveWithAssert()

        engine.getUIState() shouldBeEqualTo oldState
    }

    test("unmakes castling queen side") {
        val engine = createEngineWithStateFromFen("""
            R-0,0,0,0-1,1,1,1-1,1,1,1-0,0,0,0-0-
            3,yR,yN,yB,yK,yQ,yB,yN,yR,3/
            3,yP,yP,3,yP,yP,yP,3/
            5,yP,1,yP,6/
            bR,bP,4,yP,5,gP,gR/
            bN,bP,10,gP,gN/
            bB,bP,9,gP,1,gB/
            bK,1,bP,9,gP,gQ/
            bQ,1,bP,8,gP,1,gK/
            bB,bP,bN,8,gN,gP,gB/
            1,bP,9,gP,2/
            bR,1,bP,9,gP,gR/
            5,rN,rP,rB,6/
            3,rP,rP,rP,rQ,rP,rP,rP,rP,3/
            3,rR,3,rK,rB,rN,rR,3
        """.trimIndent())
        val oldState = engine.getUIState()

        engine.makeMoveWithAssert(from = "h1", to = "f1")
        engine.unmakeMoveWithAssert()

        engine.getUIState() shouldBeEqualTo oldState
    }

    test("unmakes promotion") {
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
        val oldState = engine.getUIState()

        engine.makeMove(
                Promotion(
                        from = Coordinates.parse("i7"),
                        to = Coordinates.parse("i8"),
                        pieceType = PromotionPieceType.Knight
                )
        ).also { assert(it) }
        engine.unmakeMoveWithAssert()

        engine.getUIState() shouldBeEqualTo oldState
    }

    test("unmakes promotion with capture") {
        val engine = createEngineWithStateFromFen("""
            R-0,0,0,1-0,0,0,0-0,0,0,0-0,0,0,0-0-
            14/
            5,yP,yK,yB,6/
            6,yP,7/
            6,bR,1,yP,4,gR/
            12,gP,1/
            3,bK,bP,7,gP,gB/
            4,bP,2,yB,3,gB,2/
            8,rP,rP,1,gP,1,gK/
            8,rN,rK,2,gP,gN/
            10,rB,1,gP,1/
            14/
            14/
            14/
            14
        """.trimIndent())
        val oldState = engine.getUIState()

        engine.makeMove(
                Promotion(
                        from = Coordinates.parse("i7"),
                        to = Coordinates.parse("h8"),
                        pieceType = PromotionPieceType.Knight
                )
        ).also { assert(it) }
        engine.unmakeMoveWithAssert()

        engine.getUIState() shouldBeEqualTo oldState
    }

    test("clears check") {
        val engine = createEngineWithStateFromFen("""
            R-0,0,0,1-0,0,0,0-0,0,0,0-0,0,0,0-0-
            14/
            5,yP,yK,yB,6/
            6,yP,7/
            6,bR,1,yP,4,gR/
            9,yB,2,gP,1/
            3,bK,bP,7,gP,gB/
            5,bP,5,gB,2/
            8,rP,rP,1,gP,1,gK/
            8,rN,rK,2,gP,gN/
            9,rB,2,gP,1/
            14/
            14/
            14/
            14
        """.trimIndent())

        engine.makeMoveWithAssert(from = "j5", to = "i4")
        engine.unmakeMoveWithAssert()

        engine.getUIState().checks shouldBeEqualTo mapOf(
                Color.Red to emptySet(),
                Color.Blue to emptySet(),
                Color.Yellow to emptySet(),
                Color.Green to emptySet()
        )
    }

    test("clears three fold repetition") {
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

        engine.makeMoveWithAssert("k9", "k4")
        engine.makeMoveWithAssert("m5", "m4")
        engine.makeMoveWithAssert("k4", "n7")
        engine.makeMoveWithAssert("m4", "n5")
        engine.makeMoveWithAssert("n7", "k7")
        engine.makeMoveWithAssert("n5", "m5")
        engine.makeMoveWithAssert("k7", "k4")
        engine.makeMoveWithAssert("m5", "m4")
        engine.makeMoveWithAssert("k4", "k7")
        engine.makeMoveWithAssert("m4", "m5")
        engine.makeMoveWithAssert("k7", "k4")
        engine.unmakeMoveWithAssert()

        engine.getUIState().isDrawByClaimAllowed shouldBeEqualTo false
    }

    test("clears fifty move rule") {
        val engine = createEngineWithStateFromFen("""
            Y-0,1,0,1-0,0,0,0-0,0,0,0-0,0,0,0-99-
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

        engine.makeMoveWithAssert("j6", "k4")
        engine.unmakeMoveWithAssert()

        engine.getUIState().isDrawByClaimAllowed shouldBeEqualTo false
    }

    test("is not breaking internal state") {
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

        engine.makeMoveWithAssert("i1", "h2")
        engine.unmakeMoveWithAssert()
        val newState = engine.getStateAfterMove("i1", "b8")

        newState.fenState shouldBeEqualTo parseStateFromFenOrThrow("""
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
})