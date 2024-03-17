package aldtoll.twiligihts.model

data class BattleSettings(
    val clearStocksAfterDamage: Boolean = false,
    val types: Int = 4,
    val gemSettings: ArrayList<GemSettings> = arrayListOf(),
    val bonusType: Int? = null
) {
    @Suppress("unused")
    constructor() : this(false, 5)

    data class GemSettings(
        val type: String = "",
        val name: String = "",
        var uri: String = "",
        val fullValue: Int = Gem.GEM_FULL_VALUE,
        val halfProbability: Int = Gem.GEM_HALF_PROBABILITY,
        /**
         * если этот цвет используется для бонуса - сколько очков он дает
         */
        val bonusValue: Int = Gem.GEM_BONUS_VALUE,
        /**
         * вероятность того, что данный гем имеет бонус. Не является им
         */
        val bonusProbability: Int = Gem.GEM_BONUS_PROBABILITY,
    )
}
