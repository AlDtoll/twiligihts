package aldtoll.twiligihts.model

data class BattleSettings(
    val clearStocksAfterDamage: Boolean = false,
    val types: Int = 5,
    val iconNames: ArrayList<String> = arrayListOf()
) {
    @Suppress("unused")
    constructor() : this(false, 5)
}
