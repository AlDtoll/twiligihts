package aldtoll.twiligihts.storage

import aldtoll.twiligihts.model.BattleSettings
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BattleSettingsInteractor @Inject constructor() {

    private val liveData = MutableLiveData<BattleSettings>()

    fun update(item: BattleSettings) {
        liveData.value = item
    }

    fun get() = liveData

    fun value() = liveData.value
}