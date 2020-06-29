package models.block

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import models.block.content.CalloutBlockContent

class CalloutBlock(val format: JsonObject, children: JsonArray) : Block(BlockType.CALLOUT) {
    init {
        super.content = CalloutBlockContent(format, children)
    }
}