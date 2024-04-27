package aldtoll.twiligihts.logic.database.hero

import aldtoll.twiligihts.logic.database.IDownloadFromDataBase
import aldtoll.twiligihts.model.Status
import aldtoll.twiligihts.model.characters.Hero
import aldtoll.twiligihts.storage.hero.HeroStatusesInteractor
import com.google.firebase.database.DataSnapshot
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HeroStatusesDownLoadInteractor @Inject constructor(
    private val interactor: HeroStatusesInteractor
) : IDownloadFromDataBase() {

    override fun saveStartedData(dataSnapshot: DataSnapshot) {
        val data = dataSnapshot.children.mapNotNull { it.getValue(getClazzForDataBase()) }
        data.run {
            interactor.startData = ArrayList(this)
        }
    }

    override fun getClazzForDataBase() = Status::class.java

    override fun getNameForDataBase(): String =
        "${Hero::class.java.simpleName}${getClazzForDataBase().simpleName}es"
}