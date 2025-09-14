package aldtoll.twiligihts.logic.database.hero

import aldtoll.twiligihts.logic.database.IDownloadFromDataBase
import aldtoll.twiligihts.model.MatchRule
import aldtoll.twiligihts.model.characters.Hero
import aldtoll.twiligihts.storage.hero.HeroRulesInteractor
import com.google.firebase.database.DataSnapshot
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HeroRulesDownLoadInteractor @Inject constructor(
    private val interactor: HeroRulesInteractor
) : IDownloadFromDataBase() {

    override fun saveStartedData(dataSnapshot: DataSnapshot) {
        val rules = dataSnapshot.children.mapNotNull { it.getValue(getClazzForDataBase()) }
        rules.run {
            interactor.startData = ArrayList(this)
        }
    }

    override fun getClazzForDataBase() = MatchRule::class.java

    override fun getNameForDataBase(): String =
        "${Hero::class.java.simpleName}${getClazzForDataBase().simpleName}s"
}
