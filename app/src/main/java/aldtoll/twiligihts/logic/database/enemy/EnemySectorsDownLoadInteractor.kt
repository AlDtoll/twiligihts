package aldtoll.twiligihts.logic.database.enemy

import aldtoll.twiligihts.logic.database.IDownloadFromDataBase
import aldtoll.twiligihts.logic.database.fillSectorEffects
import aldtoll.twiligihts.model.Sector
import aldtoll.twiligihts.storage.enemy.EnemySectorsInteractor
import com.google.firebase.database.DataSnapshot
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnemySectorsDownLoadInteractor @Inject constructor(
    private val interactor: EnemySectorsInteractor
) : IDownloadFromDataBase() {

    override fun saveStartedData(dataSnapshot: DataSnapshot) {
        val data = dataSnapshot.children.mapNotNull { it.getValue(getClazzForDataBase()) }
        data.fillSectorEffects(dataSnapshot)
        data.run {
            interactor.startData = ArrayList(this)
        }
    }

    override fun getClazzForDataBase() = Sector::class.java

    override fun getNameForDataBase(): String = "EnemySectors"
}

