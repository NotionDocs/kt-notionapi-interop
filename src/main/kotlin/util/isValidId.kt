package util

val isValidId = isValidId@{ id: String ->
    if (id.length != DASH_ID_LENGTH_VALID) return@isValidId false

    val parts = id.split('-')
    parts.size == 5
}