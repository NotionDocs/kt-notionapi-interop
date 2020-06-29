package models.block.content

import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonLiteral
import kotlinx.serialization.json.json
import kotlinx.serialization.serializer
import util.get
import util.json

open class TextItemsBlockContent(children: JsonArray) : BlockContent {
    companion object {
        fun parseText(children: List<JsonElement>): MutableList<TextBlockItem> {
            val items = mutableListOf<TextBlockItem>()
            children.forEach {
                it as JsonArray

                val item: TextBlockItem

                var bold = false
                var italic = false
                var underline = false
                var strikethrough = false
                var link: String? = null
                var textColor: String? = null
                var bgColor: String? = null

                if (it[0].primitive.content == "⁍") {
                    item = TextBlockItem(
                        bold = false,
                        italic = false,
                        underline = false,
                        strikethrough = false,
                        link = null,
                        textColor = null,
                        bgColor = null,
                        isEquation = true,
                        text = (it[1][0][1] as JsonLiteral).content
                    )
                } else {
                    if (it.size > 1)
                        it[1].jsonArray.forEach {
                            it as JsonArray
                            when (it[0].primitive.content) {
                                "b" -> bold = true
                                "i" -> italic = true
                                "_" -> underline = true
                                "s" -> strikethrough = true
                                "a" -> link = (it[1] as JsonLiteral).content
                                "h" -> {
                                    val color = (it[1] as JsonLiteral).content.split("_background")
                                    if (color.size == 1) textColor = color[0]
                                    else bgColor = color[0]
                                }
                            }
                        }
                    item = TextBlockItem(
                        bold,
                        italic,
                        underline,
                        strikethrough,
                        link,
                        textColor,
                        bgColor,
                        false,
                        it[0].primitive.content
                    )
                }

                items += item
            }
            return items
        }
    }

    override val items = parseText(children)

    override fun toJson() = json {
        "type" to "TextItemsBlockContent"
        "items" to json.toJson(TextBlockItem::class.serializer().list, items)
    }
}