package pl.tomaszstankowski.fourplayerchess.game

import java.util.*

sealed class CreateGameResult {
    data class Success(val game: GameDto) : CreateGameResult()
    sealed class Error : CreateGameResult() {
        data class NotEnoughPlayers(val playersCount: Int) : Error()
        data class TooManyPlayers(val playersCount: Int) : Error()
    }
}

sealed class MakeMoveResult {
    data class Success(val newGameState: GameStateDto) : MakeMoveResult()
    sealed class Error : MakeMoveResult() {
        data class GameNotFound(val id: UUID) : Error()
        object GameNotActive : Error()
        object PlayerIsNotInTheGame : Error()
        data class PlayerDoesNotHaveNextMove(val nextMoveColor: String, val playerColor: String) : Error()
        data class InvalidPosition(val position: String) : Error()
        data class IllegalPromotionPiece(val legalPieces: List<String>, val given: String) : Error()
        object IllegalMove : Error()

        val message: String
            get() = when (this) {
                is GameNotFound -> "Game with id $id not found"
                is GameNotActive -> "Game is not active"
                is InvalidPosition -> "Illegal position: $position"
                is IllegalPromotionPiece -> "Illegal promotion piece, expected one of ${legalPieces}, got: $given"
                IllegalMove -> "Illegal move"
                PlayerIsNotInTheGame -> "Requesting player is not in the game"
                is PlayerDoesNotHaveNextMove -> "$nextMoveColor has next move, requesting player's color is $playerColor"
            }
    }
}

sealed class SubmitResignationResult {
    data class Success(val newGameState: GameStateDto, val resignedColor: String) : SubmitResignationResult()
    sealed class Error : SubmitResignationResult() {
        data class GameNotFound(val id: UUID) : Error()
        object GameNotActive : Error()
        object PlayerNotInTheGame : Error()
        object NotAllowed : Error()

        val message: String
            get() = when (this) {
                is GameNotFound -> "Game with id $id not found"
                GameNotActive -> "Game is not active"
                PlayerNotInTheGame -> "Requesting player is not in the game"
                NotAllowed -> "Resignation is not allowed in current game state"
            }
    }
}

sealed class ClaimDrawResult {
    data class Success(val newGameState: GameStateDto, val claimingColor: String) : ClaimDrawResult()
    sealed class Error : ClaimDrawResult() {
        data class GameNotFound(val id: UUID) : Error()
        object GameNotActive : Error()
        object PlayerNotInTheGame : Error()
        object NotAllowed : Error()

        val message: String
            get() = when (this) {
                is GameNotFound -> "Game with id $id not found"
                GameNotActive -> "Game is not active"
                PlayerNotInTheGame -> "Requesting player is not in the game"
                NotAllowed -> "Draw by claim is not allowed in current game state"
            }
    }
}

sealed class GetGameStateResult {
    data class Success(val gameState: GameStateDto) : GetGameStateResult()
    data class GameNotFound(val id: UUID) : GetGameStateResult()
    object GameNotActive : GetGameStateResult()
}