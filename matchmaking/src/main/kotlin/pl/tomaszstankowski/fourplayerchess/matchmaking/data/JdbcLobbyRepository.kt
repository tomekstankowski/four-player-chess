package pl.tomaszstankowski.fourplayerchess.matchmaking.data

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import pl.tomaszstankowski.fourplayerchess.common.utils.toInstantAtUTC
import pl.tomaszstankowski.fourplayerchess.common.utils.toLocalDateTimeAtUTC
import pl.tomaszstankowski.fourplayerchess.matchmaking.Lobby
import pl.tomaszstankowski.fourplayerchess.matchmaking.LobbyRepository
import pl.tomaszstankowski.fourplayerchess.matchmaking.data.LobbyTable.Columns.CREATED_AT
import pl.tomaszstankowski.fourplayerchess.matchmaking.data.LobbyTable.Columns.ID
import pl.tomaszstankowski.fourplayerchess.matchmaking.data.LobbyTable.Columns.NAME
import java.time.LocalDateTime
import java.util.*
import javax.sql.DataSource

internal class JdbcLobbyRepository(dataSource: DataSource) : LobbyRepository {
    private val jdbcTemplate = JdbcTemplate(dataSource)

    private val insert = SimpleJdbcInsert(dataSource)
            .withTableName(LobbyTable.NAME)

    private val mapper = RowMapper { rs, _ ->
        Lobby(
                id = rs.getObject(ID, UUID::class.java),
                name = rs.getString(NAME),
                createdAt = rs.getObject(CREATED_AT, LocalDateTime::class.java).toInstantAtUTC()
        )
    }

    override fun create(lobby: Lobby) {
        insert.execute(
                mapOf(
                        ID to lobby.id,
                        NAME to lobby.name,
                        CREATED_AT to lobby.createdAt.toLocalDateTimeAtUTC()
                )
        )
    }

    override fun findById(id: UUID): Lobby? =
            jdbcTemplate.query("SELECT * FROM ${LobbyTable.NAME} WHERE $ID = ?", mapper, id)
                    .firstOrNull()

    override fun findByName(name: String): Lobby? =
            jdbcTemplate.query("SELECT * FROM ${LobbyTable.NAME} WHERE $NAME = ?", mapper, name)
                    .firstOrNull()

    override fun findAll(): List<Lobby> =
            jdbcTemplate.query("SELECT * FROM ${LobbyTable.NAME}", mapper)

    override fun update(lobby: Lobby) {
        jdbcTemplate.update(
                """
           UPDATE ${LobbyTable.NAME} SET 
           $NAME = ?,
           $CREATED_AT = ?
           WHERE $ID = ?
        """,
                lobby.name,
                lobby.createdAt.toLocalDateTimeAtUTC(),
                lobby.id
        )
    }

    override fun delete(id: UUID) {
        jdbcTemplate.update("DELETE FROM ${LobbyTable.NAME} WHERE $ID = ?", id)
    }
}