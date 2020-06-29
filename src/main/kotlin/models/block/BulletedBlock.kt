package models.block

import kotlinx.serialization.json.JsonArray
import models.block.content.TextItemsBlockContent


class BulletedBlock(children: JsonArray) : Block(BlockType.BULLETED) {
    init {
        super.content = TextItemsBlockContent(children)
    }
}