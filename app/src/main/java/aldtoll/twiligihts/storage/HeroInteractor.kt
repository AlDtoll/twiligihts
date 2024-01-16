package aldtoll.twiligihts.storage

import aldtoll.twiligihts.model.Hero
import aldtoll.twiligihts.model.Person
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HeroInteractor @Inject constructor() : PersonInteractor {

    private val liveData = MutableLiveData<Hero>()

    override fun update(item: Person) {
        liveData.postValue(item as Hero)
    }

    fun get() = liveData

    override fun value() = liveData.value
}