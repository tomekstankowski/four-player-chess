package pl.tomaszstankowski.fourplayerchess.data

import org.springframework.transaction.support.TransactionOperations
import java.sql.ResultSet
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*

fun ResultSet.getUUID(column: String): UUID =
        getObject(column, UUID::class.java)!!

fun ResultSet.getInstantAtUTC(column: String): Instant =
        getObject(column, LocalDateTime::class.java).toInstant(ZoneOffset.UTC)

fun Instant.toLocalDateTimeAtUTC(): LocalDateTime = LocalDateTime.ofInstant(this, ZoneId.of("UTC"))

fun <T> TransactionOperations.executeWithResult(callback: () -> T): T =
        this.execute { callback() }!!