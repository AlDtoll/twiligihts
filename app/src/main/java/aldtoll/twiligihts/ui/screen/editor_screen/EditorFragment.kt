package aldtoll.twiligihts.ui.screen.editor_screen

import aldtoll.twiligihts.databinding.FragmentEditorBinding
import aldtoll.twiligihts.model.Hand
import aldtoll.twiligihts.model.characters.Hero
import aldtoll.twiligihts.model.characters.Person
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ConcatAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditorFragment : Fragment() {

    private lateinit var binding: FragmentEditorBinding
    private val editorFragmentViewModel by viewModels<EditorFragmentViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupList()
        binding.saveButton.setOnClickListener {
            editorFragmentViewModel.saveData(
                editPersonAdapter.getData()[0] as Hero,
                editHandsAdapter.getData()
            )
        }
    }

    private lateinit var editPersonAdapter: EditPersonAdapter
    private lateinit var editHandsAdapter: EditHandsAdapter

    private fun setupList() {
        val list = binding.editList
        editPersonAdapter = EditPersonAdapter.newInstance()
        editHandsAdapter = EditHandsAdapter.newInstance()
        list.adapter = ConcatAdapter(editPersonAdapter, editHandsAdapter)
        val persons = arrayListOf<Person>()
        persons.add(
            Hero()
        )
        editPersonAdapter.updateData(persons)
        val hands = arrayListOf<Hand>()
        hands.add(
            Hand()
        )
        editHandsAdapter.updateData(hands)
    }
}