package util

const val DASH_ID_LENGTH_VALID = 36
const val DASH_ID_CLEAN_LENGTH_VALID = 32

val dashifyId = dashifyId@{ id: String ->
    if (isValidId(id)) return@dashifyId id

    val clean = id.replace("/-/g".toRegex(), "")
    if (clean.length != DASH_ID_CLEAN_LENGTH_VALID)
        throw Exception("Incorrect id format: $id")

    listOf(
        clean.substring(0, 8),
        "-",
        clean.substring(8, 12),
        "-",
        clean.substring(12, 16),
        "-",
        clean.substring(16, 20),
        "-",
        clean.substring(20, 32)
    ).reduce { acc: String, element -> "$acc$element" }
}