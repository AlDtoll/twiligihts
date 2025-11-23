package aldtoll.twiligihts.logic.database.enemy

import aldtoll.twiligihts.logic.database.IDownloadFromDataBase
import aldtoll.twiligihts.logic.database.fillStateReactionEffects
import aldtoll.twiligihts.model.State
import aldtoll.twiligihts.model.characters.Enemy
import aldtoll.twiligihts.storage.enemy.EnemyStatesInteractor
import com.google.firebase.database.DataSnapshot
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnemyStatesDownLoadInteractor @Inject constructor(
    private val interactor: EnemyStatesInteractor
) : IDownloadFromDataBase() {

    override fun saveStartedData(dataSnapshot: DataSnapshot) {
        val states = dataSnapshot.children.mapNotNull { it.getValue(getClazzForDataBase()) }
        states.fillStateReactionEffects(dataSnapshot)
        states.run {
            interactor.startData = ArrayList(this)
        }
    }

    override fun getClazzForDataBase() = State::class.java

    override fun getNameForDataBase(): String =
        "${Enemy::class.java.simpleName}${getClazzForDataBase().simpleName}s"
}