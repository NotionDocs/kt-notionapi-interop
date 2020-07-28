package util

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

val json = Json(JsonConfiguration.Stable)

operator fun Any?.get(key: String): Any? {
    return this.toMap<Any>()[key]
}

operator fun Any?.get(key: Int): Any? {
    return this.toList<Any>()[key]
}

fun <T> Any?.toMap(): Map<String, T> {
    return this as Map<String, T>
}

fun <T> Any?.toList(): List<T> {
    return this as List<T>
}