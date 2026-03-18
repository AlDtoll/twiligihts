package aldtoll.twiligihts.model

/**
 * Таймерный навык (TimePerk): на конкретной секунде автоматически срабатывает указанный перк.
 */
data class TimePerk(
    val id: Int = 0,
    val time: Int = 0,
    val perk: Perk = Perk(),
) {
    // Пустой конструктор для Firebase
    @Suppress("unused")
    constructor() : this(0, 0, Perk())
}

