package aldtoll.twiligihts.logic

import aldtoll.twiligihts.model.Hand
import aldtoll.twiligihts.model.Perk
import aldtoll.twiligihts.model.Stock
import aldtoll.twiligihts.storage.enemy.EnemyHandsListInteractor
import aldtoll.twiligihts.storage.enemy.EnemyInteractor
import aldtoll.twiligihts.storage.enemy.EnemyResourcesInteractor
import aldtoll.twiligihts.storage.enemy.EnemyStockListInteractor
import aldtoll.twiligihts.storage.hero.HeroHandsListInteractor
import aldtoll.twiligihts.storage.hero.HeroInteractor
import aldtoll.twiligihts.storage.hero.HeroResourcesInteractor
import aldtoll.twiligihts.storage.hero.HeroStockListInteractor
import javax.inject.Singleton

/**
 *
 */
@Singleton
class UpdatePerksStateExecutor(
    private val heroHandsListInteractor: HeroHandsListInteractor,
    private val enemyHandsListInteractor: EnemyHandsListInteractor,
    private val heroInteractor: HeroInteractor,
    private val enemyInteractor: EnemyInteractor,
    private val heroStockListInteractor: HeroStockListInteractor,
    private val enemyStockListInteractor: EnemyStockListInteractor,
    private val heroResourcesInteractor: HeroResourcesInteractor,
    private val enemyResourcesInteractor: EnemyResourcesInteractor,
    private val checkConditionExecutor: CheckConditionExecutor,
) {

    fun updateEnableStatus() {
        val hero = heroInteractor.value()
        val enemy = enemyInteractor.value()
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
                    //todo не очень удобно, что проход по стокам персонажа - иногда хочется иметь другие цвета, как 0
                    val stock = stocks.find { it.gemType == price.gemType }
                    if (stock != null) {
                        /**
                         * навык доступен для применения если:
                         * очков больше, чем его цена
                         * он не на перезарядке
                         * выполняются все условия
                         * достаточно ресурсов
                         */
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
                            perk.resources.forEach { perkResource ->
                                val find = heroResourcesInteractor.value()
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
                        //todo надо разделить цену и доступность
                        if (price.value > stock.value || perk.isReloading() || notAllConditionAreMet || notEnoughResources) {
                            perk.enable = false
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
                perk.enable = true
                if (perk.isReloading()) {
                    perk.enable = false
                }
            }
        }
        enemyHandsListInteractor.update(newEnemyHands)
    }

    fun updateShowStatus() {
        heroHandsListInteractor.value()?.run {
            this.forEach { hand ->
                hand.perks.forEach { perk ->
                    changePerkDisplay(perk)
                }
            }
        }
        enemyHandsListInteractor.value()?.run {
            this.forEach { hand ->
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