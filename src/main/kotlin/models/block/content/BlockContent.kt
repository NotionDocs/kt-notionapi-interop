package models.block.content

import kotlinx.serialization.json.JsonObject

interface BlockContent {
    val items: Any
    fun toJson(): JsonObject
}