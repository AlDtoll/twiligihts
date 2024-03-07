package aldtoll.twiligihts.logic

import aldtoll.twiligihts.model.Condition
import aldtoll.twiligihts.model.Effect
import aldtoll.twiligihts.model.Perk
import aldtoll.twiligihts.model.Status
import aldtoll.twiligihts.model.characters.Enemy
import aldtoll.twiligihts.model.characters.Hero
import aldtoll.twiligihts.model.characters.Person
import aldtoll.twiligihts.storage.BattleLogListInteractor
import aldtoll.twiligihts.storage.EnemyHandsListInteractor
import aldtoll.twiligihts.storage.EnemyInteractor
import aldtoll.twiligihts.storage.GoToFinishScreenInteractor
import aldtoll.twiligihts.storage.HeroHandsListInteractor
import aldtoll.twiligihts.storage.HeroInteractor
import aldtoll.twiligihts.storage.PersonInteractor
import aldtoll.twiligihts.storage.TurnNumberInteractor
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class PerkExecutor @Inject constructor(
    private val updateStockExecutor: UpdateStockExecutor,
    private val heroInteractor: HeroInteractor,
    private val enemyInteractor: EnemyInteractor,
    private val battleLogListInteractor: BattleLogListInteractor,
    private val heroHandsListInteractor: HeroHandsListInteractor,
    private val enemyHandsListInteractor: EnemyHandsListInteractor,
    private val turnNumberInteractor: TurnNumberInteractor,
    private val goToFinishScreenInteractor: GoToFinishScreenInteractor
) {

    private var perk: Perk? = null
    private var isHeroPerk = false

    /**
     * при выполнении перка:
     * если нужно, то платим за него
     * применяем эффекты перка
     * это могла привести к изменению в дебаффах - применяем их
     * после этого проверяем отображение навыков
     */
    fun execute(perk: Perk, isHero: Boolean = false) {
        //todo! важно! perk это не perk из руки, а его копия.
        // Если его изменить, это не отобразится на перках руки
        this.perk = perk
        this.isHeroPerk = isHero
        if (isHero) {
            payPerkPrice(perk)
        }
        executePerkEffects(perk)

        //todo т.к. щиты чистятся перед ходом противника, то проверят по ним касание нельзя
        updatePersonsStates()
    }

    /**
     * при снижениее здоровья до какого процента, добавляет статус
     * подразумевается, что это будет дефаф, но может быть и ярость
     */
    private fun applyDebuffes() {
        val hero = heroInteractor.value()
        val enemy = enemyInteractor.value()
        hero?.run {
            applyDebuffes()
            heroInteractor.update(this)
        }
        enemy?.run {
            applyDebuffes()
            enemyInteractor.update(this)
        }
    }

    /**
     * применяем статусы к персонажу, если выполняется условие
     * для каждого дебафа смотрим
     * если условие выполняется, то
     * нужно проверить не был ли уже такой статус добавлен,
     * если нет, то добавить его.
     * Если условие не выполняется, то нужно убрать статус
     */
    private fun Person.applyDebuffes() {
        debuffes.forEach {
            //todo сейчас проверяется только условие для самого персонажа
            if (checkConditionForPerson(it.condition)) {
                if (!this.statuses.contains(it.status)) {
                    this.statuses.add(it.status)
                }
            } else {
                this.statuses.remove(it.status)
            }
        }
    }

    /**
     * после изменения параметров боя (ход, действия персонажей)
     * надо обновить состояние перков и применить дебафы
     */
    fun updatePersonsStates() {
        applyDebuffes()
        changePerksDisplay()
    }

    private fun changePerksDisplay() {
        val hero = heroInteractor.value()
        val enemy = enemyInteractor.value()
        heroHandsListInteractor.value()?.run {
            this.forEach { hand ->
                hand.perks.forEach { perk ->
                    changePerkDisplay(perk, enemy, hero)
                }
            }
        }
        enemyHandsListInteractor.value()?.run {
            this.forEach { hand ->
                hand.perks.forEach { perk ->
                    changePerkDisplay(perk, enemy, hero)
                }
            }
        }
    }

    private fun changePerkDisplay(
        perk: Perk,
        enemy: Enemy?,
        hero: Hero?
    ) {
        //todo когда появится перезарядка, то переделать
        if (perk == this.perk) {
            perk.decreaseCharges()
        }
        var showPerk = true
        if (perk.currentCharges != null) {
            if (perk.currentCharges != 0) {
                if (perk.conditionsForDisplay.isEmpty()) {
                    showPerk =
                        perk.conditionForDisplay?.checkConditionIsMet(enemy!!, hero!!) ?: true
                } else {
                    perk.conditionsForDisplay.forEach {
                        if (!it.checkConditionIsMet(enemy!!, hero!!)) {
                            showPerk = false
                        }
                    }
                }
            } else {
                showPerk = false
            }
        } else {
            if (perk.conditionsForDisplay.isEmpty()) {
                showPerk =
                    perk.conditionForDisplay?.checkConditionIsMet(enemy!!, hero!!) ?: true
            } else {
                perk.conditionsForDisplay.forEach {
                    if (!it.checkConditionIsMet(enemy!!, hero!!)) {
                        showPerk = false
                    }
                }
            }
        }
        perk.show = showPerk
    }

    private fun executePerkEffects(perk: Perk) {
        val perkMessage = if (isHeroPerk) {
            "Герой применяет ${perk.name}:${perk.description}"
        } else {
            "Противник применяет ${perk.name}:${perk.description}"
        }
        battleLogListInteractor.add(perkMessage)
        perk.effects.forEach { originalEffect ->
            val hero = heroInteractor.value()
            val enemy = enemyInteractor.value()
            if (originalEffect.condition != null) {
                if (originalEffect.condition!!.checkConditionIsMet(enemy!!, hero!!)) {
                    applyEffect(originalEffect, enemy, hero)
                }
            } else {
                applyEffect(originalEffect, enemy, hero)
            }
        }
    }

    private fun Condition.checkConditionIsMet(
        enemy: Enemy,
        hero: Hero
    ): Boolean {
        return when (this.target) {
            Effect.EffectTarget.ENEMY -> {
                return enemy.checkConditionForPerson(this)
            }

            Effect.EffectTarget.HERO -> hero.checkConditionForPerson(this)
            Effect.EffectTarget.ALL -> {
                return enemy.checkConditionForPerson(this)
                        && hero.checkConditionForPerson(this)
            }
        }
    }

    private fun Person.checkConditionForPerson(
        condition: Condition
    ): Boolean {
        val valueForCompare = when (condition.parameter) {
            Condition.Parameter.HP -> this.hp
            Condition.Parameter.SP -> this.shield
            Condition.Parameter.STATUS -> this.statuses.find { it.name == condition.name }?.value
                ?: 0

            Condition.Parameter.TURN -> turnNumberInteractor.value() ?: 0
            Condition.Parameter.HP_P -> this.hp * 100 / maxHp
        }
        return when (condition.symbol) {
            Condition.Symbol.MORE -> valueForCompare > condition.value
            Condition.Symbol.LESS -> valueForCompare < condition.value
            Condition.Symbol.EQUALS -> valueForCompare == condition.value
            Condition.Symbol.HAVE -> valueForCompare > 0
            Condition.Symbol.EMPTY -> valueForCompare == 0
        }
    }

    private fun applyEffect(
        originalEffect: Effect,
        enemy: Enemy?,
        hero: Hero?
    ) {
        /**
         * для атак направленных против себя статусы не применяются, т.к. это аналог жертвы
         */
        val selfTarget = isHeroPerk && originalEffect.target == Effect.EffectTarget.HERO ||
                !isHeroPerk && originalEffect.target == Effect.EffectTarget.ENEMY
        val effect = if (selfTarget) {
            originalEffect
        } else {
            changeEffectByPersonsStatuses(originalEffect)
        }
        when (effect) {
            is Effect.Attack -> {
                when (effect.target) {
                    Effect.EffectTarget.ENEMY -> {
                        attackPerson(effect, enemy!!)
                    }

                    Effect.EffectTarget.HERO -> {
                        attackPerson(effect, hero!!)
                    }

                    Effect.EffectTarget.ALL -> {
                        attackPerson(effect, hero!!, enemy!!)
                    }
                }
            }

            is Effect.Defend -> {
                when (effect.target) {
                    Effect.EffectTarget.ENEMY -> {
                        defendPerson(effect, false)
                    }

                    Effect.EffectTarget.HERO -> {
                        defendPerson(effect, true)
                    }

                    Effect.EffectTarget.ALL -> {
                        defendPerson(effect, false)
                        defendPerson(effect, true)
                    }
                }
            }

            is Effect.EditStatus -> {
                when (originalEffect.target) {
                    Effect.EffectTarget.ENEMY -> {
                        editPersonStatus(effect, false)
                    }

                    Effect.EffectTarget.HERO -> {
                        editPersonStatus(effect, true)
                    }

                    Effect.EffectTarget.ALL -> {
                        editPersonStatus(effect, false)
                        editPersonStatus(effect, true)
                    }
                }
            }

            is Effect.ChangeStock -> {
                /**
                 * сначала отнимает, потом половинится в конце хода
                 */
                updateStockExecutor.updateStocks(Pair(effect.gemType, effect.value))
            }

            is Effect.Heal -> {
                val persons = arrayListOf<Person>()
                when (originalEffect.target) {
                    Effect.EffectTarget.ENEMY -> {
                        persons.add(enemy!!)
                    }

                    Effect.EffectTarget.HERO -> {
                        persons.add(hero!!)
                    }

                    Effect.EffectTarget.ALL -> {
                        persons.add(hero!!)
                        persons.add(enemy!!)
                    }
                }
                healPerson(effect, *persons.toTypedArray())
            }

            is Effect.FinishBattle -> {
                goToFinishScreenInteractor.update(Pair(true, effect.ask))
            }
        }
    }

    private fun healPerson(heal: Effect.Heal, vararg persons: Person) {
        persons.forEach { person: Person ->
            val isHeroTarget = person is Hero
            val personInteractor = personInteractor(isHeroTarget)
            person.run {
                healDamage(heal)
                personInteractor.update(person)
            }
        }
    }

    private fun attackPerson(attack: Effect.Attack, vararg persons: Person) {
        persons.forEach { person: Person ->
            val isHeroTarget = person is Hero
            val personInteractor = personInteractor(isHeroTarget)
            person.run {
                /**
                 * при атаке персонажа ищем у него активный статус, который позволяет избежать атаки
                 * [Status.EffectType.DODGE] или [Status.EffectType.SMART_DODGE]
                 */
                val dodgeStatus =
                    this.statuses.find { status: Status -> (status.type == Status.EffectType.DODGE || status.type == Status.EffectType.SMART_DODGE) && status.isActive() }
                if (dodgeStatus != null) {
                    if (dodgeStatus.smartValue != null) {
                        if (attack.value > dodgeStatus.smartValue) {
                            dodge(isHeroTarget, dodgeStatus)
                        }
                    } else {
                        dodge(isHeroTarget, dodgeStatus)
                    }
                } else {
                    applyAttack(attack)
                }
                /**
                 * для атак направленных против себя контратака не применяется
                 */
                val selfTarget = isHeroTarget && isHeroPerk || !isHeroTarget && !isHeroPerk
                if (!selfTarget) {
                    /**
                     *  при атаке персонажа ищем у него активный статус, который наносит урон в ответ, типа
                     *  [Status.EffectType.COUNTERATTACK] или [Status.EffectType.HARM]
                     */
                    val answerStatus =
                        this.statuses.find { status: Status -> (status.type == Status.EffectType.COUNTERATTACK || status.type == Status.EffectType.HARM) && status.isActive() }
                    if (answerStatus != null) {
                        answerOnAttack(answerStatus)
                    }
                }
                personInteractor.update(person)
            }
        }
    }

    private fun Person.applyAttack(
        attack: Effect.Attack
    ) {
        val damageForSp = countDamageForSp(attack)
        val damageBlockedByShield = damageShields(damageForSp)
        val damageForHp = damageForHp(attack, damageBlockedByShield)
        damageHp(damageForHp)
    }

    private fun Person.healDamage(
        heal: Effect.Heal
    ) {
        val isHeroTarget = this is Hero
        var message = ""
        message += if (isHeroTarget) {
            "Герой "
        } else {
            "Противник "
        }
        message += "восстанавливает ${heal.value} здоровья. "
        if (this.hp + heal.value > this.maxHp) {
            message += "Здоровье полностью восстановлено"
        }
        battleLogListInteractor.add(message)
        this.increaseHp(heal.value)
    }

    /**
     * изменение силы навыков в зависимости от статусов сражающихся
     */
    private fun changeEffectByPersonsStatuses(effect: Effect): Effect {
        val effectForChange = effect.copyEffect()
        val effectChangeByHeroStatuses = effectChangeByPersonStatuses(effectForChange, true)
        val effectChangeByEnemyStatuses =
            effectChangeByPersonStatuses(effectChangeByHeroStatuses, false)
        if (effectChangeByEnemyStatuses is Effect.Attack) {
            effectChangeByEnemyStatuses.value = effectChangeByEnemyStatuses.value.coerceAtLeast(0)
        }
        return effectChangeByEnemyStatuses
    }

    private fun effectChangeByPersonStatuses(
        effectForChange: Effect,
        isHeroTarget: Boolean
    ): Effect {
        val effect = effectForChange.copyEffect()
        val personInteractor = personInteractor(isHeroTarget)
        val person = personInteractor.value()
        person?.run {
            val statuses = this.statuses
            if (statuses.isNotEmpty()) {
                statuses.forEach { status ->
                    if (status.isActive()) {
                        val isPersonPerk = if (isHeroTarget) {
                            //todo тоже нужно сделать параметр, который игнорирует навыки
                            isHeroPerk
                        } else {
                            !isHeroPerk && !effect.place
                        }
                        if (isPersonPerk) {
                            if (status.type == Status.EffectType.WEAK || status.type == Status.EffectType.REDUCE) {
                                when (effect) {
                                    is Effect.Attack -> effect.value =
                                        decreaseEffectValueByStatus(effect, status)

                                    else -> {}
                                }
                                if (status.type == Status.EffectType.REDUCE) {
                                    status.value = 0
                                }

                            }
                            if (status.type == Status.EffectType.STRONG || status.type == Status.EffectType.GAIN) {
                                when (effect) {
                                    is Effect.Attack -> effect.value =
                                        effect.value + status.value

                                    else -> {}
                                }
                                if (status.type == Status.EffectType.GAIN) {
                                    status.value = 0
                                }
                            }
                        }
                        val isPersonTarget = if (isHeroTarget) {
                            Effect.EffectTarget.HERO
                        } else {
                            Effect.EffectTarget.ENEMY
                        }
                        if (effect.target == isPersonTarget || effect.target == Effect.EffectTarget.ALL) {
                            if (status.type == Status.EffectType.VULNERABLE) {
                                when (effect) {
                                    is Effect.Attack -> effect.value =
                                        effect.value + status.value

                                    else -> {}
                                }
                            }
                            if (status.type == Status.EffectType.ARMOR) {
                                when (effect) {
                                    is Effect.Attack ->
                                        effect.value = decreaseEffectValueByStatus(effect, status)

                                    else -> {}
                                }
                            }
                        }
                        personInteractor.update(person)
                    }
                }
            }
        }
        return effect
    }

    private fun decreaseEffectValueByStatus(
        attack: Effect.Attack,
        status: Status
    ): Int {
        val i = attack.value - status.value
        return i
    }

    private fun damageForHp(
        attack: Effect.Attack,
        damageBlockedByShield: Int
    ): Int {
        return when (attack.type) {
            Effect.Attack.Type.BOTH -> attack.value - damageBlockedByShield
            Effect.Attack.Type.HP -> attack.value
            Effect.Attack.Type.SP -> 0
        }
    }

    private fun dodge(
        isHeroTarget: Boolean,
        dodgeStatus: Status
    ) {
        var message = ""
        message += if (isHeroTarget) {
            "Герой "
        } else {
            "Противник "
        }
        message += "уворачивается."
        dodgeStatus.value = dodgeStatus.value - 1
        battleLogListInteractor.add(message)
    }

    /**
     * сейчас в отве наносится только урон, но можно делать что-то еще
     */
    private fun Person.answerOnAttack(
        counterAttackStatus: Status,
    ) {
        val isHeroTarget = this is Hero
        isHeroPerk = isHeroTarget
        var message = ""
        message += if (isHeroTarget) {
            "Герой "
        } else {
            "Противник "
        }
        message += "в ответ наносит ${counterAttackStatus.value} урона."
        battleLogListInteractor.add(message)
        val attack = Effect.Attack(
            counterAttackStatus.value,
            Effect.Attack.Type.BOTH,
            target = if (isHeroTarget) Effect.EffectTarget.ENEMY else Effect.EffectTarget.HERO
        )
        //todo здесь надо разграничивать HARM и COUNTERATTACK
        val effectChangeByPersonStatuses = changeEffectByPersonsStatuses(attack)

        val personInteractor = personInteractor(!isHeroTarget)
        personInteractor.value()?.run {
            //todo сейчас при контр атаке будет проигнорировано уклонение,
            // может надо на attackPerson заменить, но тогда будут проблемы,
            // что контратаки будут друг друга бить
            this.applyAttack(effectChangeByPersonStatuses as Effect.Attack)
            personInteractor.update(this)
        }
    }

    private fun Person.damageHp(
        damage: Int
    ) {
        val isHeroTarget = this is Hero
        var message = ""
        message += if (isHeroTarget) {
            "Герой "
        } else {
            "Противник "
        }
        message += "получает $damage урона. "
        battleLogListInteractor.add(message)
        this.decreaseHp(damage)
        updateStockExecutor.updateStockAfterDamage()
        if (damage > 0) {
            //inflictWound(damageForHp, isHeroTarget)
        }
    }

    private fun Person.damageShields(
        damage: Int
    ): Int {
        var message = ""
        val damageForSp: Int = if (damage >= this.shield) {
            this.shield
        } else {
            damage
        }
        if (this.shield > 0) {
            message += "Щиты блокируют $damageForSp урона. "
            if (damageForSp >= this.shield && damageForSp > 0) {
                message += "Щиты уничтожены. "
                this.shield = 0
            } else {
                this.shield = this.shield - damageForSp
            }
        }
        if (message.isNotEmpty()) {
            battleLogListInteractor.add(message)
        }
        return damageForSp
    }

    private fun Person.inflictWound(damageForHp: Int, isHeroTarget: Boolean) {
        val message = if (isHeroTarget) {
            "Герой получает рану"
        } else {
            "Противник получает рану"
        }
        if (damageForHp > this.hp) {
            battleLogListInteractor.add(message)
            this.wounds = this.wounds + 1
        } else {
            val percentOfDamage = 100 * damageForHp / this.hp
            val r = Random.nextInt(1, 100)
            if (r < percentOfDamage) {
                battleLogListInteractor.add(message)
                this.wounds = this.wounds + 1
            }
        }
    }

    private fun defendPerson(defend: Effect.Defend, isHeroTarget: Boolean) {
        val personInteractor = personInteractor(isHeroTarget)
        val person = personInteractor.value()
        person?.run {
            this.shield = this.shield + defend.value
            personInteractor.update(person)
        }
    }

    private fun countDamageForSp(attack: Effect.Attack): Int {
        return when (attack.type) {
            Effect.Attack.Type.BOTH -> attack.value
            Effect.Attack.Type.HP -> 0
            Effect.Attack.Type.SP -> attack.value
        }
    }

    private fun editPersonStatus(effect: Effect.EditStatus, isHeroTarget: Boolean) {
        val personInteractor = personInteractor(isHeroTarget)
        val person = personInteractor.value()
        person?.run {
            val newPerson = this.recreate()
            effect.status.let { effectStatus ->
                val statusForChange =
                    newPerson.statuses.find { personStatus -> personStatus.name == effectStatus.name }
                if (statusForChange != null) {
                    statusForChange.duration = effectStatus.duration
                    when (effect.type) {
                        Effect.EditStatus.Type.SET -> {
                            statusForChange.value = effectStatus.value
                        }

                        Effect.EditStatus.Type.CHANGE -> statusForChange.value =
                            statusForChange.value + effectStatus.value

                    }
                } else {
                    newPerson.statuses.add(effectStatus.copy())
                }
            }
            personInteractor.update(newPerson)
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

    private fun payPerkPrice(perk: Perk) {
        updateStockExecutor.payPriceForPerk(perk)
    }
}