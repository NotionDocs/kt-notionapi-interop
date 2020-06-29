package models.block

import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.json

data class ColumnBlock(val blocks: JsonArray) : Block(BlockType.COLUMN) {
    override fun toJson() = json {
        "content" to json {
            "type" to "SectionBlockContent"
            "blocks" to util.json.toJson(JsonElement.serializer().list, blocks)
        }
    }
}