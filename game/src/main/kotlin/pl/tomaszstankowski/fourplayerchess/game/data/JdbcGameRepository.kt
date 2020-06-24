package pl.tomaszstankowski.fourplayerchess.game.data

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import pl.tomaszstankowski.fourplayerchess.data.getInstantAtUTC
import pl.tomaszstankowski.fourplayerchess.data.getUUID
import pl.tomaszstankowski.fourplayerchess.data.toLocalDateTimeAtUTC
import pl.tomaszstankowski.fourplayerchess.game.Game
import pl.tomaszstankowski.fourplayerchess.game.GameRepository
import pl.tomaszstankowski.fourplayerchess.game.data.GameTable.Columns.CANCELLED
import pl.tomaszstankowski.fourplayerchess.game.data.GameTable.Columns.COMMITTED
import pl.tomaszstankowski.fourplayerchess.game.data.GameTable.Columns.CREATED_AT
import pl.tomaszstankowski.fourplayerchess.game.data.GameTable.Columns.ID
import java.util.*
import javax.sql.DataSource

internal class JdbcGameRepository(dataSource: DataSource) : GameRepository {
    private val insert = SimpleJdbcInsert(dataSource)
            .withTableName(GameTable.NAME)
    private val template = JdbcTemplate(dataSource)
    private val rowMapper = RowMapper { rs, _ ->
        Game(
                id = rs.getUUID(ID),
                createdAt = rs.getInstantAtUTC(CREATED_AT),
                isCommitted = rs.getBoolean(COMMITTED),
                isCancelled = rs.getBoolean(CANCELLED)
        )
    }

    override fun insert(game: Game) {
        insert.execute(
                mapOf(
                        ID to game.id,
                        CREATED_AT to game.createdAt.toLocalDateTimeAtUTC(),
                        COMMITTED to game.isCommitted,
                        CANCELLED to game.isCancelled
                )
        )
    }

    override fun update(game: Game) {
        template.update(
                "UPDATE ${GameTable.NAME} " +
                        "SET $CREATED_AT = ?, " +
                        "$COMMITTED = ?, " +
                        "$CANCELLED = ? " +
                        "WHERE $ID = ?",
                game.createdAt.toLocalDateTimeAtUTC(),
                game.isCommitted,
                game.isCancelled,
                game.id
        )
    }

    override fun findById(id: UUID): Game? {
        val sql = "SELECT * FROM ${GameTable.NAME} WHERE $ID = ?"
        val rows = template.query(sql, rowMapper, id)
        return rows.firstOrNull()
    }

    override fun findByPlayerId(playerId: UUID): List<Game> {
        val sql = "SELECT * FROM ${GameTable.NAME} g " +
                "LEFT JOIN ${GamePlayerTable.NAME} p ON p.${GamePlayerTable.Columns.GAME_ID} = g.$ID " +
                "WHERE p.${GamePlayerTable.Columns.PLAYER_ID} = ?"
        return template.query(sql, rowMapper, playerId)
    }

    override fun findByIsCommittedIsTrueAndIsCancelledIsFalse(): List<Game> {
        val sql = "SELECT * FROM ${GameTable.NAME} WHERE $COMMITTED = TRUE AND $CANCELLED = FALSE"
        return template.query(sql, rowMapper)
    }
}