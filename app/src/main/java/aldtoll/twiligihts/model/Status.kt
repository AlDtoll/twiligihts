package aldtoll.twiligihts.model

data class Status(
    val name: String,
    var value: Int,
    val type: EffectType
) {
    @Suppress("unused")
    constructor() : this("", 0, EffectType.DODGE)

    fun isActive(): Boolean = this.value > 0

    enum class EffectType {
        DODGE,
    }

}
