package aldtoll.twiligihts.model

import com.google.firebase.database.Exclude

/**
 * сектор
 * может обозначать как отряд,
 * так и область противника,
 * так и просто служить переключателем,
 */
data class Sector(
    val id: Int,
    val name: String = "",
    @get:Exclude
    val iconRes: Int,
    @get:Exclude
    val backgroundRes: Int = -1,
    val iconResName: String? = null,
    val backgroundResName: String? = null,
    val perk: Perk
)