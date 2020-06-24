package pl.tomaszstankowski.fourplayerchess.matchmaking.data

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import pl.tomaszstankowski.fourplayerchess.data.getInstantAtUTC
import pl.tomaszstankowski.fourplayerchess.data.getUUID
import pl.tomaszstankowski.fourplayerchess.matchmaking.LobbyListDto
import pl.tomaszstankowski.fourplayerchess.matchmaking.LobbySearchRepository
import pl.tomaszstankowski.fourplayerchess.matchmaking.data.LobbyTable.Columns.CREATED_AT
import pl.tomaszstankowski.fourplayerchess.matchmaking.data.LobbyTable.Columns.DELETED
import pl.tomaszstankowski.fourplayerchess.matchmaking.data.LobbyTable.Columns.GAME_ID
import pl.tomaszstankowski.fourplayerchess.matchmaking.data.LobbyTable.Columns.ID
import pl.tomaszstankowski.fourplayerchess.matchmaking.data.LobbyTable.Columns.NAME
import javax.sql.DataSource

internal class JdbcLobbySearchRepository(dataSource: DataSource) : LobbySearchRepository {
    private val template = JdbcTemplate(dataSource)
    private val mapper = RowMapper { rs, _ ->
        LobbyListDto(
                id = rs.getUUID(ID),
                name = rs.getString(NAME),
                createdAt = rs.getInstantAtUTC(CREATED_AT),
                numberOfPlayers = rs.getInt("num_of_players")
        )
    }

    override fun findAllWhereIsDeletedIsFalseAndGameIdIsNullOrderByCreatedAt(): List<LobbyListDto> {
        val sql = "SELECT l.*, COUNT(*) AS num_of_players FROM ${LobbyTable.NAME} l " +
                "LEFT JOIN ${LobbyMembershipTable.NAME} lm ON l.$ID = lm.${LobbyMembershipTable.Columns.LOBBY_ID} " +
                "WHERE l.$GAME_ID IS NULL AND l.$DELETED = FALSE " +
                "GROUP BY l.$ID " +
                "ORDER BY l.$CREATED_AT"
        return template.query(sql, mapper)
    }
}