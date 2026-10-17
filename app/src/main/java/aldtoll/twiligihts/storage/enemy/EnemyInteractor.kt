package aldtoll.twiligihts.storage.enemy

import aldtoll.twiligihts.model.characters.Enemy
import aldtoll.twiligihts.model.characters.Person
import aldtoll.twiligihts.storage.PersonInteractor
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnemyInteractor @Inject constructor() : PersonInteractor {

    var startedValue: Enemy? = null
    private val liveData = MutableLiveData<Enemy>()

    /** рабочий объект: его меняет логика, наблюдатели получают снимок */
    private var working: Enemy? = null

    override fun update(item: Person) {
        val person = item as Enemy
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