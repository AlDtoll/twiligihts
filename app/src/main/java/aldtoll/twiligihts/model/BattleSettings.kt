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
        val fullValue: Int = 10,
        val halfProbability: Int = 20,
        val bonusValue: Int = 2,
        /**
         * вероятность того, что данный гем имеет бонус. Не является им
         */
        val bonusProbability: Int = 10,
    )
}
