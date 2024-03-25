package aldtoll.twiligihts.ui.screen.editor_screen

import aldtoll.twiligihts.logic.database.DatabaseInteractor
import aldtoll.twiligihts.model.Hand
import aldtoll.twiligihts.model.characters.Hero
import androidx.lifecycle.ViewModel
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EditorFragmentViewModel @Inject constructor(
) : ViewModel() {

    private val database = Firebase.database
    fun saveData(hero: Hero, hands: ArrayList<Hand>) {
        val heroReference = database.getReference("${DatabaseInteractor.PREFIX}/Hero")
        heroReference.setValue(
            hero
        )
        val heroHandsReference = database.getReference("${DatabaseInteractor.PREFIX}/HeroHands")
        heroHandsReference.setValue(
            hands
        )
    }

}