package schemas.block

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonLiteral
import kotlinx.serialization.json.JsonObject

data class BlockValue(
    val id: String,
    val type: String,
    val properties: JsonObject?,
    val format: JsonObject?,
    val content: JsonArray?
) {
    companion object {
        val from = { map: Map<String, Any> ->
            val id by map
            val type by map
            val properties = map["properties"]
            val format = map["format"]
            val content = map["content"]
            BlockValue(
                (id as JsonLiteral).content,
                (type as JsonLiteral).content,
                properties as JsonObject?,
                format as JsonObject?,
                content as JsonArray?
            )
        }
    }
}