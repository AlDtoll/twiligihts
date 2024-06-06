package aldtoll.twiligihts.logic.database.enemy

import aldtoll.twiligihts.logic.database.IDownloadFromDataBase
import aldtoll.twiligihts.model.Stock
import aldtoll.twiligihts.model.characters.Enemy
import aldtoll.twiligihts.storage.enemy.EnemyStockListInteractor
import com.google.firebase.database.DataSnapshot
import javax.inject.Inject

class EnemyStocksDownLoadInteractor @Inject constructor(
    private val enemyStockListInteractor: EnemyStockListInteractor
) : IDownloadFromDataBase() {

    override fun saveStartedData(dataSnapshot: DataSnapshot) {
        val stocks = dataSnapshot.children.mapNotNull { it.getValue(getClazzForDataBase()) }
        stocks.run {
            enemyStockListInteractor.startedValue = ArrayList(this)
        }
    }

    override fun getClazzForDataBase() = Stock::class.java

    override fun getNameForDataBase(): String =
        "${Enemy::class.java.simpleName}${getClazzForDataBase().simpleName}s"
}