package aldtoll.twiligihts.logic.database.enemy

import aldtoll.twiligihts.logic.database.IDownloadFromDataBase
import aldtoll.twiligihts.logic.database.fillStatusReactionEffects
import aldtoll.twiligihts.model.Status
import aldtoll.twiligihts.model.characters.Enemy
import aldtoll.twiligihts.storage.enemy.EnemyStatusesInteractor
import com.google.firebase.database.DataSnapshot
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnemyStatusesDownLoadInteractor @Inject constructor(
    private val interactor: EnemyStatusesInteractor
) : IDownloadFromDataBase() {

    override fun saveStartedData(dataSnapshot: DataSnapshot) {
        val data = dataSnapshot.children.mapNotNull { it.getValue(getClazzForDataBase()) }
        data.fillStatusReactionEffects(dataSnapshot)
        data.run {
            interactor.startData = ArrayList(this)
        }
    }

    override fun getClazzForDataBase() = Status::class.java

    override fun getNameForDataBase(): String =
        "${Enemy::class.java.simpleName}${getClazzForDataBase().simpleName}es"
}