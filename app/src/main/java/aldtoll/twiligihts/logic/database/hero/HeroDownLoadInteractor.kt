package aldtoll.twiligihts.logic.database.hero

import aldtoll.twiligihts.logic.database.IDownloadFromDataBase
import aldtoll.twiligihts.model.characters.Hero
import aldtoll.twiligihts.storage.hero.HeroInteractor
import com.google.firebase.database.DataSnapshot
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HeroDownLoadInteractor @Inject constructor(
    private val heroInteractor: HeroInteractor
) : IDownloadFromDataBase() {
    override fun saveStartedData(dataSnapshot: DataSnapshot) {
        val value = dataSnapshot.getValue(getClazzForDataBase())
        value?.run {
            heroInteractor.startedValue = this.recreate()
        }
    }

    override fun getClazzForDataBase() = Hero::class.java

    override fun getNameForDataBase(): String = Hero::class.java.simpleName
}