package models.block.content

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.json

class TextBlockContent(val level: Int, children: JsonArray) : TextItemsBlockContent(children) {
    override fun toJson() = json {
        "type" to "TextBlockContent"
        "level" to level
        super.toJson().entries.forEach {
            if (it.key != "type") it.key to it.value
        }
    }
}