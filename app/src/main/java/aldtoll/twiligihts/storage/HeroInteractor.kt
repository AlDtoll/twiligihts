package aldtoll.twiligihts.storage

import aldtoll.twiligihts.model.Hero
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HeroInteractor @Inject constructor() {

    private val liveData = MutableLiveData<Hero>()

    fun update(item: Hero) {
        liveData.postValue(item)
    }

    fun get() = liveData

    fun value() = liveData.value
}