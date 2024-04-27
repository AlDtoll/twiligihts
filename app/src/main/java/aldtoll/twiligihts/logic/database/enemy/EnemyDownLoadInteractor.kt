package aldtoll.twiligihts.logic.database.enemy

import aldtoll.twiligihts.logic.database.IDownloadFromDataBase
import aldtoll.twiligihts.model.characters.Enemy
import aldtoll.twiligihts.storage.enemy.EnemyInteractor
import com.google.firebase.database.DataSnapshot
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnemyDownLoadInteractor @Inject constructor(
    private val enemyInteractor: EnemyInteractor
) : IDownloadFromDataBase() {

    override fun saveStartedData(dataSnapshot: DataSnapshot) {
        val value = dataSnapshot.getValue(getClazzForDataBase())
        value?.run {
            enemyInteractor.startedValue = this.recreate()
        }
    }

    override fun getClazzForDataBase() = Enemy::class.java

    override fun getNameForDataBase(): String = Enemy::class.java.simpleName
}