package aldtoll.twiligihts.model

/**
 * сектор
 * может обозначать как отряд,
 * так и область противника,
 * так и просто служить переключателем,
 */
data class Sector(
    val id: Int,
    val name: String = "",
    val iconRes: Int,
    val backgroundRes: Int = -1,
    val perk: Perk
)