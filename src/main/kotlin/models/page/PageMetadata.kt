package models.page

import schemas.collection.CollectionSchemaProperty

interface PageMetadata {
    val properties: Map<String, CollectionSchemaProperty>
    val createdTime: Long?
    val lastEditedTime: Long?
}