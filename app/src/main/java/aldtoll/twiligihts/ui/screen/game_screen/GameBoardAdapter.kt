package aldtoll.twiligihts.ui.screen.game_screen

import aldtoll.twiligihts.R
import aldtoll.twiligihts.model.Gem
import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlin.random.Random

class GameBoardAdapter(
    private val context: Context,
    private val gameBoard: Array<Array<Gem>>,
    private val gameBoardRecyclerView: RecyclerView
) : RecyclerView.Adapter<GameBoardAdapter.TileHolder>() {

    private var selectedPosition: Pair<Int, Int>? = null

    private fun getBoardPosition(position: Pair<Int, Int>): Int {
        return position.first * gameBoard[0].size + position.second
    }

    private fun swapItems(position1: Pair<Int, Int>, position2: Pair<Int, Int>) {
        if (!areAdjacent(position1, position2)) {
            // Positions are not adjacent, return or handle accordingly
            return
        }

        val holder1 = holderForPosition(position1)
        val holder2 = holderForPosition(position2)

        // Calculate the translation values for both row and column swaps
        val translationX1 = holder2.itemView.x - holder1.itemView.x
        val translationY1 = holder2.itemView.y - holder1.itemView.y

        val translationX2 = holder1.itemView.x - holder2.itemView.x
        val translationY2 = holder1.itemView.y - holder2.itemView.y

        // Create ObjectAnimators for both horizontal and vertical movements
        val animatorX1 = ObjectAnimator.ofFloat(holder1.itemView, "translationX", translationX1)
        val animatorY1 = ObjectAnimator.ofFloat(holder1.itemView, "translationY", translationY1)

        val animatorX2 = ObjectAnimator.ofFloat(holder2.itemView, "translationX", translationX2)
        val animatorY2 = ObjectAnimator.ofFloat(holder2.itemView, "translationY", translationY2)

        // Set the duration of the animation (you can adjust this value)
        val duration = 500L
        animatorX1.duration = duration
        animatorY1.duration = duration
        animatorX2.duration = duration
        animatorY2.duration = duration

        // Set up an AnimatorSet to play all four animations together
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(
            animatorX1,
            animatorY1,
            animatorX2,
            animatorY2
        )// can add animatorX2 and animatorY2

        animatorSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                holder1.itemView.translationZ = 1f // You can adjust the value
                holder2.itemView.translationZ = -1f // You can adjust the value
            }

            override fun onAnimationEnd(animation: Animator) {
                // Animation ended, swap the items in the game board and update the UI
                changeItems(position1, position2, holder1, holder2)
            }

            override fun onAnimationCancel(animation: Animator) {
                // Animation canceled
            }

            override fun onAnimationRepeat(animation: Animator) {
                // Animation repeated
            }
        })

        // Start the animation
        animatorSet.start()
    }

    private fun changeItems(
        position1: Pair<Int, Int>,
        position2: Pair<Int, Int>,
        holder1: TileHolder,
        holder2: TileHolder
    ) {
        val temp = gameBoard[position1.first][position1.second]
        gameBoard[position1.first][position1.second] =
            gameBoard[position2.first][position2.second]
        gameBoard[position2.first][position2.second] = temp
        selectedPosition = null
        holder1.frameView.visibility = View.INVISIBLE
        holder2.frameView.visibility = View.INVISIBLE
        notifyItemChanged(getBoardPosition(position1))
        notifyItemChanged(getBoardPosition(position2))

        // Check for matches after the swap
        if (hasMatches()) {
            // Handle matches (e.g., remove matched items)
            // You might want to implement a method to remove matched items and update the UI
            handleMatches()
        }
    }


    // Check if two positions are adjacent
    private fun areAdjacent(position1: Pair<Int, Int>, position2: Pair<Int, Int>): Boolean {
        val (row1, col1) = position1
        val (row2, col2) = position2

        // Check if positions are horizontally or vertically adjacent
        return (row1 == row2 && (col1 == col2 - 1 || col1 == col2 + 1)) ||
                (col1 == col2 && (row1 == row2 - 1 || row1 == row2 + 1))
    }

    private fun handleMatches() {
        var matchesFound: Boolean

        do {
            matchesFound = false

            // List to store positions of matched items
            val matchedPositions = findMatches()

            if (matchedPositions.isNotEmpty()) {
                // Remove matched items from the game board
                removeMatches(matchedPositions)

                // Apply gravity effect: shift gems downward to fill empty spaces
                applyGravityEffect()

                matchesFound = true
            }
        } while (matchesFound)
    }

    private fun removeMatches(matchedPositions: MutableList<Pair<Int, Int>>) {
        for (position in matchedPositions) {
            gameBoard[position.first][position.second] =
                    /* Your representation of an empty cell */ Gem(0)
            notifyItemChanged(getBoardPosition(Pair(position.first, position.second)))
        }
    }

    private fun findMatches(): MutableList<Pair<Int, Int>> {
        val matchedPositions = mutableListOf<Pair<Int, Int>>()

        // Check for horizontal matches
        for (i in gameBoard.indices) {
            for (j in 0 until gameBoard[0].size - 2) {
                val gemType = gameBoard[i][j]
                if (gemType != Gem(0) && gemType == gameBoard[i][j + 1] && gemType == gameBoard[i][j + 2]) {
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
                if (gemType != Gem(0) && gemType == gameBoard[i + 1][j] && gemType == gameBoard[i + 2][j]) {
                    // Add matched items to the list
                    matchedPositions.add(Pair(i, j))
                    matchedPositions.add(Pair(i + 1, j))
                    matchedPositions.add(Pair(i + 2, j))
                }
            }
        }
        return matchedPositions
    }

    private fun applyGravityEffect() {
        // Iterate through each column in reverse order
        for (col in gameBoard[0].indices.reversed()) {
            // Iterate through each row in reverse order
            for (row in gameBoard.indices.reversed()) {
                // If the current cell is empty, find the nearest non-empty cell above it
                if (gameBoard[row][col] == Gem(0)) {
                    var aboveRow = row - 1
                    while (aboveRow >= 0 && gameBoard[aboveRow][col] == Gem(0)) {
                        aboveRow--
                    }

                    // If a non-empty cell is found, move the gem down with animation
                    if (aboveRow >= 0) {

                        val fromPosition = getBoardPosition(Pair(aboveRow, col))
                        val toPosition = getBoardPosition(Pair(row, col))
                        val holderFromPosition = holderForPosition(Pair(aboveRow, col))
                        val holderToPosition = holderForPosition(Pair(row, col))
                        gameBoard[row][col] = gameBoard[aboveRow][col]
                        gameBoard[aboveRow][col] = Gem(0)
                        // Add translation animation for the gem
                        val animator = ObjectAnimator.ofFloat(
                            holderFromPosition.itemView,
                            "translationY",
                            holderToPosition.itemView.y - holderFromPosition.itemView.y
                        )
                        animator.duration =
                            500 // Set the duration of the animation (in milliseconds)
                        animator.addListener(object : Animator.AnimatorListener {
                            override fun onAnimationStart(animation: Animator) {
                                holderFromPosition.itemView.translationZ =
                                    1f // You can adjust the value
                                holderToPosition.itemView.translationZ =
                                    -1f // You can adjust the value
                            }

                            override fun onAnimationEnd(animation: Animator) {
                                // Update the game board


                                // Notify the adapter about the item change
                                notifyItemChanged(fromPosition)
                                notifyItemChanged(toPosition)
                            }

                            override fun onAnimationCancel(animation: Animator) {
                                // Animation canceled
                            }

                            override fun onAnimationRepeat(animation: Animator) {
                                // Animation repeated
                            }
                        })
                        animator.start()
                    }
                    // Notify the adapter about the item change
                }
            }
        }
    }


    private fun generateNewGem(): Gem {
        // For simplicity, let's assume there are 4 gem types (1, 2, 3, 4)
        return Gem(Random.nextInt(1, 5))
    }


    private fun hasMatches(): Boolean {
        val numRows = gameBoard.size
        val numCols = gameBoard[0].size

        // Check for horizontal matches
        for (i in 0 until numRows) {
            for (j in 0 until numCols - 2) {
                val gemType = gameBoard[i][j]
                if (gemType != Gem(0) && gemType == gameBoard[i][j + 1] && gemType == gameBoard[i][j + 2]) {
                    return true
                }
            }
        }

        // Check for vertical matches
        for (i in 0 until numRows - 2) {
            for (j in 0 until numCols) {
                val gemType = gameBoard[i][j]
                if (gemType != Gem(0) && gemType == gameBoard[i + 1][j] && gemType == gameBoard[i + 2][j]) {
                    return true
                }
            }
        }

        return false
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TileHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_game_cell, parent, false)
        return TileHolder(view)
    }

    override fun onBindViewHolder(holder: TileHolder, position: Int) {
        val row = position / gameBoard[0].size
        val col = position % gameBoard[0].size
        val gemType = gameBoard[row][col]
        val gemColor = getGemColor(gemType)

        holder.gameCell.setBackgroundColor(ContextCompat.getColor(context, gemColor))
        val tileNumberText = (position).toString()
        holder.tileNumber.text = tileNumberText

        holder.itemView.setOnClickListener {
            // Handle item click and swap logic
            if (selectedPosition != null) {
                if (selectedPosition == Pair(row, col)) {
                    // Clicked on the already selected item, treat it as deselection
                    selectedPosition = null
                    holder.frameView.visibility = View.INVISIBLE
                } else {
                    // Clicked on a different item, initiate the swap
                    swapItems(selectedPosition!!, Pair(row, col))
                }
            } else {
                // No item is currently selected, select the clicked item
                selectedPosition = Pair(row, col)
                holder.frameView.visibility = View.VISIBLE
            }
            Log.d(
                "MY",
                "click=" + Pair(row, col).toString() + " " +
                        "selected=" + selectedPosition.toString() + " " +
                        "getAdapterPosition=" + getBoardPosition(Pair(row, col))
            )
        }
    }

    override fun getItemCount(): Int {
        return gameBoard.size * gameBoard[0].size
    }

    private fun getGemColor(gem: Gem): Int {
        return when (gem.type) {
            1 -> R.color.gem_color_1
            2 -> R.color.gem_color_2
            3 -> R.color.gem_color_3
            4 -> R.color.gem_color_4
            else -> R.color.default_color
        }
    }

    private fun holderForPosition(position: Pair<Int, Int>): TileHolder {
        val adapterPosition = getBoardPosition(position)
        return gameBoardRecyclerView.findViewHolderForAdapterPosition(adapterPosition) as TileHolder
    }

    inner class TileHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val gameCell: View = itemView.findViewById(R.id.gameCell)
        val frameView: View = itemView.findViewById(R.id.frameView)
        val tileNumber: TextView = itemView.findViewById(R.id.tileNumber)
    }
}




