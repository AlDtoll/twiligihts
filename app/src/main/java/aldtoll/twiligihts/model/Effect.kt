package aldtoll.twiligihts.model

data class Effect(
    var value: Int,
    val type: EffectType,
    val target: EffectTarget,
    val status: Status? = null
) {

    @Suppress("unused")
    constructor() : this(0, EffectType.ATTACK, EffectTarget.ENEMY, null)

    enum class EffectType {
        ATTACK,
        ATTACK_HP,
        ATTACK_SP,
        DEFEND,
        ADD_STATUS
    }

    enum class EffectTarget {
        ENEMY,
        HERO,
        ALL
    }
}