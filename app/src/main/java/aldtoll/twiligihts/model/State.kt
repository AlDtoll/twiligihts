package aldtoll.twiligihts.model

data class State(
    //todo добавить description
    @Deprecated("use conditions")
    val condition: Condition,
    val conditions: ArrayList<Condition> = arrayListOf(),
    //todo подумать о том, чтобы заменить на эффект - но учесть сколько раз вызывается
    val status: Status,
    val name: String? = null,
) {
    // Add a no-argument constructor
    @Suppress("unused")
    constructor() : this(Condition(), arrayListOf(), Status())
}