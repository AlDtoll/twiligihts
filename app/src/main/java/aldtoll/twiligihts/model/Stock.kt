package aldtoll.twiligihts.model

data class Stock(
    var value: Int,
    val gemType: Int,
    val maxValue: Int? = null,
) {
    @Suppress("unused")
    constructor() : this(0, 0)

    fun increaseStock(addingValue: Int) {
        if (this.maxValue != null) {
            if (this.value + addingValue > this.maxValue) {
                this.value = this.maxValue
            } else {
                this.value = this.value + addingValue
            }
        } else {
            this.value = this.value + addingValue
        }
    }
}