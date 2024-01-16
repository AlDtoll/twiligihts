package aldtoll.twiligihts.ext

import aldtoll.twiligihts.model.Gem

fun Array<Array<Gem>>.hasMatches(): Boolean {
    // Check for horizontal matches
    for (row in indices) {
        for (col in 0 until this[0].size - 2) {
            val gemType = this[row][col]
            if (gemType == this[row][col + 1] && gemType == this[row][col + 2]) {
                return true
            }
        }
    }

    // Check for vertical matches
    for (row in 0 until this.size - 2) {
        for (col in 0 until this[0].size) {
            val gemType = this[row][col]
            if (gemType == this[row + 1][col] && gemType == this[row + 2][col]) {
                return true
            }
        }
    }

    return false
}