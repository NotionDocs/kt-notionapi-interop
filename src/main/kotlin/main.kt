import exceptions.NotionAPIException
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveText
import io.ktor.response.respondText
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonLiteral
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.content
import schemas.inputs.Filter
import schemas.inputs.Sort
import util.get
import util.json

suspend fun main() {
    embeddedServer(
        Netty, port = 8081,
        module = Application::module,
        watchPaths = listOf("NotionAPI")
    ).apply {
        start(wait = true)
    }
}

fun Application.module() {
    routing {
        post("/fetchPage") {
            val req = json.parseJson(call.receiveText())

            if (!req.contains("pageId") || !call.request.headers.contains("Token"))
                return@post call.response.status(HttpStatusCode.BadRequest)

            val pageId = (req["pageId"] as JsonLiteral).content
            val token = call.request.headers["Token"] as String
            val api = NotionAPI(token)
            val message: JsonObject
            try {
                message = api.fetchPage(pageId)
            } catch (e: Throwable) {
                return@post call.response.status(
                    when (e) {
                        is NotionAPIException -> HttpStatusCode.ServiceUnavailable
                        else -> HttpStatusCode.InternalServerError
                    }
                )
            }
            call.respondText(message.toString(), ContentType.Application.Json)
        }

        post("/fetchCollection") {
            println("heyyy")
            val req = json.parseJson(call.receiveText())

            if (!req.contains("collectionId") || !req.contains("collectionViewId") || !call.request.headers.contains("Token")) {
                return@post call.response.status(HttpStatusCode.BadRequest)
            }

            val collectionId = (req["collectionId"] as JsonLiteral).content
            val collectionViewId = (req["collectionViewId"] as JsonLiteral).content
            val token = call.request.headers["Token"] as String
            val api = NotionAPI(token)

            val filters: MutableList<Filter> = mutableListOf()
            if (req.contains("filters")) {
                val filterArray = (req["filters"] as JsonArray).content
                filterArray.forEach {
                    it as JsonObject
                    if (!it.containsKey("property") || !it.containsKey("operator"))
                        return@post call.response.status(HttpStatusCode.BadRequest)
                    filters += object : Filter {
                        override val property = (it["property"] as JsonLiteral).content
                        override val operator = (it["operator"] as JsonLiteral).content
                        override val value = (it["value"] as JsonLiteral?)?.content
                    }
                }
            }

            val sort: Sort? = if (req.contains("sort")) {
                val sortReq = (req["sort"] as JsonObject).content
                if (!sortReq.containsKey("property") || !sortReq.containsKey("value"))
                    return@post call.response.status(HttpStatusCode.BadRequest)
                object : Sort {
                    override val property = sortReq["property"]!!.content
                    override val value = sortReq["value"]!!.content
                }
            } else null

            val message: JsonObject
            try {
                message = api.fetchCollection(
                    collectionId,
                    collectionViewId,
                    filters,
                    sort,
                    (req["cursor"] as JsonLiteral?)?.content,
                    (req["limit"] as JsonLiteral?)?.content?.toInt()
                )
            } catch (e: Throwable) {
                return@post call.response.status(
                    when (e) {
                        is NotionAPIException -> {
                            println(e)
                            HttpStatusCode.ServiceUnavailable
                        }
                        else -> HttpStatusCode.InternalServerError
                    }
                )
            }
            call.respondText(message.toString(), ContentType.Application.Json)
        }
    }
}
