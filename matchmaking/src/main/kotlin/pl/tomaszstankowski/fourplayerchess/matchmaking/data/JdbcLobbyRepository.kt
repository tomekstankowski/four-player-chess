package pl.tomaszstankowski.fourplayerchess.matchmaking.data

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import pl.tomaszstankowski.fourplayerchess.data.getInstantAtUTC
import pl.tomaszstankowski.fourplayerchess.data.getUUID
import pl.tomaszstankowski.fourplayerchess.data.getUUIDOrNull
import pl.tomaszstankowski.fourplayerchess.data.toLocalDateTimeAtUTC
import pl.tomaszstankowski.fourplayerchess.matchmaking.Lobby
import pl.tomaszstankowski.fourplayerchess.matchmaking.LobbyRepository
import pl.tomaszstankowski.fourplayerchess.matchmaking.data.LobbyTable.Columns.CREATED_AT
import pl.tomaszstankowski.fourplayerchess.matchmaking.data.LobbyTable.Columns.DELETED
import pl.tomaszstankowski.fourplayerchess.matchmaking.data.LobbyTable.Columns.GAME_ID
import pl.tomaszstankowski.fourplayerchess.matchmaking.data.LobbyTable.Columns.ID
import pl.tomaszstankowski.fourplayerchess.matchmaking.data.LobbyTable.Columns.NAME
import pl.tomaszstankowski.fourplayerchess.matchmaking.data.LobbyTable.Columns.OWNER_ID
import pl.tomaszstankowski.fourplayerchess.matchmaking.data.LobbyTable.Columns.VERSION
import java.util.*
import javax.sql.DataSource

internal class JdbcLobbyRepository(dataSource: DataSource) : LobbyRepository {
    private val jdbcTemplate = JdbcTemplate(dataSource)
    private val insert = SimpleJdbcInsert(dataSource)
            .withTableName(LobbyTable.NAME)
    private val mapper = RowMapper { rs, _ ->
        Lobby(
                id = rs.getUUID(ID),
                name = rs.getString(NAME),
                createdAt = rs.getInstantAtUTC(CREATED_AT),
                ownerId = rs.getUUID(OWNER_ID),
                version = rs.getInt(VERSION),
                isDeleted = rs.getBoolean(DELETED),
                gameId = rs.getUUIDOrNull(GAME_ID)
        )
    }

    override fun create(lobby: Lobby) {
        insert.execute(
                mapOf(
                        ID to lobby.id,
                        NAME to lobby.name,
                        CREATED_AT to lobby.createdAt.toLocalDateTimeAtUTC(),
                        OWNER_ID to lobby.ownerId,
                        GAME_ID to lobby.gameId,
                        DELETED to lobby.isDeleted,
                        VERSION to lobby.version
                )
        )
    }

    override fun findById(id: UUID): Lobby? =
            jdbcTemplate.query("SELECT * FROM ${LobbyTable.NAME} WHERE $ID = ?", mapper, id)
                    .firstOrNull()

    override fun findByName(name: String): Lobby? =
            jdbcTemplate.query("SELECT * FROM ${LobbyTable.NAME} WHERE $NAME = ?", mapper, name)
                    .firstOrNull()

    override fun findByPlayerId(playerId: UUID): List<Lobby> {
        val sql = "SELECT * FROM ${LobbyTable.NAME} l " +
                "LEFT JOIN ${LobbyMembershipTable.NAME} lm ON l.$ID = lm.${LobbyMembershipTable.Columns.LOBBY_ID} " +
                "WHERE lm.${LobbyMembershipTable.Columns.USER_ID} = ?"
        return jdbcTemplate.query(sql, mapper, playerId)
    }

    override fun updateIfVersionEquals(lobby: Lobby, version: Int): Boolean {
        val numOfRowsUpdated = jdbcTemplate.update(
                """
           UPDATE ${LobbyTable.NAME} SET 
           $NAME = ?,
           $CREATED_AT = ?,
           $OWNER_ID = ?,
           $VERSION = ?,
           $GAME_ID = ?,
           $DELETED = ?
           WHERE $ID = ? AND $VERSION = ?
        """,
                lobby.name,
                lobby.createdAt.toLocalDateTimeAtUTC(),
                lobby.ownerId,
                lobby.version,
                lobby.gameId,
                lobby.isDeleted,
                lobby.id,
                version
        )
        return numOfRowsUpdated > 0
    }

    override fun delete(id: UUID) {
        jdbcTemplate.update("DELETE FROM ${LobbyTable.NAME} WHERE $ID = ?", id)
    }
}