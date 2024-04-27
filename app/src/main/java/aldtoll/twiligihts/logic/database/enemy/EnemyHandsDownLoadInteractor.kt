package aldtoll.twiligihts.logic.database.enemy

import aldtoll.twiligihts.logic.database.IDownloadFromDataBase
import aldtoll.twiligihts.logic.database.fillEffects
import aldtoll.twiligihts.model.Hand
import aldtoll.twiligihts.model.characters.Enemy
import aldtoll.twiligihts.storage.enemy.EnemyHandsListInteractor
import com.google.firebase.database.DataSnapshot
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnemyHandsDownLoadInteractor @Inject constructor(
    private val interactor: EnemyHandsListInteractor
) : IDownloadFromDataBase() {

    override fun saveStartedData(dataSnapshot: DataSnapshot) {
        val hands = dataSnapshot.children.mapNotNull { it.getValue(getClazzForDataBase()) }
        hands.fillEffects(dataSnapshot)
        hands.run {
            interactor.startData = ArrayList(this)
        }
    }

    override fun getClazzForDataBase() = Hand::class.java

    override fun getNameForDataBase(): String =
        "${Enemy::class.java.simpleName}${getClazzForDataBase().simpleName}s"
}