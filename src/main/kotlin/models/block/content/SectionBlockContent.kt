package models.block.content

import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.json

class SectionBlockContent(val blocks: JsonArray) : BlockContent {
    override val items: Any
        get() = error("")

    override fun toJson() = json {
        "type" to "SectionBlockContent"
        "blocks" to util.json.toJson(JsonElement.serializer().list, blocks)
    }
}