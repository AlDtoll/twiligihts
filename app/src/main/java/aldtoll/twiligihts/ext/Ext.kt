package aldtoll.twiligihts.ext

import aldtoll.twiligihts.model.Gem
import aldtoll.twiligihts.model.Move

fun Array<Array<Gem>>.hasMatches(): Boolean {
    // Check for horizontal matches
    for (row in indices) {
        for (col in 0 until this[0].size - 2) {
            val gem = this[row][col]
            if (gem.type != 0 &&
                matches(gem, this[row][col + 1]) &&
                matches(gem, this[row][col + 2])
            ) {
                return true
            }
        }
    }

    // Check for vertical matches
    for (row in 0 until this.size - 2) {
        for (col in 0 until this[0].size) {
            val gem = this[row][col]
            if (gem.type != 0 &&
                matches(gem, this[row + 1][col]) &&
                matches(gem, this[row + 2][col])
            ) {
                return true
            }
        }
    }

    return false
}

// Helper function to check if two gems match in either type or extraType
fun matches(gem1: Gem, gem2: Gem): Boolean {
    return (gem1.type == gem2.type || gem1.extraType == gem2.type || gem1.type == gem2.extraType)
}

fun Array<Array<Gem>>.checkPossibleMoves(): Boolean {
    // Helper function to check if two gems are different in both type and extraType
    fun areDifferent(gem1: Gem, gem2: Gem): Boolean {
        return !(gem1.type == gem2.type || gem1.extraType == gem2.type || gem1.type == gem2.extraType)
    }

    // Check for possible swaps vertically
    for (col in 0 until this[0].size) {
        for (row in 0 until this.size - 1) {
            // Check if the current cell and the one below are different
            if (areDifferent(this[row][col], this[row + 1][col])) {
                // Swap the cells
                val temp = this[row][col]
                this[row][col] = this[row + 1][col]
                this[row + 1][col] = temp

                // Check if the swap results in a match
                if (hasMatches()) {
                    // Swap back the cells
                    this[row + 1][col] = this[row][col]
                    this[row][col] = temp
                    return true
                } else {
                    // Swap back the cells
                    this[row + 1][col] = this[row][col]
                    this[row][col] = temp
                }
            }
        }
    }

    // Check for possible swaps horizontally
    for (row in 0 until this.size) {
        for (col in 0 until this[0].size - 1) {
            // Check if the current cell and the one to the right are different
            if (areDifferent(this[row][col], this[row][col + 1])) {
                // Swap the cells
                val temp = this[row][col]
                this[row][col] = this[row][col + 1]
                this[row][col + 1] = temp

                // Check if the swap results in a match
                if (hasMatches()) {
                    // Swap back the cells
                    this[row][col + 1] = this[row][col]
                    this[row][col] = temp
                    return true
                } else {
                    // Swap back the cells
                    this[row][col + 1] = this[row][col]
                    this[row][col] = temp
                }
            }
        }
    }

    return false
}

fun Array<Array<Gem>>.findPossibleMoves(): List<Move> {
    val possibleMoves = mutableListOf<Move>()

    // Helper function to check if two gems are different in both type and extraType
    fun areDifferent(gem1: Gem, gem2: Gem): Boolean {
        return !(gem1.type == gem2.type || gem1.extraType == gem2.type || gem1.type == gem2.extraType)
    }

    // Check for possible swaps vertically
    for (col in 0 until this[0].size) {
        for (row in 0 until this.size - 1) {
            // Check if the current cell and the one below are different
            if (areDifferent(this[row][col], this[row + 1][col])) {
                // Swap the cells
                val temp = this[row][col]
                this[row][col] = this[row + 1][col]
                this[row + 1][col] = temp

                // Check if the swap results in a match
                if (hasMatches()) {
                    // Swap back the cells
                    this[row + 1][col] = this[row][col]
                    this[row][col] = temp
                    possibleMoves.add(Move(Pair(row, col), Pair(row + 1, col)))
                } else {
                    // Swap back the cells
                    this[row + 1][col] = this[row][col]
                    this[row][col] = temp
                }
            }
        }
    }

    // Check for possible swaps horizontally
    for (row in 0 until this.size) {
        for (col in 0 until this[0].size - 1) {
            // Check if the current cell and the one to the right are different
            if (areDifferent(this[row][col], this[row][col + 1])) {
                // Swap the cells
                val temp = this[row][col]
                this[row][col] = this[row][col + 1]
                this[row][col + 1] = temp

                // Check if the swap results in a match
                if (hasMatches()) {
                    // Swap back the cells
                    this[row][col + 1] = this[row][col]
                    this[row][col] = temp
                    possibleMoves.add(Move(Pair(row, col), Pair(row, col + 1)))
                } else {
                    // Swap back the cells
                    this[row][col + 1] = this[row][col]
                    this[row][col] = temp
                }
            }
        }
    }

    return possibleMoves
}