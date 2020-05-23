package pl.tomaszstankowski.fourplayerchess.matchmaking

import org.valiktor.ConstraintViolationException
import org.valiktor.functions.hasSize
import org.valiktor.functions.isNotBlank
import org.valiktor.validate
import pl.tomaszstankowski.fourplayerchess.common.validation.ValidationError
import pl.tomaszstankowski.fourplayerchess.common.validation.validationErrors

internal fun validate(lobbyEditableDetails: LobbyEditableDetails): Set<ValidationError> {
    return try {
        validate(lobbyEditableDetails) {
            validate(LobbyEditableDetails::name).hasSize(min = 5, max = 50).isNotBlank()
        }
        emptySet()
    } catch (ex: ConstraintViolationException) {
        ex.validationErrors
    }
}