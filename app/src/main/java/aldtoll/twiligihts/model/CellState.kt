package aldtoll.twiligihts.model

enum class CellType {
    NONE,
    MULTIPLIER,
    ADDITIVE,
    TRIGGER,
}

/**
 * Состояние ячейки под гемом.
 */
data class CellState(
    val row: Int,
    val col: Int,
    val cellType: CellType = CellType.NONE,
    val modifierValue: Float = 0f,
    val triggerPerk: Perk? = null,
)

/**
 * Данные о разрушенной ячейке: какой гем стоял и где.
 */
data class CrushedCell(
    val gem: Gem,
    val row: Int,
    val col: Int,
    val cell: CellState,
)

