package aldtoll.twiligihts.storage

import aldtoll.twiligihts.model.Person
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PersonInteractor @Inject constructor() {

    private val liveData = MutableLiveData<Person>()

    fun update(item: Person) {
        liveData.postValue(item)
    }

    fun get() = liveData

    fun value() = liveData.value
}