package aldtoll.twiligihts.ui.screen.editor_screen

import aldtoll.twiligihts.databinding.FragmentEditorBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditorFragment : Fragment() {

    private lateinit var binding: FragmentEditorBinding
    private val editorFragmentViewModel by viewModels<EditorFragmentViewModel>()

}