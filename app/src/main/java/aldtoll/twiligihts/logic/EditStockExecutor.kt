package aldtoll.twiligihts.logic

import aldtoll.twiligihts.storage.enemy.EnemyStockListInteractor
import aldtoll.twiligihts.storage.hero.HeroStockListInteractor
import javax.inject.Inject
import javax.inject.Singleton

/**
 * заготовка экзекутора
 */
@Singleton
class EditStockExecutor @Inject constructor(
    private val heroStockListInteractor: HeroStockListInteractor,
    private val enemyStockListInteractor: EnemyStockListInteractor
) {
}