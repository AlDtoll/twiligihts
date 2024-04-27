package aldtoll.twiligihts.logic.database.hero

import aldtoll.twiligihts.logic.database.IDownloadFromDataBase
import aldtoll.twiligihts.model.Stock
import aldtoll.twiligihts.model.characters.Hero
import aldtoll.twiligihts.storage.hero.HeroStockListInteractor
import com.google.firebase.database.DataSnapshot
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HeroStocksDownLoadInteractor @Inject constructor(
    private val heroStockListInteractor: HeroStockListInteractor
) : IDownloadFromDataBase() {

    override fun saveStartedData(dataSnapshot: DataSnapshot) {
        val stocks = dataSnapshot.children.mapNotNull { it.getValue(getClazzForDataBase()) }
        stocks.run {
            heroStockListInteractor.startedValue = ArrayList(this)
        }
    }

    override fun getClazzForDataBase() = Stock::class.java

    override fun getNameForDataBase(): String =
        "${Hero::class.java.simpleName}${getClazzForDataBase().simpleName}s"
}