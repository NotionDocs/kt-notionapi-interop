import exceptions.NotionAPIException
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.*
import kotlinx.serialization.serializer
import models.page.PageData
import schemas.inputs.Filter
import schemas.inputs.Sort
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

        return json {
            "id" to pageId
            page.toJson().content.entries.forEach {
                it.key to it.value
            }
        }
    }

    suspend fun fetchCollection(
        collectionId: String,
        collectionViewId: String,
        filters: List<Filter>?,
        sort: Sort?,
        cursor: String?,
        limit: Int?
    ): JsonObject {
        println(json {
            "pageId" to dashifyId(collectionId)
            "limit" to Int.MAX_VALUE
            "chunkNumber" to 0
            "verticalColumns" to false
        })
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

        println("gotcha")

        val metadata = PageData.getMetadata(pageChunk)

        val filtersList: MutableList<JsonObject> = mutableListOf()
        filters?.forEach { filter ->
            val property = metadata["properties"]?.jsonObject?.entries?.find {
                it.value.jsonObject["name"]?.content == filter.property
            }?.key

            filtersList += json {
                "property" to property
                "filter" to json {
                    "operator" to filter.operator
                    "value" to json {
                        "type" to "exact"
                        when (filter.value) {
                            "true", "false" -> filter.value?.toBoolean()?.let { "value" to JsonLiteral(it) }
                            else -> filter.value?.let {
                                "value" to JsonLiteral(
                                    if (filter.operator == "relation_contains") dashifyId(it)
                                    else it
                                )
                            }
                        }
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
                    "query" to json {
                        if (filtersList.size > 0) {
                            "filter" to json {
                                "operator" to "and"
                                "filters" to jsonArray {
                                    filtersList.forEach { +it }
                                }
                            }
                        }
                        if (sort != null) {
                            println("hehe sort")
                            "sort" to json {
                                "property" to sort.property
                                "value" to sort.value
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