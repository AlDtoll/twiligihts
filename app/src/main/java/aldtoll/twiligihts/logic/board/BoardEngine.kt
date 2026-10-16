package aldtoll.twiligihts.logic.board

import aldtoll.twiligihts.model.CrushedCell
import aldtoll.twiligihts.model.GameBoard
import aldtoll.twiligihts.model.Gem
import aldtoll.twiligihts.model.MatchGroupInfo
import aldtoll.twiligihts.model.MatchOrientation
import kotlin.random.Random

/**
 * Перемещение гема при гравитации: из клетки [from] в клетку [to].
 */
data class GemMove(val from: Pair<Int, Int>, val to: Pair<Int, Int>)

/**
 * Новый гем, появившийся в верхней строке при генерации.
 */
data class NewGem(val position: Pair<Int, Int>, val gem: Gem)

/**
 * Результат одного шага "удаление совпадений".
 *
 * @param positions позиции в том виде, как их вернул поиск: БЕЗ дедупликации
 * (клетка, входящая в две линии, встречается дважды) - адаптер анимирует каждую запись
 * @param crushedCells по одной записи на каждую запись из [positions]; повторная запись
 * содержит уже пустой гем (Gem(0)) - так было в адаптере
 * @param groups группы совпадений (считаются до обнуления клеток)
 * @param removedByType сколько записей удалено по каждому type (в т.ч. 0 для повторов)
 */
data class MatchRemoval(
    val positions: List<Pair<Int, Int>>,
    val crushedCells: List<CrushedCell>,
    val groups: List<MatchGroupInfo>,
    val removedByType: Map<Int, Int>,
)

/**
 * Что делать дальше в каскаде (результат [BoardEngine.nextStep]).
 */
sealed class CascadeStep {

    /**
     * Каскад закончен: движение остановилось, ход можно отдать игроку.
     *
     * @param hasPossibleMoves результат GameBoard.checkPossibleMoves() в момент остановки
     * @param finishBattleIfNoMatches true, если генерация выключена (режим "без генерации")
     */
    data class Stop(
        val hasPossibleMoves: Boolean,
        val finishBattleIfNoMatches: Boolean,
    ) : CascadeStep()

    /**
     * Совпадения удалены с доски (клетки уже Gem(0)). Дальше нужно вызвать [BoardEngine.applyGravity].
     */
    data class Remove(val removal: MatchRemoval) : CascadeStep()

    /**
     * В верхней строке появились новые гемы (могут быть пустым списком, если сверху нет дыр).
     * Дальше нужно вызвать [BoardEngine.applyGravity].
     */
    data class Generate(val newGems: List<NewGem>) : CascadeStep()

    /**
     * Каскад завис: есть пустые клетки, а генерация запрещена, в режиме "падение и генерация".
     * В штатной работе недостижимо (stopGenerate не меняется после создания), но в старом
     * коде в этой точке просто ничего не происходило - сохранено.
     */
    object Stalled : CascadeStep()
}

/**
 * Игровая логика доски match-3 без Android и без времени: поиск совпадений и групп,
 * удаление, гравитация, генерация, пошаговый каскад.
 * Перенесено механически из GameBoardAdapter, поведение 1-в-1 (включая странности).
 *
 * Каскад: адаптер вызывает [nextStep], анимирует результат, ждёт, затем для Remove/Generate
 * вызывает [applyGravity] (анимирует перемещения), ждёт и снова вызывает [nextStep].
 *
 * @param stopGenerate режим "без генерации" (новые гемы не появляются)
 * @param random источник случайности для новых гемов (в тестах - с фиксированным seed)
 */
