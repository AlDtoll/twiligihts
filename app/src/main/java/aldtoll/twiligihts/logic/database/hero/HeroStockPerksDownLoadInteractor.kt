package aldtoll.twiligihts.logic.database.hero

import aldtoll.twiligihts.logic.database.IDownloadFromDataBase
import aldtoll.twiligihts.logic.database.fillStockPerkEffects
import aldtoll.twiligihts.model.StockPerk
import aldtoll.twiligihts.model.characters.Hero
import aldtoll.twiligihts.storage.hero.HeroStockPerksInteractor
import com.google.firebase.database.DataSnapshot
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HeroStockPerksDownLoadInteractor @Inject constructor(
    private val interactor: HeroStockPerksInteractor
) : IDownloadFromDataBase() {

    override fun saveStartedData(dataSnapshot: DataSnapshot) {
        val perks = dataSnapshot.children.mapNotNull { it.getValue(getClazzForDataBase()) }
        perks.fillStockPerkEffects(dataSnapshot)
        interactor.startData = ArrayList(perks)
    }

    override fun getClazzForDataBase() = StockPerk::class.java

    override fun getNameForDataBase(): String =
        "${Hero::class.java.simpleName}StockPerks"
}

