package aldtoll.twiligihts.logic.database.hero

import aldtoll.twiligihts.logic.database.IDownloadFromDataBase
import aldtoll.twiligihts.logic.database.fillTimePerkEffects
import aldtoll.twiligihts.model.TimePerk
import aldtoll.twiligihts.model.characters.Hero
import aldtoll.twiligihts.storage.hero.HeroTimePerksInteractor
import com.google.firebase.database.DataSnapshot
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HeroTimePerksDownLoadInteractor @Inject constructor(
    private val interactor: HeroTimePerksInteractor
) : IDownloadFromDataBase() {

    override fun saveStartedData(dataSnapshot: DataSnapshot) {
        val perks = dataSnapshot.children.mapNotNull { it.getValue(getClazzForDataBase()) }
        perks.fillTimePerkEffects(dataSnapshot)
        perks.run {
            interactor.startData = ArrayList(this)
        }
    }

    override fun getClazzForDataBase() = TimePerk::class.java

    override fun getNameForDataBase(): String =
        "${Hero::class.java.simpleName}TimePerks"
}

