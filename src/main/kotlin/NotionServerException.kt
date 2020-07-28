data class NotionServerException(
    val stage: NotionServerRequestStage,
    override val cause: io.ktor.client.features.ClientRequestException
) :
    Exception("Encountered an error at the following stage: $stage", cause)