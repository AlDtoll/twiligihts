package aldtoll.twiligihts.ui.screen.start_screen

import aldtoll.twiligihts.R
import aldtoll.twiligihts.databinding.FragmentStartScreenBinding
import aldtoll.twiligihts.model.BattleSettings
import aldtoll.twiligihts.model.Gem.Companion.GEM_MAP
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint


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

        val options = navOptions {
            anim {
                enter = android.R.anim.fade_in
                exit = android.R.anim.fade_out
                popEnter = android.R.anim.fade_in
                popExit = android.R.anim.fade_out
            }
        }
        binding.startGameButton.setOnClickListener {
            findNavController().navigate(R.id.gameScreenFragment, null, options)
        }
        viewModel.resultData().observe(viewLifecycleOwner) {
            binding.startGameButton.isEnabled = !it.finished
        }

        viewModel.settingsData().observe(viewLifecycleOwner) {
            preloadIcons(it)
        }

    }

    private fun preloadIcons(battleSettings: BattleSettings) {
        val storage = FirebaseStorage.getInstance()
        var i = 0
        var tempMap = hashMapOf<String, String>()
        battleSettings.iconNames.forEach { iconName ->
            val gsReference = storage.reference.child("$iconName.png")
            gsReference.downloadUrl
                .addOnSuccessListener { uri ->
                    tempMap[iconName] = uri.toString()
                    if (i == battleSettings.iconNames.size - 1) {
                        battleSettings.iconNames.forEach {
                            GEM_MAP[it] = tempMap[it] ?: ""
                            Glide.with(this)
                                .load(GEM_MAP[it])
                                .timeout(60000)
                                .into(binding.testIcon)
                        }
                    }
                    i++
                }
        }

    }
}