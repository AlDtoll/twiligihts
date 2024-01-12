package aldtoll.twiligihts.model

data class Hand(
    val gemType: Int,
    val perks: ArrayList<Perk>
) {

    data class Perk(
        val prices: ArrayList<Price>,
        val effects: ArrayList<Effect>,
        val description: String?
    ) {
        data class Price(
            val value: Int,
            val gemType: Int
        )

        data class Effect(
            val value: Int,
            val effect: EffectType
        ) {
            enum class EffectType {
                ATTACK,
                DEFEND,
                DODGE
            }
        }
    }
}
