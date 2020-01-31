package pl.tomaszstankowski.fourplayerchess.app.error

import pl.tomaszstankowski.fourplayerchess.common.validation.ValidationError

fun ValidationError.toMap() =
    mapOf(
        "property" to this.property,
        "message" to this.message
    )