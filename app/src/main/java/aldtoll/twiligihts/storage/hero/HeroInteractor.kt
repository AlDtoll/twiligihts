package aldtoll.twiligihts.storage.hero

import aldtoll.twiligihts.model.characters.Hero
import aldtoll.twiligihts.model.characters.Person
import aldtoll.twiligihts.storage.PersonInteractor
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HeroInteractor @Inject constructor() : PersonInteractor {

    var startedValue: Hero? = null
    private val liveData = MutableLiveData<Hero>()

    /** рабочий объект: его меняет логика, наблюдатели получают снимок */
    private var working: Hero? = null

    override fun update(item: Person) {
        val person = item as Hero
        working = person
        liveData.value = person.recreate()
    }

    fun get() = liveData

    override fun value() = working
    fun init() {
        startedValue?.run {
            update(this.recreate())
        }
    }
}