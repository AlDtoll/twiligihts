package aldtoll.twiligihts.model

data class Condition(
    val value: Int = 0,
    val target: Effect.EffectTarget = Effect.EffectTarget.HERO,
    val parameter: Parameter = Parameter.HP,
    /**
     * используется только с [Parameter.STATUS]
     * todo сделать sealed class
     */
    val statusName: String? = null,
    val symbol: Symbol = Symbol.LESS
) {

    @Suppress("unused")
    constructor() : this(0)

    enum class Parameter {
        HP,

        /**
         *
         */
        HP_P,
        SP,
        STATUS,

        /**
         * количество ходов от начала боя
         */
        TURN,
    }

    enum class Symbol {
        MORE,
        LESS,
        EQUALS,
        HAVE,
        //todo добавить пустоту
    }
}