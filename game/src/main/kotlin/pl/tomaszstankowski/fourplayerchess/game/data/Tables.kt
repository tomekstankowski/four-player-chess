package pl.tomaszstankowski.fourplayerchess.game.data

internal object GameTable {
    const val NAME = "game"

    object Columns {
        const val ID = "id"
        const val CREATED_AT = "created_at"
        const val COMMITTED = "committed"
        const val CANCELLED = "cancelled"
    }
}

internal object GamePlayerTable {
    const val NAME = "game_player"

    object Columns {
        const val GAME_ID = "game_id"
        const val PLAYER_ID = "player_id"
        const val COLOR = "color"
    }
}