package aldtoll.twiligihts.ext

import aldtoll.twiligihts.model.Gem

fun Array<Array<Gem>>.hasMatches(): Boolean {
    // Check for horizontal matches
    for (row in indices) {
        for (col in 0 until this[0].size - 2) {
            val gemType = this[row][col].type
            if (gemType == this[row][col + 1].type && gemType == this[row][col + 2].type) {
                return true
            }
        }
    }

    // Check for vertical matches
    for (row in 0 until this.size - 2) {
        for (col in 0 until this[0].size) {
            val gemType = this[row][col].type
            if (gemType == this[row + 1][col].type && gemType == this[row + 2][col].type) {
                return true
            }
        }
    }

    return false
}

fun Array<Array<Gem>>.checkPossibleMoves(): Boolean {
    // Check for possible swaps vertically
    for (col in 0 until this[0].size) {
        for (row in 0 until this.size - 1) {
            // Check if the current cell and the one below are different
            if (this[row][col].type != this[row + 1][col].type) {
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
            if (this[row][col].type != this[row][col + 1].type) {
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