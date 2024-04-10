package aldtoll.twiligihts.logic

import aldtoll.twiligihts.model.BattleSettings
import aldtoll.twiligihts.model.Condition
import aldtoll.twiligihts.model.Effect
import aldtoll.twiligihts.model.Gem
import aldtoll.twiligihts.model.Perk
import aldtoll.twiligihts.model.Status
import aldtoll.twiligihts.model.characters.Enemy
import aldtoll.twiligihts.model.characters.Hero
import aldtoll.twiligihts.model.characters.Person
import aldtoll.twiligihts.storage.BattleLogListInteractor
import aldtoll.twiligihts.storage.ExecutedPerkInteractor
import aldtoll.twiligihts.storage.GoToFinishScreenInteractor
import aldtoll.twiligihts.storage.PersonInteractor
import aldtoll.twiligihts.storage.TurnNumberInteractor
import aldtoll.twiligihts.storage.enemy.EnemyHandsListInteractor
import aldtoll.twiligihts.storage.enemy.EnemyInteractor
import aldtoll.twiligihts.storage.enemy.EnemyStatesInteractor
import aldtoll.twiligihts.storage.hero.HeroHandsListInteractor
import aldtoll.twiligihts.storage.hero.HeroInteractor
import aldtoll.twiligihts.storage.hero.HeroStatesInteractor
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
    private val goToFinishScreenInteractor: GoToFinishScreenInteractor,
    private val enemyStatesInteractor: EnemyStatesInteractor,
    private val heroStatesInteractor: HeroStatesInteractor,
    private val executedPerkInteractor: ExecutedPerkInteractor,
) {

    private var perk: Perk? = null
    private var isHeroPerk = false
    private val hero = heroInteractor.value()
    private val enemy = enemyInteractor.value()

    /**
     * при выполнении перка:
     * проверяется вероятность выполнение
     * если перк выполняется:
     * нужно произвести перезарядку перков (этот перк может влиять на другие)
     * нужно потратить ресурсы (заряды)
     * нужно выставить перезарядку в 0 для данного навыка
     * отобразить изменения в перках
     * если нужно, то заплатить за перк
     * применяем эффекты перка
     * это могла привести к изменению в дебаффах - применяем их
     * для действий не героя - запускается следуюий перк
     */
    fun execute(perk: Perk, isHero: Boolean = false) {
        val numberForCompareWithPerkProbability = Random.nextInt(0, 101)
        /**
         * дефолтная вероятность применения навыка 100%
         */
        if (numberForCompareWithPerkProbability <= perk.probability) {
            /**
             * важно! perk это не perk из руки, а его копия.
             * Если его изменить, это не отобразится на перках руки
             */
            this.perk = perk
            this.isHeroPerk = isHero
            reloadPerksAfterUse()
            usePerkCharge()
            ifPerkHasReloadDownTimeIt()
            changePerksDisplay()
            if (isHero) {
                payPerkPrice(perk)
            }
            executePerkEffects(perk)

            updatePersonsStates()
            /**
             * если это автоматические действия противника, то нужно вызвать следующий перк
             */
            if (!isHero && BattleSettings.ANIMATE_ENEMY_ACTIONS) {
                callNextPerk(perk)
            }
        }
    }

    private fun ifPerkHasReloadDownTimeIt() {
        heroHandsListInteractor.value()?.run {
            this.forEach { hand ->
                hand.perks.forEach { perk ->
                    if (perk.isSame(this@PerkExecutor.perk)) {
                        perk.reload = 0
                    }
                }
            }
        }
        enemyHandsListInteractor.value()?.run {
            this.forEach { hand ->
                hand.perks.forEach { perk ->
                    if (perk.isSame(this@PerkExecutor.perk)) {
                        perk.reload = 0
                    }
                }
            }
        }
    }

    private fun usePerkCharge() {
        heroHandsListInteractor.value()?.run {
            this.forEach { hand ->
                hand.perks.forEach { perk ->
                    if (perk == this@PerkExecutor.perk) {
                        perk.decreaseCharges()
                    }
                }
            }
        }
        enemyHandsListInteractor.value()?.run {
            this.forEach { hand ->
                hand.perks.forEach { perk ->
                    if (perk == this@PerkExecutor.perk) {
                        perk.decreaseCharges()
                    }
                }
            }
        }
    }

    fun callNextPerk(currentPerk: Perk) {
        val enemyHands = enemyHandsListInteractor.value()
        enemyHands?.run {
            /**
             * находим какой руке принадлежал навык
             */
            val find = enemyHands.find {
                it.perks.any { perk -> perk == currentPerk }
            }
            /**
             * если в руке есть еще навыки, то надо использовать их
             */
            find?.run {
                val indexOfCurrentPerk = find.perks.indexOf(currentPerk)
                /**
                 * если есть следующий навык, то использовать его
                 * если нет, то взять первый навык следующей руки
                 * если рука была последняя, то конец действий противника
                 */
                if (indexOfCurrentPerk != -1 && indexOfCurrentPerk + 1 < find.perks.size) {
                    val nextPerk = find.perks[indexOfCurrentPerk + 1]
                    executedPerkInteractor.update(Pair(nextPerk, this.gemType))
                } else {
                    val indexOfCurrentHand = enemyHands.indexOf(find)
                    if (indexOfCurrentHand != -1 && indexOfCurrentHand + 1 < enemyHands.size) {
                        val nextPerk = enemyHands[indexOfCurrentHand + 1].perks[0]
                        executedPerkInteractor.update(
                            Pair(
                                nextPerk,
                                enemyHands[indexOfCurrentHand + 1].gemType
                            )
                        )
                    } else {
                        executedPerkInteractor.update(
                            Pair(
                                Perk(
                                    name = Perk.LAST,
                                    arrayListOf(),
                                    arrayListOf()
                                ),
                                0
                            )
                        )
                    }
                }
            }
        }

    }

    private fun reloadPerksAfterUse() {
        heroHandsListInteractor.value()?.run {
            this.forEach { hand ->
                hand.perks.forEach { perk ->
                    perk.reloadPerkAfterUse()
                }
            }
        }
        enemyHandsListInteractor.value()?.run {
            this.forEach { hand ->
                hand.perks.forEach { perk ->
                    perk.reloadPerkAfterUse()
                }
            }
        }
    }

    /**
     * при снижениее здоровья до какого процента, добавляет статус
     * подразумевается, что это будет дефаф, но может быть и ярость
     */
    private fun applyStates() {
        val hero = heroInteractor.value()
        val enemy = enemyInteractor.value()
        hero?.run {
            val newPerson = this.recreate()
            newPerson.applyStates()
            heroInteractor.update(newPerson)
        }
        enemy?.run {
            val newPerson = this.recreate()
            newPerson.applyStates()
            enemyInteractor.update(newPerson)
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
    private fun Person.applyStates() {
        val states = if (this is Hero) {
            heroStatesInteractor.value()
        } else {
            enemyStatesInteractor.value()
        }
        states?.forEach { state ->
            //todo сейчас проверяется только условие для самого персонажа
            val statuses = this.statuses
            if (checkConditionForPerson(state.condition)) {
                val find = statuses.find { it.name == state.status.name }
                if (find == null) {
                    statuses.add(state.status.copy())
                    var message = ""
                    message += if (this is Hero) {
                        "Герой "
                    } else {
                        "Противник "
                    }
                    message += "получает ${state.status.name} ${state.status.value}"
                    battleLogListInteractor.add(message, Gem.LOG_COLOR)
                }
            } else {
                val find = statuses.find { it.name == state.status.name }
                find?.run {
                    statuses.remove(find)
                }
            }
        }
    }

    /**
     * после изменения параметров боя (ход, действия персонажей)
     * надо обновить состояние перков и применить дебафы
     */
    fun updatePersonsStates() {
        applyStates()
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

    fun messageAboutUsedPerk(perk: Perk, isHeroPerk: Boolean) {
        val perkMessage =
            if (perk.place) {
                "${perk.name}:${perk.description}"
            } else {
                if (isHeroPerk) {
                    "Герой применяет ${perk.name}:${perk.description}"
                } else {
                    "Противник применяет ${perk.name}:${perk.description}"
                }
            }
        val gemType = if (perk.prices.isNotEmpty()) {
            perk.prices[0].gemType
        } else {
            0
        }
        battleLogListInteractor.add(perkMessage, gemType)
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
            //todo почему то здесь статус оказывается зануленым
            Condition.Parameter.STATUS -> this.statuses.find { it.name == condition.name }?.value
                ?: 0

            Condition.Parameter.TURN -> turnNumberInteractor.value() ?: 0
            Condition.Parameter.HP_P -> this.hp * 100 / maxHp
            Condition.Parameter.HITS -> this.hits
            Condition.Parameter.TOUCHES -> this.touches
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
        if (originalEffect.currentCharges != null) {
            if (originalEffect.currentCharges != 0) {
                originalEffect.decreaseCharges()
                useEffect(originalEffect, enemy, hero)
            }
        } else {
            useEffect(originalEffect, enemy, hero)
        }
    }

    private fun useEffect(
        originalEffect: Effect,
        enemy: Enemy?,
        hero: Hero?
    ) {
        val numberForCompareWithEffectProbability = Random.nextInt(0, 101)
        /**
         * дефолтная вероятность применения навыка 100%
         */
        if (numberForCompareWithEffectProbability <= originalEffect.probability) {
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
                            attackPerson(effect, false, enemy!!)
                        }

                        Effect.EffectTarget.HERO -> {
                            attackPerson(effect, false, hero!!)
                        }

                        Effect.EffectTarget.ALL -> {
                            attackPerson(effect, false, hero!!, enemy!!)
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
                     * добавляет или отнимает значение
                     */
                    /**
                     * добавляет или отнимает значение
                     */
                    updateStockExecutor.updateStocks(Pair(effect.gemType, effect.value))
                    effect.gemTypes.forEach {
                        updateStockExecutor.updateStocks(Pair(it, effect.value))
                    }
                }

                is Effect.SetStock -> {
                    /**
                     * устаналивает заданное значение очков
                     */
                    /**
                     * устаналивает заданное значение очков
                     */
                    updateStockExecutor.setStocks(Pair(effect.gemType, effect.value))
                    effect.gemTypes.forEach {
                        updateStockExecutor.setStocks(Pair(it, effect.value))
                    }
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

                is Effect.Info -> {
                    effect.message?.run {
                        battleLogListInteractor.add(effect.message)
                    }
                }

                is Effect.EditStock -> {
                    /**
                     * добавляет или отнимает значение или устанавливает, в зависимости от
                     * [Effect.EditStock.Type]
                     */
                    /**
                     * добавляет или отнимает значение или устанавливает, в зависимости от
                     * [Effect.EditStock.Type]
                     */
                    if (effect.type == Effect.EditStock.Type.CHANGE) {
                        updateStockExecutor.updateStocks(Pair(effect.gemType, effect.value))
                        effect.gemTypes.forEach {
                            updateStockExecutor.updateStocks(Pair(it, effect.value))
                        }
                    } else {
                        updateStockExecutor.setStocks(Pair(effect.gemType, effect.value))
                        effect.gemTypes.forEach {
                            updateStockExecutor.setStocks(Pair(it, effect.value))
                        }
                    }
                }
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

    private fun attackPerson(
        attack: Effect.Attack,
        ignoreAnswer: Boolean = false,
        vararg persons: Person,
    ) {
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
                        } else {
                            applyAttack(attack, battleLogListInteractor, updateStockExecutor)
                        }
                    } else {
                        dodge(isHeroTarget, dodgeStatus)
                    }
                } else {
                    applyAttack(attack, battleLogListInteractor, updateStockExecutor)
                }
                /**
                 * для атак направленных против себя контратака не применяется
                 */
                val selfTarget = isHeroTarget && isHeroPerk || !isHeroTarget && !isHeroPerk
                /**
                 * против "помощников" также не применяются контратаки на персонажа
                 */
                if (!ignoreAnswer && !selfTarget && !attack.ignoreStatusesAndCounterAttacks) {
                    /**
                     *  при атаке персонажа ищем у него активный статус, который наносит урон в ответ, типа
                     *  [Status.EffectType.COUNTERATTACK] или [Status.EffectType.HARM]
                     */
                    val answerStatuses =
                        this.statuses.filter { status: Status ->
                            (status.type == Status.EffectType.COUNTERATTACK
                                    || status.type == Status.EffectType.HARM) && status.isActive()
                        }
                    answerStatuses.forEach {
                        answerOnAttack(it)
                    }
                }
                personInteractor.update(person)
            }
        }
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
        message += "восстанавливает ${heal.value} здоровья."
        if (this.hp + heal.value > this.maxHp) {
            message += "Здоровье полностью восстановлено"
        }
        battleLogListInteractor.add(message)
        when (heal.type) {
            Effect.Heal.Type.CHANGE -> this.increaseHp(heal.value)
            Effect.Heal.Type.SET -> this.setHpValue(heal.value)
        }
    }

    /**
     * изменение силы навыков в зависимости от статусов сражающихся
     */
    private fun changeEffectByPersonsStatuses(effect: Effect): Effect {
        val effectForChange = effect.copyEffect()
        val effectChangeByHeroStatuses = effectChangeByPersonStatuses(effectForChange, true)
        val effectChangeByEnemyStatuses =
            effectChangeByPersonStatuses(effectChangeByHeroStatuses, false)
        /**
         * для эффектов атак точно не нужно, чтобы они были меньше нуля
         */
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
                            isHeroPerk
                        } else {
                            !isHeroPerk
                        }
                        if (isPersonPerk) {
                            if (status.type == Status.EffectType.WEAK) {
                                when (effect) {
                                    is Effect.Attack -> {
                                        if (!effect.ignoreStatusesAndCounterAttacks) {
                                            effect.value =
                                                decreaseEffectValueByStatus(effect, status)
                                            status.decreaseTimes()
                                        }
                                    }

                                    else -> {}
                                }
                            }
                            if (status.type == Status.EffectType.STRONG) {
                                when (effect) {
                                    is Effect.Attack -> {
                                        if (!effect.ignoreStatusesAndCounterAttacks) {
                                            effect.value =
                                                effect.value + status.value
                                            status.decreaseTimes()
                                        }
                                    }

                                    else -> {}
                                }
                            }

                            if (status.type == Status.EffectType.STRONG_DEFEND) {
                                when (effect) {
                                    is Effect.Defend -> {
                                        effect.value =
                                            effect.value + status.value
                                        status.decreaseTimes()
                                    }

                                    else -> {}
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
                                    is Effect.Attack -> {
                                        effect.value =
                                            effect.value + status.value
                                        status.decreaseTimes()
                                    }

                                    else -> {}
                                }
                            }
                            if (status.type == Status.EffectType.ARMOR) {
                                when (effect) {
                                    is Effect.Attack -> {
                                        effect.value = decreaseEffectValueByStatus(effect, status)
                                        status.decreaseTimes()
                                    }

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
        dodgeStatus.decreaseTimes()
        battleLogListInteractor.add(message, Gem.DODGE_COLOR)
    }

    /**
     * сейчас в ответ наносится только урон, но можно делать что-то еще
     */
    private fun Person.answerOnAttack(
        counterAttackStatus: Status,
    ) {
        if (counterAttackStatus.type == Status.EffectType.COUNTERATTACK) {
            counterAttackStatus.decreaseTimes()
        }
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
            attackPerson(
                effectChangeByPersonStatuses as Effect.Attack,
                //todo добавить натсройку на паенль управления - давать возмоность тратитьу уклонения или нет
                ignoreAnswer = true,
                persons = arrayOf(this)
            )
        }
    }

    private fun defendPerson(defend: Effect.Defend, isHeroTarget: Boolean) {
        val personInteractor = personInteractor(isHeroTarget)
        val person = personInteractor.value()
        person?.run {
            when (defend.type) {
                Effect.Defend.Type.CHANGE -> this.shield = this.shield + defend.value
                Effect.Defend.Type.SET -> this.shield = defend.value
            }
            personInteractor.update(person)
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
                        //todo не только параметров, но и значения
                        Effect.EditStatus.Type.SET -> {
                            statusForChange.value = effectStatus.value
                            statusForChange.times = effectStatus.times
                        }

                        Effect.EditStatus.Type.CHANGE -> statusForChange.value =
                            statusForChange.value + effectStatus.value

                        Effect.EditStatus.Type.TIMES -> {
                            //todo
                            effectStatus.times?.run {
                                statusForChange.times = statusForChange.times?.plus(this)
                            }
                        }
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