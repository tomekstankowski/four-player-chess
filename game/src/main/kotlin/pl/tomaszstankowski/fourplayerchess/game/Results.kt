package pl.tomaszstankowski.fourplayerchess.game

import java.util.*

sealed class MakeMoveResult {
    data class Success(val newGameState: GameStateDto) : MakeMoveResult()
    sealed class Error : MakeMoveResult() {
        data class GameNotFound(val id: UUID) : Error()
        object GameNotActive : Error()
        object PlayerIsNotInTheGame : Error()
        data class NoPlayerTurn(val playerWithNextTurnId: UUID) : Error()
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
                is NoPlayerTurn -> "It's not a turn of requesting player. Player with id '$playerWithNextTurnId' has next turn."
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

sealed class GetGameStateResult {
    data class Success(val gameState: GameStateDto) : GetGameStateResult()
    data class GameNotFound(val id: UUID) : GetGameStateResult()
    object GameNotActive : GetGameStateResult()
}