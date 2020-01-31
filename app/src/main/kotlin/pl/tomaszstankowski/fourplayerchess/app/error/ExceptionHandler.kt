package pl.tomaszstankowski.fourplayerchess.app.error

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ExceptionHandler {

    @ExceptionHandler(ApiException::class)
    fun handleApiException(e: ApiException): ResponseEntity<ApiExceptionResponse> =
        ResponseEntity.status(e.httpStatus)
            .body(
                ApiExceptionResponse(
                    message = e.message ?: "",
                    data = e.data
                )
            )
}