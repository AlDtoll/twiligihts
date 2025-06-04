package aldtoll.twiligihts.ui.screen.game_screen

import aldtoll.twiligihts.R
import aldtoll.twiligihts.databinding.FragmentGameScreenBinding
import aldtoll.twiligihts.ext.addChangeAnimation
import aldtoll.twiligihts.logic.ApplyFunctionExecutor
import aldtoll.twiligihts.logic.PerkExecutor
import aldtoll.twiligihts.model.BattleEvent
import aldtoll.twiligihts.model.BattleSettings
import aldtoll.twiligihts.model.BattleSettings.Companion.SHOW_ENEMY_ANIMATION
import aldtoll.twiligihts.model.BattleSettings.Companion.SHOW_HERO_ANIMATION
import aldtoll.twiligihts.model.BattleSettings.Companion.SHOW_HERO_PORTRAIT
import aldtoll.twiligihts.model.ExecutedPerk
import aldtoll.twiligihts.model.GameBoard
import aldtoll.twiligihts.model.Gem
import aldtoll.twiligihts.model.Hand
import aldtoll.twiligihts.model.Perk
import aldtoll.twiligihts.model.Perk.Companion.EMPTY_PERK
import aldtoll.twiligihts.model.Sector
import aldtoll.twiligihts.model.Status
import aldtoll.twiligihts.model.effects.Effect
import aldtoll.twiligihts.ui.screen.game_screen.adapter.GameBoardAdapter
import aldtoll.twiligihts.ui.screen.game_screen.adapter.HandsAdapter
import aldtoll.twiligihts.ui.screen.game_screen.adapter.LogAdapter
import aldtoll.twiligihts.ui.screen.game_screen.adapter.PerksAdapter
import aldtoll.twiligihts.ui.screen.game_screen.adapter.StatusAdapter
import aldtoll.twiligihts.ui.screen.game_screen.adapter.StockAdapter
import aldtoll.twiligihts.ui.screen.game_screen.logs.LogBottomSheetDialog
import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random


@AndroidEntryPoint
class GameScreen : Fragment() {

    private lateinit var binding: FragmentGameScreenBinding
    private val viewModel by viewModels<GameScreenViewModel>()

    @Inject
    lateinit var gameBoard: GameBoard

    @Inject
    lateinit var applyFunctionExecutor: ApplyFunctionExecutor

