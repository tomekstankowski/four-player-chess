package pl.tomaszstankowski.fourplayerchess.app.error

data class ApiExceptionResponse(val message: String, val data: Map<String, Any?>)