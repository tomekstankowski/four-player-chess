package pl.tomaszstankowski.fourplayerchess.matchmaking.data

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import pl.tomaszstankowski.fourplayerchess.data.getInstantAtUTC
import pl.tomaszstankowski.fourplayerchess.data.getUUID
import pl.tomaszstankowski.fourplayerchess.data.toLocalDateTimeAtUTC
import pl.tomaszstankowski.fourplayerchess.matchmaking.HumanPlayerMembershipRepository
import pl.tomaszstankowski.fourplayerchess.matchmaking.LobbyMembership.HumanPlayerMembership
import pl.tomaszstankowski.fourplayerchess.matchmaking.data.LobbyMembershipTable.Columns.CREATED_AT
import pl.tomaszstankowski.fourplayerchess.matchmaking.data.LobbyMembershipTable.Columns.LOBBY_ID
import pl.tomaszstankowski.fourplayerchess.matchmaking.data.LobbyMembershipTable.Columns.USER_ID
import java.sql.ResultSet
import java.util.*
import javax.sql.DataSource

internal class JdbcHumanPlayerMembershipRepository(dataSource: DataSource) : HumanPlayerMembershipRepository {
    private val jdbcTemplate = NamedParameterJdbcTemplate(dataSource)
    private val jdbcInsert = SimpleJdbcInsert(dataSource)
            .withTableName(LobbyMembershipTable.NAME)

    override fun insert(membership: HumanPlayerMembership) {
        jdbcInsert.execute(
                MapSqlParameterSource()
                        .addValue(LOBBY_ID, membership.lobbyId)
                        .addValue(USER_ID, membership.userId)
                        .addValue(CREATED_AT, membership.joinedAt.toLocalDateTimeAtUTC())
        )
    }

    override fun findByLobbyId(lobbyId: UUID): List<HumanPlayerMembership> {
        val sql = "SELECT * FROM ${LobbyMembershipTable.NAME} WHERE $LOBBY_ID = :lobbyId ORDER BY $CREATED_AT DESC"
        val params = MapSqlParameterSource("lobbyId", lobbyId)
        return jdbcTemplate.query(sql, params, this::mapRow)
    }

    override fun deleteByLobbyIdAndPlayerId(lobbyId: UUID, playerId: UUID) {
        val sql = "DELETE FROM ${LobbyMembershipTable.NAME} WHERE $LOBBY_ID = :lobbyId AND $USER_ID = :playerId"
        val params = MapSqlParameterSource()
                .addValue("lobbyId", lobbyId)
                .addValue("playerId", playerId)
        jdbcTemplate.update(sql, params)
    }

    private fun mapRow(rs: ResultSet, rowNum: Int): HumanPlayerMembership =
            HumanPlayerMembership(
                    lobbyId = rs.getUUID(LOBBY_ID),
                    userId = rs.getUUID(USER_ID),
                    joinedAt = rs.getInstantAtUTC(CREATED_AT)
            )
}