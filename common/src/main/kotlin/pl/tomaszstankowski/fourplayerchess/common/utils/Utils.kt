package pl.tomaszstankowski.fourplayerchess.common.utils

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*

fun Instant.toLocalDateTimeAtUTC(): LocalDateTime = LocalDateTime.ofInstant(this, ZoneId.of("UTC"))

fun LocalDateTime.toInstantAtUTC(): Instant = this.toInstant(ZoneOffset.UTC)

fun String.toUUID() = UUID.fromString(this)