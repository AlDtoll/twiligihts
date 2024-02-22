package aldtoll.twiligihts.ui.screen.final_screen

import aldtoll.twiligihts.R
import aldtoll.twiligihts.databinding.FragmentFinalScreenBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FinalScreen : Fragment() {

    private lateinit var binding: FragmentFinalScreenBinding
    private val finalScreenViewModel by viewModels<FinalScreenViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFinalScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        finalScreenViewModel.finishBattle()
        finalScreenViewModel.reinit()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigate(R.id.action_finalScreen_to_startScreenFragment)
        }
    }
}