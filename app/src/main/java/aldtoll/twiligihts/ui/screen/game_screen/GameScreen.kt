package aldtoll.twiligihts.ui.screen.game_screen

import aldtoll.twiligihts.databinding.FragmentGameScreenBinding
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager

class GameScreen : Fragment() {

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
        val adapter = GameBoardAdapter(requireContext(), gameBoard)
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

}
