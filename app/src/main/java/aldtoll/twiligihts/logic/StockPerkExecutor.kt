package aldtoll.twiligihts.logic

import aldtoll.twiligihts.model.StockPerk
import aldtoll.twiligihts.storage.enemy.EnemyStockListInteractor
import aldtoll.twiligihts.storage.enemy.EnemyStockPerksInteractor
import aldtoll.twiligihts.storage.hero.HeroStockListInteractor
import aldtoll.twiligihts.storage.hero.HeroStockPerksInteractor
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Исполнитель навыков по ресурсам (StockPerk): при каждом изменении ресурсов проверяет,
 * пересёк ли какой-то ресурс порог снизу вверх, и запускает соответствующий [StockPerk.perk].
 */
@Singleton
class StockPerkExecutor @Inject constructor(
    private val heroStockPerksInteractor: HeroStockPerksInteractor,
    private val enemyStockPerksInteractor: EnemyStockPerksInteractor,
    private val heroStockListInteractor: HeroStockListInteractor,
    private val enemyStockListInteractor: EnemyStockListInteractor,
    private val perkExecutor: PerkExecutor,
    private val checkConditionExecutor: CheckConditionExecutor,
) {

    private val previousHeroStockValueByGemType = mutableMapOf<Int, Int>()
    private val previousEnemyStockValueByGemType = mutableMapOf<Int, Int>()

    fun onStocksChanged(isHero: Boolean) {
        val (stockPerks, currentStockValueByGemType, previousStockValueByGemType) = if (isHero) {
            Triple(
                heroStockPerksInteractor.value(),
                buildStockValueByGemType(heroStockListInteractor.value()),
                previousHeroStockValueByGemType
            )
        } else {
            Triple(
                enemyStockPerksInteractor.value(),
                buildStockValueByGemType(enemyStockListInteractor.value()),
                previousEnemyStockValueByGemType
            )
        }

        if (stockPerks.isNullOrEmpty()) {
            previousStockValueByGemType.clear()
            previousStockValueByGemType.putAll(currentStockValueByGemType)
            return
        }

        val perksToApply = stockPerks.filter { stockPerk ->
            val previousValue = previousStockValueByGemType[stockPerk.gemType] ?: 0
            val currentValue = currentStockValueByGemType[stockPerk.gemType] ?: 0
            previousValue < stockPerk.threshold && currentValue >= stockPerk.threshold
        }

        perksToApply.forEach { stockPerk ->
            val perk = stockPerk.perk
            val conditions = perk.conditionsForDisplay
            val canShow = conditions.isEmpty() || conditions.all { condition ->
                checkConditionExecutor.execute(condition, isHero)
            }
            if (canShow) {
                perkExecutor.execute(perk, isHero)
            }
        }

        previousStockValueByGemType.clear()
        previousStockValueByGemType.putAll(currentStockValueByGemType)
    }

    private fun buildStockValueByGemType(stocks: List<aldtoll.twiligihts.model.Stock>?): Map<Int, Int> {
        if (stocks.isNullOrEmpty()) {
            return emptyMap()
        }
        val map = mutableMapOf<Int, Int>()
        stocks.forEach { stock ->
            map[stock.gemType] = stock.value
        }
        return map
    }
}

