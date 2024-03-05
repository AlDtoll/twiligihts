package aldtoll.twiligihts.storage

import aldtoll.twiligihts.model.characters.Hero
import aldtoll.twiligihts.model.characters.Person
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HeroInteractor @Inject constructor() : PersonInteractor {

    var startedValue: Hero? = null
    private val liveData = MutableLiveData<Hero>()

    override fun update(item: Person) {
        liveData.value = item as Hero
    }

    fun get() = liveData

    override fun value() = liveData.value
    fun init() {
        startedValue?.run {
            update(this.recreate())
        }
    }
}