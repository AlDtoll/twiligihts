package aldtoll.twiligihts.model

data class Perk(
    val prices: ArrayList<Price> = arrayListOf(),
    val effects: ArrayList<Effect>,
    val description: String? = effects.toString(),
    var enable: Boolean = false
) {
    data class Price(
        val value: Int,
        val gemType: Int
    )

    data class Effect(
        val value: Int,
        val effectType: EffectType,
        val target: EffectTarget
    ) {
        enum class EffectType {
            ATTACK,
            DEFEND,
            DODGE
        }

        enum class EffectTarget {
            ENEMY,
            HERO,
            ALL
        }
    }
}