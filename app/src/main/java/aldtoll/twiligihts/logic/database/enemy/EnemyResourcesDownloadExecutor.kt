package aldtoll.twiligihts.logic.database.enemy

import aldtoll.twiligihts.logic.database.IDownloadFromDataBase
import aldtoll.twiligihts.model.Resource
import aldtoll.twiligihts.model.characters.Enemy
import aldtoll.twiligihts.storage.enemy.EnemyResourcesInteractor
import com.google.firebase.database.DataSnapshot
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnemyResourcesDownloadExecutor @Inject constructor(
    private val interactor: EnemyResourcesInteractor
) : IDownloadFromDataBase() {

    override fun saveStartedData(dataSnapshot: DataSnapshot) {
        val data = dataSnapshot.children.mapNotNull { it.getValue(getClazzForDataBase()) }
        data.run {
            interactor.startData = ArrayList(this)
        }
    }

    override fun getClazzForDataBase() = Resource::class.java

    override fun getNameForDataBase(): String =
        "${Enemy::class.java.simpleName}${getClazzForDataBase().simpleName}s"
}