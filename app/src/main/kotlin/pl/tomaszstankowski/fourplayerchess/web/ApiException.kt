package pl.tomaszstankowski.fourplayerchess.web

import org.springframework.http.HttpStatus
import pl.tomaszstankowski.fourplayerchess.common.validation.ValidationError
import java.util.*

class ApiException private constructor(
        message: String,
        val httpStatus: HttpStatus,
        val data: Map<String, Any?>
) : RuntimeException(message) {

    companion object {

        fun resourceNotFound(resource: String, id: UUID) = ApiException(
                message = "$resource with id $id not found",
                httpStatus = HttpStatus.NOT_FOUND,
                data = mapOf(
                        "id" to id
                )
        )

        fun unprocessableEntity(message: String, data: Map<String, Any?> = emptyMap()) = ApiException(
                message = message,
                httpStatus = HttpStatus.UNPROCESSABLE_ENTITY,
                data = data
        )

        fun invalidBody(errors: Set<ValidationError>) = ApiException(
                message = "Given body is invalid",
                httpStatus = HttpStatus.UNPROCESSABLE_ENTITY,
                data = mapOf(
                        "cause" to "INVALID_BODY",
                        "errors" to errors.map { it.toMap() }
                )
        )

        fun forbidden(message: String) = ApiException(
                message = message,
                httpStatus = HttpStatus.FORBIDDEN,
                data = emptyMap()
        )

        fun internalServerError() = ApiException(
                message = "Internal server error occurred",
                httpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
                data = emptyMap()
        )
    }
}