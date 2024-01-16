package aldtoll.twiligihts.logic

import aldtoll.twiligihts.model.Gem
import aldtoll.twiligihts.model.Hand
import aldtoll.twiligihts.model.Perk
import aldtoll.twiligihts.model.Stock
import aldtoll.twiligihts.storage.HandsListInteractor
import aldtoll.twiligihts.storage.StockListInteractor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateStockExecutor @Inject constructor(
    private val stockListInteractor: StockListInteractor,
    private val handsListInteractor: HandsListInteractor,
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
        stockListInteractor.value()?.run {
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
        stockListInteractor.update(arrayListOf)
        updatePerksState()
    }

    fun payPriceForPerk(perk: Perk) {
        val arrayListOf = arrayListOf<Stock>()
        stockListInteractor.value()?.run {
            arrayListOf.addAll(this)
        }
        perk.prices.forEach { price ->
            val find = arrayListOf.find { it.gemType == price.gemType }
            if (find != null) {
                find.value = find.value - price.value
            }
        }
        stockListInteractor.update(arrayListOf)
        updatePerksState()
    }

    private fun updatePerksState() {
        val newHands = arrayListOf<Hand>()
        val hands = handsListInteractor.value()
        hands?.run {
            newHands.addAll(this)
        }
        newHands.forEach { hand ->
            hand.perks.forEach { perk: Perk ->
                val stocks = arrayListOf<Stock>()
                stockListInteractor.value()?.run {
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
        handsListInteractor.update(newHands)
    }
}