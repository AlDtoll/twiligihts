package aldtoll.twiligihts.storage

import aldtoll.twiligihts.model.BattleSettings
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BattleSettingsInteractor @Inject constructor() {

    var startData: BattleSettings? = null
    private val liveData = MutableLiveData<BattleSettings>()

    fun update(item: BattleSettings) {
        liveData.value = item
    }

    fun get() = liveData

    fun value() = liveData.value

    fun init() {
        startData?.run {
            update(this.copy())
        }
    }
}