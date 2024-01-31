package aldtoll.twiligihts.model

data class BattleSettings(
    val clearStocksAfterDamage: Boolean = false,
    val types: Int = 5,
    val iconNames: ArrayList<IconName> = arrayListOf()
) {
    @Suppress("unused")
    constructor() : this(false, 5)

    data class IconName(
        val type: String = "",
        val name: String = ""
    )
}
