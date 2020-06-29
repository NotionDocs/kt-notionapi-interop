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
import kotlinx.serialization.json.JsonLiteral
import kotlinx.serialization.json.JsonObject
import schemas.filter.Filter
import util.get
import util.json

suspend fun main() {

    val server = embeddedServer(
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
                throw (e)
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
            val req = json.parseJson(call.receiveText())

            if (!req.contains("collectionId") || !req.contains("collectionViewId") || !call.request.headers.contains("Token"))
                return@post call.response.status(HttpStatusCode.BadRequest)

            val collectionId = (req["collectionId"] as JsonLiteral).content
            val collectionViewId = (req["collectionViewId"] as JsonLiteral).content
            val token = call.request.headers["Token"] as String
            val api = NotionAPI(token)
            var filter: Filter? = null

            if (req.contains("filter")) {
                val filterReq = (req["filter"] as JsonObject).content
                if (!filterReq.containsKey("property") || !filterReq.containsKey("operator"))
                    return@post call.response.status(HttpStatusCode.BadRequest)
                filter = object : Filter {
                    override val property = (filterReq["property"] as JsonLiteral).content
                    override val operator = (filterReq["operator"] as JsonLiteral).content
                    override val value = (filterReq["value"] as JsonLiteral?)?.content
                }
            }

            val message: JsonObject
            try {
                message = api.fetchCollection(collectionId, collectionViewId, filter)
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
    }
}
