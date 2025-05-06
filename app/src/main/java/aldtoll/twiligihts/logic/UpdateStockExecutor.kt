package aldtoll.twiligihts.logic

import aldtoll.twiligihts.model.Gem
import aldtoll.twiligihts.model.Gem.Companion.GEM_BONUS_VALUE
import aldtoll.twiligihts.model.Gem.Companion.GEM_FULL_VALUE
import aldtoll.twiligihts.model.Gem.Companion.GEM_MAP
import aldtoll.twiligihts.model.Status
import aldtoll.twiligihts.model.Stock
import aldtoll.twiligihts.model.findWorkStatuses
import aldtoll.twiligihts.storage.enemy.EnemyInteractor
import aldtoll.twiligihts.storage.enemy.EnemyStockListInteractor
import aldtoll.twiligihts.storage.hero.HeroInteractor
import aldtoll.twiligihts.storage.hero.HeroStockListInteractor
import android.util.Log
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
         * основные гемы
         */
        val removedBaseGemsCount = mutableMapOf<Int, Double>()

        /**
         * экстра гемы
         */
        val removedExtraGemsCount = mutableMapOf<Int, Double>()

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
            val removedExtraGemColor = gem.extraType


            // Учет бонусного типа
            if (removedGemBonusColor != null) {
                removedBonusGemsCount[removedGemBonusColor] =
                    (removedBonusGemsCount[removedGemBonusColor] ?: 0) + 1
            }

            // Если есть extraType, то очки распределяются между type и extraType
            if (removedExtraGemColor != null) {
                // Для основного типа
                removedBaseGemsCount[removedGemColor] =
                    (removedBaseGemsCount[removedGemColor] ?: 0.0).plus(0.5)

                // Для дополнительного типа
                removedExtraGemsCount[removedExtraGemColor] =
                    (removedExtraGemsCount[removedExtraGemColor] ?: 0.0).plus(0.5)
            } else {
                removedBaseGemsCount[removedGemColor] =
                    (removedBaseGemsCount[removedGemColor] ?: 0.0).plus(1.0)
            }
        }

        // Логирование уничтоженных полных гемов
        Log.d("Game", "Полные уничтоженные гемы: $removedExtraGemsCount")

        // Логирование уничтоженных бонусных гемов
        Log.d("Game", "Бонусные уничтоженные гемы: $removedBonusGemsCount")

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
            personInteractor.value()?.statuses?.findWorkStatuses(Status.StatusType.CHANGE_STOCK)

        // Обработка основного цвета
        removedBaseGemsCount.forEach { removedGemColor ->
            if (removedGemColor.key != 0) {
                val find = personActiveStock.find { it.gemType == removedGemColor.key }
                find?.run {
                    val gemValue = GEM_MAP[(this.gemType).toString()]?.fullValue ?: GEM_FULL_VALUE
                    val additionalValue =
                        findActiveStatuses?.find { it.gemType == this.gemType }?.value ?: 0
                    val totalValue = removedGemColor.value * (gemValue + additionalValue)

                    // Логируем начисленные очки
                    Log.d("Game", "Начислено $totalValue очков за $removedGemColor (основной цвет)")

                    this.increaseStock(totalValue.toInt())
                }
            }
        }

        // Обработка экстра цвета
        removedExtraGemsCount.forEach { removedGemColor ->
            if (removedGemColor.key != 0) {
                val find = personActiveStock.find { it.gemType == removedGemColor.key }
                find?.run {
                    val gemValue = GEM_MAP[(this.gemType).toString()]?.fullValue ?: GEM_FULL_VALUE
                    val additionalValue =
                        findActiveStatuses?.find { it.gemType == this.gemType }?.value ?: 0
                    val totalValue = removedGemColor.value * (gemValue + additionalValue)

                    // Логируем начисленные очки
                    Log.d("Game", "Начислено $totalValue очков за $removedGemColor (экстра цвет)")

                    this.increaseStock(totalValue.toInt())
                }
            }
        }

        // Обработка бонусных гемов
        removedBonusGemsCount.forEach { removedGemColor ->
            if (removedGemColor.key != 0) {
                val find = personActiveStock.find { it.gemType == removedGemColor.key }
                find?.run {
                    val bonusValue =
                        GEM_MAP[(this.gemType).toString()]?.bonusValue ?: GEM_BONUS_VALUE
                    val totalValue = removedGemColor.value * bonusValue

                    // Логируем начисленные очки
                    Log.d("Game", "Начислено $totalValue очков за $removedGemColor (бонусные гемы)")

                    this.increaseStock(totalValue)
                }
            }
        }

        iStocks.update(personActiveStock)
        updatePerksStateExecutor.updateEnableStatus()
    }
}