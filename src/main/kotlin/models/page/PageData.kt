package models.page

import kotlinx.serialization.json.*
import models.block.Block
import models.block.BlockType
import models.block.PageBlock
import models.block.content.SectionBlockContent
import models.block.content.TextBlockContent
import models.block.content.TextItemsBlockContent
import models.block.content.TodoBlockContent
import schemas.block.BlockValue
import schemas.collection.CollectionSchemaProperty
import util.get
import util.toMap

class PageData(message: JsonObject) {
    private val metadata: JsonObject
    private var title: String
    private var properties: JsonObject
    var content: JsonArray

    companion object {
        fun getMetadata(message: JsonObject): JsonObject {
            val properties =
                try {
                    val properties = mutableMapOf<String, CollectionSchemaProperty>()
                    val collectionSchema =
                        message["recordMap"]["collection"].toMap<JsonObject>().entries.first()
                            .value["value"]["schema"].toMap<JsonObject>()
                    collectionSchema.entries.forEach {
                        properties[it.value["name"]!!.content] = CollectionSchemaProperty.from(it.key, it.value)
                    }
                    properties
                } catch (e: Throwable) {
                    mutableMapOf<String, CollectionSchemaProperty>()
                }

            val createdTime: Long? =
                try {
                    val firstBlock =
                        message["recordMap"]["block"].toMap<JsonObject>().entries.first()
                            .value["value"].toMap<Any>()
                    (firstBlock["created_time"] as JsonLiteral).double.toLong()
                } catch (e: Throwable) {
                    null
                }

            val lastEditedTime: Long? =
                try {
                    val firstBlock =
                        message["recordMap"]["block"].toMap<JsonObject>().entries.first()
                            .value["value"].toMap<Any>()
                    (firstBlock["last_edited_time"] as JsonLiteral).double.toLong()
                } catch (e: Throwable) {
                    null
                }

            val format: JsonObject? =
                try {
                    val firstBlock =
                        message["recordMap"]["block"].toMap<JsonObject>().entries.first()
                            .value["value"].toMap<Any>()
                    val format = (firstBlock["format"] as JsonObject)
                    json {
                        "font" to format["page_font"]?.content
                        "cover" to format["page_cover"]?.content
                    }
                } catch (e: Throwable) {
                    null
                }

            return json {
                "properties" to json {
                    properties.entries.map {
                        it.key to util.json.toJson(it.value)
                    }
                }
                "createdTime" to createdTime
                "lastEditedTime" to lastEditedTime
                "format" to json { format?.let { format -> format.entries.forEach { it.key to it.value } } }
            }
        }

        fun getContent(message: JsonObject, metadata: JsonObject): Map<String, Any?> {
            var title: String? = null
            var properties: JsonObject? = null
            val content: JsonArray?

            val blocks: MutableList<Pair<String, Block>> = mutableListOf()

            try {
                message["recordMap"]["block"].toMap<JsonObject>().entries.forEach { recordMapBlock ->
                    val block = recordMapBlock.value["value"].toMap<Any>()
                    val blockValue = BlockValue.from(block)

                    when (blockValue.type) {
                        "page" -> {
                            if (properties == null) {
                                val pageBlock = PageBlock(blockValue, metadata)
                                properties = JsonObject(pageBlock.properties)
                                title = pageBlock.title.toString()
                            }
                        }
                        "column_list" -> {
                            blocks += recordMapBlock.key to Block(
                                type = BlockType.ROW,
                                content = SectionBlockContent(blockValue.content as JsonArray)
                            )
                        }
                        "column" -> {
                            blocks += recordMapBlock.key to Block(
                                type = BlockType.COLUMN,
                                content = SectionBlockContent(blockValue.content as JsonArray)
                            )
                        }
                        "header", "sub_header", "sub_sub_header", "text" -> {
                            blocks += recordMapBlock.key to Block(
                                type = BlockType.TEXT,
                                content = TextBlockContent(
                                    when (blockValue.type) {
                                        "header" -> 1
                                        "sub_header" -> 2
                                        "sub_sub_header" -> 3
                                        else -> 4
                                    },
                                    blockValue.properties?.let { it["title"] as JsonArray }
                                        ?: JsonArray(listOf()))
                            )
                        }
                        "to_do" -> {
                            blocks += recordMapBlock.key to Block(
                                type = BlockType.TODO,
                                content = TodoBlockContent(
                                    (blockValue.properties["checked"])?.let { (it[0][0] as JsonLiteral).content == "Yes" }
                                        ?: false,
                                    blockValue.properties?.let { it["title"] as JsonArray }
                                        ?: JsonArray(listOf())
                                ))
                        }
                        "numbered_list" -> {
                            blocks += recordMapBlock.key to Block(
                                type = BlockType.NUMBERED,
                                content = TextItemsBlockContent(
                                    blockValue.properties?.let { it["title"] as JsonArray }
                                        ?: JsonArray(listOf())
                                ))
                        }
                        "bulleted_list" -> {
                            blocks += recordMapBlock.key to Block(
                                type = BlockType.BULLETED,
                                content = TextItemsBlockContent(
                                    blockValue.properties?.let { it["title"] as JsonArray }
                                        ?: JsonArray(listOf())
                                ))
                        }
                        "quote" -> {
                            blocks += recordMapBlock.key to Block(
                                type = BlockType.QUOTE,
                                content = TextItemsBlockContent(
                                    blockValue.properties?.let { it["title"] as JsonArray }
                                        ?: JsonArray(listOf())
                                ))
                        }
                        "callout" -> {
                            blocks += recordMapBlock.key to Block(
                                type = BlockType.CALLOUT,
                                content = TextItemsBlockContent(
                                    blockValue.properties?.let { it["title"] as JsonArray }
                                        ?: JsonArray(listOf())
                                ))
                        }
                        //else -> println(blockValue.type)
                    }
                }
                content = jsonArray {
                    blocks.forEach {
                        +json {
                            "id" to it.first
                            "type" to it.second.type.name
                            "content" to it.second.toJson()
                        }
                    }
                }
            } catch (e: Throwable) {
                throw (e)
            }

            return mapOf(
                "title" to title,
                "properties" to properties,
                "content" to content
            )
        }
    }

    init {
        metadata = getMetadata(message)
        val pageContent = getContent(message, metadata)
        title = pageContent["title"] as String
        properties = pageContent["properties"] as JsonObject
        content = pageContent["content"] as JsonArray
    }

    fun toJson() = json {
        "metadata" to metadata
        "title" to title
        "properties" to properties
        "content" to content
    }

    fun component1() = metadata
    fun component2() = title
    fun component3() = properties
    fun component4() = content
}