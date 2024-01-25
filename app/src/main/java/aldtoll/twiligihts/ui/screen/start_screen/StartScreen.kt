package aldtoll.twiligihts.ui.screen.start_screen

import aldtoll.twiligihts.R
import aldtoll.twiligihts.databinding.FragmentStartScreenBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
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
    }
}