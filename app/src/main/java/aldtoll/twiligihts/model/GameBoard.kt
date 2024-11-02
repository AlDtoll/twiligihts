package aldtoll.twiligihts.model

import javax.inject.Singleton

@Singleton
class GameBoard(
    private val numRows: Int = 8,
    private val numCols: Int = 8
) {
    private val board: Array<Array<Gem>> =
        Array(numRows) { Array(numCols) { Gem.generateNewGem() } }

    init {
        initializeBoard() // Генерация начальной доски без совпадений при создании объекта
    }

    fun initializeBoard() {
        do {
            generateNewBoard() // Генерируем новую доску
        } while (hasMatches()) // Проверяем наличие совпадений; если есть, генерируем заново
    }

    private fun generateNewBoard() {
        for (row in 0 until numRows) {
            for (col in 0 until numCols) {
                board[row][col] = Gem.generateNewGem()
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

    // Доступ через Pair<Int, Int>
    operator fun get(position: Pair<Int, Int>): Gem = get(position.first, position.second)

    operator fun set(position: Pair<Int, Int>, gem: Gem) = set(position.first, position.second, gem)

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

