package aldtoll.twiligihts.logic

import aldtoll.twiligihts.model.BattleSettings
import aldtoll.twiligihts.model.Perk
import aldtoll.twiligihts.model.Status
import aldtoll.twiligihts.model.Stock
import aldtoll.twiligihts.model.findWorkStatuses
import aldtoll.twiligihts.storage.BattleSettingsInteractor
import aldtoll.twiligihts.storage.IStocks
import aldtoll.twiligihts.storage.enemy.EnemyInteractor
import aldtoll.twiligihts.storage.enemy.EnemyStockListInteractor
import aldtoll.twiligihts.storage.hero.HeroInteractor
import aldtoll.twiligihts.storage.hero.HeroStockListInteractor
import javax.inject.Inject
import javax.inject.Singleton

/**
 * логика обновления шкал очков персонажей
 * все кроме получения очков за 3 в ряд
 */
@Singleton
class EditStockExecutor @Inject constructor(
    private val heroStockListInteractor: HeroStockListInteractor,
    private val enemyStockListInteractor: EnemyStockListInteractor,
    private val battleSettingsInteractor: BattleSettingsInteractor,
    private val updatePerksStateExecutor: UpdatePerksStateExecutor,
    private val heroInteractor: HeroInteractor,
    private val enemyInteractor: EnemyInteractor,
) {

    /**
     * количество очков не может быть отрицательным
     */
    fun updateStocks(pair: Pair<Int, Int>, isHeroTarget: Boolean = true) {
        val arrayListOf = arrayListOf<Stock>()
        val iStocks = iStocks(isHeroTarget)
        iStocks.value()?.run {
            arrayListOf.addAll(this)
        }
        val find = arrayListOf.find { it.gemType == pair.first }
        if (find != null) {
            val newValue = find.value + pair.second
            find.value = newValue.coerceAtLeast(0)
        }
        iStocks.update(arrayListOf)
        updatePerksStateExecutor.updateEnableStatus()
    }

    fun setStocks(pair: Pair<Int, Int>, isHeroTarget: Boolean = true) {
        val arrayListOf = arrayListOf<Stock>()
        val iStocks = iStocks(isHeroTarget)
        iStocks.value()?.run {
            arrayListOf.addAll(this)
        }
        val find = arrayListOf.find { it.gemType == pair.first }
        if (find != null) {
            val i = pair.second
            find.value = i.coerceAtLeast(0)
        }
        iStocks.update(arrayListOf)
        updatePerksStateExecutor.updateEnableStatus()
    }

    fun payPriceForPerk(perk: Perk, isHero: Boolean) {
        val arrayListOf = arrayListOf<Stock>()
        val iStocks = iStocks(isHero)
        iStocks.value()?.run {
            arrayListOf.addAll(this)
        }
        perk.prices.forEach { price ->
            val find = arrayListOf.find { it.gemType == price.gemType }
            if (find != null) {
                find.value -= price.value
            }
        }
        iStocks.update(arrayListOf)
        updatePerksStateExecutor.updateEnableStatus()
    }

    private fun iStocks(isHero: Boolean): IStocks {
        val iStocks = if (isHero) {
            heroStockListInteractor
        } else {
            enemyStockListInteractor
        }
        return iStocks
    }

    fun updatePersonStocksAfterDamage(isHero: Boolean = true) {
        val iStocks = if (isHero) {
            heroStockListInteractor
        } else {
            enemyStockListInteractor
        }
        val battleSettings = battleSettingsInteractor.value()
        battleSettings?.run {
            val stocks = arrayListOf<Stock>()
            iStocks.value()?.run {
                stocks.addAll(this)
            }
            stocks.forEach { stock ->
                val damageKeepStrategy =
                    battleSettings.gemSettings.find { it.type == stock.gemType.toString() }?.damageKeepStrategy
                        ?: BattleSettings.DEFAULT_DAMAGE_KEEP_STRATEGY
                stock.value =
                    stock.value * damageKeepStrategy / 100
            }
            iStocks.update(stocks)
            updatePerksStateExecutor.updateEnableStatus()
        }
    }

    /**
     * в конце хода количество накопленных очков изменяется
     * как правило становится меньше
     */
    fun updatePersonStocksAfterTurn(isHero: Boolean = true) {
        val (iStocks, personInteractor) = if (isHero) {
            Pair(heroStockListInteractor, heroInteractor)
        } else {
            Pair(enemyStockListInteractor, enemyInteractor)
        }
        /**
         * какие-нибудь эффекты типа статусов могут повлиять на количество полученных от разрушения очков
         * также количество получемых очков зависит от настроек битвы
         */
        val findWorkChangeStockStatuses =
            personInteractor.value()?.statuses?.findWorkStatuses(Status.StatusType.CHANGE_TURN_KEEP_STRATEGY)
        val battleSettings = battleSettingsInteractor.value()
        battleSettings?.run {
            val stocks = arrayListOf<Stock>()
            iStocks.value()?.run {
                stocks.addAll(this)
            }
            stocks.forEach { stock ->
                /**
                 * общая для всех стратегия
                 */
                val baseTurnKeepStrategy =
                    battleSettings.gemSettings.find { it.type == stock.gemType.toString() }?.turnKeepStrategy
                        ?: BattleSettings.DEFAULT_TURN_KEEP_STRATEGY

                /**
                 * статусы персонажа, которые изменяют его стратегию
                 */
                val additionalTurnKeepStrategyValue = findWorkChangeStockStatuses
                    ?.filter { status -> status.gemTypes.contains(stock.gemType) }
                    ?.sumOf { it.value } ?: 0

                /**
                 * результирующий процент, не может быть отрицательным
                 */
                val turnKeepStrategy =
                    (baseTurnKeepStrategy + additionalTurnKeepStrategyValue).coerceAtLeast(0)
                stock.value =
                    stock.value * turnKeepStrategy / 100
            }
            iStocks.update(stocks)
            updatePerksStateExecutor.updateEnableStatus()
        }
    }
}