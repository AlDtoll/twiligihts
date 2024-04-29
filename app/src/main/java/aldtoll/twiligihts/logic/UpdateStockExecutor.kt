package aldtoll.twiligihts.logic

import aldtoll.twiligihts.model.BattleSettings
import aldtoll.twiligihts.model.Gem
import aldtoll.twiligihts.model.Gem.Companion.GEM_BONUS_VALUE
import aldtoll.twiligihts.model.Gem.Companion.GEM_FULL_VALUE
import aldtoll.twiligihts.model.Gem.Companion.GEM_MAP
import aldtoll.twiligihts.model.Perk
import aldtoll.twiligihts.model.Stock
import aldtoll.twiligihts.storage.BattleSettingsInteractor
import aldtoll.twiligihts.storage.enemy.EnemyStockListInteractor
import aldtoll.twiligihts.storage.hero.HeroStockListInteractor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateStockExecutor @Inject constructor(
    private val heroStockListInteractor: HeroStockListInteractor,
    private val enemyStockListInteractor: EnemyStockListInteractor,
    private val battleSettingsInteractor: BattleSettingsInteractor,
    private val updatePerksStateExecutor: UpdatePerksStateExecutor,
) {

    fun addValueFromCrushedGems(removedGems: MutableList<Gem>) {
        // Get the color of the gem being removed
        val removedGemsCount = mutableMapOf<Int, Double>()
        val removedGemsBonusCount = mutableMapOf<Int, Int>()
        for (gem in removedGems) {
            val removedGemColor = gem.type
            val removedGemBonusColor = gem.bonusType
            if (removedGemColor != removedGemBonusColor) {
                removedGemsBonusCount[removedGemBonusColor] =
                    (removedGemsBonusCount[removedGemBonusColor] ?: 0) + 1
            }
            // Increment the count for the removed gem color in the map
            val i = if (gem.half) 0.5 else 1.0
            removedGemsCount[removedGemColor] = (removedGemsCount[removedGemColor] ?: 0.0).plus(i)
        }
        val arrayListOf = arrayListOf<Stock>()
        heroStockListInteractor.value()?.run {
            arrayListOf.addAll(this)
        }
        removedGemsCount.forEach { removedGemColor ->
            if (removedGemColor.key != 0) {
                val find = arrayListOf.find { it.gemType == removedGemColor.key }
                find?.run {
                    val fullValue =
                        GEM_MAP[(this.gemType).toString()]?.fullValue ?: GEM_FULL_VALUE
                    this.increaseStock((removedGemColor.value * fullValue).toInt())
                }
            }
        }
        removedGemsBonusCount.forEach { removedGemColor ->
            if (removedGemColor.key != 0) {
                val find = arrayListOf.find { it.gemType == removedGemColor.key }
                find?.run {
                    val bonusValue =
                        GEM_MAP[(this.gemType).toString()]?.bonusValue ?: GEM_BONUS_VALUE
                    this.increaseStock(removedGemColor.value * bonusValue)
                }
            }
        }
        heroStockListInteractor.update(arrayListOf)
        updatePerksStateExecutor.updateEnableStatus()
    }

    fun updateStocks(pair: Pair<Int, Int>) {
        val arrayListOf = arrayListOf<Stock>()
        heroStockListInteractor.value()?.run {
            arrayListOf.addAll(this)
        }
        val find = arrayListOf.find { it.gemType == pair.first }
        if (find != null) {
            val i = find.value + pair.second
            find.value = i.coerceAtLeast(0)
        }
        heroStockListInteractor.update(arrayListOf)
        updatePerksStateExecutor.updateEnableStatus()
    }

    fun setStocks(pair: Pair<Int, Int>) {
        val arrayListOf = arrayListOf<Stock>()
        heroStockListInteractor.value()?.run {
            arrayListOf.addAll(this)
        }
        val find = arrayListOf.find { it.gemType == pair.first }
        if (find != null) {
            val i = pair.second
            find.value = i.coerceAtLeast(0)
        }
        heroStockListInteractor.update(arrayListOf)
        updatePerksStateExecutor.updateEnableStatus()
    }

    fun payPriceForPerk(perk: Perk) {
        val arrayListOf = arrayListOf<Stock>()
        heroStockListInteractor.value()?.run {
            arrayListOf.addAll(this)
        }
        perk.prices.forEach { price ->
            val find = arrayListOf.find { it.gemType == price.gemType }
            if (find != null) {
                find.value = find.value - price.value
            }
        }
        heroStockListInteractor.update(arrayListOf)
        updatePerksStateExecutor.updateEnableStatus()
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