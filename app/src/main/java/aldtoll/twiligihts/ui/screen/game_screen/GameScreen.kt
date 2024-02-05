package aldtoll.twiligihts.ui.screen.game_screen

import aldtoll.twiligihts.R
import aldtoll.twiligihts.databinding.FragmentGameScreenBinding
import aldtoll.twiligihts.ext.addChangeAnimation
import aldtoll.twiligihts.ext.checkPossibleMoves
import aldtoll.twiligihts.ext.hasMatches
import aldtoll.twiligihts.model.BattleEvent
import aldtoll.twiligihts.model.Effect
import aldtoll.twiligihts.model.Gem
import aldtoll.twiligihts.model.Perk
import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
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
        setupLogList()
        setupHeroStockList()
        setupHeroHandsList()
        setupHeroStatusList()
        setupPerksList()
        setupHeroBlock()
        setupEnemyHandsList()
        setupEnemyStatusList()
        setupEnemyBlock()
        binding.endTurnButton.setOnClickListener {
            gameScreenViewModel.endTurn()
            binding.coverBoard.visibility = View.GONE
        }
        binding.createBoardAgainButton.setOnClickListener {
            binding.createBoardAgainButton.visibility = View.GONE
            initializeGameBoard()
        }
        gameScreenViewModel.initBattle()
        initializeGameBoard()
        setupGameBoardRecyclerView()
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
        if (!gameBoard.checkPossibleMoves()) {
            binding.createBoardAgainButton.visibility = View.VISIBLE
        }
    }

    private fun setupGameBoardRecyclerView() {
        val adapter = GameBoardAdapter(requireContext(), gameBoard, binding.gameBoardRecyclerView,
            object : GameBoardAdapter.Callback {
                override fun crushGems(removedGems: MutableList<Gem>) {
                    gameScreenViewModel.crushGems(removedGems)
                }

                override fun checkPossibleMoves(checkPossibleMoves: Boolean) {
                    binding.createBoardAgainButton.visibility = if (!checkPossibleMoves) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
                }

                override fun onHandleMatches() {
                    binding.coverBoard.visibility = View.VISIBLE
                    binding.endTurnButton.isEnabled = false
                    binding.createBoardAgainButton.isEnabled = false
                }

                override fun allowEndTurn() {
                    binding.endTurnButton.isEnabled = true
                    binding.createBoardAgainButton.isEnabled = true
                }
            })
        val layoutManager = GridLayoutManager(requireContext(), numCols)

        binding.gameBoardRecyclerView.layoutManager = layoutManager
        binding.gameBoardRecyclerView.adapter = adapter
    }

    private fun setupHeroStockList() {
        val stockList = binding.heroStockList

        val stockAdapter = StockAdapter.newInstance(object : StockAdapter.Callback {
            override fun clickStock() {
                if (binding.heroStatusList.visibility == View.VISIBLE) {
                    binding.heroStatusList.visibility = View.GONE
                    binding.heroHands.visibility = View.VISIBLE
                } else {
                    binding.heroStatusList.visibility = View.VISIBLE
                    binding.heroHands.visibility = View.GONE
                }
            }
        })
        stockList.adapter = stockAdapter
        stockList.layoutManager = LinearLayoutManager(context)
        gameScreenViewModel.stockData().observe(viewLifecycleOwner) {
            stockAdapter.updateData(ArrayList(it.map { stock -> stock.copy() }))
            if (binding.perksBlock.visibility == View.VISIBLE) {
                handsAdapter.refreshPerks()
            }
            gameScreenViewModel.updatePerksState()
        }
    }

    private fun setupLogList() {
        val logList = binding.logList
        val logAdapter = LogAdapter.newInstance()
        logList.adapter = logAdapter
        gameScreenViewModel.logData().observe(viewLifecycleOwner) {
            val arrayListOf = arrayListOf<BattleEvent>()
            arrayListOf.addAll(it)
            logAdapter.updateData(arrayListOf)
            Handler(Looper.getMainLooper()).postDelayed({
                logList.smoothScrollToPosition(logAdapter.itemCount - 1)
            }, 100)
        }
    }

    private lateinit var heroStatusAdapter: StatusAdapter
    private fun setupHeroStatusList() {
        val statusList = binding.heroStatusList
        heroStatusAdapter = StatusAdapter.newInstance()
        statusList.adapter = heroStatusAdapter
    }

    private lateinit var enemyStatusAdapter: StatusAdapter
    private fun setupEnemyStatusList() {
        val statusList = binding.enemyStatusList
        enemyStatusAdapter = StatusAdapter.newInstance()
        statusList.adapter = enemyStatusAdapter
    }

    private lateinit var handsAdapter: HandsAdapter

    private fun setupHeroHandsList() {
        val handsList = binding.heroHands
        handsAdapter = HandsAdapter.newInstance(
            object : HandsAdapter.Callback {
                override fun clickPerk(perk: Perk) {
                    launchSparkAnimation(perk)
                }

                override fun showOrHidePerksForHand(
                    perks: ArrayList<Perk>,
                    notChangeVisibility: Boolean
                ) {
                    val newHandWasClicked =
                        perksAdapter.differ.currentList != handsAdapter.savedPerks
                    perksAdapter.updateData(ArrayList(perks.map { perk -> perk.copy() }))
                    if (newHandWasClicked) {
                        if (binding.perksBlock.visibility == View.GONE) {
                            binding.perksBlock.visibility = View.VISIBLE
                        }
                    } else {
                        if (!notChangeVisibility) {
                            if (binding.perksBlock.visibility == View.VISIBLE) {
                                binding.perksBlock.visibility = View.GONE
                            } else {
                                binding.perksBlock.visibility = View.VISIBLE
                            }
                        }
                    }
                }
            },
            requireContext()
        )
        handsList.adapter = handsAdapter
        handsList.layoutManager = LinearLayoutManager(context)
        gameScreenViewModel.heroHandsData().observe(viewLifecycleOwner)
        {
            handsAdapter.updateData(ArrayList(it.map { hand -> hand.copy() }))
        }
    }

    private fun setupEnemyHandsList() {
        val enemyHands = binding.enemyHands
        val adapter = HandsAdapter.newInstance(
            object : HandsAdapter.Callback {},
            requireContext()
        )
        enemyHands.adapter = adapter
        enemyHands.layoutManager = LinearLayoutManager(context)
        gameScreenViewModel.enemyHandsData().observe(viewLifecycleOwner) {
            adapter.updateData(ArrayList(it.map { hand -> hand.copy() }))
        }
    }

    private lateinit var perksAdapter: PerksAdapter

    private fun setupPerksList() {
        val perksList = binding.perksList
        perksAdapter = PerksAdapter.newInstance(
            object : PerksAdapter.Callback {
                override fun clickPerk(perk: Perk) {
                    launchSparkAnimation(perk)
                }
            },
            requireContext()
        )
        perksList.adapter = perksAdapter
        binding.perksBlock.setOnClickListener {
            if (binding.perksBlock.visibility == View.VISIBLE) {
                binding.perksBlock.visibility = View.GONE
            } else {
                binding.perksBlock.visibility = View.VISIBLE
            }
        }
    }

    private var isSparking = false

    private fun launchSparkAnimation(perk: Perk) {
        if (!isSparking && binding.endTurnButton.isEnabled) {
            isSparking = true
            binding.endTurnButton.isEnabled = false
            binding.createBoardAgainButton.isEnabled = false
            val spark = binding.spark
            spark.setBackgroundColor(resources.getColor(Gem.getColor(perk.prices[0].gemType)))
            spark.visibility = ImageView.VISIBLE
            val sourceView = binding.spark
            val targetView = when (perk.effects[0].target) {
                Effect.EffectTarget.ENEMY -> {
                    binding.enemyBlock
                }

                else -> binding.heroBlock
            }
            val sparkAnimator =
                ObjectAnimator.ofFloat(
                    spark,
                    "translationX",
                    targetView.x - sourceView.x
                )
                    .apply { interpolator = AccelerateInterpolator() }
            val sparkAnimator2 =
                ObjectAnimator.ofFloat(spark, "translationY", targetView.y - sourceView.y)
                    .apply { interpolator = AccelerateInterpolator() }

            val animatorSet = AnimatorSet().apply {
                play(sparkAnimator).with(sparkAnimator2)
                duration = 700
                addListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        binding.spark.animate().translationX(0f).translationY(0f)
                        binding.spark.visibility = ImageView.INVISIBLE
                        gameScreenViewModel.clickPerk(perk)
                        isSparking = false
                        binding.endTurnButton.isEnabled = true
                        binding.createBoardAgainButton.isEnabled = true
                    }

                    override fun onAnimationCancel(animation: Animator) {
                        // Animation canceled
                    }

                    override fun onAnimationRepeat(animation: Animator) {
                        // Animation repeated
                    }
                })
            }
            animatorSet.start()
        }
    }

    private fun setupHeroBlock() {
        binding.personHp.addChangeAnimation()
        gameScreenViewModel.personData().observe(viewLifecycleOwner) {
            val hp = "${it.hp}/${it.maxHp} HP"
            binding.personHp.text = hp
            val sp = "${it.shield} SP"
            binding.personSp.text = sp
            val wound = "${it.wounds}/${it.maxWounds} Ран"
            binding.personWounds.text = wound
            heroStatusAdapter.updateData(ArrayList(it.statuses.map { status -> status.copy() }))
            if (it.hp == 0) {
                finishBattle()
            }
        }
        binding.heroBlock.setOnClickListener {
            if (binding.heroStatusList.visibility == View.VISIBLE) {
                binding.heroStatusList.visibility = View.GONE
                binding.heroHands.visibility = View.VISIBLE
            } else {
                binding.heroStatusList.visibility = View.VISIBLE
                binding.heroHands.visibility = View.GONE
            }
        }
    }

    private fun finishBattle() {
        gameScreenViewModel.finishBattle()
        findNavController().navigate(R.id.finalScreen)
    }

    private fun setupEnemyBlock() {
        binding.enemyHp.addChangeAnimation()
        gameScreenViewModel.enemyData().observe(viewLifecycleOwner) {
            val hp = "${it.hp}/${it.maxHp} HP"
            binding.enemyHp.text = hp
            val sp = "${it.shield} SP"
            binding.enemySp.text = sp
            val wound = "${it.wounds}/${it.maxWounds} Ран"
            binding.enemyWounds.text = wound
            enemyStatusAdapter.updateData(ArrayList(it.statuses.map { status -> status.copy() }))
            if (it.hp == 0) {
                finishBattle()
            }
        }
        binding.enemyBlock.setOnClickListener {
            if (binding.enemyStatusList.visibility == View.VISIBLE) {
                binding.enemyStatusList.visibility = View.GONE
                binding.enemyHands.visibility = View.VISIBLE
            } else {
                binding.enemyStatusList.visibility = View.VISIBLE
                binding.enemyHands.visibility = View.GONE
            }
        }
    }

}
