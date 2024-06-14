package aldtoll.twiligihts.logic

import aldtoll.twiligihts.model.BattleSettings.Companion.USE_HALF_FOR_NEW_GEMS
import aldtoll.twiligihts.model.Gem
import aldtoll.twiligihts.model.Gem.Companion.GEM_BONUS_VALUE
import aldtoll.twiligihts.model.Gem.Companion.GEM_FULL_VALUE
import aldtoll.twiligihts.model.Gem.Companion.GEM_HALF_VALUE
import aldtoll.twiligihts.model.Gem.Companion.GEM_MAP
import aldtoll.twiligihts.model.Status
import aldtoll.twiligihts.model.Stock
import aldtoll.twiligihts.model.findActiveStatuses
import aldtoll.twiligihts.storage.enemy.EnemyInteractor
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
    private val enemyInteractor: EnemyInteractor,
    private val enemyStockListInteractor: EnemyStockListInteractor,
    private val updatePerksStateExecutor: UpdatePerksStateExecutor,
) {

    fun addValueFromCrushedGems(removedGems: MutableList<Gem>, heroTurn: Boolean) {
        val iStocks = if (heroTurn) {
            heroStockListInteractor
        } else {
            enemyStockListInteractor
        }
        val personInteractor = if (heroTurn) {
            heroInteractor
        } else {
            enemyInteractor
        }
        // Get the color of the gem being removed
        val removedFullGemsCount = mutableMapOf<Int, Int>()
        val removedHalfGemsCount = mutableMapOf<Int, Int>()
        val removedBonusGemsCount = mutableMapOf<Int, Int>()
        for (gem in removedGems) {
            val removedGemColor = gem.type
            val removedGemBonusColor = gem.bonusType
            if (removedGemColor != removedGemBonusColor) {
                removedBonusGemsCount[removedGemBonusColor] =
                    (removedBonusGemsCount[removedGemBonusColor] ?: 0) + 1
            }
            if (gem.half) {
                removedHalfGemsCount[removedGemColor] =
                    (removedHalfGemsCount[removedGemColor] ?: 0) + 1
            } else {
                removedFullGemsCount[removedGemColor] =
                    (removedFullGemsCount[removedGemColor] ?: 0) + 1
            }
        }
        val arrayListOf = arrayListOf<Stock>()
        iStocks.value()?.run {
            arrayListOf.addAll(this)
        }
        val findActiveStatuses =
            personInteractor.value()?.statuses?.findActiveStatuses(Status.EffectType.CHANGE_STOCK)
        removedFullGemsCount.forEach { removedGemColor ->
            if (removedGemColor.key != 0) {
                val find = arrayListOf.find { it.gemType == removedGemColor.key }
                find?.run {
                    /**
                     * есть гемы, которые были на доске и есть те, которые падают в результате генерации
                     */
                    val fullValue = if (USE_HALF_FOR_NEW_GEMS) {
                        if (GameBoardAdapter.CRUSH_GENERATED_GEMS) {
                            GEM_MAP[(this.gemType).toString()]?.halfValue ?: GEM_HALF_VALUE
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
        removedHalfGemsCount.forEach { removedGemColor ->
            if (removedGemColor.key != 0) {
                val find = arrayListOf.find { it.gemType == removedGemColor.key }
                find?.run {
                    val halfValue =
                        GEM_MAP[(this.gemType).toString()]?.halfValue ?: GEM_HALF_VALUE
                    this.increaseStock(removedGemColor.value * halfValue)
                }
            }
        }
        removedBonusGemsCount.forEach { removedGemColor ->
            if (removedGemColor.key != 0) {
                val find = arrayListOf.find { it.gemType == removedGemColor.key }
                find?.run {
                    val bonusValue =
                        GEM_MAP[(this.gemType).toString()]?.bonusValue ?: GEM_BONUS_VALUE
                    this.increaseStock(removedGemColor.value * bonusValue)
                }
            }
        }
        iStocks.update(arrayListOf)
        updatePerksStateExecutor.updateEnableStatus()
    }
}