package pl.tomaszstankowski.fourplayerchess.game

import java.util.*

object Fixture {
    const val NOW = "2020-06-21T19:41:32.326902Z"
    const val RANDOM_SEED = 2137

    val CREATE_GAME_DTO = CreateGameDto(
            humanPlayersIds = setOf(
                    UUID.fromString("c37d4c5f-3880-4689-ac2f-8eaf46583a4c"),
                    UUID.fromString("496bbf40-a106-49cc-9c4b-ab466b79d7de"),
                    UUID.fromString("d4fd97e0-7321-4e30-b72a-349d79bc9798"),
                    UUID.fromString("446cc4d9-ca17-4e6f-be8e-b32d3367f5a8")
            ),
            randomBotsCount = 0
    )

    val MAKE_MOVE_DTO = MakeMoveDto(
            gameId = UUID.fromString("00000000-0000-0000-0000-000000000001"),
            playerId = UUID.fromString("c37d4c5f-3880-4689-ac2f-8eaf46583a4c"),
            from = "d2",
            to = "d3",
            promotionPiece = null
    )

    val RESIGN_DTO = SubmitResignationDto(
            gameId = UUID.fromString("00000000-0000-0000-0000-000000000001"),
            requestingPlayerId = UUID.fromString("c37d4c5f-3880-4689-ac2f-8eaf46583a4c")
    )

    val CLAIM_DRAW_DTO = ClaimDrawDto(
            gameId = UUID.fromString("00000000-0000-0000-0000-000000000001"),
            requestingPlayerId = UUID.fromString("c37d4c5f-3880-4689-ac2f-8eaf46583a4c")
    )
}