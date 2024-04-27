package aldtoll.twiligihts.logic.database

import aldtoll.twiligihts.model.BattleSettings
import aldtoll.twiligihts.storage.BattleSettingsInteractor
import com.google.firebase.database.DataSnapshot
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BattleSettingsDowloadExecutor @Inject constructor(
    private val interactor: BattleSettingsInteractor
) : IDownloadFromDataBase() {

    override fun saveStartedData(dataSnapshot: DataSnapshot) {
        val settings = dataSnapshot.getValue(getClazzForDataBase())
        settings.run {
            interactor.startData = settings
            interactor.init()
        }
    }

    override fun getClazzForDataBase() = BattleSettings::class.java

    //todo заменить
    override fun getNameForDataBase(): String = "Settings"
}