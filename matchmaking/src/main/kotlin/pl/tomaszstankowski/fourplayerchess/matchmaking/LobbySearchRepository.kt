package pl.tomaszstankowski.fourplayerchess.matchmaking

internal interface LobbySearchRepository {

    fun findAllWhereIsDeletedIsFalseAndGameIdIsNullOrderByCreatedAt(): List<LobbyListDto>
}