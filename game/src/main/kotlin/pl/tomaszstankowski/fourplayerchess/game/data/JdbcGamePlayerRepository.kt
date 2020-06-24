package pl.tomaszstankowski.fourplayerchess.game.data

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import pl.tomaszstankowski.fourplayerchess.data.getUUID
import pl.tomaszstankowski.fourplayerchess.engine.Color
import pl.tomaszstankowski.fourplayerchess.game.GamePlayer
import pl.tomaszstankowski.fourplayerchess.game.GamePlayerRepository
import pl.tomaszstankowski.fourplayerchess.game.data.GamePlayerTable.Columns.COLOR
import pl.tomaszstankowski.fourplayerchess.game.data.GamePlayerTable.Columns.GAME_ID
import pl.tomaszstankowski.fourplayerchess.game.data.GamePlayerTable.Columns.PLAYER_ID
import java.util.*
import javax.sql.DataSource

internal class JdbcGamePlayerRepository(dataSource: DataSource) : GamePlayerRepository {
    private val insert = SimpleJdbcInsert(dataSource)
            .withTableName(GamePlayerTable.NAME)
    private val template = JdbcTemplate(dataSource)
    private val mapper = RowMapper<GamePlayer> { rs, _ ->
        GamePlayer(
                gameId = rs.getUUID(GAME_ID),
                playerId = rs.getUUID(PLAYER_ID),
                color = rs.getString(COLOR).let { Color.valueOf(it) }
        )
    }

    override fun insert(gamePlayer: GamePlayer) {
        insert.execute(
                mapOf(
                        GAME_ID to gamePlayer.gameId,
                        PLAYER_ID to gamePlayer.playerId,
                        COLOR to gamePlayer.color.name
                )
        )
    }

    override fun findByGameId(gameId: UUID): List<GamePlayer> {
        val sql = "SELECT * FROM ${GamePlayerTable.NAME} WHERE $GAME_ID = ?"
        return template.query(sql, mapper, gameId)
    }
}