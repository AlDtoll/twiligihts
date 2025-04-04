package aldtoll.twiligihts.logic

import aldtoll.twiligihts.logic.database.WriteTemporaryLogExecutor
import aldtoll.twiligihts.model.BattleSettings
import aldtoll.twiligihts.model.ExecutedPerk
import aldtoll.twiligihts.model.Gem
import aldtoll.twiligihts.model.Perk
import aldtoll.twiligihts.model.Status
import aldtoll.twiligihts.model.effects.Effect
import aldtoll.twiligihts.model.findActiveStatuses
import aldtoll.twiligihts.storage.BattleLogListInteractor
import aldtoll.twiligihts.storage.EnemyMoveEventInteractor
import aldtoll.twiligihts.storage.ExecutedPerkInteractor
import aldtoll.twiligihts.storage.PersonInteractor
import aldtoll.twiligihts.storage.StartTimerAgainEventInteractor
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
    private val editStockExecutor: EditStockExecutor,
    private val updatePerksStateExecutor: UpdatePerksStateExecutor,
    private val turnNumberInteractor: TurnNumberInteractor,
    private val executedPerkInteractor: ExecutedPerkInteractor,
    private val startTimerAgainEventInteractor: StartTimerAgainEventInteractor,
    private val enemyMoveEventInteractor: EnemyMoveEventInteractor,
    private val applyAttackExecutor: ApplyAttackExecutor,
    private val writeTemporaryLogExecutor: WriteTemporaryLogExecutor,
) {

    /**
     * порядок раунда такой:
     * Ход игрока:
     * обновляются очки
     * очищаются щиты (если есть статусы на сбережение щитов, то применить здесь)
     * срабатывают статусы игрока: лечение, повреждение и т.д.
     * статусы героя с ограничением по раундам уменьшают свое значение длительности
     * игрок действует
     * попадания обновляются
     * ход переходит противнику
     * Ход противника:
     * обновляются очки
     * очищаются щиты
     * срабатывают статусы противника
     * статусы противника с ограничением по раундам уменьшают свое значение длительности
     * действует противник
     * попадания обновляются
     *
     */
    fun execute() {
        /**
         * этот метод вызвался после того как герой закончил действовать
         * очищаем попадания
         */
        clearHitsAndTouches(true)
        /**
         * в лог выводится информация, что противник начал действовать
         */
        battleLogListInteractor.add("${heroInteractor.value()?.name} закончил действовать")
        battleLogListInteractor.add("")
        battleLogListInteractor.add("Действует ${enemyInteractor.value()?.name}")
        /**
         * перед началом действий противника, его нужно приготовить
         */
        prepareEnemyBeforeActions()
        if (BattleSettings.MAKE_ENEMY_MOVE) {
            startEnemyMove()
        } else {
            startEnemyTurn()
        }
    }

    fun startEnemyTurn() {
        if (BattleSettings.ANIMATE_ENEMY_ACTIONS) {
            startEnemyActionWithAnimation()
        } else {
            enemyTurn()
            afterEnemyAction()
        }
    }

    private fun startEnemyMove() {
        enemyMoveEventInteractor.update(Unit)
    }

    private fun clearHitsAndTouches(isHero: Boolean) {
        val personInteractor = personInteractor(isHero)
        val person = personInteractor.value()
        person?.run {
            this.clearHitsAndTouches()
        }
    }

    /**
     * после того как противник закончил действовать, то ход переходит герою:
     * счетчик хода обновляется
     * перезаряжаются навыки
     * обновляются очки
     * чистятся щиты
     * применяются статусы
     * обновляются статусы
     *
     * после этого герой может действовать
     */
    private fun giveTurnToHero() {
        //todo очки должны обновляться перед ходом или после хода?
        turnNumberInteractor.increment()
        reloadPerksWithTurn()
        //todo здесь вызывается снова, потому что обновление enable вызывается только в prepareHeroForTurn
        /**
         * перки могут зависеть от значения хода, поэтому нужно обновлять их доступность после хода
         */
        updatePerksStateExecutor.updateEnableStatus()
        perkExecutor.updatePersonsStates()
        writeTemporaryLogExecutor.execute()
        battleLogListInteractor.add("")
        battleLogListInteractor.add("Ход ${turnNumberInteractor.value()}")
        editStockExecutor.updatePersonStocksAfterTurn()
        clearPersonShield(true)
        applyPersonStatus(true)
        updatePersonStatus(true)
        battleLogListInteractor.add("Действует ${heroInteractor.value()?.name}")
        startTimerAgainEventInteractor.update(Unit)
    }

    private fun reloadPerksWithTurn() {
        heroHandsListInteractor.value()?.run {
            this.forEach { hand ->
                hand.perks.forEach { perk ->
                    when (perk.reloadType) {
                        Perk.ReloadType.TURN -> {
                            if (perk.show && perk.isReloading()) {
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
        //todo разнести перезарядку
        enemyHandsListInteractor.value()?.run {
            this.forEach { hand ->
                hand.perks.forEach { perk ->
                    if (perk.reloadType == Perk.ReloadType.TURN) {
                        if (perk.show && perk.isReloading()) {
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
     * приняются эффекты статусов: урон и генерация
     * обнуляются щиты
     * обновляются статусы
     * потом противник начинает действовать
     */
    private fun enemyTurn() {
        /**
         * внутри вызывается [PerkExecutor.updatePersonsStates]
         */
        enemyActions()
    }

    /**
     * обновляются очки противника
     * очищаются щиты противника
     * срабатывают статусы противника
     * статусы противника с ограничением по раундам уменьшают свое значение длительности
     */
    private fun prepareEnemyBeforeActions() {
        editStockExecutor.updatePersonStocksAfterTurn(false)
        clearEnemyShields()
        applyPersonStatus(false)
        updatePersonStatus(false)
    }

    private fun clearEnemyHitsAndTouches() {
        clearHitsAndTouches(false)
    }

    /**
     * сначала урон
     * потом лечение
     * потом генерация очков
     * потом получение щитов
     */
    private fun applyPersonStatus(isHeroTarget: Boolean) {
        val personInteractor = personInteractor(isHeroTarget)
        val person = personInteractor.value()
        person?.run {
            /**
             * эффекты повреждений не попадают в зачет ударов
             * не изменяются броней, уязвимостями и т.д
             */
            val damageStatuses = this.statuses.findActiveStatuses(Status.StatusType.DAMAGE)
            damageStatuses.forEach {
                val message = "${it.name} действует и наносит ${it.value} урона"
                battleLogListInteractor.add(message, Gem.APPLY_STATUS_COLOR)
                val attack = Effect.Attack(
                    it.value,
                    Effect.Attack.Type.BOTH,
                    target = if (isHeroTarget) Effect.EffectTarget.ENEMY else Effect.EffectTarget.HERO
                )
                applyAttackExecutor.execute(person, attack, true)
            }
            /**
             * эффекты повреждений не попадают в зачет ударов
             * не изменяются броней, уязвимостями и т.д
             */
            val damageHpStatuses = this.statuses.findActiveStatuses(Status.StatusType.DAMAGE_HP)
            damageHpStatuses.forEach {
                val message = "${it.name} действует и наносит ${it.value} неблокируемого урона"
                battleLogListInteractor.add(message, Gem.APPLY_STATUS_COLOR)
                val attack = Effect.Attack(
                    it.value,
                    Effect.Attack.Type.HP,
                    target = if (isHeroTarget) Effect.EffectTarget.ENEMY else Effect.EffectTarget.HERO
                )
                applyAttackExecutor.execute(person, attack, true)
            }
            val healStatuses = this.statuses.findActiveStatuses(Status.StatusType.HEAL)
            healStatuses.forEach {
                val message = "${it.name} действует и восстанавливает ${it.value} урона"
                battleLogListInteractor.add(message, Gem.APPLY_STATUS_COLOR)
                person.increaseHp(it.value)
            }
            val generateStatuses = this.statuses.findActiveStatuses(Status.StatusType.GENERATE)
            generateStatuses.forEach { status ->
                status.gemType?.run {
                    val message = "${status.name} действует и создает ${status.value} очков"
                    battleLogListInteractor.add(message, Gem.APPLY_STATUS_COLOR)
                    editStockExecutor.updateStocks(Pair(status.gemType, status.value), isHeroTarget)
                }
                status.gemTypes.forEach { gemType ->
                    val message = "${status.name} действует и создает ${status.value} очков ${
                        Gem.getName(gemType)
                    }"
                    battleLogListInteractor.add(message, Gem.APPLY_STATUS_COLOR)
                    editStockExecutor.updateStocks(Pair(gemType, status.value), isHeroTarget)
                }
            }
            val defendStatuses = this.statuses.findActiveStatuses(Status.StatusType.DEFEND)
            defendStatuses.forEach {
                val message = "${it.name} действует и создает ${it.value} щитов"
                battleLogListInteractor.add(message, Gem.APPLY_STATUS_COLOR)
                person.shield += it.value
            }
        }
    }

    private fun startEnemyActionWithAnimation() {
        val enemyHands = enemyHandsListInteractor.value()
        val showedHands = enemyHands?.filter { it.show }
        showedHands?.run {
            if (this.isEmpty()) {
                afterEnemyAction()
            } else {
                this.first().run {
                    val hand = this
                    this.perks.first().run {
                        executedPerkInteractor.update(ExecutedPerk(this, hand))
                    }
                }
            }
        }
    }

    private fun enemyActions() {
        val enemyHands = enemyHandsListInteractor.value()
        enemyHands?.run {
            this.forEach { hand ->
                /**
                 * если рука видна, то использовать ее
                 */
                if (hand.show) {
                    hand.perks.forEach { perk: Perk ->
                        /**
                         * если перк показан и доступен, то использовать его
                         */
                        if (perk.show && perk.enable) {
                            perkExecutor.messageAboutUsedPerk(perk, false)
                            perkExecutor.execute(perk)
                        }
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

    /**
     * после действий противника:
     * нужно очистить попадания по противнику, которые могли быть получены в результате контратак
     * после этого ход переходит игроку
     */
    fun afterEnemyAction() {
        clearEnemyHitsAndTouches()
        giveTurnToHero()
    }
}