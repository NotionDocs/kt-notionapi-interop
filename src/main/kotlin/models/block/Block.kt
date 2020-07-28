package models.block

import kotlinx.serialization.json.json
import models.block.content.BlockContent

class Block(
    val type: BlockType,
    var content: BlockContent
) {
    fun toJson() = json {
        "content" to content.toJson().content.entries.forEach {
            it.key to it.value
        }
    }

}