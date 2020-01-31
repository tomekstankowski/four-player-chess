package pl.tomaszstankowski.fourplayerchess.lobby

import pl.tomaszstankowski.fourplayerchess.common.validation.ValidationError

private object Name {
    const val MIN_LENGTH = 5
    const val MAX_LENGTH = 50
}

internal class LobbyDetailsValidator {

    fun validate(lobbyDetails: LobbyDetails): Set<ValidationError> {
        val errors = HashSet<ValidationError>()
        if (lobbyDetails.name.isBlank()) {
            errors.add(
                ValidationError.Blank("name")
            )
        }
        if (lobbyDetails.name.length !in Name.MIN_LENGTH..Name.MAX_LENGTH) {
            errors.add(
                ValidationError.InvalidLength(
                    property = "name",
                    minLength = Name.MIN_LENGTH,
                    maxLength = Name.MAX_LENGTH
                )
            )
        }
        return errors
    }
}