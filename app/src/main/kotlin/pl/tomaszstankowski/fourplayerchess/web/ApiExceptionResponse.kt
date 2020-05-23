package pl.tomaszstankowski.fourplayerchess.web

data class ApiExceptionResponse(val message: String, val data: Map<String, Any?>)