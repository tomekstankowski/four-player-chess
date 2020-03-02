package pl.tomaszstankowski.fourplayerchess.lobby

import pl.tomaszstankowski.fourplayerchess.common.validation.ValidationError

private object Name {
    const val MIN_LENGTH = 5
    const val MAX_LENGTH = 50
}

private typealias Validator = (LobbyDetails) -> ValidationError?

private val nameNotBlank: Validator = { details ->
    if (details.name.isBlank())
        ValidationError.Blank("name")
    else
        null
}
private val nameHasCorrectLength: Validator = { details ->
    if (details.name.length !in Name.MIN_LENGTH..Name.MAX_LENGTH)
        ValidationError.InvalidLength(
                property = "name",
                minLength = Name.MIN_LENGTH,
                maxLength = Name.MAX_LENGTH
        )
    else null
}

internal fun validate(lobbyDetails: LobbyDetails): Set<ValidationError> =
        listOf(nameNotBlank, nameHasCorrectLength)
                .mapNotNull { validator -> validator(lobbyDetails) }
                .toSet()