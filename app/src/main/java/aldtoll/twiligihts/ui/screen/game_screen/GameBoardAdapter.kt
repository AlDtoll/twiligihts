package aldtoll.twiligihts.ui.screen.game_screen

import aldtoll.twiligihts.R
import android.animation.ObjectAnimator
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlin.random.Random

class GameBoardAdapter(
    private val context: Context,
    private val gameBoard: Array<IntArray>,
    private val gemAnimationListener: GemAnimationListener
) : RecyclerView.Adapter<GameBoardAdapter.ViewHolder>() {

    private var selectedPosition: Pair<Int, Int>? = null

    fun selectItem(position: Pair<Int, Int>?) {
        val previousSelected = selectedPosition
        selectedPosition = position
        if (previousSelected != null) {
            notifyItemChanged(getAdapterPosition(previousSelected))
        }
        if (selectedPosition != null) {
            notifyItemChanged(getAdapterPosition(selectedPosition!!))
        }
    }

    private fun getAdapterPosition(position: Pair<Int, Int>): Int {
        return position.first * gameBoard[0].size + position.second
    }

    fun swapItems(position1: Pair<Int, Int>, position2: Pair<Int, Int>) {
        val temp = gameBoard[position1.first][position1.second]
        gameBoard[position1.first][position1.second] = gameBoard[position2.first][position2.second]
        gameBoard[position2.first][position2.second] = temp
        notifyItemChanged(getAdapterPosition(position1))
        notifyItemChanged(getAdapterPosition(position2))

        // Check for matches after the swap
        if (hasMatches()) {
            // Handle matches (e.g., remove matched items)
            // You might want to implement a method to remove matched items and update the UI
            handleMatches()
        }
    }

//    private fun handleMatches() {
//        val numRows = gameBoard.size
//        val numCols = gameBoard[0].size
//
//        // List to store positions of matched items
//        val matchedPositions = mutableListOf<Pair<Int, Int>>()
//
//        // Check for horizontal matches
//        for (i in 0 until numRows) {
//            for (j in 0 until numCols - 2) {
//                val gemType = gameBoard[i][j]
//                if (gemType == gameBoard[i][j + 1] && gemType == gameBoard[i][j + 2]) {
//                    // Add matched items to the list
//                    matchedPositions.add(Pair(i, j))
//                    matchedPositions.add(Pair(i, j + 1))
//                    matchedPositions.add(Pair(i, j + 2))
//                }
//            }
//        }
//
//        // Check for vertical matches
//        for (i in 0 until numRows - 2) {
//            for (j in 0 until numCols) {
//                val gemType = gameBoard[i][j]
//                if (gemType == gameBoard[i + 1][j] && gemType == gameBoard[i + 2][j]) {
//                    // Add matched items to the list
//                    matchedPositions.add(Pair(i, j))
//                    matchedPositions.add(Pair(i + 1, j))
//                    matchedPositions.add(Pair(i + 2, j))
//                }
//            }
//        }
//
//        // Remove matched items from the game board
//        for (position in matchedPositions) {
//            gameBoard[position.first][position.second] = 0 // Assuming EMPTY_GEM is a constant representing an empty cell
//        }
//
//        // Apply gravity effect: shift gems downward to fill empty spaces
//        applyGravityEffect()
//
//        // Notify the adapter about the data change
//        notifyDataSetChanged()
//    }

    private fun handleMatches() {
        var matchesFound: Boolean

        do {
            matchesFound = false

            // List to store positions of matched items
            val matchedPositions = mutableListOf<Pair<Int, Int>>()

            // Check for horizontal matches
            for (i in 0 until gameBoard.size) {
                for (j in 0 until gameBoard[0].size - 2) {
                    val gemType = gameBoard[i][j]
                    if (gemType == gameBoard[i][j + 1] && gemType == gameBoard[i][j + 2]) {
                        // Add matched items to the list
                        matchedPositions.add(Pair(i, j))
                        matchedPositions.add(Pair(i, j + 1))
                        matchedPositions.add(Pair(i, j + 2))
                    }
                }
            }

            // Check for vertical matches
            for (i in 0 until gameBoard.size - 2) {
                for (j in 0 until gameBoard[0].size) {
                    val gemType = gameBoard[i][j]
                    if (gemType == gameBoard[i + 1][j] && gemType == gameBoard[i + 2][j]) {
                        // Add matched items to the list
                        matchedPositions.add(Pair(i, j))
                        matchedPositions.add(Pair(i + 1, j))
                        matchedPositions.add(Pair(i + 2, j))
                    }
                }
            }

            if (matchedPositions.isNotEmpty()) {
                // Remove matched items from the game board
                for (position in matchedPositions) {
                    gameBoard[position.first][position.second] = /* Your representation of an empty cell */ 0
                }

                // Apply gravity effect: shift gems downward to fill empty spaces
                applyGravityEffect()

                // Notify the adapter about the data change
                notifyDataSetChanged()

                matchesFound = true
            }
        } while (matchesFound)
    }

//    private fun applyGravityEffect() {
//        val numRows = gameBoard.size
//        val numCols = gameBoard[0].size
//
//        // Iterate through each column
//        for (j in 0 until numCols) {
//            // Start from the bottom and move upward
//            var k = numRows - 1
//            for (i in numRows - 1 downTo 0) {
//                if (gameBoard[i][j] != /* Your representation of an empty cell */ 0) {
//                    // If the cell is not empty, move the gem downward
//                    gameBoard[k][j] = gameBoard[i][j]
//                    k--
//                }
//            }
//
//            // Fill the remaining empty cells at the top with new gems
//            while (k >= 0) {
//                gameBoard[k][j] = /* Generate a new gem type */ generateNewGemType()
//                k--
//            }
//        }
//    }

    private fun applyGravityEffect() {
        val numRows = gameBoard.size
        val numCols = gameBoard[0].size

        // Iterate through each column
        for (j in 0 until numCols) {
            // Start from the bottom and move upward
            var k = numRows - 1
            for (i in numRows - 1 downTo 0) {
                if (gameBoard[i][j] != /* Your representation of an empty cell */ 0) {
                    // If the cell is not empty, move the gem downward with animation
                    gemAnimationListener.animateGemDown(Pair(i, j), Pair(k, j))

                    gameBoard[k][j] = gameBoard[i][j]
                    k--
                }
            }

            // Fill the remaining empty cells at the top with new gems
            while (k >= 0) {
                gameBoard[k][j] = /* Generate a new gem type */ generateNewGemType()

                // Create a new gem view with animation
                gemAnimationListener.animateNewGem(Pair(k, j), generateNewGemType())

                k--
            }
        }
    }


    private fun generateNewGemType(): Int {
        // TODO: Implement logic to generate a new gem type
        // For simplicity, let's assume there are 3 gem types (1, 2, 3)
        return Random.nextInt(1, 4)
    }


    private fun hasMatches(): Boolean {
        val numRows = gameBoard.size
        val numCols = gameBoard[0].size

        // Check for horizontal matches
        for (i in 0 until numRows) {
            for (j in 0 until numCols - 2) {
                val gemType = gameBoard[i][j]
                if (gemType == gameBoard[i][j + 1] && gemType == gameBoard[i][j + 2]) {
                    return true
                }
            }
        }

        // Check for vertical matches
        for (i in 0 until numRows - 2) {
            for (j in 0 until numCols) {
                val gemType = gameBoard[i][j]
                if (gemType == gameBoard[i + 1][j] && gemType == gameBoard[i + 2][j]) {
                    return true
                }
            }
        }

        return false
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_game_cell, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val row = position / gameBoard[0].size
        val col = position % gameBoard[0].size
        val gemType = gameBoard[row][col]
        val gemColor = getGemColor(gemType)

        holder.gameCell.setBackgroundColor(ContextCompat.getColor(context, gemColor))

        // Highlight selected item
        if (selectedPosition != null && selectedPosition == Pair(row, col)) {
            holder.frameView.visibility = View.VISIBLE
        } else {
            holder.frameView.visibility = View.INVISIBLE
        }

        holder.itemView.setOnClickListener {
            // Handle item click and swap logic
            if (selectedPosition != null) {
                if (selectedPosition == Pair(row, col)) {
                    // Clicked on the already selected item, treat it as deselection
                    selectItem(null)
                } else {
                    // Clicked on a different item, initiate the swap
                    swapItems(selectedPosition!!, Pair(row, col))
                    notifyDataSetChanged() // Notify the adapter about the data change
                    selectItem(null) // Deselect after the swap
                }
            } else {
                // No item is currently selected, select the clicked item
                selectItem(Pair(row, col))
            }
        }
    }

    override fun getItemCount(): Int {
        return gameBoard.size * gameBoard[0].size
    }

    private fun getGemColor(gemType: Int): Int {
        // TODO: Implement logic to map gem types to colors
        // For simplicity, let's assume gemType corresponds to color resource IDs
        return when (gemType) {
            1 -> R.color.gem_color_1
            2 -> R.color.gem_color_2
            3 -> R.color.gem_color_3
            else -> R.color.default_color
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val gameCell: View = itemView.findViewById(R.id.gameCell)
        val frameView: View = itemView.findViewById(R.id.frameView)
    }
}




