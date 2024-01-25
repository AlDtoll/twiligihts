package aldtoll.twiligihts.model

data class Perk(
    val name: String,
    val prices: ArrayList<Price> = arrayListOf(),
    val effects: ArrayList<Effect>,
    val description: String? = effects.toString(),
    var enable: Boolean = false
) {

    constructor() : this("", arrayListOf(), arrayListOf())

    data class Price(
        val value: Int,
        val gemType: Int
    ) {
        @Suppress("unused")
        constructor() : this(0, 0)
    }

    data class Effect(
        val value: Int,
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
}