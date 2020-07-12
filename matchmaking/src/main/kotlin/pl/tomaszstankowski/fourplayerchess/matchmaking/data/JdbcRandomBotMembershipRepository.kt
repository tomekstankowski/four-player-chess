package pl.tomaszstankowski.fourplayerchess.matchmaking.data

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import pl.tomaszstankowski.fourplayerchess.data.getInstantAtUTC
import pl.tomaszstankowski.fourplayerchess.data.getUUID
import pl.tomaszstankowski.fourplayerchess.data.toLocalDateTimeAtUTC
import pl.tomaszstankowski.fourplayerchess.matchmaking.LobbyMembership.RandomBotMembership
import pl.tomaszstankowski.fourplayerchess.matchmaking.RandomBotMembershipRepository
import pl.tomaszstankowski.fourplayerchess.matchmaking.data.RandomBotMembershipTable.Columns.BOT_ID
import pl.tomaszstankowski.fourplayerchess.matchmaking.data.RandomBotMembershipTable.Columns.CREATED_AT
import pl.tomaszstankowski.fourplayerchess.matchmaking.data.RandomBotMembershipTable.Columns.LOBBY_ID
import java.util.*
import javax.sql.DataSource

internal class JdbcRandomBotMembershipRepository(dataSource: DataSource) : RandomBotMembershipRepository {
    private val jdbcTemplate = JdbcTemplate(dataSource)
    private val jdbcInsert = SimpleJdbcInsert(dataSource).withTableName(RandomBotMembershipTable.NAME)
    private val rowMapper = RowMapper { rs, _ ->
        RandomBotMembership(
                lobbyId = rs.getUUID(LOBBY_ID),
                botId = rs.getUUID(BOT_ID),
                joinedAt = rs.getInstantAtUTC(CREATED_AT)
        )
    }

    override fun insert(membership: RandomBotMembership) {
        val paramSource = MapSqlParameterSource()
                .addValue(BOT_ID, membership.botId)
                .addValue(LOBBY_ID, membership.lobbyId)
                .addValue(CREATED_AT, membership.joinedAt.toLocalDateTimeAtUTC())
        jdbcInsert.execute(paramSource)
    }

    override fun findByLobbyId(lobbyId: UUID): List<RandomBotMembership> {
        val sql = "SELECT * FROM ${RandomBotMembershipTable.NAME} WHERE $LOBBY_ID = ?"
        return jdbcTemplate.query(sql, rowMapper, lobbyId)
    }

    override fun deleteByLobbyIdAndBotId(lobbyId: UUID, botId: UUID) {
        val sql = "DELETE FROM ${RandomBotMembershipTable.NAME} WHERE $LOBBY_ID = ? AND $BOT_ID = ?"
        jdbcTemplate.update(sql, lobbyId, botId)
    }
}