package aldtoll.twiligihts.logic

import aldtoll.twiligihts.model.BattleSettings.Companion.DECREASE_NEW_GEMS_VALUE
import aldtoll.twiligihts.model.Gem
import aldtoll.twiligihts.model.Gem.Companion.GEM_BONUS_VALUE
import aldtoll.twiligihts.model.Gem.Companion.GEM_FULL_VALUE
import aldtoll.twiligihts.model.Gem.Companion.GEM_HALF_VALUE
import aldtoll.twiligihts.model.Gem.Companion.GEM_MAP
import aldtoll.twiligihts.model.Status
import aldtoll.twiligihts.model.Stock
import aldtoll.twiligihts.model.findActiveStatuses
import aldtoll.twiligihts.storage.enemy.EnemyStockListInteractor
import aldtoll.twiligihts.storage.hero.HeroInteractor
import aldtoll.twiligihts.storage.hero.HeroStockListInteractor
import aldtoll.twiligihts.ui.screen.game_screen.GameBoardAdapter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateStockExecutor @Inject constructor(
    private val heroStockListInteractor: HeroStockListInteractor,
    private val heroInteractor: HeroInteractor,
    private val enemyStockListInteractor: EnemyStockListInteractor,
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
        val findActiveStatuses =
            heroInteractor.value()?.statuses?.findActiveStatuses(Status.EffectType.CHANGE_STOCK)
        removedGemsCount.forEach { removedGemColor ->
            if (removedGemColor.key != 0) {
                val find = arrayListOf.find { it.gemType == removedGemColor.key }
                find?.run {
                    /**
                     * есть гемы, которые были на доске и есть те, которые падают в результате генерации
                     */
                    val fullValue = if (DECREASE_NEW_GEMS_VALUE) {
                        if (GameBoardAdapter.CRUSH_GENERATED_GEMS) {
                            GEM_HALF_VALUE
                        } else {
                            GEM_MAP[(this.gemType).toString()]?.fullValue ?: GEM_FULL_VALUE
                        }
                    } else {
                        GEM_MAP[(this.gemType).toString()]?.fullValue ?: GEM_FULL_VALUE
                    }

                    val additionalValue =
                        findActiveStatuses?.find { it.gemType == this.gemType }?.value ?: 0
                    val gemValue = fullValue + additionalValue
                    this.increaseStock((removedGemColor.value * gemValue).toInt())
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
}