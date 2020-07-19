package schemas.collection

import kotlinx.serialization.ContextualSerialization
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.content

@Serializable
data class CollectionSchemaProperty(
    val key: String,
    val type: String,
    @ContextualSerialization
    val options: Any?
) {

    companion object {
        val from = { key: String, map: JsonObject ->
            val type by map
            val options = map["options"]
            CollectionSchemaProperty(
                key, type.content, options
            )
        }
    }
}