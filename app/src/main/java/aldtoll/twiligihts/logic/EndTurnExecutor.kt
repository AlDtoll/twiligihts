package aldtoll.twiligihts.logic

import aldtoll.twiligihts.model.Perk
import aldtoll.twiligihts.model.Status
import aldtoll.twiligihts.storage.BattleLogListInteractor
import aldtoll.twiligihts.storage.PersonInteractor
import aldtoll.twiligihts.storage.TurnNumberInteractor
import aldtoll.twiligihts.storage.enemy.EnemyHandsListInteractor
import aldtoll.twiligihts.storage.enemy.EnemyInteractor
import aldtoll.twiligihts.storage.hero.HeroHandsListInteractor
import aldtoll.twiligihts.storage.hero.HeroInteractor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EndTurnExecutor @Inject constructor(
    private val perkExecutor: PerkExecutor,
    private val enemyInteractor: EnemyInteractor,
    private val heroHandsListInteractor: HeroHandsListInteractor,
    private val enemyHandsListInteractor: EnemyHandsListInteractor,
    private val heroInteractor: HeroInteractor,
    private val battleLogListInteractor: BattleLogListInteractor,
    private val updateStockExecutor: UpdateStockExecutor,
    private val turnNumberInteractor: TurnNumberInteractor,
) {

    /**
     * в конце хода игрока ход переходит противнику
     */
    fun execute() {
        battleLogListInteractor.add("${heroInteractor.value()?.name} закончил действовать")
        battleLogListInteractor.add("")
        battleLogListInteractor.add("Действует ${enemyInteractor.value()?.name}")
        enemyTurn()
        //todo у героя не сработает начальный статус на генерацию щитов
        prepareHeroForTurn()
        turnNumberInteractor.increment()
        reloadPerksWithTurn()
        //todo здесь вызывается снова, потому что обновление enable вызывается только в prepareHeroForTurn
        updateStockExecutor.updatePerksState()
        perkExecutor.updatePersonsStates()
        battleLogListInteractor.add("")
        battleLogListInteractor.add("Ход ${turnNumberInteractor.value()}")
        battleLogListInteractor.add("Действует ${heroInteractor.value()?.name}")
    }

    private fun reloadPerksWithTurn() {
        heroHandsListInteractor.value()?.run {
            this.forEach { hand ->
                hand.perks.forEach { perk ->
                    when (perk.reloadType) {
                        Perk.ReloadType.TURN -> {
                            if (perk.isReloading()) {
                                perk.reload = perk.reload + 1
                            }
                        }

                        Perk.ReloadType.PERK -> {
                            if (perk.isReloading()) {
                                perk.reload = perk.coolDown ?: 0
                            }
                        }

                        Perk.ReloadType.COMBO -> {
                            perk.reload = 0
                        }
                    }
                }
            }
        }
        enemyHandsListInteractor.value()?.run {
            this.forEach { hand ->
                hand.perks.forEach { perk ->
                    if (perk.reloadType == Perk.ReloadType.TURN) {
                        if (perk.isReloading()) {
                            perk.reload = perk.reload + 1
                        }
                    } else {
                        if (perk.isReloading()) {
                            perk.reload = perk.coolDown ?: 0
                        }
                    }
                }
            }
        }
    }

    /**
     * перед началом действий противника:
     * обнуляются щиты
     * приняются эффекты статусов: урон и генерация
     * обновляются статусы
     * потом противник начинает действовать
     */
    private fun enemyTurn() {
        clearEnemyShields()
        applyPersonStatus(false)
        updatePersonStatus(false)
        /**
         * внутри вызывается [PerkExecutor.updatePersonsStates]
         */
        enemyActions()
    }

    private fun applyPersonStatus(isHeroTarget: Boolean) {
        val personInteractor = personInteractor(isHeroTarget)
        val person = personInteractor.value()
        person?.run {
            val damageStatuses = this.statuses.filter { it.type == Status.EffectType.DAMAGE }
            damageStatuses.forEach {
                val message = "${it.name} действует и наносит ${it.value} урона"
                battleLogListInteractor.add(message)
                person.decreaseHp(it.value)
            }
            val healStatuses = this.statuses.filter { it.type == Status.EffectType.HEAL }
            healStatuses.forEach {
                val message = "${it.name} действует и восстанавливает ${it.value} урона"
                battleLogListInteractor.add(message)
                person.increaseHp(it.value)
            }
            val generateStatus = this.statuses.filter { it.type == Status.EffectType.GENERATE }
            generateStatus.forEach {
                it.gemType?.run {
                    //todo gemType
                    val message = "${it.name} действует и создает ${it.value} очков"
                    battleLogListInteractor.add(message)
                    updateStockExecutor.updateStocks(Pair(it.gemType, it.value))
                }
            }
            val defendStatus = this.statuses.filter { it.type == Status.EffectType.DEFEND }
            defendStatus.forEach {
                it.gemType?.run {
                    val message = "${it.name} действует и создает ${it.value} щитов"
                    battleLogListInteractor.add(message)
                    person.increaseHp(it.value)
                }
            }
            personInteractor.update(this)
        }
    }

    private fun prepareHeroForTurn() {
        clearPersonShield(true)
        applyPersonStatus(true)
        updatePersonStatus(true)
        updateStockExecutor.updateHeroStocksAfterTurn()
    }

    private fun enemyActions() {
        val enemyHands = enemyHandsListInteractor.value()
        enemyHands?.run {
            this.forEach { hand ->
                hand.perks.forEach { perk: Perk ->
                    /**
                     * пока тут используется видимость
                     * в дальнейшем будет отдельное условие для доступности
                     * //todo создать conditionForEnable
                     */
                    if (perk.show && perk.enable) {
                        perkExecutor.execute(perk)
                    }
                }
            }
        }
    }

    private fun clearEnemyShields() {
        clearPersonShield(false)
    }

    private fun clearPersonShield(isHeroTarget: Boolean) {
        val personInteractor = personInteractor(isHeroTarget)
        val person = personInteractor.value()
        person?.run {
            this.shield = 0
            this.hits = 0
            this.touches = 0
            personInteractor.update(this)
        }
    }

    private fun personInteractor(isHeroTarget: Boolean): PersonInteractor {
        val personInteractor = if (isHeroTarget) {
            heroInteractor
        } else {
            enemyInteractor
        }
        return personInteractor
    }

    private fun updatePersonStatus(isHeroTarget: Boolean) {
        val personInteractor = personInteractor(isHeroTarget)
        val person = personInteractor.value()
        person?.run {
            val newPerson = this.recreate()
            newPerson.statuses.forEach {
                if (it.isActive()) {
                    if (it.duration != -1) {
                        it.duration = it.duration - 1
                    }
                    if (it.duration == 0) {
                        it.value = 0
                    }
                }
            }
            personInteractor.update(newPerson)
        }
    }
}