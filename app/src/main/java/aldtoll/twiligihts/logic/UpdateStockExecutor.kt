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

/**
 * логика получения очков после совпадения 3 в ряд
 * зависит от параметров персонажей и настроек поля
 */
@Singleton
class UpdateStockExecutor @Inject constructor(
    private val heroStockListInteractor: HeroStockListInteractor,
    private val heroInteractor: HeroInteractor,
    private val enemyInteractor: EnemyInteractor,
    private val enemyStockListInteractor: EnemyStockListInteractor,
    private val updatePerksStateExecutor: UpdatePerksStateExecutor,
) {

    fun addValueFromCrushedGems(removedGems: MutableList<Gem>, heroTurn: Boolean) {
        /**
         * нужно выбрать чьи очки обновлять
         */
        val (iStocks, personInteractor) = if (heroTurn) {
            Pair(heroStockListInteractor, heroInteractor)
        } else {
            Pair(enemyStockListInteractor, enemyInteractor)
        }
        /**
         * далее нужно подсчитать какие и сколько гемов было уничтожено в результате совпадения
         * мапа состоит из уничтоженый цвет - количество
         */
        /**
         * полные гемы
         */
        val removedFullGemsCount = mutableMapOf<Int, Int>()

        /**
         * гемы-половинки
         */
        val removedHalfGemsCount = mutableMapOf<Int, Int>()

        /**
         * гемов с бонусами
         * и тогда в мапе цвет уничтоженого бонуса, а не гема
         */
        val removedBonusGemsCount = mutableMapOf<Int, Int>()
        /**
         * проходимся по списку уничтоженых гемов и заполняем соответствующую мапу
         */
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
        /**
         * важны только те шкалы, которые есть у персонажа,
         * поэтому нужно взять их
         */
        val personActiveStock = arrayListOf<Stock>()
        iStocks.value()?.run {
            personActiveStock.addAll(this)
        }
        /**
         * какие-нибудь эффекты типа статусов могут повлиять на количество полученных от разрушения очков
         * также количество получемых очков зависит от настроек битвы
         */
        val findActiveStatuses =
            personInteractor.value()?.statuses?.findActiveStatuses(Status.EffectType.CHANGE_STOCK)
        removedFullGemsCount.forEach { removedGemColor ->
            if (removedGemColor.key != 0) {
                val find = personActiveStock.find { it.gemType == removedGemColor.key }
                find?.run {
                    /**
                     * есть гемы, которые были на доске и есть те, которые падают в результате генерации
                     * чтобы смягчить каскадные эффекты заполнения шкал, которые происходят при 4 цветах
                     * используется настройка - давать половину очков за совпадения,
                     * которые произошли уже после генерации поля
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
                val find = personActiveStock.find { it.gemType == removedGemColor.key }
                find?.run {
                    val halfValue =
                        GEM_MAP[(this.gemType).toString()]?.halfValue ?: GEM_HALF_VALUE
                    this.increaseStock(removedGemColor.value * halfValue)
                }
            }
        }
        removedBonusGemsCount.forEach { removedGemColor ->
            if (removedGemColor.key != 0) {
                val find = personActiveStock.find { it.gemType == removedGemColor.key }
                find?.run {
                    val bonusValue =
                        GEM_MAP[(this.gemType).toString()]?.bonusValue ?: GEM_BONUS_VALUE
                    this.increaseStock(removedGemColor.value * bonusValue)
                }
            }
        }
        iStocks.update(personActiveStock)
        updatePerksStateExecutor.updateEnableStatus()
    }
}