package aldtoll.twiligihts.storage

import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoToFinishScreenInteractor @Inject constructor() {

    private val data = MutableLiveData<Boolean>()

    fun update(item: Boolean) {
        data.value = item
    }

    fun get() = data

}