package models.block

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonLiteral
import kotlinx.serialization.json.JsonObject
import schemas.block.BlockValue
import util.get
import util.toMap
import kotlin.collections.set

class PageBlock(blockValue: BlockValue, val metadata: JsonObject) {
    var title: String? = null
    val properties: MutableMap<String, JsonElement> = mutableMapOf()

    init {
        blockValue.properties.toMap<Any>().entries.forEach {
            when (metadata.content["properties"][it.key]?.let { (it["type"] as JsonLiteral).content }) {
                "relation" -> properties[it.key] = (it.value[0][1][0][1] as JsonLiteral)
                "select" -> properties[it.key] = (it.value[0][0] as JsonLiteral)
                "title" -> title = (it.value[0][0] as JsonLiteral).content
            }
        }
    }
}