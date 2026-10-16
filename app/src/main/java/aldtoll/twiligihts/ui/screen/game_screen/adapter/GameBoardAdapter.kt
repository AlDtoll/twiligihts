package aldtoll.twiligihts.ui.screen.game_screen.adapter

import aldtoll.twiligihts.R
import aldtoll.twiligihts.databinding.ItemGemBinding
import aldtoll.twiligihts.ext.dpToPx
import aldtoll.twiligihts.logic.board.BoardEngine
import aldtoll.twiligihts.logic.board.CascadeStep
import aldtoll.twiligihts.logic.board.MatchRemoval
import aldtoll.twiligihts.logic.board.NewGem
import aldtoll.twiligihts.model.CrushedCell
import aldtoll.twiligihts.model.GameBoard
import aldtoll.twiligihts.model.Gem
import aldtoll.twiligihts.model.MatchGroupInfo
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

private const val ANIMATION_TIME = 400L

class GameBoardAdapter(
    private val context: Context,
    private val gameBoard: GameBoard,
    private val gameBoardRecyclerView: RecyclerView,
    private val callback: Callback,
    private var stopGenerate: Boolean = false,
) : RecyclerView.Adapter<GameBoardAdapter.TileHolder>() {

    interface Callback {

        fun crushGems(
            crushedCells: List<CrushedCell>,
            groups: List<MatchGroupInfo>,
            heroTurn: Boolean
        )
        fun onGemsCrushedForEffect(crushedCells: List<CrushedCell>)
        fun checkPossibleMoves(checkPossibleMoves: Boolean, finishBattleIfNoMatches: Boolean)
        fun onHandleMatches()
        fun allowEndTurn()
        fun makeEnemyTurn()
    }

    private var selectedPosition: Pair<Int, Int>? = null

    /**
     * флаг, который определяет, что сейчас ход героя
     * если ход противника, то гемы не должны заполнять очки и вызывать конец хода
     */
    var heroTurn = true

    private fun getBoardPosition(position: Pair<Int, Int>): Int {
        return position.first * gameBoard.rowSize + position.second
    }

    /**
     * обмен гемов местами
     */
    private fun swapItems(
        position1: Pair<Int, Int>,
        position2: Pair<Int, Int>,
        returnBack: Boolean = false
    ) {
        /**
         * проверка, что гемы соседние и поэтому их можно поменять местами
         * если нет, то ничего не происходит
         */
        if (!BoardEngine.areAdjacent(position1, position2)) {
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
        engine.swap(position1, position2)
        selectedPosition = null
    }


    private fun changeItems(
        position1: Pair<Int, Int>,
        position2: Pair<Int, Int>,
        holder1: TileHolder,
        holder2: TileHolder,
        returnBack: Boolean = false
    ) {
        holder1.hideFrame()
        holder2.hideFrame()
        notifyItemChanged(getBoardPosition(position1))
        notifyItemChanged(getBoardPosition(position2))

        Handler(Looper.getMainLooper()).postDelayed({
            // Check for matches after the swap
            if (engine.hasMatches()) {
                // Handle matches (e.g., remove matched items)
                // You might want to implement a method to remove matched items and update the UI
                handleMatches()
                if (heroTurn) {
                    callback.onHandleMatches()
                }
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


    private val engine = BoardEngine(gameBoard, stopGenerate)

    private var allowSelect = true

    /**
     * обработка совпадения с точки зрения анимаций: логику (что делать дальше) решает движок,
     * здесь анимируется результат шага и планируется следующий
     * а может это конец серии и нужно передать ход игроку
     */
    private fun handleMatches() {
        Log.d("MY", "handleMatches")
        Log.d("MY", "counter  $counter")
        when (val step = engine.nextStep()) {
            /**
             * если движение прекратилось - передаем ход пользователю:
             * можно выбрать клетку, закончить ход и т.д.
             */
            is CascadeStep.Stop -> {
                CRUSH_GENERATED_GEMS = false
                //just wait user
                allowSelect = true
                Log.d("MY", "stop handle")
                callback.checkPossibleMoves(step.hasPossibleMoves, step.finishBattleIfNoMatches)
                callback.allowEndTurn()
                if (!heroTurn) {
                    callback.makeEnemyTurn()
                }
                heroTurn = true
            }

            is CascadeStep.Remove -> {
                allowSelect = false
                removeMatches(step.removal)
            }

            is CascadeStep.Generate -> {
                allowSelect = false
                CRUSH_GENERATED_GEMS = true
                generateNewGems(step.newGems)
            }

            CascadeStep.Stalled -> {
                allowSelect = false
            }
        }
    }

    private fun generateNewGems(newGems: List<NewGem>) {
        for (newGem in newGems) {
            val col = newGem.position.second
            Log.d("MY", "generate ${0},${col}")
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
        Handler(Looper.getMainLooper()).postDelayed({
            applyGravityEffect()
        }, ANIMATION_TIME + 100)
    }

    private fun removeMatches(removal: MatchRemoval) {
        Log.d("MY", "removeMatches")

        for (position in removal.positions) {
            Log.d("MY", "remove ${position.first},${position.second}")
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
        callback.onGemsCrushedForEffect(removal.crushedCells)
        Handler(Looper.getMainLooper()).postDelayed({
            callback.crushGems(removal.crushedCells, removal.groups, heroTurn)
            applyGravityEffect()
            for ((color, count) in removal.removedByType) {
                Log.d("MY", "Removed $count gems of color $color")
            }
        }, ANIMATION_TIME + 100)
    }

    var counter = 0

    private fun applyGravityEffect() {
        Log.d("MY", "applyGravityEffect")
        for (move in engine.applyGravity()) {
            val row = move.to.first
            val col = move.to.second
            val fromPosition = getBoardPosition(move.from)
            val toPosition = getBoardPosition(move.to)
            val holderFromPosition = holderForPosition(move.from)
            val holderToPosition = holderForPosition(move.to)
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
        Handler(Looper.getMainLooper()).postDelayed({
            handleMatches()
        }, ANIMATION_TIME + 100)
    }



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
        val row = position / gameBoard.rowSize
        val col = position % gameBoard.rowSize
        val gem = gameBoard[row, col]

        holder.gameCell.setGemGradient(gem)
        if (gem.bonusType != null) {
            holder.gemBonus.visibility = View.VISIBLE
            holder.gemBonus.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    Gem.getColor(gem.bonusType!!)
                )
            )
        } else {
            holder.gemBonus.visibility = View.GONE
        }
        holder.tileNumber.text = Pair(row, col).toString()
        Glide.with(context)
            .load(Gem.getIconUri(gem.type))
            .placeholder(Gem.getPlaceHolder(gem.type))
            .timeout(60000)
            .into(holder.gemIcon)


        holder.itemView.setOnClickListener {
            // Handle item click and swap logic
            if (allowSelect) {
                if (selectedPosition != null) {
                    if (selectedPosition == Pair(row, col)) {
                        // Clicked on the already selected item, treat it as deselection
                        selectedPosition = null
                        holder.hideFrame()
                    } else {
                        // Clicked on a different item, initiate the swap
                        swapItems(selectedPosition!!, Pair(row, col))
                    }
                } else {
                    // No item is currently selected, select the clicked item
                    selectedPosition = Pair(row, col)
                    holder.showFrame()
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
        return gameBoard.columnSize * gameBoard.rowSize
    }

    private fun holderForPosition(position: Pair<Int, Int>): TileHolder {
        val adapterPosition = getBoardPosition(position)
        return gameBoardRecyclerView.findViewHolderForAdapterPosition(adapterPosition) as TileHolder
    }

    inner class TileHolder(val binding: ItemGemBinding) : RecyclerView.ViewHolder(binding.root) {
        val gameCell = binding.gameCell
        val tileNumber = binding.tileNumber
        val gemIcon = binding.gemIcon
        val gemBonus = binding.gemBonus

        fun showFrame() {
            binding.root.setBackgroundResource(R.drawable.gem_border)
            pulseAnimator.start()
        }

        fun hideFrame() {
            binding.root.setBackgroundResource(0)
            pulseAnimator.cancel()
            binding.root.scaleX = 1f
            binding.root.scaleY = 1f
        }

        private val pulseAnimator by lazy {
            val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.1f, 1f)
            val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.1f, 1f)
            ObjectAnimator.ofPropertyValuesHolder(binding.root, scaleX, scaleY).apply {
                duration = 1000
                repeatCount = ObjectAnimator.INFINITE
                repeatMode = ObjectAnimator.REVERSE
            }
        }
    }

    companion object {
        var CRUSH_GENERATED_GEMS = false
    }
}

fun ImageView.setGemGradient(gem: Gem) {
    val context = this.context
    val typeColor =
        ContextCompat.getColor(
            context,
            Gem.getColor(gem.type)
        )
    var extraTypeColor = typeColor
    gem.extraType?.run {
        extraTypeColor = ContextCompat.getColor(
            context,
            Gem.getColor(this)
        )
    }

    val colors =
        intArrayOf(typeColor, extraTypeColor) // Два цвета для градиента


    // Создание объекта градиента
    val gradientDrawable = GradientDrawable(
        GradientDrawable.Orientation.LEFT_RIGHT, // Ориентация градиента
        colors
    )
    val radius = 10.dpToPx.toFloat()
    gradientDrawable.cornerRadii = (floatArrayOf(
        radius, radius, radius, radius, radius, radius, radius, radius
    ))
    // Применение градиента к ImageView
    this.setImageDrawable(gradientDrawable)
}




