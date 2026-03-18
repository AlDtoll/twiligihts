package aldtoll.twiligihts.model

/**
 * Навык по ресурсу (StockPerk): автоматически срабатывает, когда значение ресурса (Stock)
 * конкретного типа пересекает порог [threshold] снизу вверх.
 */
data class StockPerk(
    val id: Int = 0,
    val gemType: Int = 0,
    val threshold: Int = 0,
    val perk: Perk = Perk(),
) {
    // Пустой конструктор для Firebase
    @Suppress("unused")
    constructor() : this(0, 0, 0, Perk())
}

