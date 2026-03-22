package aldtoll.twiligihts.logic

import aldtoll.twiligihts.model.CellType
import aldtoll.twiligihts.model.CrushedCell
import aldtoll.twiligihts.model.Gem.Companion.GEM_BONUS_VALUE
import aldtoll.twiligihts.model.Gem.Companion.GEM_FULL_VALUE
import aldtoll.twiligihts.model.Gem.Companion.GEM_MAP
import aldtoll.twiligihts.model.MatchGroupInfo
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
    private val matchPerkExecutor: MatchPerkExecutor,
    private val stockPerkExecutor: StockPerkExecutor,
    private val perkExecutor: PerkExecutor,
    private val checkConditionExecutor: CheckConditionExecutor,
) {

    /**
     * после соединения трех в ряд и более
     * добавить соответствующие очки
     */
    fun addValueFromCrushedGems(
        crushedCells: List<CrushedCell>,
        groups: List<MatchGroupInfo>,
        heroTurn: Boolean
    ) {
        /**
         * нужно выбрать чьи очки обновлять
         */
        val (iStocks, personInteractor) = if (heroTurn) {
            Pair(heroStockListInteractor, heroInteractor)
        } else {
            Pair(enemyStockListInteractor, enemyInteractor)
        }
        // Логирование групп совпадений
        groups.forEachIndexed { index, info ->
            Log.d(
                "Game",
                "Группа ${index + 1}: цвет=${info.gemType}, ориентация=${info.orientation}, размер=${info.size}"
            )
        }

        // После начисления очков выполнить навыки по правилам совпадений. Пока только для героя
        if (heroTurn) {
            matchPerkExecutor.execute(groups, heroTurn)
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
        val findWorkChangeStockStatuses =
            personInteractor.value()?.statuses?.findWorkStatuses(Status.StatusType.CHANGE_STOCK)

        fun pointsForGemType(gemType: Int): Int {
            val gemValue = GEM_MAP[gemType.toString()]?.fullValue ?: GEM_FULL_VALUE
            val additionalValue = findWorkChangeStockStatuses
                ?.filter { status -> status.gemTypes.contains(gemType) }
                ?.sumOf { it.value } ?: 0
            return gemValue + additionalValue
        }

        fun applyCellModifier(baseValue: Double, crushedCell: CrushedCell): Double {
            val modified = when (crushedCell.cell.cellType) {
                CellType.MULTIPLIER -> baseValue * crushedCell.cell.modifierValue
                CellType.ADDITIVE -> baseValue + crushedCell.cell.modifierValue
                // TRIGGER может дополнительно нести modifierValue (например, -2 квадрат в центре),
                // поэтому трактуем его как ADDITIVE для расчёта очков.
                CellType.NONE -> baseValue
                CellType.TRIGGER -> baseValue + crushedCell.cell.modifierValue
            }
            return modified.coerceAtLeast(0.0)
        }

        // Начисление по типам стоков с учетом модификаторов ячейки
        val stockDeltaByGemType = mutableMapOf<Int, Double>()
        val bonusDeltaByGemType = mutableMapOf<Int, Double>()

        for (crushedCell in crushedCells) {
            val gem = crushedCell.gem
            val mainType = gem.type
            val extraType = gem.extraType
            val bonusType = gem.bonusType

            if (mainType != 0) {
                val mainWeight = if (extraType != null) 0.5 else 1.0
                val mainBase = pointsForGemType(mainType) * mainWeight
                val mainResult = applyCellModifier(mainBase, crushedCell)
                stockDeltaByGemType[mainType] =
                    (stockDeltaByGemType[mainType] ?: 0.0) + mainResult
            }

            if (extraType != null && extraType != 0) {
                val extraBase = pointsForGemType(extraType) * 0.5
                val extraResult = applyCellModifier(extraBase, crushedCell)
                stockDeltaByGemType[extraType] =
                    (stockDeltaByGemType[extraType] ?: 0.0) + extraResult
            }

            if (bonusType != null && bonusType != 0) {
                val bonusValue = GEM_MAP[bonusType.toString()]?.bonusValue ?: GEM_BONUS_VALUE
                val bonusResult = applyCellModifier(bonusValue.toDouble(), crushedCell)
                bonusDeltaByGemType[bonusType] =
                    (bonusDeltaByGemType[bonusType] ?: 0.0) + bonusResult
            }
        }

        Log.d("Game", "Начисление по стокам: $stockDeltaByGemType")
        Log.d("Game", "Начисление по бонусам: $bonusDeltaByGemType")

        // Применяем суммарные изменения стоков
        stockDeltaByGemType.forEach { deltaByType ->
            if (deltaByType.key != 0) {
                val stock = personActiveStock.find { it.gemType == deltaByType.key }
                stock?.run {
                    Log.d("Game", "Начислено ${deltaByType.value} очков за тип ${deltaByType.key}")
                    increaseStock(deltaByType.value.toInt())
                }
            }
        }

        bonusDeltaByGemType.forEach { deltaByType ->
            if (deltaByType.key != 0) {
                val stock = personActiveStock.find { it.gemType == deltaByType.key }
                stock?.run {
                    Log.d("Game", "Бонус: ${deltaByType.value} очков за тип ${deltaByType.key}")
                    increaseStock(deltaByType.value.toInt())
                }
            }
        }

        // Триггерные клетки запускают встроенный навык (1 раз на клетку в батче)
        val processedTriggerCells = mutableSetOf<Pair<Int, Int>>()
        for (crushedCell in crushedCells) {
            val key = crushedCell.row to crushedCell.col
            if (processedTriggerCells.contains(key)) continue
            val triggerPerk = crushedCell.cell.triggerPerk
            if (crushedCell.cell.cellType == CellType.TRIGGER && triggerPerk != null) {
                processedTriggerCells.add(key)
                val conditions = triggerPerk.conditionsForDisplay
                val canShow = conditions.isEmpty() || conditions.all {
                    checkConditionExecutor.execute(it, heroTurn)
                }
                if (canShow) {
                    // Используем executeIfAvailable, чтобы корректно отрабатывали заряды, перезарядка и видимость.
                    perkExecutor.executeIfAvailable(triggerPerk, heroTurn)
                }
            }
        }

        iStocks.update(personActiveStock)
        updatePerksStateExecutor.updateEnableStatus()
        stockPerkExecutor.onStocksChanged(heroTurn)
    }
}