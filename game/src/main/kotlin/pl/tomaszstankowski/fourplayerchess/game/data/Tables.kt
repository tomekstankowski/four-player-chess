package pl.tomaszstankowski.fourplayerchess.game.data

internal object GameTable {
    const val NAME = "game"

    object Columns {
        const val ID = "id"
        const val CREATED_AT = "created_at"
        const val COMMITTED = "committed"
        const val CANCELLED = "cancelled"
        const val FINISHED = "finished"
    }
}

internal object HumanPlayerTable {
    const val NAME = "human_game_player"

    object Columns {
        const val GAME_ID = "game_id"
        const val USER_ID = "user_id"
        const val COLOR = "color"
    }
}

internal object RandomBotTable {
    const val NAME = "random_bot_game_player"

    object Columns {
        const val GAME_ID = "game_id"
        const val COLOR = "color"
    }
}