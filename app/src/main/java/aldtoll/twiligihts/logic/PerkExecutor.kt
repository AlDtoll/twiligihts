package aldtoll.twiligihts.logic

import aldtoll.twiligihts.model.Hand
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PerkExecutor @Inject constructor(
    private val fillStockExecutor: FillStockExecutor
) {
    fun execute(perk: Hand.Perk) {
        payPerkPrice(perk)
    }

    private fun payPerkPrice(perk: Hand.Perk) {
        fillStockExecutor.payPriceForPerk(perk)
    }
}