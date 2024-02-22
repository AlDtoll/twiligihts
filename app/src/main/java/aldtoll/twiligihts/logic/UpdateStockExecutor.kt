package aldtoll.twiligihts.logic

import aldtoll.twiligihts.model.Gem
import aldtoll.twiligihts.model.Hand
import aldtoll.twiligihts.model.Perk
import aldtoll.twiligihts.model.Stock
import aldtoll.twiligihts.storage.BattleSettingsInteractor
import aldtoll.twiligihts.storage.EnemyHandsListInteractor
import aldtoll.twiligihts.storage.HeroHandsListInteractor
import aldtoll.twiligihts.storage.HeroStockListInteractor
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
        val removedGemsCount = mutableMapOf<Int, Int>()
        for (gem in removedGems) {
            val removedGemColor = gem.type
            // Increment the count for the removed gem color in the map
            removedGemsCount[removedGemColor] = (removedGemsCount[removedGemColor] ?: 0) + 1
        }
        val arrayListOf = arrayListOf<Stock>()
        heroStockListInteractor.value()?.run {
            arrayListOf.addAll(this)
        }
        removedGemsCount.forEach { removedGemColor ->
            if (removedGemColor.key != 0) {
                val find = arrayListOf.find { it.gemType == removedGemColor.key }
                if (find != null) {
                    find.value = find.value + removedGemColor.value * 10
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
            find.value = find.value + pair.second
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
                        if (price.value > find.value) {
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