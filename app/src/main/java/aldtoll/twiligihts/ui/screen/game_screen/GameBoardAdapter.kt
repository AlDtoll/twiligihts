package aldtoll.twiligihts.ui.screen.game_screen

import aldtoll.twiligihts.databinding.ItemGemBinding
import aldtoll.twiligihts.ext.checkPossibleMoves
import aldtoll.twiligihts.ext.hasMatches
import aldtoll.twiligihts.model.Gem
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

private const val ANIMATION_TIME = 400L

class GameBoardAdapter(
    private val context: Context,
    private val gameBoard: Array<Array<Gem>>,
    private val gameBoardRecyclerView: RecyclerView,
    private val callback: Callback,
    private var stopGenerate: Boolean = false,
) : RecyclerView.Adapter<GameBoardAdapter.TileHolder>() {

    interface Callback {

        fun crushGems(removedGems: MutableList<Gem>)
        fun checkPossibleMoves(checkPossibleMoves: Boolean, finishBattleIfNoMatches: Boolean)
        fun onHandleMatches()
        fun allowEndTurn()
    }

    private var selectedPosition: Pair<Int, Int>? = null

    private fun getBoardPosition(position: Pair<Int, Int>): Int {
        return position.first * gameBoard[0].size + position.second
    }

    private fun swapItems(
        position1: Pair<Int, Int>,
        position2: Pair<Int, Int>,
        returnBack: Boolean = false
    ) {
        if (!areAdjacent(position1, position2)) {
            // Positions are not adjacent, return or handle accordingly
            return
        }
        changeBoardPosition(position1, position2)
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
        val duration = ANIMATION_TIME
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
                changeItems(position1, position2, holder1, holder2, returnBack)
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

    private fun changeBoardPosition(
        position1: Pair<Int, Int>,
        position2: Pair<Int, Int>
    ) {
        val temp = gameBoard[position1.first][position1.second]
        gameBoard[position1.first][position1.second] =
            gameBoard[position2.first][position2.second]
        gameBoard[position2.first][position2.second] = temp
        selectedPosition = null
    }

    private fun changeItems(
        position1: Pair<Int, Int>,
        position2: Pair<Int, Int>,
        holder1: TileHolder,
        holder2: TileHolder,
        returnBack: Boolean = false
    ) {
        holder1.frameView.visibility = View.INVISIBLE
        holder2.frameView.visibility = View.INVISIBLE
        notifyItemChanged(getBoardPosition(position1))
        notifyItemChanged(getBoardPosition(position2))

        Handler(Looper.getMainLooper()).postDelayed({
            // Check for matches after the swap
            if (gameBoard.hasMatches()) {
                // Handle matches (e.g., remove matched items)
                // You might want to implement a method to remove matched items and update the UI
                handleMatches()
                callback.onHandleMatches()
            } else {
                if (!returnBack) {
                    swapWithMistake(position1, position2)
                }
            }
        }, 100)

    }

    private fun swapWithMistake(
        position1: Pair<Int, Int>,
        position2: Pair<Int, Int>
    ) {
        swapItems(position2, position1, true)
    }


    // Check if two positions are adjacent
    private fun areAdjacent(position1: Pair<Int, Int>, position2: Pair<Int, Int>): Boolean {
        val (row1, col1) = position1
        val (row2, col2) = position2

        // Check if positions are horizontally or vertically adjacent
        return (row1 == row2 && (col1 == col2 - 1 || col1 == col2 + 1)) ||
                (col1 == col2 && (row1 == row2 - 1 || row1 == row2 + 1))
    }

    private var nowStartGenerateAndDrop = false
    private var allowSelect = true

    private fun handleMatches() {
        Log.d("MY", "handleMatches")
        Log.d("MY", "counter  $counter")
        // List to store positions of matched items
        val matchedPositions = findMatches()
        val hasEmpty = gameBoard.any { it.any { gem -> gem.type == 0 } }
        val allowGenerateNewGems = !stopGenerate
        val needStopHandle = if (allowGenerateNewGems) {
            matchedPositions.isEmpty() && !hasEmpty
        } else {
            matchedPositions.isEmpty()
        }
        if (needStopHandle) {
            //just wait user
            allowSelect = true
            Log.d("MY", "stop handle")
            callback.checkPossibleMoves(gameBoard.checkPossibleMoves(), !allowGenerateNewGems)
            callback.allowEndTurn()
        } else {
            allowSelect = false
            if (nowStartGenerateAndDrop) {
                Log.d("MY", "nowStartGenerateAndDrop  true")
                if (!hasEmpty) {
                    Log.d("MY", "stop nowStartGenerateAndDrop")
                    nowStartGenerateAndDrop = false
                    handleMatches()
                } else {
                    Log.d("MY", "generate")
                    if (allowGenerateNewGems) {
                        generateNewGems()
                    }
                }
            } else {
                Log.d("MY", "nowStartGenerateAndDrop  false")
                if (matchedPositions.isNotEmpty()) {
                    // Remove matched items from the game board
                    Log.d("MY", "has matches")
                    removeMatches(matchedPositions)
                } else {
                    Log.d("MY", "no matches")
                    if (hasEmpty) {
                        nowStartGenerateAndDrop = true
                        Log.d("MY", "start nowStartGenerateAndDrop")
                    }
                    handleMatches()
                }
            }
        }
    }

    private fun generateNewGems() {
        // Iterate through each column in reverse order
        for (col in gameBoard[0].indices) {
            if (gameBoard[0][col] == Gem(0)) {
                Log.d("MY", "generate ${0},${col}")
                val newGem = generateNewGem()
                gameBoard[0][col] = newGem
                val holder = holderForPosition(Pair(0, col))
                // Create an ObjectAnimator to animate the alpha property of the Gem
                val animator = ObjectAnimator.ofFloat(holder.itemView, "alpha", 0f, 1f)
                animator.duration =
                    ANIMATION_TIME // Set the duration of the animation in milliseconds

                // Set up a listener to remove the Gem after the animation ends
                animator.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        notifyItemChanged(getBoardPosition(Pair(0, col)))
                    }
                })

                // Start the animation
                animator.start()
            }
        }
        Handler(Looper.getMainLooper()).postDelayed({
            applyGravityEffect()
        }, ANIMATION_TIME + 100)
    }

    private fun removeMatches(matchedPositions: MutableList<Pair<Int, Int>>) {
        Log.d("MY", "removeMatches")

        // Map to store the count of removed gems for each color
        val removedGems = mutableListOf<Gem>()
        val removedGemsCount = mutableMapOf<Int, Int>()

        for (position in matchedPositions) {
            Log.d("MY", "remove ${position.first},${position.second}")
            removedGems.add(gameBoard[position.first][position.second])
            val removedGemColor = gameBoard[position.first][position.second].type
            // Increment the count for the removed gem color in the map
            removedGemsCount[removedGemColor] = (removedGemsCount[removedGemColor] ?: 0) + 1
            gameBoard[position.first][position.second] = Gem(0)
            val holder = holderForPosition(Pair(position.first, position.second))
            // Create an ObjectAnimator to animate the alpha property of the Gem
            val animator = ObjectAnimator.ofFloat(holder.itemView, "alpha", 1f, 0f)
            animator.duration = ANIMATION_TIME // Set the duration of the animation in milliseconds

            // Set up a listener to remove the Gem after the animation ends
            animator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    notifyItemChanged(getBoardPosition(Pair(position.first, position.second)))
                }
            })

            // Start the animation
            animator.start()
        }
        Handler(Looper.getMainLooper()).postDelayed({
            callback.crushGems(removedGems)
            applyGravityEffect()
            for ((color, count) in removedGemsCount) {
                Log.d("MY", "Removed $count gems of color $color")
            }
        }, ANIMATION_TIME + 100)
    }

    private fun findMatches(): MutableList<Pair<Int, Int>> {
        val matchedPositions = mutableListOf<Pair<Int, Int>>()

        // Check for horizontal matches
        for (row in gameBoard.indices) {
            for (col in 0 until gameBoard[0].size - 2) {
                val gemType = gameBoard[row][col].type
                if (gemType != 0 && gemType == gameBoard[row][col + 1].type && gemType == gameBoard[row][col + 2].type) {
                    // Add matched items to the list
                    matchedPositions.add(Pair(row, col))
                    matchedPositions.add(Pair(row, col + 1))
                    matchedPositions.add(Pair(row, col + 2))
                }
            }
        }

        // Check for vertical matches
        for (row in 0 until gameBoard.size - 2) {
            for (col in 0 until gameBoard[0].size) {
                val gemType = gameBoard[row][col].type
                if (gemType != 0 && gemType == gameBoard[row + 1][col].type && gemType == gameBoard[row + 2][col].type) {
                    // Add matched items to the list
                    matchedPositions.add(Pair(row, col))
                    matchedPositions.add(Pair(row + 1, col))
                    matchedPositions.add(Pair(row + 2, col))
                }
            }
        }
        return matchedPositions
    }

    var counter = 0

    private fun applyGravityEffect() {
        Log.d("MY", "applyGravityEffect")
        // Iterate through each column in reverse order
        for (col in gameBoard[0].indices.reversed()) {
            // Iterate through each row in reverse order
            for (row in gameBoard.indices.reversed()) {
                // If the current cell is empty, find the nearest non-empty cell above it
                if (gameBoard[row][col] == Gem(0)) {
                    Log.d("MY", "${row},${col} is empty")
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
                        Log.d("MY", "move down ${row},${col}")
                        // Add translation animation for the gem
                        val animator = ObjectAnimator.ofFloat(
                            holderFromPosition.itemView,
                            "translationY",
                            holderToPosition.itemView.y - holderFromPosition.itemView.y
                        )
                        animator.duration =
                            ANIMATION_TIME // Set the duration of the animation (in milliseconds)
                        animator.addListener(object : Animator.AnimatorListener {
                            override fun onAnimationStart(animation: Animator) {
                                counter++
                                holderFromPosition.itemView.translationZ =
                                    1f // You can adjust the value
                                holderToPosition.itemView.translationZ =
                                    -1f // You can adjust the value
                            }

                            override fun onAnimationEnd(animation: Animator) {
                                // Update the game board
                                counter--
                                // Notify the adapter about the item change
                                notifyItemChanged(fromPosition)
                                notifyItemChanged(toPosition)
                                Log.d("MY", "applyGravityEffect ${row},${col}")
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
                }
            }
        }
        Handler(Looper.getMainLooper()).postDelayed({
            handleMatches()
        }, ANIMATION_TIME + 100)
    }


    private fun generateNewGem() = Gem.generateNewGem()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TileHolder {
        return TileHolder(
            ItemGemBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: TileHolder, position: Int) {
        val row = position / gameBoard[0].size
        val col = position % gameBoard[0].size
        val gem = gameBoard[row][col]
        val gemColor = gem.getGemColor()

        holder.gameCell.setBackgroundColor(ContextCompat.getColor(context, gemColor))
        holder.gemBonus.setBackgroundColor(ContextCompat.getColor(context, gem.getGemBonusColor()))
        holder.half.visibility = if (gem.half) View.VISIBLE else View.GONE
        holder.tileNumber.text = Pair(row, col).toString()
        Glide.with(context)
            .load(gem.getIconUri())
            .timeout(60000)
            .into(holder.gemIcon)


        holder.itemView.setOnClickListener {
            // Handle item click and swap logic
            if (allowSelect) {
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

    private fun holderForPosition(position: Pair<Int, Int>): TileHolder {
        val adapterPosition = getBoardPosition(position)
        return gameBoardRecyclerView.findViewHolderForAdapterPosition(adapterPosition) as TileHolder
    }

    inner class TileHolder(val binding: ItemGemBinding) : RecyclerView.ViewHolder(binding.root) {
        val gameCell = binding.gameCell
        val frameView = binding.frameView
        val tileNumber = binding.tileNumber
        val gemIcon = binding.gemIcon
        val gemBonus = binding.gemBonus
        val half = binding.half
    }
}




