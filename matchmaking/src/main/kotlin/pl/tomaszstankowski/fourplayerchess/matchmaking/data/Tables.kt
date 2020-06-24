package pl.tomaszstankowski.fourplayerchess.matchmaking.data

internal object LobbyTable {
    const val NAME = "lobby"

    object Columns {
        const val ID = "id"
        const val NAME = "name"
        const val CREATED_AT = "created_at"
        const val OWNER_ID = "owner_id"
        const val VERSION = "version"
        const val DELETED = "deleted"
        const val GAME_ID = "game_id"
    }
}

internal object LobbyMembershipTable {
    const val NAME = "lobby_membership"

    object Columns {
        const val LOBBY_ID = "lobby_id"
        const val PLAYER_ID = "player_id"
        const val CREATED_AT = "created_at"
    }
}