package aldtoll.twiligihts.model

data class BattleSettings(
    val clearStocksAfterDamage: Boolean = false,
    val types: Int = 5,
    val iconNames: ArrayList<IconName> = arrayListOf(),
    val fullValue: Int = 10,
    val halfProbability: Int = 10,
    val bonusValue: Int = 2,
    val bonusProbability: Int = 10,
) {
    @Suppress("unused")
    constructor() : this(false, 5)

    data class IconName(
        val type: String = "",
        val name: String = ""
    )
}
