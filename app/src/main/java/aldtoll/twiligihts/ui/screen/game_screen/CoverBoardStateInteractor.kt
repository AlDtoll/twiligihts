package aldtoll.twiligihts.ui.screen.game_screen

import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoverBoardStateInteractor @Inject constructor() {

    private val liveData = MutableLiveData<Int>()

    fun update(item: Int) {
        liveData.value = item
    }

    fun get() = liveData
}