package aldtoll.twiligihts.ui.screen.game_screen.logs

import aldtoll.twiligihts.databinding.FragmentLogDialogBinding
import aldtoll.twiligihts.model.BattleEvent
import aldtoll.twiligihts.ui.screen.game_screen.adapter.LogAdapter
import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class LogBottomSheetDialog : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentLogDialogBinding
    private val viewModel by viewModels<LogBottomSheetDialogViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (!::binding.isInitialized) {
            binding = FragmentLogDialogBinding.inflate(inflater, container, false)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupLogList()
        binding.closeLogButton.setOnClickListener {
            dismiss()
        }
    }

    private lateinit var logAdapter: LogAdapter

    private fun setupLogList() {
        val logList = binding.list
        logAdapter = LogAdapter.newInstance(object : LogAdapter.Callback {
            override fun clickLog() {

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

    fun updateData(list: ArrayList<BattleEvent>) {
        setupLogList()
        logAdapter.updateData(list)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), theme).apply {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    companion object {
        fun newInstance(): LogBottomSheetDialog {
            return LogBottomSheetDialog()
        }
    }
}