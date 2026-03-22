package aldtoll.twiligihts.model

import aldtoll.twiligihts.storage.BattleSettingsInteractor
import javax.inject.Singleton

@Singleton
class GameBoard(
    private val numRows: Int = 8,
    private val numCols: Int = 8,
    private val battleSettingsInteractor: BattleSettingsInteractor,
) {
    private val board: Array<Array<Gem>> =
        Array(numRows) { Array(numCols) { Gem.generateNewGem() } }

    private val cells: Array<Array<CellState>> =
        Array(numRows) { row -> Array(numCols) { col -> CellState(row = row, col = col) } }

    init {
        initializeBoard() // Генерация начальной доски без совпадений при создании объекта
        initializeCells()
    }

    fun initializeBoard() {
        do {
            generateNewBoard() // Генерируем новую доску
        } while (hasMatches()) // Проверяем наличие совпадений; если есть, генерируем заново
        initializeCells()
    }

    private fun generateNewBoard() {
        for (row in 0 until numRows) {
            for (col in 0 until numCols) {
                board[row][col] = Gem.generateNewGem()
            }
        }
    }

    private fun initializeCells() {
        // Сброс к базовым значениям
        for (row in 0 until numRows) {
            for (col in 0 until numCols) {
                cells[row][col] = CellState(row = row, col = col)
            }
        }

        val sceneCells = battleSettingsInteractor.value()?.cells.orEmpty()
        if (sceneCells.isNotEmpty()) {
            for (sceneCell in sceneCells) {
                if (sceneCell.row !in 0 until numRows || sceneCell.col !in 0 until numCols) continue
                cells[sceneCell.row][sceneCell.col] = CellState(
                    row = sceneCell.row,
                    col = sceneCell.col,
                    cellType = sceneCell.cellType,
                    modifierValue = sceneCell.modifierValue,
                    triggerPerk = sceneCell.triggerPerk,
                )
            }
            return
        }
    }

    private fun initializeCellsForPrototype() {
        if (numRows == 0 || numCols == 0) return

        val centerRow = numRows / 2
        val centerCol = numCols / 2

        // Квадрат 2x2 в центре поля с множителем x1.5
        val startRow = centerRow - 1
        val startCol = centerCol - 1
        for (row in startRow..centerRow) {
            if (row !in 0 until numRows) continue
            for (col in startCol..centerCol) {
                if (col !in 0 until numCols) continue
                cells[row][col] = cells[row][col].copy(
                    cellType = CellType.MULTIPLIER,
                    modifierValue = 1.5f,
                )
            }
        }
    }

    // Вернет элемент Gem или выбросит исключение при выходе за границы массива
    operator fun get(row: Int, col: Int): Gem {
        require(row in 0 until numRows && col in 0 until numCols) {
            "Indices out of bounds: row=$row, col=$col"
        }
        return board[row][col]
    }

    operator fun set(row: Int, col: Int, gem: Gem) {
        require(row in 0 until numRows && col in 0 until numCols) {
            "Indices out of bounds: row=$row, col=$col"
        }
        board[row][col] = gem
    }

    fun getCell(row: Int, col: Int): CellState {
        require(row in 0 until numRows && col in 0 until numCols) {
            "Indices out of bounds: row=$row, col=$col"
        }
        return cells[row][col]
    }

    fun setCell(row: Int, col: Int, cellState: CellState) {
        require(row in 0 until numRows && col in 0 until numCols) {
            "Indices out of bounds: row=$row, col=$col"
        }
        cells[row][col] = cellState
    }

    // Доступ через Pair<Int, Int>
    operator fun get(position: Pair<Int, Int>): Gem = get(position.first, position.second)

    operator fun set(position: Pair<Int, Int>, gem: Gem) = set(position.first, position.second, gem)

    fun getCell(position: Pair<Int, Int>): CellState = getCell(position.first, position.second)

    fun setCell(position: Pair<Int, Int>, cellState: CellState) =
        setCell(position.first, position.second, cellState)

    // Возвращает размер строки как аналог обращения `gameBoard.size`
    val rowSize: Int
        get() = board.size

    // Возвращает размер строки как аналог обращения `gameBoard[0].size`
    val columnSize: Int
        get() = board[0].size

    // Проверка на наличие элементов с type == 0
    fun hasEmptyGems(): Boolean {
        return board.any { row -> row.any { gem -> gem.type == 0 } }
    }

    fun hasMatches(): Boolean {
        // Check for horizontal matches
        for (row in 0 until rowSize) {
            for (col in 0 until columnSize - 2) {
                val gem = board[row][col]
                if (gem.type != 0 &&
                    matches(gem, board[row][col + 1]) &&
                    matches(gem, board[row][col + 2])
                ) {
                    return true
                }
            }
        }

        // Check for vertical matches
        for (row in 0 until rowSize - 2) {
            for (col in 0 until columnSize) {
                val gem = board[row][col]
                if (gem.type != 0 &&
                    matches(gem, board[row + 1][col]) &&
                    matches(gem, board[row + 2][col])
                ) {
                    return true
                }
            }
        }

        return false
    }


    fun checkPossibleMoves(): Boolean {
        fun areDifferent(gem1: Gem, gem2: Gem): Boolean {
            return !(gem1.type == gem2.type || gem1.extraType == gem2.type || gem1.type == gem2.extraType)
        }

        // Check for possible swaps vertically
        for (col in 0 until board[0].size) {
            for (row in 0 until board.size - 1) {
                if (areDifferent(board[row][col], board[row + 1][col])) {
                    val temp = board[row][col]
                    board[row][col] = board[row + 1][col]
                    board[row + 1][col] = temp

                    if (hasMatches()) {
                        board[row + 1][col] = board[row][col]
                        board[row][col] = temp
                        return true
                    } else {
                        board[row + 1][col] = board[row][col]
                        board[row][col] = temp
                    }
                }
            }
        }

        // Check for possible swaps horizontally
        for (row in 0 until board.size) {
            for (col in 0 until board[0].size - 1) {
                if (areDifferent(board[row][col], board[row][col + 1])) {
                    val temp = board[row][col]
                    board[row][col] = board[row][col + 1]
                    board[row][col + 1] = temp

                    if (hasMatches()) {
                        board[row][col + 1] = board[row][col]
                        board[row][col] = temp
                        return true
                    } else {
                        board[row][col + 1] = board[row][col]
                        board[row][col] = temp
                    }
                }
            }
        }

        return false
    }

    fun findPossibleMoves(): List<Move> {
        val possibleMoves = mutableListOf<Move>()

        fun areDifferent(gem1: Gem, gem2: Gem): Boolean {
            return !(gem1.type == gem2.type || gem1.extraType == gem2.type || gem1.type == gem2.extraType)
        }

        // Check for possible swaps vertically
        for (col in 0 until board[0].size) {
            for (row in 0 until board.size - 1) {
                if (areDifferent(board[row][col], board[row + 1][col])) {
                    val temp = board[row][col]
                    board[row][col] = board[row + 1][col]
                    board[row + 1][col] = temp

                    if (hasMatches()) {
                        board[row + 1][col] = board[row][col]
                        board[row][col] = temp
                        possibleMoves.add(Move(Pair(row, col), Pair(row + 1, col)))
                    } else {
                        board[row + 1][col] = board[row][col]
                        board[row][col] = temp
                    }
                }
            }
        }

        // Check for possible swaps horizontally
        for (row in 0 until board.size) {
            for (col in 0 until board[0].size - 1) {
                if (areDifferent(board[row][col], board[row][col + 1])) {
                    val temp = board[row][col]
                    board[row][col] = board[row][col + 1]
                    board[row][col + 1] = temp

                    if (hasMatches()) {
                        board[row][col + 1] = board[row][col]
                        board[row][col] = temp
                        possibleMoves.add(Move(Pair(row, col), Pair(row, col + 1)))
                    } else {
                        board[row][col + 1] = board[row][col]
                        board[row][col] = temp
                    }
                }
            }
        }

        return possibleMoves
    }

    companion object {

        fun matches(gem1: Gem, gem2: Gem): Boolean {
            return (gem1.type == gem2.type || gem1.extraType == gem2.type || gem1.type == gem2.extraType)
        }
    }
}

