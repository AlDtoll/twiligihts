package aldtoll.twiligihts.storage

import aldtoll.twiligihts.model.Enemy
import aldtoll.twiligihts.model.Person
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnemyInteractor @Inject constructor() : PersonInteractor {

    var startedValue: Enemy? = null
    private val liveData = MutableLiveData<Enemy>()

    override fun update(item: Person) {
        liveData.value = item as Enemy
    }

    fun get() = liveData

    override fun value() = liveData.value

    fun init() {
        startedValue?.run {
            update(this.recreate())
        }
    }
}