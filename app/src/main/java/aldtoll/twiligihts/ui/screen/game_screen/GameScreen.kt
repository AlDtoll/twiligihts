package aldtoll.twiligihts.ui.screen.game_screen

import aldtoll.twiligihts.databinding.FragmentGameScreenBinding
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager

class GameScreen : Fragment(), GemAnimationListener {

    private lateinit var binding: FragmentGameScreenBinding
    private val numRows = 8
    private val numCols = 8
    private val gameBoard = Array(numCols) { IntArray(numCols) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGameScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeGameBoard()
        setupGameBoardRecyclerView()
    }

    private fun initializeGameBoard() {
        do {
            // Populate the game board with initial values (without matches)
            for (i in 0 until numRows) {
                for (j in 0 until numCols) {
                    gameBoard[i][j] = getRandomGemType()
                }
            }
        } while (hasMatches())

        // Update the UI to reflect the initial game board
        updateGameBoardUI()
    }

    private fun updateGameBoardUI() {
        // Notify the adapter that the data set has changed
        binding.gameBoardRecyclerView.adapter?.notifyDataSetChanged()
    }

    private fun getRandomGemType(): Int {
        return (1..3).random()
    }

    private fun setupGameBoardRecyclerView() {
        val adapter = GameBoardAdapter(requireContext(), gameBoard, this)
        val layoutManager = GridLayoutManager(requireContext(), numCols)

        binding.gameBoardRecyclerView.layoutManager = layoutManager
        binding.gameBoardRecyclerView.adapter = adapter
    }

    private fun hasMatches(): Boolean {
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

    override fun animateGemDown(fromPosition: Pair<Int, Int>, toPosition: Pair<Int, Int>) {
        // Implement the animation logic using the positions
        val currentViewHolder = binding.gameBoardRecyclerView.findViewHolderForAdapterPosition(getAdapterPosition(fromPosition))
        val targetViewHolder = binding.gameBoardRecyclerView.findViewHolderForAdapterPosition(getAdapterPosition(toPosition))

        // Use the ViewHolders for animation logic
        currentViewHolder?.itemView?.run {
            animateGemDown(this, targetViewHolder?.itemView)
        }
    }

    override fun animateNewGem(position: Pair<Int, Int>, newGemType: Int) {
        // Implement the animation logic using the position and new gem type
        val newGemViewHolder = binding.gameBoardRecyclerView.findViewHolderForAdapterPosition(getAdapterPosition(position))

        // Use the ViewHolder for animation logic
        newGemViewHolder?.itemView?.run {
            animateNewGem(this)
        }
    }

    private fun getAdapterPosition(position: Pair<Int, Int>): Int {
        return position.first * 8 + position.second
    }

    private fun animateGemDown(currentView: View, targetView: View?) {
        targetView?.let {
            ObjectAnimator.ofFloat(currentView, View.TRANSLATION_Y, it.y - currentView.y)
                .apply {
                    duration = ANIMATION_DURATION
                    start()
                }
        }
    }

    private fun animateNewGem(newGemView: View) {
        newGemView.alpha = 0f
        newGemView.translationY = -ANIMATION_DISTANCE

        ObjectAnimator.ofFloat(newGemView, View.ALPHA, 1f)
            .apply {
                duration = ANIMATION_DURATION
                start()
            }

        ObjectAnimator.ofFloat(newGemView, View.TRANSLATION_Y, 0f)
            .apply {
                duration = ANIMATION_DURATION
                startDelay = ANIMATION_DELAY
                start()
            }
    }

    companion object {
        private const val ANIMATION_DURATION = 300L
        private const val ANIMATION_DISTANCE = 50f
        private const val ANIMATION_DELAY = 0L
    }

}
