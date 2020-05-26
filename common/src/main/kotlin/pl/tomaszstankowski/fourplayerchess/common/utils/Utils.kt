package pl.tomaszstankowski.fourplayerchess.common.utils

import java.util.*

fun String.toUUID(): UUID = UUID.fromString(this)