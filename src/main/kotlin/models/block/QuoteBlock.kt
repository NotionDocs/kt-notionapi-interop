package models.block

import kotlinx.serialization.json.JsonArray
import models.block.content.TextItemsBlockContent

class QuoteBlock(children: JsonArray) : Block(BlockType.QUOTE) {
    init {
        super.content = TextItemsBlockContent(children)
    }
}