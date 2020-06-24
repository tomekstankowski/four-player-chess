package pl.tomaszstankowski.fourplayerchess.matchmaking

import pl.tomaszstankowski.fourplayerchess.matchmaking.data.JdbcLobbySearchRepository
import javax.sql.DataSource

class LobbySearchService internal constructor(private val lobbySearchRepository: LobbySearchRepository) {

    companion object {
        fun create(dataSource: DataSource) =
                LobbySearchService(JdbcLobbySearchRepository(dataSource))
    }

    fun getAllLobbies(): List<LobbyListDto> = lobbySearchRepository.findAllWhereIsDeletedIsFalseAndGameIdIsNullOrderByCreatedAt()
}