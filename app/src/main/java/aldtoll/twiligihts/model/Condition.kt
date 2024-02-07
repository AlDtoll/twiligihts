package aldtoll.twiligihts.model

data class Condition(
    val value: Int = 0,
    val target: Effect.EffectTarget = Effect.EffectTarget.HERO,
    val parameter: Parameter = Parameter.HP,
    val symbol: Symbol = Symbol.LESS
) {

    @Suppress("unused")
    constructor() : this(0)

    enum class Parameter {
        HP,
        SP,
        STATUS,
    }

    enum class Symbol {
        MORE,
        LESS
    }
}