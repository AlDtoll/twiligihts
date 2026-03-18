package aldtoll.twiligihts.logic.database.enemy

import aldtoll.twiligihts.logic.database.IDownloadFromDataBase
import aldtoll.twiligihts.logic.database.fillTimePerkEffects
import aldtoll.twiligihts.model.TimePerk
import aldtoll.twiligihts.model.characters.Enemy
import aldtoll.twiligihts.storage.enemy.EnemyTimePerksInteractor
import com.google.firebase.database.DataSnapshot
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnemyTimePerksDownLoadInteractor @Inject constructor(
    private val interactor: EnemyTimePerksInteractor
) : IDownloadFromDataBase() {

    override fun saveStartedData(dataSnapshot: DataSnapshot) {
        val perks = dataSnapshot.children.mapNotNull { it.getValue(getClazzForDataBase()) }
        perks.fillTimePerkEffects(dataSnapshot)
        perks.run {
            interactor.startData = ArrayList(this)
        }
    }

    override fun getClazzForDataBase() = TimePerk::class.java

    override fun getNameForDataBase(): String =
        "${Enemy::class.java.simpleName}TimePerks"
}

