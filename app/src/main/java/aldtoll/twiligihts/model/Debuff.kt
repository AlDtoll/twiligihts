package aldtoll.twiligihts.model

data class Debuff(
    val condition: Condition,
    //todo подумать о том, чтобы заменить на эффект - но учесть сколько раз вызывается
    val status: Status
) {
    // Add a no-argument constructor
    @Suppress("unused")
    constructor() : this(Condition(), Status())
}