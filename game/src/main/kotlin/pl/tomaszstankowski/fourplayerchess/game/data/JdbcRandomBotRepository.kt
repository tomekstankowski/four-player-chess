package pl.tomaszstankowski.fourplayerchess.game.data

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import pl.tomaszstankowski.fourplayerchess.data.getUUID
import pl.tomaszstankowski.fourplayerchess.engine.Color
import pl.tomaszstankowski.fourplayerchess.game.Player.RandomBot
import pl.tomaszstankowski.fourplayerchess.game.RandomBotRepository
import pl.tomaszstankowski.fourplayerchess.game.data.RandomBotTable.Columns.COLOR
import pl.tomaszstankowski.fourplayerchess.game.data.RandomBotTable.Columns.GAME_ID
import java.util.*
import javax.sql.DataSource

internal class JdbcRandomBotRepository(dataSource: DataSource) : RandomBotRepository {
    private val template = JdbcTemplate(dataSource)
    private val insert = SimpleJdbcInsert(dataSource).withTableName(RandomBotTable.NAME)
    private val rowMapper = RowMapper { rs, _ ->
        RandomBot(
                gameId = rs.getUUID(GAME_ID),
                color = rs.getString(COLOR).let { Color.valueOf(it) }
        )
    }

    override fun insert(randomBot: RandomBot) {
        val paramSource = MapSqlParameterSource()
                .addValue(GAME_ID, randomBot.gameId)
                .addValue(COLOR, randomBot.color.name)
        insert.execute(paramSource)
    }

    override fun findByGameId(gameId: UUID): List<RandomBot> {
        val sql = "SELECT * FROM ${RandomBotTable.NAME} WHERE $GAME_ID = ?"
        return template.query(sql, rowMapper, gameId)
    }
}