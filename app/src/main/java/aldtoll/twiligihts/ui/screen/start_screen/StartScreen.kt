package aldtoll.twiligihts.ui.screen.start_screen

import aldtoll.twiligihts.App
import aldtoll.twiligihts.App.Companion.MASTER_TOKEN
import aldtoll.twiligihts.BuildConfig
import aldtoll.twiligihts.R
import aldtoll.twiligihts.databinding.FragmentStartScreenBinding
import aldtoll.twiligihts.model.BattleSettings
import aldtoll.twiligihts.model.Gem.Companion.GEM_MAP
import aldtoll.twiligihts.ui.screen.game_screen.logs.LogBottomSheetDialog
import aldtoll.twiligihts.ui.screen.start_screen.name.NameBottomSheetDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * стартовый экран приложения
 * тут подбирается противник
 * есть кнопки, чтобы начать/продолжить/перезапустить бой
 */
@AndroidEntryPoint
class StartScreen : Fragment() {

    private lateinit var binding: FragmentStartScreenBinding
    private val viewModel by viewModels<StartScreenViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStartScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.version.text = BuildConfig.VERSION_NAME
        val options = navOptions {
            anim {
                enter = android.R.anim.fade_in
                exit = android.R.anim.fade_out
                popEnter = android.R.anim.fade_in
                popExit = android.R.anim.fade_out
            }
        }
        binding.furnaceFire.speed = 0.5F
        binding.startAnimation.frame = 10
        binding.startGameButton.setOnClickListener {
            binding.startAnimation.setAnimation("swords.json")
            binding.startAnimation.playAnimation()
            binding.startGameButton.isEnabled = false
            binding.continiueGameButton.isEnabled = false
            Handler(Looper.getMainLooper()).postDelayed({
                viewModel.newAttempt()
                viewModel.startBattle()
                findNavController().navigate(R.id.gameScreenFragment, null, options)
            }, 4000)
        }
        binding.continiueGameButton.setOnClickListener {
            binding.startAnimation.setAnimation("swords.json")
            binding.startAnimation.playAnimation()
            binding.startGameButton.isEnabled = false
            binding.continiueGameButton.isEnabled = false
            Handler(Looper.getMainLooper()).postDelayed({
                val bundle = Bundle().apply {
                    putBoolean("continue", true) // Замените true на нужное вам значение
                }
                findNavController().navigate(R.id.gameScreenFragment, bundle, options)
            }, 4000)
        }
        viewModel.resultData().observe(viewLifecycleOwner) {
            //todo может работаь некорректно
            binding.startGameButton.text = viewModel.battleName()
            binding.startGameButton.isEnabled = !it.finished
            binding.continiueGameButton.isEnabled = !it.finished
            binding.againIcon.visibility = if (!it.finished) {
                View.GONE
            } else {
                View.VISIBLE
            }
//            val started = it.started
            val started = STARTED
            binding.continiueGameButton.visibility = if (started) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        viewModel.settingsData().observe(viewLifecycleOwner) {
            it.gemSettings.forEach {
                GEM_MAP[it.type] = it
            }
            preloadIcons(it)
        }

        binding.editorButton.setOnClickListener {
            findNavController().navigate(R.id.editorFragment, null, options)
        }

        binding.againIcon.setOnClickListener {
            viewModel.startBattleAgain()
            binding.againIcon.visibility = View.GONE
        }
        binding.testIcon.setOnClickListener {
            viewModel.activateGodMode()
        }

        binding.tentIcon.setOnClickListener {
            Toast.makeText(
                context,
                "На выходе подстерегает... ${App.getPrefs().getString(NAME, "")}",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.diceButton.setOnClickListener {
            rollDice(6)
        }

        binding.diceButton20.setOnClickListener {
            rollDice(20)
        }

        viewModel.getDiceData()
        lifecycleScope.launch {
            viewModel.diceData().collect { value ->
                if (value != 0) {
                    binding.diceButtonCustom.isEnabled = true
                    binding.diceButtonCustom.text = value.toString()
                    binding.diceButtonCustom.setOnClickListener {
                        rollDice(value)
                    }
                }
            }
        }

        viewModel.getMasterTokenData()
        lifecycleScope.launch {
            viewModel.masterTokenData().collect { value ->
                MASTER_TOKEN = value
            }
        }


        viewModel.getLogData()

        binding.logButton.setOnClickListener {
            val logBottomSheetDialog = LogBottomSheetDialog.newInstance()
            logBottomSheetDialog.show(
                parentFragmentManager,
                LogBottomSheetDialog::class.java.simpleName
            )
            Handler(Looper.getMainLooper()).postDelayed({
                logBottomSheetDialog.updateData(viewModel.logData)
            }, 100)
        }

        binding.startAnimation.setOnClickListener {
            val nameBottomSheetDialog = NameBottomSheetDialog.newInstance()
            nameBottomSheetDialog.show(
                parentFragmentManager,
                LogBottomSheetDialog::class.java.simpleName
            )
        }

        binding.baseIcon.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW).setData(Uri.parse(TABLE_URL))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context?.run {
                startActivity(intent)
            }
        }

        binding.totemIcon.setOnClickListener {
            val intent =
                Intent(Intent.ACTION_VIEW).setData(Uri.parse(FIREBASE_REALTIME_DATABASE_URL))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context?.run {
                startActivity(intent)
            }
        }

        viewModel.pushData().observe(viewLifecycleOwner) {
            if (it != null) {
                if (it.data.containsKey("enemy")) {
                    val enemyName = it.data["enemy"]
                    enemyName?.run {
                        Toast.makeText(
                            requireContext(),
                            "Предстоит бой с $enemyName",
                            Toast.LENGTH_SHORT
                        ).show()
                        App.getPrefs().edit().putString(NAME, enemyName)
                            .apply()
                        viewModel.changePrefixAndLoadNewData(enemyName)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.startAnimation.setAnimation("bonfire.json")
        binding.startAnimation.playAnimation()
        binding.startGameButton.isEnabled = !(viewModel.resultData().value?.finished ?: false)
        binding.continiueGameButton.isEnabled = !(viewModel.resultData().value?.finished ?: false)
    }

    private fun rollDice(maxDiceValue: Int) {
        val dice = Random.nextInt(1, maxDiceValue + 1)
        Toast.makeText(context, "$dice", Toast.LENGTH_SHORT).show()
        viewModel.showDice(dice, maxDiceValue)
    }

    private fun preloadIcons(battleSettings: BattleSettings) {
        val storage = FirebaseStorage.getInstance()
        battleSettings.gemSettings.forEach { gemSettings ->
            val gsReference = storage.reference.child("${gemSettings.name}.png")
            gsReference.downloadUrl
                .addOnSuccessListener { uri ->
                    GEM_MAP[gemSettings.type]?.uri = uri.toString()
                    Glide.with(this)
                        .load(GEM_MAP[gemSettings.type]?.uri)
                        .timeout(60000)
                        .into(binding.testIcon)
                }
        }
    }

    companion object {
        const val NAME = "name"
        const val TABLE_URL =
            "https://docs.google.com/spreadsheets/d/14CVD8lxhDcL_jR9Q9fhe-IFyWXEudxDZULcMXAGZeDY/edit#gid=0"
        const val FIREBASE_REALTIME_DATABASE_URL =
            "https://console.firebase.google.com/u/0/project/twilights-53442/database/twilights-53442-default-rtdb/data/~2F"
        var STARTED = false
    }
}