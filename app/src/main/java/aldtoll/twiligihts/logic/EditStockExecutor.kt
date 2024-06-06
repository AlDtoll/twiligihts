package aldtoll.twiligihts.logic

import aldtoll.twiligihts.model.BattleSettings
import aldtoll.twiligihts.model.Perk
import aldtoll.twiligihts.model.Stock
import aldtoll.twiligihts.storage.BattleSettingsInteractor
import aldtoll.twiligihts.storage.IStocks
import aldtoll.twiligihts.storage.enemy.EnemyStockListInteractor
import aldtoll.twiligihts.storage.hero.HeroStockListInteractor
import javax.inject.Inject
import javax.inject.Singleton

/**
 *
 */
@Singleton
class EditStockExecutor @Inject constructor(
    private val heroStockListInteractor: HeroStockListInteractor,
    private val enemyStockListInteractor: EnemyStockListInteractor,
    private val battleSettingsInteractor: BattleSettingsInteractor,
    private val updatePerksStateExecutor: UpdatePerksStateExecutor,
) {

    fun updateStocks(pair: Pair<Int, Int>, isHeroTarget: Boolean = true) {
        val arrayListOf = arrayListOf<Stock>()
        val iStocks = iStocks(isHeroTarget)
        iStocks.value()?.run {
            arrayListOf.addAll(this)
        }
        val find = arrayListOf.find { it.gemType == pair.first }
        if (find != null) {
            val i = find.value + pair.second
            find.value = i.coerceAtLeast(0)
        }
        iStocks.update(arrayListOf)
        updatePerksStateExecutor.updateEnableStatus()
    }

    fun setStocks(pair: Pair<Int, Int>, isHeroTarget: Boolean = true) {
        val arrayListOf = arrayListOf<Stock>()
        val iStocks = iStocks(isHeroTarget)
        iStocks.value()?.run {
            arrayListOf.addAll(this)
        }
        val find = arrayListOf.find { it.gemType == pair.first }
        if (find != null) {
            val i = pair.second
            find.value = i.coerceAtLeast(0)
        }
        iStocks.update(arrayListOf)
        updatePerksStateExecutor.updateEnableStatus()
    }

    fun payPriceForPerk(perk: Perk, isHero: Boolean) {
        val arrayListOf = arrayListOf<Stock>()
        val iStocks = iStocks(isHero)
        iStocks.value()?.run {
            arrayListOf.addAll(this)
        }
        perk.prices.forEach { price ->
            val find = arrayListOf.find { it.gemType == price.gemType }
            if (find != null) {
                find.value = find.value - price.value
            }
        }
        iStocks.update(arrayListOf)
        updatePerksStateExecutor.updateEnableStatus()
    }

    private fun iStocks(isHero: Boolean): IStocks {
        val iStocks = if (isHero) {
            heroStockListInteractor
        } else {
            enemyStockListInteractor
        }
        return iStocks
    }

    fun updateStockAfterDamage() {
        val battleSettings = battleSettingsInteractor.value()
        battleSettings?.run {
            val stocks = arrayListOf<Stock>()
            heroStockListInteractor.value()?.run {
                stocks.addAll(this)
            }
            stocks.forEach { stock ->
                val damageKeepStrategy =
                    battleSettings.gemSettings.find { it.type == stock.gemType.toString() }?.damageKeepStrategy
                        ?: BattleSettings.DEFAULT_DAMAGE_KEEP_STRATEGY
                stock.value =
                    stock.value * damageKeepStrategy / 100
            }
            heroStockListInteractor.update(stocks)
            updatePerksStateExecutor.updateEnableStatus()
        }
    }

    fun updateHeroStocksAfterTurn() {
        val battleSettings = battleSettingsInteractor.value()
        battleSettings?.run {
            val stocks = arrayListOf<Stock>()
            heroStockListInteractor.value()?.run {
                stocks.addAll(this)
            }
            stocks.forEach { stock ->
                val turnKeepStrategy =
                    battleSettings.gemSettings.find { it.type == stock.gemType.toString() }?.turnKeepStrategy
                        ?: BattleSettings.DEFAULT_TURN_KEEP_STRATEGY
                stock.value =
                    stock.value * turnKeepStrategy / 100
            }
            heroStockListInteractor.update(stocks)
            updatePerksStateExecutor.updateEnableStatus()
        }
    }
}