package aldtoll.twiligihts.model

data class Stock(
    var value: Int,
    val gemType: Int,
    var maxValue: Int = 100,
) {
    @Suppress("unused")
    constructor() : this(0, 0, 0)
}