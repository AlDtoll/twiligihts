package aldtoll.twiligihts.logic.database.hero

import aldtoll.twiligihts.logic.database.IDownloadFromDataBase
import aldtoll.twiligihts.model.Resource
import aldtoll.twiligihts.model.characters.Hero
import aldtoll.twiligihts.storage.hero.HeroResourcesInteractor
import com.google.firebase.database.DataSnapshot
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HeroResourcesDownloadExecutor @Inject constructor(
    private val interactor: HeroResourcesInteractor
) : IDownloadFromDataBase() {

    override fun saveStartedData(dataSnapshot: DataSnapshot) {
        val data = dataSnapshot.children.mapNotNull { it.getValue(getClazzForDataBase()) }
        data.run {
            interactor.startData = ArrayList(this)
        }
    }

    override fun getClazzForDataBase() = Resource::class.java

    override fun getNameForDataBase(): String =
        "${Hero::class.java.simpleName}${getClazzForDataBase().simpleName}s"
}