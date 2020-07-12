package pl.tomaszstankowski.fourplayerchess.game.data

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import pl.tomaszstankowski.fourplayerchess.data.getUUID
import pl.tomaszstankowski.fourplayerchess.engine.Color
import pl.tomaszstankowski.fourplayerchess.game.HumanPlayerRepository
import pl.tomaszstankowski.fourplayerchess.game.Player.HumanPlayer
import pl.tomaszstankowski.fourplayerchess.game.data.HumanPlayerTable.Columns.COLOR
import pl.tomaszstankowski.fourplayerchess.game.data.HumanPlayerTable.Columns.GAME_ID
import pl.tomaszstankowski.fourplayerchess.game.data.HumanPlayerTable.Columns.USER_ID
import java.util.*
import javax.sql.DataSource

internal class JdbcHumanPlayerRepository(dataSource: DataSource) : HumanPlayerRepository {
    private val insert = SimpleJdbcInsert(dataSource)
            .withTableName(HumanPlayerTable.NAME)
    private val template = JdbcTemplate(dataSource)
    private val mapper = RowMapper { rs, _ ->
        HumanPlayer(
                gameId = rs.getUUID(GAME_ID),
                userId = rs.getUUID(USER_ID),
                color = rs.getString(COLOR).let { Color.valueOf(it) }
        )
    }

    override fun insert(player: HumanPlayer) {
        val paramSource = mapOf(
                GAME_ID to player.gameId,
                USER_ID to player.userId,
                COLOR to player.color.name
        )
        insert.execute(paramSource)
    }

    override fun findByGameId(gameId: UUID): List<HumanPlayer> {
        val sql = "SELECT * FROM ${HumanPlayerTable.NAME} WHERE $GAME_ID = ?"
        return template.query(sql, mapper, gameId)
    }
}