class BoardEngine(
    private val gameBoard: GameBoard,
    private val stopGenerate: Boolean = false,
    private val random: Random = Random.Default,
) {

    /**
     * идёт фаза "падение + генерация": пока на доске есть пустые клетки, совпадения не удаляются
     */
    private var nowStartGenerateAndDrop = false

    fun hasMatches(): Boolean = gameBoard.hasMatches()

    /**
     * обмен двух гемов на доске (без проверки соседства - она в [areAdjacent])
     */
    fun swap(position1: Pair<Int, Int>, position2: Pair<Int, Int>) {
        val temp = gameBoard[position1]
        gameBoard[position1] = gameBoard[position2]
        gameBoard[position2] = temp
    }

    /**
     * Один шаг каскада. Бывший handleMatches: решает, что делать - остановиться,
     * удалить совпадения или сгенерировать гемы. Внутренние переходы без действия
     * (смена фазы) выполняются сразу, как раньше рекурсией.
     */
    fun nextStep(): CascadeStep {
        while (true) {
            /**
             * находятся совпадения, т.е. 3 в ряд минимум
             */
            val matchedPositions = findMatches()

            /**
             * так же нужно проверить есть ли пустые ячейки
             */
            val hasEmpty = gameBoard.hasEmptyGems()

            /**
             * может быть включен режим "без генерации", тогда новых гемов появляться не будет
             */
            val allowGenerateNewGems = !stopGenerate

            /**
             * если разрешена генерация - стоп, когда нет совпадений и пустых ячеек
             * если нет - когда нет совпадений
             */
            val needStopHandle = if (allowGenerateNewGems) {
                matchedPositions.isEmpty() && !hasEmpty
            } else {
                matchedPositions.isEmpty()
            }
            if (needStopHandle) {
                return CascadeStep.Stop(
                    hasPossibleMoves = gameBoard.checkPossibleMoves(),
                    finishBattleIfNoMatches = !allowGenerateNewGems,
                )
            }

            if (nowStartGenerateAndDrop) {
                if (!hasEmpty) {
                    // падение и генерация закончены, смотрим новые совпадения
                    nowStartGenerateAndDrop = false
                    continue
                }
                return if (allowGenerateNewGems) {
                    CascadeStep.Generate(generateNewGems())
                } else {
                    CascadeStep.Stalled
                }
            }

            if (matchedPositions.isNotEmpty()) {
                return CascadeStep.Remove(removeMatches(matchedPositions))
            }
            // совпадений нет, но есть пустые клетки: начинается падение и генерация
            if (hasEmpty) {
                nowStartGenerateAndDrop = true
            }
            // handleMatches() рекурсивно
        }
    }

    /**
     * Верхняя строка: для каждой пустой клетки генерируется новый гем.
     */
    fun generateNewGems(): List<NewGem> {
        val result = mutableListOf<NewGem>()
        for (col in 0 until gameBoard.columnSize) {
            if (gameBoard[0, col] == Gem(0)) {
                val newGem = Gem.generateNewGem(random)
                gameBoard[0, col] = newGem
                result.add(NewGem(Pair(0, col), newGem))
            }
        }
        return result
    }

    /**
     * Удаляет гемы по позициям (список может содержать повторы - см. [MatchRemoval]).
     */
    fun removeMatches(matchedPositions: List<Pair<Int, Int>>): MatchRemoval {
        val crushedCells = mutableListOf<CrushedCell>()
        val removedGemsCount = mutableMapOf<Int, Int>()
        val uniquePositions = matchedPositions.toMutableSet()
        val groups = computeMatchGroups(uniquePositions)

        for (position in matchedPositions) {
            val gem = gameBoard[position]
            val cell = gameBoard.getCell(position)
            crushedCells.add(
                CrushedCell(
                    gem = gem,
                    row = position.first,
                    col = position.second,
                    cell = cell,
                )
            )
            val removedGemColor = gem.type
            removedGemsCount[removedGemColor] = (removedGemsCount[removedGemColor] ?: 0) + 1
            gameBoard[position] = Gem(0)
        }
        return MatchRemoval(
            positions = matchedPositions.toList(),
            crushedCells = crushedCells,
            groups = groups,
            removedByType = removedGemsCount,
        )
    }

    /**
     * Все клетки, входящие в линии из 3+ гемов с общим цветом. Горизонтали, затем вертикали.
     * Клетка, входящая в несколько троек, встречается в списке несколько раз.
     */
    fun findMatches(): MutableList<Pair<Int, Int>> {
        val matchedPositions = mutableListOf<Pair<Int, Int>>()

        // Check for horizontal matches
        for (row in 0 until gameBoard.rowSize) {
            for (col in 0 until gameBoard.columnSize - 2) {
                val gem = gameBoard[row, col]
                if (gem.type != 0 &&
                    GameBoard.isLine(gem, gameBoard[row, col + 1], gameBoard[row, col + 2])
                ) {
                    // Add matched items to the list
                    matchedPositions.add(Pair(row, col))
                    matchedPositions.add(Pair(row, col + 1))
                    matchedPositions.add(Pair(row, col + 2))
                }
            }
        }

        // Check for vertical matches
        for (row in 0 until gameBoard.rowSize - 2) {
            for (col in 0 until gameBoard.columnSize) {
                val gem = gameBoard[row, col]
                if (gem.type != 0 &&
                    GameBoard.isLine(gem, gameBoard[row + 1, col], gameBoard[row + 2, col])
                ) {
                    matchedPositions.add(Pair(row, col))
                    matchedPositions.add(Pair(row + 1, col))
                    matchedPositions.add(Pair(row + 2, col))
                }
            }
        }
        return matchedPositions
    }

    fun computeMatchGroups(matched: Set<Pair<Int, Int>>): List<MatchGroupInfo> {
        val visited = mutableSetOf<Pair<Int, Int>>()
        val result = mutableListOf<MatchGroupInfo>()
        for (pos in matched) {
            if (visited.contains(pos)) continue
            val type = gameBoard[pos].type
            // BFS to get connected component by 4-neighborhood of same type within matched set
            val queue: ArrayDeque<Pair<Int, Int>> = ArrayDeque()
            queue.add(pos)
            visited.add(pos)
            val component = mutableListOf<Pair<Int, Int>>()
            while (queue.isNotEmpty()) {
                val cur = queue.removeFirst()
                component.add(cur)
                val (r, c) = cur
                val neighbors =
                    listOf(Pair(r - 1, c), Pair(r + 1, c), Pair(r, c - 1), Pair(r, c + 1))
                for (n in neighbors) {
                    if (!matched.contains(n) || visited.contains(n)) continue
                    if (n.first !in 0 until gameBoard.rowSize || n.second !in 0 until gameBoard.columnSize) continue
                    if (gameBoard[n].type != type) continue
                    visited.add(n)
                    queue.add(n)
                }
            }

            // Determine orientation
            val rows = component.groupBy { it.first }
            val cols = component.groupBy { it.second }
            val maxRowLen = rows.values.maxOfOrNull { it.size } ?: 0
            val maxColLen = cols.values.maxOfOrNull { it.size } ?: 0
            val orientation = when {
                rows.size == 1 && component.size >= 3 -> MatchOrientation.HORIZONTAL
                cols.size == 1 && component.size >= 3 -> MatchOrientation.VERTICAL
                maxRowLen >= 3 && maxColLen >= 3 -> MatchOrientation.T_SHAPE
                (maxRowLen >= 2 && maxColLen >= 2) -> MatchOrientation.L_SHAPE
                else -> MatchOrientation.OTHER
            }
            result.add(
                MatchGroupInfo(
                    gemType = type,
                    orientation = orientation,
                    size = component.size
                )
            )
        }
        return result
    }

    /**
     * Гравитация: колонки справа налево, строки снизу вверх; в пустую клетку падает
     * ближайший гем сверху. Доска меняется сразу, возвращается список перемещений
     * в порядке выполнения.
     */
    fun applyGravity(): List<GemMove> {
        val moves = mutableListOf<GemMove>()
        for (col in (0 until gameBoard.columnSize).reversed()) {
            for (row in (0 until gameBoard.rowSize).reversed()) {
                // If the current cell is empty, find the nearest non-empty cell above it
                if (gameBoard[row, col] == Gem(0)) {
                    var aboveRow = row - 1
                    while (aboveRow >= 0 && gameBoard[aboveRow, col] == Gem(0)) {
                        aboveRow--
                    }
                    if (aboveRow >= 0) {
                        gameBoard[row, col] = gameBoard[aboveRow, col]
                        gameBoard[aboveRow, col] = Gem(0)
                        moves.add(GemMove(Pair(aboveRow, col), Pair(row, col)))
                    }
                }
            }
        }
        return moves
    }

    companion object {

        /**
         * гемы соседние по горизонтали или вертикали
         */
        fun areAdjacent(position1: Pair<Int, Int>, position2: Pair<Int, Int>): Boolean {
            val (row1, col1) = position1
            val (row2, col2) = position2

            return (row1 == row2 && (col1 == col2 - 1 || col1 == col2 + 1)) ||
                    (col1 == col2 && (row1 == row2 - 1 || row1 == row2 + 1))
        }
    }
}
