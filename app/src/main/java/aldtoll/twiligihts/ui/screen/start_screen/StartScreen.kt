package aldtoll.twiligihts.ui.screen.start_screen

import aldtoll.twiligihts.BuildConfig
import aldtoll.twiligihts.R
import aldtoll.twiligihts.databinding.FragmentStartScreenBinding
import aldtoll.twiligihts.model.BattleSettings
import aldtoll.twiligihts.model.Gem.Companion.GEM_MAP
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
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
            viewModel.newAttempt()
            findNavController().navigate(R.id.gameScreenFragment, null, options)
        }
        viewModel.resultData().observe(viewLifecycleOwner) {
            //todo может работаь некорректно
            binding.startGameButton.text = viewModel.battleName()
            binding.startGameButton.isEnabled = !it.finished
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
        }
        binding.testIcon.setOnClickListener {
            viewModel.activateGodMode()
        }

        binding.diceButton.setOnClickListener {
            val dice = Random.nextInt(1, 7)
            Toast.makeText(context, "$dice", Toast.LENGTH_SHORT).show()
            viewModel.showDice(dice)
        }

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
}