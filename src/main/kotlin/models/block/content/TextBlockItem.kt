package models.block.content

import kotlinx.serialization.Serializable

@Serializable
data class TextBlockItem(
    val bold: Boolean,
    val italic: Boolean,
    val underline: Boolean,
    val strikethrough: Boolean,
    val link: String?,
    val textColor: String?,
    val bgColor: String?,
    val isEquation: Boolean,
    val text: String
) {}