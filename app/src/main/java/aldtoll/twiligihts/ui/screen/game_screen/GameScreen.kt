package aldtoll.twiligihts.ui.screen.game_screen

import aldtoll.twiligihts.databinding.FragmentGameScreenBinding
import aldtoll.twiligihts.ext.hasMatches
import aldtoll.twiligihts.model.Gem
import aldtoll.twiligihts.model.Perk
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class GameScreen : Fragment() {

    private lateinit var binding: FragmentGameScreenBinding
    private val gameScreenViewModel by viewModels<GameScreenViewModel>()
    private val numRows = 8
    private val numCols = 8
    private val gameBoard = Array(numCols) { Array(numCols) { Gem.generateNewGem() } }

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
        setupStockList()
        setupHandsList()
        setupPersonBlock()
        setupEnemyBlock()
        binding.endTurn.setOnClickListener {
            gameScreenViewModel.endTurn()
        }
        gameScreenViewModel.initPerson()
        gameScreenViewModel.initEnemy()
    }

    private fun initializeGameBoard() {
        do {
            // Populate the game board with initial values (without matches)
            for (row in 0 until numRows) {
                for (col in 0 until numCols) {
                    gameBoard[row][col] = Gem.generateNewGem()
                }
            }
        } while (gameBoard.hasMatches())

        // Update the UI to reflect the initial game board
        updateGameBoardUI()
    }

    private fun updateGameBoardUI() {
        // Notify the adapter that the data set has changed
        binding.gameBoardRecyclerView.adapter?.notifyDataSetChanged()
//        (binding.gameBoardRecyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        binding.gameBoardRecyclerView.itemAnimator?.changeDuration = 0;
    }

    private fun setupGameBoardRecyclerView() {
        val adapter = GameBoardAdapter(requireContext(), gameBoard, binding.gameBoardRecyclerView,
            object : GameBoardAdapter.Callback {
                override fun crushGems(removedGems: MutableList<Gem>) {
                    gameScreenViewModel.crushGems(removedGems)
                }
            })
        val layoutManager = GridLayoutManager(requireContext(), numCols)

        binding.gameBoardRecyclerView.layoutManager = layoutManager
        binding.gameBoardRecyclerView.adapter = adapter
    }

    private lateinit var stockAdapter: StockAdapter

    private fun setupStockList() {
        val stockList = binding.stockList
        stockAdapter = StockAdapter.newInstance()
        stockList.adapter = stockAdapter
        stockList.layoutManager = LinearLayoutManager(context)
        gameScreenViewModel.stockData().observe(viewLifecycleOwner) {
            stockAdapter.updateData(it)
        }
    }

    private lateinit var adapter: HandsAdapter

    private fun setupHandsList() {
        val handsList = binding.handsList
        adapter = HandsAdapter.newInstance(
            object : HandsAdapter.Callback {
                override fun clickPerk(perk: Perk) {
                    gameScreenViewModel.clickPerk(perk)
                }
            },
            requireContext()
        )
        handsList.adapter = adapter
        handsList.layoutManager = LinearLayoutManager(context)
        gameScreenViewModel.handsData().observe(viewLifecycleOwner) {
            adapter.updateData(it)
        }
    }

    private fun setupPersonBlock() {
        gameScreenViewModel.personData().observe(viewLifecycleOwner) {
            val hp = "${it.hp}/${it.maxHp} HP"
            binding.personHp.text = hp
            val sp = "${it.shield} SP"
            binding.personSp.text = sp
            val wound = "${it.wounds}/${it.maxWounds} Ран"
            binding.personWounds.text = wound
        }
    }

    private fun setupEnemyBlock() {
        gameScreenViewModel.enemyData().observe(viewLifecycleOwner) {
            val hp = "${it.hp}/${it.maxHp} HP"
            binding.enemyHp.text = hp
            val sp = "${it.shield} SP"
            binding.enemySp.text = sp
            val wound = "${it.wounds}/${it.maxWounds} Ран"
            binding.enemyWounds.text = wound
        }
    }

}
