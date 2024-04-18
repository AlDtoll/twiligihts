package aldtoll.twiligihts.model

data class Resource(
    var amount: Int = 0,
    val name: String = ""
) {
    fun decreaseValue(amount: Int) {
        if (amount > this.amount) {
            this.amount = 0
        } else {
            this.amount = this.amount - amount
        }
    }

    @Suppress("unused")
    constructor() : this(0, "")
}