package schemas.inputs

interface Filter {
    val property: String
    val operator: String
    val value: String?
}