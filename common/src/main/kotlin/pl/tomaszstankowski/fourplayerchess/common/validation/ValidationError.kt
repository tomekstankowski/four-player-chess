package pl.tomaszstankowski.fourplayerchess.common.validation

sealed class ValidationError {
    abstract val property: String

    abstract val message: String

    data class Blank(override val property: String) : ValidationError() {

        override val message: String
            get() = "$property should not be blank"
    }

    data class InvalidLength(override val property: String, val minLength: Int, val maxLength: Int) :
        ValidationError() {

        override val message: String
            get() = "$property should be between $minLength and $maxLength characters"
    }
}