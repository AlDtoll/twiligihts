package aldtoll.twiligihts.logic

import aldtoll.twiligihts.model.Hand
import aldtoll.twiligihts.model.Perk
import aldtoll.twiligihts.model.Stock
import aldtoll.twiligihts.storage.enemy.EnemyHandsListInteractor
import aldtoll.twiligihts.storage.enemy.EnemyResourcesInteractor
import aldtoll.twiligihts.storage.enemy.EnemyStockListInteractor
import aldtoll.twiligihts.storage.hero.HeroHandsListInteractor
import aldtoll.twiligihts.storage.hero.HeroResourcesInteractor
import aldtoll.twiligihts.storage.hero.HeroStockListInteractor
import javax.inject.Inject
import javax.inject.Singleton

/**
 *
 */
@Singleton
class UpdatePerksStateExecutor @Inject constructor(
    private val heroHandsListInteractor: HeroHandsListInteractor,
    private val enemyHandsListInteractor: EnemyHandsListInteractor,
    private val heroStockListInteractor: HeroStockListInteractor,
    private val enemyStockListInteractor: EnemyStockListInteractor,
    private val heroResourcesInteractor: HeroResourcesInteractor,
    private val enemyResourcesInteractor: EnemyResourcesInteractor,
    private val checkConditionExecutor: CheckConditionExecutor,
) {

    fun updateEnableStatus() {
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
                    /**
                     * навык доступен для применения если:
                     * очков больше, чем его цена
                     * он не на перезарядке
                     * выполняются все условия
                     * достаточно ресурсов
                     */
                    if (price.value == 0) {
                        if (perkIsNotAvailable(perk)) {
                            perk.enable = false
                        }
                    } else {
                        val stock = stocks.find { it.gemType == price.gemType }
                        if (stock != null) {
                            if (price.value > stock.value || perkIsNotAvailable(perk)) {
                                perk.enable = false
                            }
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
        newEnemyHands.forEach { hand ->
            hand.perks.forEach { perk: Perk ->
                val stocks = arrayListOf<Stock>()
                enemyStockListInteractor.value()?.run {
                    stocks.addAll(this)
                }
                perk.enable = true
                perk.prices.forEach { price ->
                    /**
                     * навык доступен для применения если:
                     * очков больше, чем его цена
                     * он не на перезарядке
                     * выполняются все условия
                     * достаточно ресурсов
                     */
                    if (price.value == 0) {
                        if (perkIsNotAvailable(perk, false)) {
                            perk.enable = false
                        }
                    } else {
                        val stock = stocks.find { it.gemType == price.gemType }
                        if (stock != null) {
                            if (price.value > stock.value || perkIsNotAvailable(perk, false)) {
                                perk.enable = false
                            }
                        }
                    }
                }
            }
        }
        enemyHandsListInteractor.update(newEnemyHands)
    }

    private fun perkIsNotAvailable(perk: Perk, isHeroPerk: Boolean = true): Boolean {
        var notAllConditionAreMet = false
        if (perk.conditionsForEnable.isNotEmpty()) {
            perk.conditionsForEnable.forEach {
                if (!checkConditionExecutor.execute(it)) {
                    notAllConditionAreMet = true
                }
            }
        }
        var notEnoughResources = false
        if (perk.resources.isNotEmpty()) {
            val resourcesInteractor = if (isHeroPerk) {
                heroResourcesInteractor
            } else {
                enemyResourcesInteractor
            }
            perk.resources.forEach { perkResource ->
                val find = resourcesInteractor.value()
                    ?.find { it.name == perkResource.name }
                if (find != null) {
                    if (find.amount < perkResource.amount) {
                        notEnoughResources = true
                    }
                } else {
                    notEnoughResources = true
                }
            }
        }
        return perk.isReloading() || notAllConditionAreMet || notEnoughResources
    }

    fun updateShowStatus() {
        heroHandsListInteractor.value()?.run {
            showOrHideHand()
        }
        enemyHandsListInteractor.value()?.run {
            showOrHideHand()
        }
    }

    private fun ArrayList<Hand>.showOrHideHand() {
        this.forEach { hand ->
            var showHand = true
            hand.conditionsForDisplay.forEach { condition ->
                if (!checkConditionExecutor.execute(condition)) {
                    showHand = false
                }
            }
            hand.show = showHand
            if (hand.show) {
                hand.perks.forEach { perk ->
                    changePerkDisplay(perk)
                }
            }
        }
    }

    private fun changePerkDisplay(
        perk: Perk
    ) {
        val showPerk: Boolean = if (perk.currentCharges != null) {
            if (perk.currentCharges != 0) {
                checkConditionsForDisplay(perk)
            } else {
                false
            }
        } else {
            checkConditionsForDisplay(perk)
        }
        perk.show = showPerk
    }

    private fun checkConditionsForDisplay(perk: Perk): Boolean {
        var showPerk = true
        if (perk.conditionsForDisplay.isEmpty()) {
            showPerk = if (perk.conditionForDisplay != null) {
                checkConditionExecutor.execute(perk.conditionForDisplay)
            } else {
                true
            }
        } else {
            perk.conditionsForDisplay.forEach { condition ->
                if (!checkConditionExecutor.execute(condition)) {
                    showPerk = false
                }
            }
        }
        return showPerk
    }
}