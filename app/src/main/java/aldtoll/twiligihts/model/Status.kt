package aldtoll.twiligihts.model

data class Status(
    val name: String,
    val description: String? = null,
    var value: Int,
    val type: EffectType,
    /**
     * -1 будет означать бесконечность
     */
    var duration: Int = 1
) {
    @Suppress("unused")
    constructor() : this("", null, 0, EffectType.DODGE, 1)

    fun isActive(): Boolean {
        if (this.duration == -1) {
            return this.value > 0
        }
        return this.duration > 0
    }

    fun isInfinity(): Boolean = duration == -1

    enum class EffectType {
        DODGE,
        WEAK,
        STRONG,
        VULNERABLE,
        ARMOR,
        COUNTERATTACK
    }

}
