package models.block

import kotlinx.serialization.json.JsonArray
import models.block.content.TextItemsBlockContent


class NumberedBlock(children: JsonArray) : Block(BlockType.NUMBERED) {
    init {
        super.content = TextItemsBlockContent(children)
    }
}