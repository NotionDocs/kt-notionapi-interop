import exceptions.NotionAPIException
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.*
import kotlinx.serialization.serializer
import models.page.PageData
import schemas.filter.Filter
import util.dashifyId
import util.json
import java.lang.Integer.min

class NotionAPI(private val token: String) {
    private val client: HttpClient = HttpClient()

    private suspend fun makeRequest(path: String, data: JsonObject): JsonElement {
        val message = client.post<String>("https://www.notion.so/api/v3/$path") {
            contentType(ContentType.Application.Json)
            header("Cookie", "token_v2=$token")
            body = json.stringify(JsonObject::class.serializer(), data)
        }
        return json.parseJson(message)
    }

    suspend fun fetchPage(pageId: String): JsonObject {
        val message: JsonObject
        try {
            message = makeRequest(
                "loadPageChunk", json {
                    "pageId" to dashifyId(pageId)
                    "limit" to Int.MAX_VALUE
                    "chunkNumber" to 0
                    "verticalColumns" to false
                }
            ) as JsonObject
        } catch (e: Throwable) {
            when (e) {
                is io.ktor.client.features.ClientRequestException -> throw NotionAPIException(
                    RequestStage.LOAD_PAGE_CHUNK, e
                )
                else -> throw e
            }
        }

        val page = PageData(message)

        return page.toJson()
    }

    suspend fun fetchCollection(
        collectionId: String,
        collectionViewId: String,
        filter: Filter? = null,
        cursor: String? = null,
        limit: Int? = null
    ): JsonObject {
        val key: String?
        val pageChunk: JsonObject
        try {
            pageChunk = makeRequest(
                "loadPageChunk", json {
                    "pageId" to dashifyId(collectionId)
                    "limit" to Int.MAX_VALUE
                    "chunkNumber" to 0
                    "verticalColumns" to false
                }
            ) as JsonObject
            key = pageChunk["recordMap"]?.jsonObject?.get("collection")?.jsonObject?.entries?.first()?.key
            if (key == null) throw Exception()
        } catch (e: Throwable) {
            when (e) {
                is io.ktor.client.features.ClientRequestException -> throw NotionAPIException(
                    RequestStage.LOAD_PAGE_CHUNK, e
                )
                else -> throw e
            }
        }

        val metadata = PageData.getMetadata(pageChunk)

        var filterObject: JsonObject? = null
        if (filter != null) {

            val property = metadata["properties"]?.jsonObject?.entries?.find {
                it.value.jsonObject["name"]?.content == filter.property
            }?.key

            filterObject = json {
                "property" to property
                "filter" to json {
                    "operator" to filter.operator
                    "value" to json {
                        "type" to "exact"
                        "value" to filter.value
                    }
                }
            }
        }

        val message: JsonObject
        try {
            message = makeRequest(
                "queryCollection", json {
                    "collectionId" to dashifyId(key)
                    "collectionViewId" to dashifyId(collectionViewId)
                    "loader" to json {
                        "type" to "table"
                        "limit" to Int.MAX_VALUE
                        "loadContentCover" to true
                    }
                    if (filterObject != null) {
                        "query" to json {
                            "filter" to json {
                                "operator" to "and"
                                "filters" to jsonArray {
                                    +filterObject
                                }
                            }
                        }
                    }
                }
            ) as JsonObject
        } catch (e: Throwable) {
            when (e) {
                is io.ktor.client.features.ClientRequestException -> throw NotionAPIException(
                    RequestStage.QUERY_COLLECTION, e
                )
                else -> throw e
            }
        }
        val blockIds = message["result"]?.jsonObject?.get("blockIds")?.jsonArray?.map {
            it.content
        } ?: listOf()


        val pages = mutableListOf<JsonObject>()
        blockIds.subList(
            if (cursor == null) 0 else {
                if (blockIds.indexOf(cursor) < 0) blockIds.size else blockIds.indexOf(cursor)
            }, blockIds.size
        ).let {
            it.subList(0, min(it.size, limit ?: Int.MAX_VALUE)).forEach {
                pages += fetchPage(it)
            }
        }

        return json {
            "schema" to metadata["properties"]?.jsonObject as JsonObject
            "pages" to jsonArray {
                pages.forEach {
                    +it
                }
            }
        }
    }
}