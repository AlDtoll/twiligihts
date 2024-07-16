package aldtoll.twiligihts.ui.screen.game_screen

import aldtoll.twiligihts.R
import aldtoll.twiligihts.databinding.FragmentGameScreenBinding
import aldtoll.twiligihts.ext.addChangeAnimation
import aldtoll.twiligihts.ext.checkPossibleMoves
import aldtoll.twiligihts.ext.findPossibleMoves
import aldtoll.twiligihts.ext.hasMatches
import aldtoll.twiligihts.logic.PerkExecutor
import aldtoll.twiligihts.model.BattleEvent
import aldtoll.twiligihts.model.BattleSettings
import aldtoll.twiligihts.model.BattleSettings.Companion.SHOW_HERO_ANIMATION
import aldtoll.twiligihts.model.Effect
import aldtoll.twiligihts.model.ExecutedPerk
import aldtoll.twiligihts.model.Gem
import aldtoll.twiligihts.model.Hand
import aldtoll.twiligihts.model.Perk
import aldtoll.twiligihts.model.Perk.Companion.EMPTY_PERK
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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.random.Random


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
        gameScreenViewModel.eventGoToFinishScreen().observe(viewLifecycleOwner) {
            if (it.first) {
                if (it.second) {
                    askAboutFinishBattle()
                } else {
                    showInfoAboutFinishBattle()
                }
            }
        }
        binding.endTurnButton.setOnClickListener {
            isTurnTimerRunning = false
            turnTimer.cancel()
            binding.coverBoard.visibility = View.VISIBLE
            gameScreenViewModel.endTurn()
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
        gameScreenViewModel.initBattle()
        gameScreenViewModel.pushData().observe(viewLifecycleOwner) {
            if (it != null) {
                if (it.data.containsKey("message")) {
                    val message = it.data["message"]
                    message?.run {
                        gameScreenViewModel.addMessage(message)
                    }
                }
            }
        }
        initializeGameBoard()
        setupGameBoardRecyclerView()
        if (SHOW_HERO_ANIMATION) {
            binding.heroIcon.visibility = View.VISIBLE
            /**
             * установка фигурки в начальное положение
             */
            stopGifAnimation(R.raw.rook_attack)
        }
    }


    private fun loadGif(perk: Perk, isHeroTarget: Boolean = false) {
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
                                        stopGifAnimation(R.raw.rook_attack)
                                    }
                                })
                            return false;
                        }
                    })
                    .into(binding.heroIcon)
            } else {
                stopGifAnimation(gifId)
            }
        }
    }

    private fun stopGifAnimation(gifId: Int) {
        Glide.with(this)
            .asBitmap()  // Load as static image
            .load(gifId)  // Call your GIF here (url, raw, etc.)
            .into(binding.heroIcon)
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

    private var finishBattleIfNoMatches = false
    private lateinit var gameBoardAdapter: GameBoardAdapter

    private fun setupGameBoardRecyclerView() {
        gameBoardAdapter = GameBoardAdapter(
            requireContext(), gameBoard, binding.gameBoardRecyclerView,
            object : GameBoardAdapter.Callback {
                override fun crushGems(removedGems: MutableList<Gem>, heroTurn: Boolean) {
                    gameScreenViewModel.crushGems(removedGems, heroTurn)
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
                    binding.coverBoard.visibility = View.VISIBLE
                    binding.endTurnButton.isEnabled = false
                    isTurnTimerRunning = false
                    turnTimer.cancel()
                    // Calculate time spent for the turn
                    val timeSpentForTurnInSeconds = turnTimeElapsedInMillis / 1000
                    gameScreenViewModel.logTime(timeSpentForTurnInSeconds)
                    binding.createBoardAgainButton.isEnabled = false
                }

                override fun allowEndTurn() {
                    gameScreenViewModel.logPoints(gameBoardAdapter.heroTurn)
                    binding.endTurnButton.isEnabled = true
                    binding.createBoardAgainButton.isEnabled = true
                }

                override fun makeEnemyTurn() {
                    gameScreenViewModel.startEnemyTurn()
                }
            },
            BattleSettings.STOP_GENERATE
        )
        val layoutManager = GridLayoutManager(requireContext(), numCols)

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
                    binding.turnTime.text =
                        (turnTimeElapsedInMillis / TIMER_TICK).toString()
                }
            }

            override fun onFinish() {
                // Timer will never finish in this case
            }
        }

    private fun createTimerBlock() {
        startTurnTimer()

        gameScreenViewModel.startTurnAgainEventData().observe(viewLifecycleOwner) {
            startTurnTimer()
            binding.coverBoard.visibility = View.GONE
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
        gameScreenViewModel.heroHandsData().observe(viewLifecycleOwner)
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
        gameScreenViewModel.enemyHandsData().observe(viewLifecycleOwner) {
            enemyHandsAdapter.updateData(ArrayList(it.map { hand -> hand.copy() }))
        }
        gameScreenViewModel.enemySparkData().observe(viewLifecycleOwner) {
            //todo костыль
            val executedPerk = if (finishDialogIsShowing) {
                ExecutedPerk(EMPTY_PERK, Hand())
            } else {
                it
            }
            if (executedPerk.perk.name != Perk.EMPTY) {
                if (executedPerk.perk.name == Perk.LAST) {
                    gameScreenViewModel.afterEnemyActions()
                    binding.endTurnButton.isEnabled = true
                } else {
                    Handler(Looper.getMainLooper()).postDelayed({
                        val perk = executedPerk.perk
                        if (perk.show && perk.enable) {
                            val numberForCompareWithPerkProbability = Random.nextInt(0, 101)
                            /**
                             * дефолтная вероятность применения навыка 100%
                             */
                            if (numberForCompareWithPerkProbability <= perk.probability) {
                                gameScreenViewModel.messageAboutUsedPerk(perk, false)
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
                                    100
                                )
                            } else {
                                gameScreenViewModel.callNextPerk(executedPerk.perk)
                            }
                        } else {
                            gameScreenViewModel.callNextPerk(executedPerk.perk)
                        }
                    }, 100)
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
                        gameScreenViewModel.messageAboutUsedPerk(perk, true)
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

    private fun launchHeroSparkAnimation(perk: Perk) {
        if (!isSparking && binding.endTurnButton.isEnabled) {
            loadGif(perk)
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
                        gameScreenViewModel.executePerk(perk)
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
                        loadGif(perk, true)
                    }
                    gameScreenViewModel.executePerk(perk, false)
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
        gameScreenViewModel.personData().observe(viewLifecycleOwner) {
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
        gameScreenViewModel.heroResourcesData().observe(viewLifecycleOwner) {
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
        gameScreenViewModel.enemyData().observe(viewLifecycleOwner) {
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
            gameScreenViewModel.enemyMoveData().collect { value ->
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
            gameScreenViewModel.messageAboutEvaluateMove()
            val seconds = Random.nextInt(0, 4)
            Handler(Looper.getMainLooper()).postDelayed({
                gameScreenViewModel.messageAboutMakeMove()
                binding.gameBoardRecyclerView.findViewHolderForAdapterPosition(to.first * gameBoard[0].size + to.second)?.itemView?.performClick()
            }, seconds * 1000L)
            Handler(Looper.getMainLooper()).postDelayed({
                binding.gameBoardRecyclerView.findViewHolderForAdapterPosition(from.first * gameBoard[0].size + from.second)?.itemView?.performClick()
            }, 1500L)
        }
    }

    companion object {
        const val TIMER_TICK = 1000L
    }

}
