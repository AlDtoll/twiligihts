package aldtoll.twiligihts.storage.hero

import aldtoll.twiligihts.model.MatchRule
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HeroRulesInteractor @Inject constructor() {

    private val liveData = MutableLiveData<ArrayList<MatchRule>>()
    var startData = ArrayList<MatchRule>()

    fun update(list: ArrayList<MatchRule>) {
        liveData.value = list
    }

    fun get() = liveData

    fun value() = liveData.value
    fun init() {
        val list = ArrayList(startData.map { rule -> rule.copy() })
        update(list)
    }
}
