package aldtoll.twiligihts.model

data class BattleSettings(
    val clearStocksAfterDamage: Boolean = false,
    val types: Int = 5,
) {
    @Suppress("unused")
    constructor() : this(false, 5)
}
