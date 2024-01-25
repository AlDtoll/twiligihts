package aldtoll.twiligihts.storage

import aldtoll.twiligihts.model.BattleResult
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BattleResultInteractor @Inject constructor() {

    private val liveData = MutableLiveData<BattleResult>()

    fun update(item: BattleResult) {
        liveData.value = item
    }

    fun get() = liveData

    fun value() = liveData.value

}