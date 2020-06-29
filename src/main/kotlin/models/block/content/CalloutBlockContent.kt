package models.block.content

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.json

class CalloutBlockContent(val format: JsonObject, children: JsonArray) : TextItemsBlockContent(children) {
    override fun toJson() = json {
        "type" to "CalloutBlockContent"
        format.content.entries.forEach {
            it.key to it.value
        }
        super.toJson().entries.forEach {
            it.key to it.value
        }
    }
}