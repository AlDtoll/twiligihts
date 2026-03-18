package aldtoll.twiligihts.logic.database.enemy

import aldtoll.twiligihts.logic.database.IDownloadFromDataBase
import aldtoll.twiligihts.logic.database.fillStockPerkEffects
import aldtoll.twiligihts.model.StockPerk
import aldtoll.twiligihts.model.characters.Enemy
import aldtoll.twiligihts.storage.enemy.EnemyStockPerksInteractor
import com.google.firebase.database.DataSnapshot
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnemyStockPerksDownLoadInteractor @Inject constructor(
    private val interactor: EnemyStockPerksInteractor
) : IDownloadFromDataBase() {

    override fun saveStartedData(dataSnapshot: DataSnapshot) {
        val perks = dataSnapshot.children.mapNotNull { it.getValue(getClazzForDataBase()) }
        perks.fillStockPerkEffects(dataSnapshot)
        interactor.startData = ArrayList(perks)
    }

    override fun getClazzForDataBase() = StockPerk::class.java

    override fun getNameForDataBase(): String =
        "${Enemy::class.java.simpleName}StockPerks"
}

