package aldtoll.twiligihts.model

/**
 * Состояние ячейки под гемом.
 */
data class CellState(
    val row: Int,
    val col: Int,
    val scoreMultiplier: Float = 1f,
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

