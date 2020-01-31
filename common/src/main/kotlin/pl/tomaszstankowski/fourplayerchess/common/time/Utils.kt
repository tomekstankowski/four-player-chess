package pl.tomaszstankowski.fourplayerchess.common.time

import java.time.Clock
import java.time.Instant
import java.time.ZoneId

fun fixedClock(): Clock = Clock.fixed(Instant.parse("2020-01-31T20:10:01.282957Z"), ZoneId.of("UTC"))