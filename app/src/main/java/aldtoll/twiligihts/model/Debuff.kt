package aldtoll.twiligihts.model

data class Debuff(
    val condition: Condition,
    val status: Status
) {
    // Add a no-argument constructor
    @Suppress("unused")
    constructor() : this(Condition(), Status())
}