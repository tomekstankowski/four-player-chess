package pl.tomaszstankowski.fourplayerchess.game

import pl.tomaszstankowski.fourplayerchess.engine.*
import pl.tomaszstankowski.fourplayerchess.engine.Color.*
import pl.tomaszstankowski.fourplayerchess.engine.PieceType.*
import java.time.Instant
import java.util.*

data class CreateGameDto(val humanPlayersIds: Set<UUID>, val randomBotsCount: Int)

data class GameDto(val id: UUID,
                   val createdAt: Instant,
                   val isCancelled: Boolean,
                   val isFinished: Boolean)

internal fun Game.toDto() =
        GameDto(
                id = id,
                createdAt = createdAt,
                isCancelled = isCancelled,
                isFinished = isFinished
        )

data class GamePlayerDto(
        val playerId: UUID?,
        val type: String,
        val color: String
)

internal fun Player.toDto() =
        GamePlayerDto(
                playerId = (this as? Player.HumanPlayer)?.userId,
                type = when (this) {
                    is Player.HumanPlayer -> "human"
                    is Player.RandomBot -> "randomBot"
                },
                color = color.toJsonStr()
        )

data class MakeMoveDto(val gameId: UUID,
                       val playerId: UUID,
                       val from: String,
                       val to: String,
                       val promotionPiece: String?)

data class SubmitResignationDto(val gameId: UUID,
                                val requestingPlayerId: UUID)

data class ClaimDrawDto(val gameId: UUID,
                        val requestingPlayerId: UUID)

data class GameStateDto(
        val board: List<List<String>>,
        val eliminatedColors: List<String>,
        val nextMoveColor: String,
        val colorsInCheck: List<String>,
        val legalMoves: List<LegalMoveDto>,
        val isDrawByClaimAllowed: Boolean,
        val isFinished: Boolean,
        val winningColor: String?
) {

    companion object {
        internal fun create(state: State,
                            stateFeatures: StateFeatures,
                            legalMoves: List<Move>,
                            isDrawByClaimAllowed: Boolean,
                            isFinished: Boolean,
                            winningColor: Color?) =
                GameStateDto(
                        board = state.squares.map { row ->
                            row.mapNotNull { squareOpt -> squareOpt?.toJsonStr() }
                        },
                        eliminatedColors = state.eliminatedColors.map { color -> color.toJsonStr() },
                        nextMoveColor = state.nextMoveColor.toJsonStr(),
                        colorsInCheck = stateFeatures.checks.mapNotNull { (color, checks) ->
                            if (checks.isEmpty()) null
                            else color.toJsonStr()
                        },
                        legalMoves = legalMoves.map { it.toDto() },
                        isDrawByClaimAllowed = isDrawByClaimAllowed,
                        isFinished = isFinished,
                        winningColor = winningColor?.toJsonStr()
                )
    }
}

data class LegalMoveDto(
        val from: String,
        val to: String
)

internal fun Color.toJsonChar() =
        when (this) {
            Red -> 'r'
            Blue -> 'b'
            Yellow -> 'y'
            Green -> 'g'
        }

internal fun PieceType.toJsonChar() =
        when (this) {
            Pawn -> 'P'
            Knight -> 'N'
            Bishop -> 'B'
            Rook -> 'R'
            Queen -> 'Q'
            King -> 'K'
        }

internal fun Square.toJsonStr() =
        when (this) {
            is Square.Empty -> ""
            is Square.Occupied -> charArrayOf(this.piece.color.toJsonChar(), this.piece.type.toJsonChar())
                    .joinToString(separator = "")
        }

internal fun Color.toJsonStr() = name.toLowerCase()

internal fun PieceType.toJsonStr() = name.toLowerCase()

internal fun Move.toDto() =
        LegalMoveDto(
                from = from.toHumanReadableString(),
                to = to.toHumanReadableString()
        )

internal fun String.toPieceTypeOrNull(): PieceType? =
        try {
            PieceType.valueOf(this.capitalize())
        } catch (e: Exception) {
            null
        }

internal fun EngineStateSnapshot.toGameStateDto() =
        GameStateDto.create(
                state = state,
                stateFeatures = stateFeatures,
                legalMoves = legalMoves,
                isDrawByClaimAllowed = isDrawByClaimAllowed,
                isFinished = isGameOver,
                winningColor = winningColor
        )
