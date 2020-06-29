package models.block.content

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.json

class TodoBlockContent(val checked: Boolean, children: JsonArray) : TextItemsBlockContent(children) {
    override fun toJson() = json {
        "type" to "TodoBlockContent"
        "checked" to checked
        super.toJson().entries.forEach {
            it.key to it.value
        }
    }
}