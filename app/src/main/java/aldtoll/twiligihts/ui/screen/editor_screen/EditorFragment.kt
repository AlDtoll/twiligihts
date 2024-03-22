package aldtoll.twiligihts.ui.screen.editor_screen

import aldtoll.twiligihts.databinding.FragmentEditorBinding
import aldtoll.twiligihts.model.characters.Hero
import aldtoll.twiligihts.model.characters.Person
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
        binding.saveButton.setOnClickListener {
//            editorFragmentViewModel.saveData()
        }
    }

    private fun setupList() {
        val list = binding.personsList
        val personAdapter = PersonAdapter.newInstance(object : PersonAdapter.Callback {
            override fun clickLog() {
            }

        })
        list.adapter = personAdapter
        val persons = arrayListOf<Person>()
        persons.add(
            Hero()
        )
        personAdapter.updateData(persons)
    }
}