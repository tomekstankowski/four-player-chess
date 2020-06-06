package pl.tomaszstankowski.fourplayerchess.engine

import pl.tomaszstankowski.fourplayerchess.engine.PieceType.King
import pl.tomaszstankowski.fourplayerchess.engine.PieceType.Pawn

sealed class MoveClaim {
    abstract val move: Move

    data class RegularMoveClaim(override val move: Move) : MoveClaim()
    data class PromotionMoveClaim internal constructor(override val move: Move,
                                                       val pieceType: PieceType) : MoveClaim() {
        companion object {
            fun getOrNull(move: Move, pieceType: PieceType): PromotionMoveClaim? =
                    when (pieceType) {
                        Pawn, King -> null
                        else -> PromotionMoveClaim(move, pieceType)
                    }
        }
    }
}