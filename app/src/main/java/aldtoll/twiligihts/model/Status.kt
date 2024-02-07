package aldtoll.twiligihts.model

data class Status(
    val name: String,
    val description: String? = null,
    var value: Int,
    val type: EffectType,
    /**
     * -1 будет означать бесконечность [INFINITY]
     */
    var duration: Int = 1
) {
    @Suppress("unused")
    constructor() : this("", null, 0, EffectType.DODGE, 1)

    fun isActive(): Boolean {
        if (this.duration == INFINITY) {
            return this.value > 0
        }
        return this.duration > 0 && this.value > 0
    }

    fun isInfinity(): Boolean = duration == INFINITY

    enum class EffectType {
        DODGE,
        WEAK,
        STRONG,
        VULNERABLE,
        ARMOR,
        COUNTERATTACK,

        /**
         * маркерный статус
         * для выполнения условия [Condition.Parameter.STATUS]
         * должен быть только 1 на бой
         */
        INFO
    }

    companion object {
        private const val INFINITY = -1
    }

}
