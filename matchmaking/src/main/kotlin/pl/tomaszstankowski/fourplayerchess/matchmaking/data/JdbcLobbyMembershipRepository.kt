package pl.tomaszstankowski.fourplayerchess.matchmaking.data

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import pl.tomaszstankowski.fourplayerchess.data.getInstantAtUTC
import pl.tomaszstankowski.fourplayerchess.data.getUUID
import pl.tomaszstankowski.fourplayerchess.data.toLocalDateTimeAtUTC
import pl.tomaszstankowski.fourplayerchess.matchmaking.LobbyMembership
import pl.tomaszstankowski.fourplayerchess.matchmaking.LobbyMembershipRepository
import pl.tomaszstankowski.fourplayerchess.matchmaking.data.LobbyMembershipTable.Columns.CREATED_AT
import pl.tomaszstankowski.fourplayerchess.matchmaking.data.LobbyMembershipTable.Columns.LOBBY_ID
import pl.tomaszstankowski.fourplayerchess.matchmaking.data.LobbyMembershipTable.Columns.PLAYER_ID
import java.sql.ResultSet
import java.util.*
import javax.sql.DataSource

internal class JdbcLobbyMembershipRepository(dataSource: DataSource) : LobbyMembershipRepository {
    private val jdbcTemplate = NamedParameterJdbcTemplate(dataSource)
    private val jdbcInsert = SimpleJdbcInsert(dataSource)
            .withTableName(LobbyMembershipTable.NAME)

    override fun insert(lobbyMembership: LobbyMembership) {
        jdbcInsert.execute(
                MapSqlParameterSource()
                        .addValue(LOBBY_ID, lobbyMembership.lobbyId)
                        .addValue(PLAYER_ID, lobbyMembership.playerId)
                        .addValue(CREATED_AT, lobbyMembership.joinedAt.toLocalDateTimeAtUTC())
        )
    }

    override fun findByLobbyIdOrderByCreatedAtDesc(lobbyId: UUID): List<LobbyMembership> {
        val sql = "SELECT * FROM ${LobbyMembershipTable.NAME} WHERE $LOBBY_ID = :lobbyId ORDER BY $CREATED_AT DESC"
        val params = MapSqlParameterSource("lobbyId", lobbyId)
        return jdbcTemplate.query(sql, params, this::mapRow)
    }

    override fun findByPlayerId(playerId: UUID): LobbyMembership? {
        val sql = "SELECT * FROM ${LobbyMembershipTable.NAME} WHERE $PLAYER_ID = :playerId"
        val params = MapSqlParameterSource("playerId", playerId)
        return jdbcTemplate.query(sql, params, this::mapRow).firstOrNull()
    }

    override fun deleteByLobbyIdAndPlayerId(lobbyId: UUID, playerId: UUID) {
        val sql = "DELETE FROM ${LobbyMembershipTable.NAME} WHERE $LOBBY_ID = :lobbyId AND $PLAYER_ID = :playerId"
        val params = MapSqlParameterSource()
                .addValue("lobbyId", lobbyId)
                .addValue("playerId", playerId)
        jdbcTemplate.update(sql, params)
    }

    override fun deleteByLobbyId(lobbyId: UUID) {
        val sql = "DELETE FROM ${LobbyMembershipTable.NAME} WHERE $LOBBY_ID = :lobbyId"
        val params = MapSqlParameterSource()
                .addValue("lobbyId", lobbyId)
        jdbcTemplate.update(sql, params)
    }

    private fun mapRow(rs: ResultSet, rowNum: Int): LobbyMembership =
            LobbyMembership(
                    lobbyId = rs.getUUID(LOBBY_ID),
                    playerId = rs.getUUID(PLAYER_ID),
                    joinedAt = rs.getInstantAtUTC(CREATED_AT)
            )
}