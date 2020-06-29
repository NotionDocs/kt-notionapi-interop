package schemas.collection

import kotlinx.serialization.ContextualSerialization
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.content

@Serializable
data class CollectionSchemaProperty(
    val name: String,
    val type: String,
    @ContextualSerialization
    val options: Any?
) {

    companion object {
        val from = { map: JsonObject ->
            val name by map
            val type by map
            val options = map["options"]
            CollectionSchemaProperty(
                name.content, type.content, options
            )
        }
    }
}