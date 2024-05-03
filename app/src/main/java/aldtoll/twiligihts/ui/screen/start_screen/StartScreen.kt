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
        binding.startGameButton.setOnClickListener {
            binding.startAnimation.setAnimation("swords.json")
            binding.startAnimation.playAnimation()
            binding.startGameButton.isEnabled = false
            Handler(Looper.getMainLooper()).postDelayed({
                viewModel.newAttempt()
                findNavController().navigate(R.id.gameScreenFragment, null, options)
            }, 4000)
        }
        viewModel.resultData().observe(viewLifecycleOwner) {
            //todo может работаь некорректно
            binding.startGameButton.text = viewModel.battleName()
            binding.startGameButton.isEnabled = !it.finished
            binding.againIcon.visibility = if (!it.finished) {
                View.GONE
            } else {
                View.VISIBLE
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
                "Здесь живет: ${App.getPrefs().getString(NAME, "")}",
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
                binding.diceButtonCustom.text = value.toString()
                binding.diceButtonCustom.setOnClickListener {
                    rollDice(value)
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
    }

    override fun onResume() {
        super.onResume()
        binding.startAnimation.setAnimation("bonfire.json")
        binding.startAnimation.playAnimation()
        binding.startGameButton.isEnabled = !(viewModel.resultData().value?.finished ?: false)
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
    }
}