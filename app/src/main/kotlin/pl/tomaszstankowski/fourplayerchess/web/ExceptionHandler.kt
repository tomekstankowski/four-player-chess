package pl.tomaszstankowski.fourplayerchess.web

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import pl.tomaszstankowski.fourplayerchess.web.ApiException

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