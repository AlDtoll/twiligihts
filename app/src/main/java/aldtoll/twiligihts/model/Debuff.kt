package aldtoll.twiligihts.model

data class Debuff(
    val value: Int,
    val status: Status
) {
    // Add a no-argument constructor
    @Suppress("unused")
    constructor() : this(0, Status())
}