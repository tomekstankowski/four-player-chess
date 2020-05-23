package pl.tomaszstankowski.fourplayerchess.common.validation

import org.valiktor.ConstraintViolationException
import org.valiktor.i18n.mapToMessage

val ConstraintViolationException.validationErrors: Set<ValidationError>
    get() = this.constraintViolations
            .mapToMessage()
            .map {
                ValidationError(it.property, it.message)
            }
            .toSet()