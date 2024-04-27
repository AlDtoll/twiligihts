package aldtoll.twiligihts.logic.database.hero

import aldtoll.twiligihts.logic.database.IDownloadFromDataBase
import aldtoll.twiligihts.model.State
import aldtoll.twiligihts.model.characters.Hero
import aldtoll.twiligihts.storage.hero.HeroStatesInteractor
import com.google.firebase.database.DataSnapshot
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HeroStatesDownLoadInteractor @Inject constructor(
    private val interactor: HeroStatesInteractor
) : IDownloadFromDataBase() {

    override fun saveStartedData(dataSnapshot: DataSnapshot) {
        val states = dataSnapshot.children.mapNotNull { it.getValue(getClazzForDataBase()) }
        states.run {
            interactor.startData = ArrayList(this)
        }
    }

    override fun getClazzForDataBase() = State::class.java

    override fun getNameForDataBase(): String =
        "${Hero::class.java.simpleName}${getClazzForDataBase().simpleName}s"
}