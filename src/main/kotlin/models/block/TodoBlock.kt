package models.block

import kotlinx.serialization.json.JsonArray
import models.block.content.TodoBlockContent


class TodoBlock(val checked: Boolean, children: JsonArray) : Block(BlockType.TODO) {
    init {
        super.content = TodoBlockContent(checked, children)
    }
}