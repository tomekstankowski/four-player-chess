package pl.tomaszstankowski.fourplayerchess.app.data

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

fun Instant.toLocalDateTimeAtUTC(): LocalDateTime = LocalDateTime.ofInstant(this, ZoneId.of("UTC"))

fun LocalDateTime.toInstantAtUTC(): Instant = this.toInstant(ZoneOffset.UTC)