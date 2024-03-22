package aldtoll.twiligihts.ui.screen.editor_screen

import aldtoll.twiligihts.databinding.FragmentEditorBinding
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditorFragment : Fragment() {

    private lateinit var binding: FragmentEditorBinding
    private val editorFragmentViewModel by viewModels<EditorFragmentViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupList()
    }

    private fun setupList() {
        val list = binding.personsList
        val logAdapter = PersonAdapter.newInstance(object : PersonAdapter.Callback {
            override fun clickLog() {
                TODO("Not yet implemented")
            }

        })
        list.adapter = logAdapter
    }
}