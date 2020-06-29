package exceptions

import RequestStage

data class NotionAPIException(
    val stage: RequestStage,
    override val cause: io.ktor.client.features.ClientRequestException
) :
    Exception("Encountered an error at the following stage: $stage", cause)