    private lateinit var sectorSelectionView: SectorSelectionView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGameScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Получение аргумента
        val continueGame = arguments?.getBoolean("continue", false)
            ?: false
        createTimerBlock()
        setupLogList()
        setupHeroStockList()
        setupHeroHandsList()
        setupHeroStatusList()
        setupPerksList()
        setupHeroBlock()
        setupEnemyHandsList()
        setupEnemyStatusList()
        setupEnemyBlock()
        //todo снова не работает - если закончить бой, то при следующем заходе снова спрашивает закончить бой
        // надо добавить обновление не спрашивать после показа диалога goToFinishScreenInteractor.update(Pair(false, false))
        // еще трати очки даже если откажешься
        viewModel.eventGoToFinishScreen().observe(viewLifecycleOwner) {
            if (it.first) {
                if (it.second) {
                    askAboutFinishBattle()
                } else {
                    showInfoAboutFinishBattle()
                }
            }
        }
        viewModel.coverBoardData().observe(viewLifecycleOwner) {
            binding.coverBoard.visibility = it
        }
        binding.endTurnButton.setOnClickListener {
            isTurnTimerRunning = false
            turnTimer.cancel()
            viewModel.updateCoverBoard(View.VISIBLE)
            viewModel.endTurn()
        }
        binding.createBoardAgainButton.setOnClickListener {
            binding.createBoardAgainButton.visibility = View.GONE
            if (finishBattleIfNoMatches) {
                showInfoAboutFinishBattle("Нет ходов")
            } else {
                initializeGameBoard()
            }
        }
        binding.sufferCheckbox.setOnCheckedChangeListener { _, isChecked ->
            PerkExecutor.ENABLE_DODGE = isChecked
        }
        if (continueGame) {
            turnTimeElapsedInMillis = viewModel.timerValue() * TIMER_TICK
        } else {
            viewModel.initBattle()
            gameBoard.initializeBoard()
        }
        viewModel.pushData().observe(viewLifecycleOwner) {
            if (it != null) {
                if (it.data.containsKey("message")) {
                    val message = it.data["message"]
                    message?.run {
                        viewModel.addMessage(message)
                    }
                }
            }
        }
        updateGameBoardUI()
        setupGameBoardRecyclerView()
        if (SHOW_HERO_ANIMATION) {
            binding.heroIcon.visibility = View.VISIBLE
            /**
             * установка фигурки героя в начальное положение
             */
            stopHeroGifAnimation(R.raw.rook_attack)
        }
        if (SHOW_HERO_PORTRAIT) {
            binding.heroPortrait.visibility = View.VISIBLE
        }
        if (SHOW_ENEMY_ANIMATION) {
            binding.enemyIcon.visibility = View.VISIBLE
            /**
             * установка фигурки противника в начальное положение
             */
            stopEnemyGifAnimation(R.raw.enemy_attack)
        }
        sectorSelectionView = binding.enemySectorsList
        // Создаем список секторов
        val sectors = listOf(
            Sector(
                1,
                "Цель",
                R.drawable.ic_heart,
                R.drawable.selected_tile_background,
                Perk(
                    name = "Целиться",
                    effects = arrayListOf(
                        Effect.EditStatus(
                            status = Status(
                                name = "Цель: Уязвимость",
                                type = Status.StatusType.INFO,
                                value = 1
                            ),
                            target = Effect.EffectTarget.ENEMY
                        ),
                        Effect.EditStatus(
                            status = Status(
                                name = "Цель: Торс",
                                type = Status.StatusType.INFO,
                                value = 0
                            ),
                            target = Effect.EffectTarget.ENEMY
                        )
                    )
                )
            ),
            Sector(
                2,
                "Торс",
                R.drawable.ic_armor,
                R.drawable.selected_tile_background,
                Perk(
                    name = "Бить в тело",
                    effects = arrayListOf(
                        Effect.EditStatus(
                            status = Status(
                                name = "Цель: Уязвимость",
                                type = Status.StatusType.INFO,
                                value = 0
                            ),
                            target = Effect.EffectTarget.ENEMY
                        ),
                        Effect.EditStatus(
                            status = Status(
                                name = "Цель: Торс",
                                type = Status.StatusType.INFO,
                                value = 1
                            ),
                            target = Effect.EffectTarget.ENEMY
                        )
                    )
                ),
            ),
        )
        // Настройка секторов
        sectorSelectionView.setupSectors(sectors)
        // Обработчик выбора сектора
        sectorSelectionView.setOnSectorSelectedListener { sector ->
            viewModel.executePerk(sector.perk)
        }
    }

    override fun onStop() {
        super.onStop()
        turnTimer.cancel()
    }

    private fun loadHeroGif(perk: Perk, isHeroTarget: Boolean = false) {
        if (SHOW_HERO_ANIMATION) {
            var id = 0
            if (perk.gif != null) {
                id = resources.getIdentifier(
                    perk.gif, "raw", activity?.packageName
                )
            }
            if (isHeroTarget) {
                id = if (sp != 0) {
                    R.raw.rook_touched
                } else {
                    R.raw.rook_hited
                }
            }
            val gifId = if (id != 0) {
                id
            } else {
                R.raw.rook_attack
            }
            if (id != 0) {
                Glide.with(this)
                    .asGif()  // Load as animated GIF
                    .load(gifId)  // Call your GIF here (url, raw, etc.)
                    .listener(object : RequestListener<GifDrawable> {


                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<GifDrawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            return false
                        }

                        override fun onResourceReady(
                            resource: GifDrawable?,
                            model: Any?,
                            target: Target<GifDrawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            resource?.setLoopCount(1)
                            resource?.registerAnimationCallback(
                                object : Animatable2Compat.AnimationCallback() {
                                    override fun onAnimationEnd(drawable: Drawable) {
                                        binding.heroIcon.post {
                                            stopHeroGifAnimation(R.raw.rook_attack)
                                        }
                                    }
                                })
                            return false
                        }
                    })
                    .into(binding.heroIcon)
            } else {
                stopHeroGifAnimation(gifId)
            }
        }
    }

    private fun loadEnemyGif(perk: Perk, isEnemyTarget: Boolean = false) {
        if (SHOW_ENEMY_ANIMATION) {
            var id = 0
            if (perk.gif != null) {
                id = resources.getIdentifier(
                    perk.gif, "raw", activity?.packageName
                )
            }
            if (isEnemyTarget) {
                id = if (sp != 0) {
                    R.raw.rook_touched
                } else {
                    R.raw.rook_hited
                }
            }
            val gifId = if (id != 0) {
                id
            } else {
                R.raw.rook_attack
            }
            if (id != 0) {
                Glide.with(this)
                    .asGif()  // Load as animated GIF
                    .load(gifId)  // Call your GIF here (url, raw, etc.)
                    .listener(object : RequestListener<GifDrawable> {


                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<GifDrawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            return false
                        }

                        override fun onResourceReady(
                            resource: GifDrawable?,
                            model: Any?,
                            target: Target<GifDrawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            resource?.setLoopCount(1)
                            resource?.registerAnimationCallback(
                                object : Animatable2Compat.AnimationCallback() {
                                    override fun onAnimationEnd(drawable: Drawable) {
                                        binding.heroIcon.post {
                                            stopHeroGifAnimation(R.raw.rook_attack)
                                        }
                                    }
                                })
                            return false;
                        }
                    })
                    .into(binding.enemyIcon)
            } else {
                stopHeroGifAnimation(gifId)
            }
        }
    }

    private fun stopHeroGifAnimation(gifId: Int) {
        binding.heroIcon.post {
            Glide.with(this)
                .asBitmap()  // Load as static image
                .load(gifId)  // Call your GIF here (url, raw, etc.)
                .into(binding.heroIcon)
        }
    }

    private fun stopEnemyGifAnimation(gifId: Int) {
        binding.enemyIcon.post {
            Glide.with(this)
                .asBitmap()  // Load as static image
                .load(gifId)  // Call your GIF here (url, raw, etc.)
                .into(binding.enemyIcon)
        }
    }

    private fun initializeGameBoard() {
        gameBoard.initializeBoard()
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

    private var finishBattleIfNoMatches = false
    private lateinit var gameBoardAdapter: GameBoardAdapter

    private fun setupGameBoardRecyclerView() {
        gameBoardAdapter = GameBoardAdapter(
            requireContext(), gameBoard, binding.gameBoardRecyclerView,
            object : GameBoardAdapter.Callback {
                override fun crushGems(removedGems: MutableList<Gem>, heroTurn: Boolean) {
                    viewModel.crushGems(removedGems, heroTurn)
                }

                override fun checkPossibleMoves(
                    checkPossibleMoves: Boolean,
                    finishBattleIfNoMatches: Boolean
                ) {
                    binding.createBoardAgainButton.visibility = if (!checkPossibleMoves) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
                    this@GameScreen.finishBattleIfNoMatches = finishBattleIfNoMatches
                    if (finishBattleIfNoMatches) {
                        binding.createBoardAgainButton.setText(R.string.end_battle)
                    } else {
                        binding.createBoardAgainButton.setText(R.string.new_desk)
                    }
                }

                override fun onHandleMatches() {
                    viewModel.updateCoverBoard(View.VISIBLE)
                    binding.endTurnButton.isEnabled = false
                    isTurnTimerRunning = false
                    turnTimer.cancel()
                    // Calculate time spent for the turn
                    val timeSpentForTurnInSeconds = turnTimeElapsedInMillis / 1000
                    viewModel.logTime(timeSpentForTurnInSeconds)
                    binding.createBoardAgainButton.isEnabled = false
                }

                override fun allowEndTurn() {
                    viewModel.logPoints(gameBoardAdapter.heroTurn)
                    binding.endTurnButton.isEnabled = true
                    binding.createBoardAgainButton.isEnabled = true
                }

                override fun makeEnemyTurn() {
                    viewModel.startEnemyTurn()
                }
            },
            BattleSettings.STOP_GENERATE
        )
        val layoutManager = GridLayoutManager(requireContext(), gameBoard.columnSize)

        binding.gameBoardRecyclerView.layoutManager = layoutManager
        binding.gameBoardRecyclerView.adapter = gameBoardAdapter
    }

    var turnTimeElapsedInMillis: Long = 0
    var isTurnTimerRunning: Boolean = false
    val turnTimer =
        object :
            CountDownTimer(
                Long.MAX_VALUE,
                TIMER_TICK
            ) { // CountDownTimer with maximum value
            override fun onTick(millisUntilFinished: Long) {
                if (isTurnTimerRunning) {
                    turnTimeElapsedInMillis += TIMER_TICK
                    val seconds = turnTimeElapsedInMillis / TIMER_TICK
                    binding.turnTime.text =
                        seconds.toString()
                    Log.d("APP", "onTick $this seconds=$seconds")
                    viewModel.checkTime(seconds.toInt())
                }
            }

            override fun onFinish() {
                // Timer will never finish in this case
            }
        }

    private fun createTimerBlock() {
        turnTimer.cancel()
        startTurnTimer()

        viewModel.startTurnAgainEventData().observe(viewLifecycleOwner) {
            startTurnTimer()
            viewModel.updateCoverBoard(View.GONE)
        }
    }

    private fun startTurnTimer() {
        if (!isTurnTimerRunning) {
            turnTimeElapsedInMillis = 0
            isTurnTimerRunning = true
            binding.turnTime.text = "0"
            turnTimer.start()
        }
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
        viewModel.stockData().observe(viewLifecycleOwner) {
            stockAdapter.updateData(ArrayList(it.map { stock -> stock.copy() }))
            if (binding.perksBlock.visibility == View.VISIBLE) {
                handsAdapter.refreshPerks()
            }
            viewModel.updatePerksState()
        }
    }

    private fun setupLogList() {
        val logList = binding.logList
        val logAdapter = LogAdapter.newInstance(object : LogAdapter.Callback {
            override fun clickLog() {
                val logBottomSheetDialog = LogBottomSheetDialog.newInstance()
                logBottomSheetDialog.show(
                    parentFragmentManager,
                    LogBottomSheetDialog::class.java.simpleName
                )
            }
        })
        logList.adapter = logAdapter
        viewModel.logData().observe(viewLifecycleOwner) {
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
            requireContext(),
            binding.heroHands
        )
        handsList.adapter = handsAdapter
        handsList.layoutManager = LinearLayoutManager(context)
        viewModel.heroHandsData().observe(viewLifecycleOwner)
        {
            handsAdapter.updateData(ArrayList(it.map { hand -> hand.copy() }))
        }
    }

    private lateinit var enemyHandsAdapter: HandsAdapter

    private fun setupEnemyHandsList() {
        val enemyHands = binding.enemyHands
        enemyHandsAdapter = HandsAdapter.newInstance(
            object : HandsAdapter.Callback {
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
            requireContext(),
            binding.enemyHands
        )
        enemyHands.adapter = enemyHandsAdapter
        enemyHands.layoutManager = LinearLayoutManager(context)
        viewModel.enemyHandsData().observe(viewLifecycleOwner) {
            enemyHandsAdapter.updateData(ArrayList(it.map { hand -> hand.copy() }))
        }
        /**
         * применение навыков противником
         */
        viewModel.enemySparkData().observe(viewLifecycleOwner) {
            //todo костыль
            val executedPerk = if (finishDialogIsShowing) {
                ExecutedPerk(EMPTY_PERK, Hand())
            } else {
                it
            }
            if (executedPerk.perk.name != Perk.EMPTY) {
                /**
                 * если эксзекутор передал последний навык,
                 * то нужно завешить действия противника
                 */
                if (executedPerk.perk.name == Perk.LAST) {
                    viewModel.afterEnemyActions()
                    binding.endTurnButton.isEnabled = true
                    binding.perksBlock.visibility = View.GONE
                } else {
                    /**
                     * если нет, то нужно найти следующий по порядку навык
                     */
                    perksAdapter.updateData(arrayListOf())
                    handsAdapter.savedPerks = null
                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            binding.perksBlock.visibility = View.VISIBLE
                            val perk = executedPerk.perk
                            if (perk.show && perk.enable) {
                                val numberForCompareWithPerkProbability = Random.nextInt(0, 101)

                                /**
                                 * дефолтная вероятность применения навыка 100%
                                 */
                                val probability = if (perk.pFunc != null) {
                                    perk.pFunc.run {
                                        val functionValue =
                                            applyFunctionExecutor.execute(this@run, false)
                                        perk.probability + functionValue
                                    }
                                } else {
                                    perk.probability
                                }
                                if (numberForCompareWithPerkProbability <= probability) {
                                    viewModel.messageAboutUsedPerk(perk, false)
                                    val findHandPosition =
                                        enemyHandsAdapter.findHandPosition(executedPerk.fromHand)
                                    binding.enemyHands.smoothScrollToPosition(findHandPosition)
                                    Handler(Looper.getMainLooper()).postDelayed(
                                        {
                                            launchEnemySparkAnimation(
                                                executedPerk.perk,
                                                executedPerk.fromHand
                                            )
                                        },
                                        //todo если много рук, то вылетает, т.к.запускает спарк раньше окончания прокрутки
                                        500
                                    )
                                } else {
                                    viewModel.callNextPerk(executedPerk.perk)
                                }
                            } else {
                                viewModel.callNextPerk(executedPerk.perk)
                            }
                        },
                        100
                    )
                }
            }
        }
    }

    private lateinit var perksAdapter: PerksAdapter

    private fun setupPerksList() {
        val perksList = binding.perksList
        perksAdapter = PerksAdapter.newInstance(
            object : PerksAdapter.Callback {
                override fun clickPerk(perk: Perk, isHeroPerk: Boolean) {
                    if (isHeroPerk) {
                        viewModel.messageAboutUsedPerk(perk, true)
                        launchHeroSparkAnimation(perk)
                    } else {
//                        gameScreenViewModel.messageAboutUsedPerk(perk, false)
//                        launchEnemySparkAnimation(perk)
                    }
                }
            },
            requireContext(),
            binding.perksList
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


    /**
     * применение навыка героем
     */
    private fun launchHeroSparkAnimation(perk: Perk) {
        if (!isSparking && binding.endTurnButton.isEnabled) {
            loadHeroGif(perk)
            isSparking = true
            binding.endTurnButton.isEnabled = false
            binding.createBoardAgainButton.isEnabled = false
            val findHolder = perksAdapter.findHolder(perk)
            val spark = if (findHolder != null) {
                // Create a copy of the ImageView
                val imageViewCopy = ImageView(context)
                val perkIcon = findHolder.first.binding.perkIcon
                imageViewCopy.setImageDrawable(perkIcon.drawable)
                val location = IntArray(2)
                perkIcon.getLocationOnScreen(location)
                val startX = location[0].toFloat()
                val startY = location[1].toFloat()

                val layoutParams = ViewGroup.LayoutParams(
                    perkIcon.width,
                    perkIcon.height
                )
                imageViewCopy.layoutParams = layoutParams

                // Calculate the position of imageViewCopy relative to the root layout
                val rootLayoutLocation = IntArray(2)
                binding.root.getLocationOnScreen(rootLayoutLocation)
                val copyX = startX - rootLayoutLocation[0]
                val copyY = startY - rootLayoutLocation[1]

                // Add the copy to the root layout of your activity/fragment
                // Set the position of imageViewCopy
                imageViewCopy.x = copyX
                imageViewCopy.y = copyY
                binding.root.addView(imageViewCopy)
                imageViewCopy
            } else {
                binding.spark
            }
            val gemType = if (perk.prices.isNotEmpty()) {
                perk.prices[0].gemType
            } else {
                1
            }
            spark.setColorFilter(
                ContextCompat.getColor(
                    binding.root.context,
                    Gem.getColor(gemType)
                ), android.graphics.PorterDuff.Mode.SRC_IN
            )
            val iconForSpark = Perk.PERK_MAP[perk.icon]
            if (iconForSpark.isNullOrEmpty()) {
                Gem.getIconUri(gemType)
            }
            Glide.with(binding.root.context)
                .load(iconForSpark)
                .placeholder(Gem.getPlaceHolder(gemType))
                .timeout(60000)
                .into(spark)
            spark.visibility = ImageView.VISIBLE
            val sourceView = spark
            val attackEffect = perk.effects.find { it.name == Effect.EffectName.ATTACK }
            val targetView: View?
            if (attackEffect != null) {
                targetView = when (attackEffect.target) {
                    Effect.EffectTarget.ENEMY -> {
                        binding.enemyBlock
                    }

                    else -> {
                        binding.heroBlock
                    }
                }
            } else {
                targetView = when (perk.effects[0].target) {
                    Effect.EffectTarget.ENEMY -> {
                        binding.enemyBlock
                    }

                    else -> {
                        binding.heroBlock
                    }
                }
            }
            /**
             * не понимаю почему, но при копировании вьюхи с карточки навыка приходится использовать абсолютное перемещие,
             * а при уже заданной в разметке вьюхе - относительное
             * загадка
             */
            val translationX = if (findHolder != null) {
                targetView.x
            } else {
                targetView.x - sourceView.x
            }
            val translationY = if (findHolder != null) {
                targetView.y
            } else {
                targetView.y - sourceView.y
            }
            val sparkAnimator =
                ObjectAnimator.ofFloat(
                    spark,
                    "translationX",
                    translationX
                )
                    .apply { interpolator = AccelerateInterpolator() }
            val sparkAnimator2 =
                ObjectAnimator.ofFloat(spark, "translationY", translationY)
                    .apply { interpolator = AccelerateInterpolator() }

            val animatorSet = AnimatorSet().apply {
                play(sparkAnimator).with(sparkAnimator2)
                duration = 700
                addListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        spark.animate().translationX(0f).translationY(0f)
                        spark.visibility = ImageView.INVISIBLE
                        val effectForCustomMessage =
                            perk.effects.find { it is Effect.Info && it.title != null }
                        if (effectForCustomMessage != null) {
                            val inputEditText = EditText(requireContext()).apply {
                                hint = "Свое действие"
                                setSingleLine(false)
                            }

                            val dialog = MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Описание действия")
                                .setMessage((effectForCustomMessage as Effect.Info).title)
                                .setView(inputEditText)
                                .setPositiveButton("Подтвердить") { _, _ ->
                                    val description = inputEditText.text.toString()
                                    CUSTOM_MESSAGE = description
                                    viewModel.executePerk(perk)
                                }
                                .create()

                            // Запрещаем закрытие диалога
                            dialog.setCancelable(false)
                            dialog.setCanceledOnTouchOutside(false)

                            dialog.show()
                        } else {
                            viewModel.executePerk(perk)
                        }
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

    private fun launchEnemySparkAnimation(perk: Perk, hand: Hand) {
        binding.endTurnButton.isEnabled = false
        val findHolder = enemyHandsAdapter.findHolder(hand)
        val spark = if (findHolder != null) {
            // Create a copy of the ImageView
            loadEnemyGif(perk)
            val imageViewCopy = ImageView(context)
            imageViewCopy.setImageDrawable(findHolder.first.binding.perkIcon.drawable)
            val location = IntArray(2)
            findHolder.first.binding.perkIcon.getLocationOnScreen(location)
            val startX = location[0].toFloat()
            val startY = location[1].toFloat()

            val layoutParams = ViewGroup.LayoutParams(
                findHolder.first.binding.perkIcon.width,
                findHolder.first.binding.perkIcon.height
            )
            imageViewCopy.layoutParams = layoutParams

            // Calculate the position of imageViewCopy relative to the root layout
            val rootLayoutLocation = IntArray(2)
            binding.root.getLocationOnScreen(rootLayoutLocation)
            val copyX = startX - rootLayoutLocation[0]
            val copyY = startY - rootLayoutLocation[1]

            // Add the copy to the root layout of your activity/fragment
            // Set the position of imageViewCopy
            imageViewCopy.x = copyX
            imageViewCopy.y = copyY
            binding.root.addView(imageViewCopy)
            imageViewCopy
        } else {
            binding.enemySpark
        }
        val gemType = if (perk.prices.isNotEmpty()) {
            perk.prices[0].gemType
        } else {
            hand.gemType
        }
        spark.setColorFilter(
            ContextCompat.getColor(
                binding.root.context,
                Gem.getColor(gemType)
            ), android.graphics.PorterDuff.Mode.SRC_IN
        )
        Glide.with(binding.root.context)
            .load(Gem.getIconUri(gemType))
            .placeholder(Gem.getPlaceHolder(gemType))
            .timeout(60000)
            .into(spark)
        spark.visibility = ImageView.VISIBLE
        val sourceView = spark
        val attackEffect = perk.effects.find { it.name == Effect.EffectName.ATTACK }
        val targetView: View?
        if (attackEffect != null) {
            targetView = when (attackEffect.target) {
                Effect.EffectTarget.ENEMY -> {
                    binding.enemyBlock
                }

                else -> {
                    binding.heroBlock
                }
            }
        } else {
            targetView = when (perk.effects[0].target) {
                Effect.EffectTarget.ENEMY -> {
                    binding.enemyBlock
                }

                else -> {
                    binding.heroBlock
                }
            }
        }
        val translationX = if (findHolder?.first != null) {
            targetView.x
        } else {
            targetView.x - sourceView.x
        }
        val translationY = if (findHolder?.first != null) {
            targetView.y
        } else {
            targetView.y - sourceView.y
        }
        val sparkAnimator =
            ObjectAnimator.ofFloat(
                spark,
                "translationX",
                translationX
            )
                .apply { interpolator = AccelerateInterpolator() }
        val sparkAnimator2 =
            ObjectAnimator.ofFloat(spark, "translationY", translationY)
                .apply { interpolator = AccelerateInterpolator() }

        val animatorSet = AnimatorSet().apply {
            play(sparkAnimator).with(sparkAnimator2)
            duration = 1500
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {
                }

                override fun onAnimationEnd(animation: Animator) {
                    spark.animate().translationX(0f).translationY(0f).duration = 0
                    spark.visibility = ImageView.INVISIBLE
                    if (perk.effects.any { it.name == Effect.EffectName.ATTACK }) {
                        loadHeroGif(perk, true)
                    }
                    viewModel.executePerk(perk, false)
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

    var sp = 0

    private fun setupHeroBlock() {
        binding.personHp.addChangeAnimation()
        binding.personSp.addChangeAnimation(Color.BLUE)
        viewModel.personData().observe(viewLifecycleOwner) {
            it.name?.run {
                binding.personName.text = this
                binding.personName.visibility = View.VISIBLE
            }
            val hp = "${it.hp}/${it.maxHp} HP"
            val hpPercent = " ${it.hp * 100 / it.maxHp}%"
            val hpText = hp + hpPercent
            binding.personHp.text = hpText
            val sp = "${it.shield}"
            this.sp = it.shield
            binding.personSp.text = sp
            val hits = "${it.hits}/${it.touches}"
            binding.personHits.text = hits
            val wound = "${it.wounds}/${it.maxWounds} Ран"
            binding.personWounds.text = wound
            heroStatusAdapter.updateData(ArrayList(it.statuses.map { status -> status.copy() }))
            if (it.hp == 0) {
                showInfoAboutFinishBattle("Герой повержен")
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
        viewModel.heroResourcesData().observe(viewLifecycleOwner) {
            var resourcesText = ""
            it.forEach {
                resourcesText += "${it.name} ${it.amount}\n"
            }
            binding.heroResources.text = resourcesText.substringBeforeLast("\n")
        }
    }

    private fun goToFinishScreen() {
        findNavController().navigate(R.id.finalScreen)
    }

    private fun askAboutFinishBattle() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Закончить бой?")
            .setPositiveButton("Да") { dialog, id ->
                goToFinishScreen()
            }
        val alertDialog = builder.create()
        alertDialog.show()
    }

    private var finishDialogIsShowing = false

    private fun showInfoAboutFinishBattle(message: String = "") {
        if (!finishDialogIsShowing) {
            FinishDialog(message) {
                finishDialogIsShowing = false
                goToFinishScreen()
            }.show(
                childFragmentManager, GameScreen::class.java.simpleName
            )
        }
        finishDialogIsShowing = true
    }

    class FinishDialog(
        val message: String,
        val listener: () -> Unit
    ) : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
            AlertDialog.Builder(requireContext())
                .setTitle("Бой закончен")
                .setMessage(message)
                .setPositiveButton("Понятно") { _, _ -> }
                .create()

        override fun onDismiss(dialog: DialogInterface) {
            listener.invoke()
            super.onDismiss(dialog)
        }
    }

    private fun setupEnemyBlock() {
        binding.enemyHp.addChangeAnimation()
        binding.enemySp.addChangeAnimation(Color.BLUE)
        viewModel.enemyData().observe(viewLifecycleOwner) {
            it.name?.run {
                binding.enemyName.text = this
                binding.enemyName.visibility = View.VISIBLE
            }
            it.info?.run {
                binding.enemyInfo.text = this
                binding.enemyInfo.visibility = View.VISIBLE
            }
            val hp = "${it.hp}/${it.maxHp}"
            val hpPercent = "${it.hp * 100 / it.maxHp}%"
            val hpText = hp + "\n" + hpPercent
            binding.enemyHp.text = hpText
            val sp = "${it.shield} SP"
            binding.enemySp.text = sp
            val hits = "${it.hits}/${it.touches}"
            binding.enemyHits.text = hits
            val wound = "${it.wounds}/${it.maxWounds} Ран"
            binding.enemyWounds.text = wound
            enemyStatusAdapter.updateData(ArrayList(it.statuses.map { status -> status.copy() }))
            if (it.hp == 0) {
                showInfoAboutFinishBattle("Противник повержен")
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

        lifecycleScope.launch {
            viewModel.enemyMoveData().collect { value ->
                gameBoardAdapter.heroTurn = false
                makeMove()
            }
        }
    }

    private fun makeMove() {
        val findPossibleMoves = gameBoard.findPossibleMoves()
        if (findPossibleMoves.isNotEmpty()) {
            val numberOfPossibleMove = Random.nextInt(0, findPossibleMoves.size)
            val move = findPossibleMoves[numberOfPossibleMove]
            val from = move.from
            val to = move.to
            viewModel.messageAboutEvaluateMove()
            val seconds = Random.nextInt(0, 4)
            Handler(Looper.getMainLooper()).postDelayed({
                viewModel.messageAboutMakeMove()
                binding.gameBoardRecyclerView.findViewHolderForAdapterPosition(to.first * gameBoard.rowSize + to.second)?.itemView?.performClick()
            }, seconds * 1000L)
            Handler(Looper.getMainLooper()).postDelayed({
                binding.gameBoardRecyclerView.findViewHolderForAdapterPosition(from.first * gameBoard.rowSize + from.second)?.itemView?.performClick()
            }, 1500L)
        }
    }

    companion object {
        const val TIMER_TICK = 1000L
        var CUSTOM_MESSAGE = "Прочерк"
    }

}
