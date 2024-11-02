package aldtoll.twiligihts.model

data class BattleResult(
    val finished: Boolean = false,
    val heroHp: Int = 0,
    val enemyHp: Int = 0,
    val turn: Int = 0,
    var attempt: Int = 0,
    val heroStocks: ArrayList<Stock> = arrayListOf(),
    val started: Boolean = false
) {

    @Suppress("unused")
    constructor() : this(false)
}