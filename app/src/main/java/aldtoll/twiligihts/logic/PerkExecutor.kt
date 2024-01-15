package aldtoll.twiligihts.logic

import aldtoll.twiligihts.model.Hand
import aldtoll.twiligihts.storage.PersonInteractor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PerkExecutor @Inject constructor(
    private val fillStockExecutor: FillStockExecutor,
    private val personInteractor: PersonInteractor
) {
    fun execute(perk: Hand.Perk) {
        payPerkPrice(perk)
        executePerkEffect(perk)
    }

    private fun executePerkEffect(perk: Hand.Perk) {
        perk.effects.forEach { effect ->
            when (effect.effectType) {
                Hand.Perk.Effect.EffectType.ATTACK -> {

                }

                Hand.Perk.Effect.EffectType.DEFEND -> {
                    val person = personInteractor.value()
                    person?.run {
                        this.shield = this.shield + effect.value
                        personInteractor.update(this)
                    }
                }

                Hand.Perk.Effect.EffectType.DODGE -> {

                }
            }
        }
    }

    private fun payPerkPrice(perk: Hand.Perk) {
        fillStockExecutor.payPriceForPerk(perk)
    }
}