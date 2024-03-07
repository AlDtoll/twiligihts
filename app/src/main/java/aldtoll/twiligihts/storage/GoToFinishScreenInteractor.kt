package aldtoll.twiligihts.storage

import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoToFinishScreenInteractor @Inject constructor() {

    //todo исправить
    private val data = MutableLiveData<Pair<Boolean, Boolean>>()

    fun update(item: Pair<Boolean, Boolean>) {
        data.value = item
    }

    fun get() = data

}