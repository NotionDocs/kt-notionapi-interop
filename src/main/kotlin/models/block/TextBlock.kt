package models.block

import kotlinx.serialization.json.JsonArray
import models.block.content.TextBlockContent

class TextBlock(val level: Int, children: JsonArray) : Block(BlockType.TEXT) {
    init {
        super.content = TextBlockContent(level, children)
    }
}