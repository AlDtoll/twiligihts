package aldtoll.twiligihts.model

/**
 * Правило для совпадений 3-в-ряд: фильтры по ориентации, цвету и размеру,
 * и перк, который нужно применить при выполнении условия.
 */
data class MatchRule(
    val name: String,
    val orientation: MatchOrientation? = null,
    val gemType: Int? = null,
    val minSize: Int = 3,
    val perk: Perk
) {
    @Suppress("unused")
    constructor() : this("", perk = Perk())
}


