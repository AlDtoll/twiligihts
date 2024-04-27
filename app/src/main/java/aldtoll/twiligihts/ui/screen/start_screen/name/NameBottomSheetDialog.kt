package aldtoll.twiligihts.ui.screen.start_screen.name

import aldtoll.twiligihts.App
import aldtoll.twiligihts.databinding.FragmentNameDialogBinding
import aldtoll.twiligihts.ui.screen.start_screen.StartScreen
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NameBottomSheetDialog : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentNameDialogBinding
    private val viewModel by viewModels<NameBottomSheetDialogViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (!::binding.isInitialized) {
            binding = FragmentNameDialogBinding.inflate(inflater, container, false)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.heroName.setText(App.getPrefs().getString(StartScreen.NAME, ""))
        binding.saveName.setOnClickListener {
            val name = binding.heroName.text.toString()
            App.getPrefs().edit().putString(StartScreen.NAME, name)
                .apply()
            viewModel.changePrefixAndLoadNewData(name)
            dismiss()
        }
    }

    companion object {
        fun newInstance(): NameBottomSheetDialog {
            return NameBottomSheetDialog()
        }
    }
}