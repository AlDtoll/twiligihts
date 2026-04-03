package aldtoll.twiligihts.ui.screen.game_screen.logs

import aldtoll.twiligihts.R
import aldtoll.twiligihts.databinding.FragmentLogDialogBinding
import aldtoll.twiligihts.model.BattleEvent
import aldtoll.twiligihts.ui.screen.game_screen.adapter.LogAdapter
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.google.android.material.R as MaterialR


@AndroidEntryPoint
class LogBottomSheetDialog : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentLogDialogBinding
    private val viewModel by viewModels<LogBottomSheetDialogViewModel>()

    private lateinit var logAdapter: LogAdapter

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
        val logList = binding.list
        logAdapter = LogAdapter.newInstance(
            object : LogAdapter.Callback {
                override fun clickLog() {}
            },
            textSelectable = true
        )
        logList.adapter = logAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.battleLog.collect { logs ->
                    val arrayListOf = arrayListOf<BattleEvent>()
                    arrayListOf.addAll(logs)
                    logAdapter.updateData(arrayListOf)
                    Handler(Looper.getMainLooper()).postDelayed({
                        if (logAdapter.itemCount > 0) {
                            logList.smoothScrollToPosition(logAdapter.itemCount - 1)
                        }
                    }, 100)
                }
            }
        }

        var suppressHideTechnicalLogsToggle = false
        binding.hideTechnicalLogsSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (!suppressHideTechnicalLogsToggle) {
                viewModel.setHideTechnicalLogs(isChecked)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.hideTechnicalLogs.collect { hide ->
                    if (binding.hideTechnicalLogsSwitch.isChecked != hide) {
                        suppressHideTechnicalLogsToggle = true
                        binding.hideTechnicalLogsSwitch.isChecked = hide
                        suppressHideTechnicalLogsToggle = false
                    }
                }
            }
        }

        binding.copyLogButton.setOnClickListener {
            val text = viewModel.displayedLogTextForClipboard()
            val cm =
                requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("battle_log", text))
            Toast.makeText(requireContext(), R.string.battle_log_copied, Toast.LENGTH_SHORT).show()
        }

        binding.closeLogButton.setOnClickListener {
            dismiss()
        }
    }

    fun updateData(list: ArrayList<BattleEvent>) {
        if (::logAdapter.isInitialized) {
            logAdapter.updateData(list)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), theme).apply {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog as? BottomSheetDialog ?: return
        val bottomSheet = dialog.findViewById<View>(MaterialR.id.design_bottom_sheet) ?: return
        bottomSheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        bottomSheet.requestLayout()
        val behavior = BottomSheetBehavior.from(bottomSheet)
        behavior.skipCollapsed = true
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    companion object {
        fun newInstance(): LogBottomSheetDialog {
            return LogBottomSheetDialog()
        }
    }
}
