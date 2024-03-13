package aldtoll.twiligihts.logic

import aldtoll.twiligihts.model.Gem
import aldtoll.twiligihts.model.Gem.Companion.GEM_BONUS_VALUE
import aldtoll.twiligihts.model.Gem.Companion.GEM_FULL_VALUE
import aldtoll.twiligihts.model.Gem.Companion.GEM_MAP
import aldtoll.twiligihts.model.Hand
import aldtoll.twiligihts.model.Perk
import aldtoll.twiligihts.model.Stock
import aldtoll.twiligihts.storage.BattleSettingsInteractor
import aldtoll.twiligihts.storage.enemy.EnemyHandsListInteractor
import aldtoll.twiligihts.storage.hero.HeroHandsListInteractor
import aldtoll.twiligihts.storage.hero.HeroStockListInteractor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateStockExecutor @Inject constructor(
    private val heroStockListInteractor: HeroStockListInteractor,
    private val heroHandsListInteractor: HeroHandsListInteractor,
    private val enemyHandsListInteractor: EnemyHandsListInteractor,
    private val battleSettingsInteractor: BattleSettingsInteractor,
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
        updatePerksState()
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
        updatePerksState()
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
        updatePerksState()
    }

    fun updatePerksState() {
        val newHeroHands = arrayListOf<Hand>()
        val heroHands = heroHandsListInteractor.value()
        heroHands?.run {
            newHeroHands.addAll(this)
        }
        newHeroHands.forEach { hand ->
            hand.perks.forEach { perk: Perk ->
                val stocks = arrayListOf<Stock>()
                heroStockListInteractor.value()?.run {
                    stocks.addAll(this)
                }
                perk.enable = true
                perk.prices.forEach { price ->
                    val find = stocks.find { it.gemType == price.gemType }
                    if (find != null) {
                        if (price.value > find.value || perk.isReloading()) {
                            perk.enable = false
                        }
                    }
                }
            }
        }
        heroHandsListInteractor.update(newHeroHands)
        val newEnemyHands = arrayListOf<Hand>()
        val enemyHands = enemyHandsListInteractor.value()
        enemyHands?.run {
            newEnemyHands.addAll(this)
        }
//        newEnemyHands.forEach { hand ->
//            hand.perks.forEach { perk: Perk ->
//                val stocks = arrayListOf<Stock>()
//                heroStockListInteractor.value()?.run {
//                    stocks.addAll(this)
//                }
//                perk.enable = true
//                perk.prices.forEach { price ->
//                    val find = stocks.find { it.gemType == price.gemType }
//                    if (find != null) {
//                        if (price.value > find.value) {
//                            perk.enable = false
//                        }
//                    }
//                }
//            }
//        }
        enemyHandsListInteractor.update(newEnemyHands)
    }

    fun updateStockAfterDamage() {
        val value = battleSettingsInteractor.value()
        if (value?.clearStocksAfterDamage == true) {
            val stocks = arrayListOf<Stock>()
            heroStockListInteractor.value()?.run {
                stocks.addAll(this)
            }
            stocks.forEach {
                it.value = 0
            }
            heroStockListInteractor.update(stocks)
            updatePerksState()
        }
    }

    fun updateHeroStocksAfterTurn() {
        val stocks = arrayListOf<Stock>()
        heroStockListInteractor.value()?.run {
            stocks.addAll(this)
        }
        stocks.forEach {
            it.value = it.value / 2
        }
        heroStockListInteractor.update(stocks)
        updatePerksState()
    }
}