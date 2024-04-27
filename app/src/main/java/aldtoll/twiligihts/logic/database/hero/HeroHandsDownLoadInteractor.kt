package aldtoll.twiligihts.logic.database.hero

import aldtoll.twiligihts.logic.database.IDownloadFromDataBase
import aldtoll.twiligihts.logic.database.fillEffects
import aldtoll.twiligihts.model.Hand
import aldtoll.twiligihts.model.characters.Hero
import aldtoll.twiligihts.storage.hero.HeroHandsListInteractor
import com.google.firebase.database.DataSnapshot
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HeroHandsDownLoadInteractor @Inject constructor(
    private val interactor: HeroHandsListInteractor
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
        "${Hero::class.java.simpleName}${getClazzForDataBase().simpleName}s"
}