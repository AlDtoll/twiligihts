package aldtoll.twiligihts.model

/**
 * Информация о группе совпадения за один шаг: ориентация и размер.
 */
data class MatchGroupInfo(
    val gemType: Int,
    val orientation: MatchOrientation,
    val size: Int
)

enum class MatchOrientation {
    HORIZONTAL,
    VERTICAL,
    T_SHAPE,
    L_SHAPE,
    OTHER
}


