package aldtoll.twiligihts.logic

import aldtoll.twiligihts.logic.perks.DefendEffectHandler
import aldtoll.twiligihts.logic.perks.EditStatusHandler
import aldtoll.twiligihts.logic.perks.EditStockHandler
import aldtoll.twiligihts.model.BattleSettings
import aldtoll.twiligihts.model.Condition
import aldtoll.twiligihts.model.ExecutedPerk
import aldtoll.twiligihts.model.Gem
import aldtoll.twiligihts.model.Hand
import aldtoll.twiligihts.model.Perk
import aldtoll.twiligihts.model.Status
import aldtoll.twiligihts.model.characters.Enemy
import aldtoll.twiligihts.model.characters.Hero
import aldtoll.twiligihts.model.characters.Person
import aldtoll.twiligihts.model.effects.Effect
import aldtoll.twiligihts.model.findActiveStatus
import aldtoll.twiligihts.model.findWorkStatuses
import aldtoll.twiligihts.storage.BattleLogListInteractor
import aldtoll.twiligihts.storage.EffectValueForDescriptionInteractor
import aldtoll.twiligihts.storage.ExecutedPerkInteractor
import aldtoll.twiligihts.storage.GoToFinishScreenInteractor
import aldtoll.twiligihts.storage.PersonInteractor
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
    private val editStockExecutor: EditStockExecutor,
    private val heroInteractor: HeroInteractor,
    private val enemyInteractor: EnemyInteractor,
    private val battleLogListInteractor: BattleLogListInteractor,
    private val heroHandsListInteractor: HeroHandsListInteractor,
    private val enemyHandsListInteractor: EnemyHandsListInteractor,
    private val goToFinishScreenInteractor: GoToFinishScreenInteractor,
    private val enemyStatesInteractor: EnemyStatesInteractor,
    private val heroStatesInteractor: HeroStatesInteractor,
    private val executedPerkInteractor: ExecutedPerkInteractor,
    private val checkConditionExecutor: CheckConditionExecutor,
    private val editResourcesExecutor: EditResourcesExecutor,
    private val applyAttackExecutor: ApplyAttackExecutor,
    private val updatePerksStateExecutor: UpdatePerksStateExecutor,
    private val effectValueForDescriptionInteractor: EffectValueForDescriptionInteractor,
    private val defendEffectHandler: DefendEffectHandler,
    private val editStatusHandler: EditStatusHandler,
    private val editStockHandler: EditStockHandler,
) {

    private var perk: Perk? = null
    private var isHeroPerk = false

    //todo нельзя использовать т.к. пустые на момент создания интерактора
    private val hero = heroInteractor.value()
    private val enemy = enemyInteractor.value()
    private var stopCallNextPerk = false

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
        /**
         * важно! perk это не perk из руки, а его копия.
         * Если его изменить, это не отобразится на перках руки
         */
        stopCallNextPerk = false
        this.perk = perk
        this.isHeroPerk = isHero
        val personInteractor = personInteractor(isHeroPerk)
        val activeStunStatus =
            personInteractor.value()?.statuses?.findActiveStatus(Status.StatusType.STUN)
        //todo здесь тоже стан ?
        reloadPerksAfterUse()
        usePerkCharge()
        usePerkResources()
        ifPerkHasReloadDownTimeIt()
        updatePerksStateExecutor.updateEnableStatus()
        updatePerksStateExecutor.updateShowStatus()
        editStockExecutor.payPriceForPerk(perk, isHero)
        /**
         * если у персонажа есть стан, то это может помешать использовать навыки
         */
        if (activeStunStatus != null) {
            battleLogListInteractor.add("Оглушение не позволило применить навык", Gem.LOG_COLOR)
            activeStunStatus.decreaseTimes()
        } else {
            executePerkEffects(perk)
        }

        updatePersonsStates()
        /**
         * если это автоматические действия противника, то нужно вызвать следующий перк
         * если не было завершения боя
         */
        //todo проблема - навык уже был применен. Если
        if (!stopCallNextPerk && !isHero && BattleSettings.ANIMATE_ENEMY_ACTIONS) {
            callNextPerk(perk)
        }
    }

    /**
     * потратить ресурсы, которые нужны для навыка
     * ресурсы также тратятся для остальных навыков
     */
    private fun usePerkResources() {
        perk?.run {
            editResourcesExecutor.spendResourcesForPerk(this, isHeroPerk)
        }
    }

    private fun ifPerkHasReloadDownTimeIt() {
        heroHandsListInteractor.value()?.run {
            this.forEach { hand ->
                hand.perks.forEach { perk ->
                    //todo можно просто категорию сделать
                    if (perk.isSame(this@PerkExecutor.perk) || (this@PerkExecutor.perk?.category != null && this@PerkExecutor.perk?.category == perk.category)) {
                        perk.reload = 0
                    }
                }
            }
        }
        enemyHandsListInteractor.value()?.run {
            this.forEach { hand ->
                hand.perks.forEach { perk ->
                    if (perk.isSame(this@PerkExecutor.perk) || (this@PerkExecutor.perk?.category != null && this@PerkExecutor.perk?.category == perk.category)) {
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
        findAndExecuteNextPerk(currentPerk)
    }

    //todo если к руке применяются условия видимости, то навык руки не должен на это влиять - иначе потеряется индекс
    private fun findAndExecuteNextPerk(currentPerk: Perk) {
        val enemyHands = enemyHandsListInteractor.value()
        val showedHands = enemyHands?.filter { it.show }
        showedHands?.run {
            /**
             * находим какой руке принадлежал навык
             */
            val find = this.find {
                it.perks.any { perk -> perk == currentPerk }
            }
            /**
             * если в руке есть еще навыки, то надо использовать их
             */
            find?.run {
                val indexOfCurrentPerk = find.perks.indexOf(currentPerk)
                /**
                 * если есть следующий навык, то использовать его
                 * если нет, то взять первый навык следующей видимой руки
                 * если рука была последняя, то конец действий противника
                 */
                if (indexOfCurrentPerk != -1 && indexOfCurrentPerk + 1 < find.perks.size) {
                    val nextPerk = find.perks[indexOfCurrentPerk + 1]
                    executedPerkInteractor.update(ExecutedPerk(nextPerk, this))
                } else {
                    val indexOfCurrentHand = showedHands.indexOf(find)
                    if (indexOfCurrentHand != -1 && indexOfCurrentHand + 1 < showedHands.size) {
                        val nextPerk = showedHands[indexOfCurrentHand + 1].perks[0]
                        executedPerkInteractor.update(
                            ExecutedPerk(
                                nextPerk,
                                showedHands[indexOfCurrentHand + 1]
                            )
                        )
                    } else {
                        executedPerkInteractor.update(
                            ExecutedPerk(
                                Perk.LAST_PERK,
                                Hand()
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
        val checkHero = this is Hero
        val states = if (checkHero) {
            heroStatesInteractor.value()
        } else {
            enemyStatesInteractor.value()
        }
        states?.forEach { state ->
            val statuses = this.statuses
            var addState = true
            if (state.conditions.isEmpty()) {
                addState = checkConditionExecutor.execute(state.condition, checkHero)
            } else {
                state.conditions.forEach { condition ->
                    if (!checkConditionExecutor.execute(condition, checkHero)) {
                        addState = false
                    }
                }
            }
            if (addState) {
                val find = statuses.find { it.name == state.status.name }
                if (find == null) {
                    statuses.add(state.status.copy())
                    var message = ""
                    message += if (checkHero) {
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
        updatePerksStateExecutor.updateEnableStatus()
        updatePerksStateExecutor.updateShowStatus()
    }

    private fun executePerkEffects(perk: Perk) {
        perk.effects.forEach { originalEffect ->
            val hero = heroInteractor.value()
            val enemy = enemyInteractor.value()
            val repeats = if (originalEffect.rFunc != null) {
                useFunctionForChangeEffectRepeats(originalEffect)
            } else {
                originalEffect.repeats
            }
            /**
             * на каждое применение эффекта нужно проверить, что условия не изменились
             * todo на каждое ли?
             */
            for (i in 1..repeats) {
                if (originalEffect.conditions.isEmpty()) {
                    applyEffect(originalEffect, enemy, hero)
                } else {
                    var applyEffect = true
                    originalEffect.conditions.forEach { condition ->
                        if (!checkConditionExecutor.execute(condition, isHeroPerk)) {
                            applyEffect = false
                        }
                    }
                    if (applyEffect) {
                        applyEffect(originalEffect, enemy, hero)
                    }
                }
            }
        }
    }

    fun messageAboutUsedPerk(perk: Perk, isHeroPerk: Boolean) {
        val perkMessage =
            if (perk.place) {
                perk.name
            } else {
                if (isHeroPerk) {
                    "Герой применяет ${perk.name}"
                } else {
                    "Противник применяет ${perk.name}"
                }
            }
        val gemType = if (perk.prices.isNotEmpty()) {
            perk.prices[0].gemType
        } else {
            0
        }
        battleLogListInteractor.add(perkMessage, gemType)
    }

    /**
     * уменьшить заряд эффекта
     * посчитать вероятность применения и применить
     */
    private fun applyEffect(
        originalEffect: Effect,
        enemy: Enemy?,
        hero: Hero?
    ) {
        /**
         * перед применением эффекта сбрасывается информация о предыдущих ударах и касаниях
         * т.е. если было попадание/касание, то мы проавалились в этот метод - теперь цепочка зависит от его результатов
         */
        hero!!.undo()
        enemy!!.undo()
        if (originalEffect.currentCharges != null) {
            if (originalEffect.currentCharges != 0) {
                originalEffect.decreaseCharges()
                countProbabilityAndUseEffect(originalEffect, enemy, hero)
            }
        } else {
            countProbabilityAndUseEffect(originalEffect, enemy, hero)
        }
    }

    private fun countProbabilityAndUseEffect(
        originalEffect: Effect,
        enemy: Enemy?,
        hero: Hero?
    ) {
        effectValueForDescriptionInteractor.item = ""
        if (originalEffect is Effect.Attack) {
            effectValueForDescriptionInteractor.item = "${originalEffect.value}"
        }
        val numberForCompareWithEffectProbability = Random.nextInt(0, 101)
        /**
         * дефолтная вероятность применения навыка 100%
         */
        if (numberForCompareWithEffectProbability <= originalEffect.probability) {
            /**
             * проверяем и применяем функцию,
             * если есть
             */
            val effectAfterFunction = useFunctionForChangeEffectValue(originalEffect)

            /**
             * для атак направленных против себя статусы не применяются, т.к. это аналог жертвы
             * если это атака, то проверяется цель эффекта
             */
            val isSelfAttack =
                originalEffect is Effect.Attack &&
                        (
                                originalEffect.target == Effect.EffectTarget.SELF ||
                                        isHeroPerk && originalEffect.target == Effect.EffectTarget.HERO ||
                                        !isHeroPerk && originalEffect.target == Effect.EffectTarget.ENEMY
                                )

            val effect = if (isSelfAttack) {
                effectAfterFunction
            } else {
                changeEffectByPersonsStatuses(effectAfterFunction)
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

                        Effect.EffectTarget.SELF -> {
                            attackPerson(effect, false, if (isHeroPerk) hero!! else enemy!!)
                        }

                        Effect.EffectTarget.FOE -> {
                            attackPerson(effect, false, if (!isHeroPerk) hero!! else enemy!!)
                        }
                    }
                }

                is Effect.Defend -> {
                    defendEffectHandler.handleDefendEffect(effect, isHeroPerk)
                }

                is Effect.EditStatus -> {
                    editStatusHandler.handleEffect(effect, isHeroPerk)
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

                        Effect.EffectTarget.SELF -> {
                            val person = if (isHeroPerk) {
                                hero!!
                            } else {
                                enemy!!
                            }
                            persons.add(person)
                        }

                        Effect.EffectTarget.FOE -> {
                            val person = if (!isHeroPerk) {
                                hero!!
                            } else {
                                enemy!!
                            }
                            persons.add(person)
                        }
                    }
                    healPerson(effect, *persons.toTypedArray())
                }

                is Effect.FinishBattle -> {
                    stopCallNextPerk = true
                    goToFinishScreenInteractor.update(Pair(true, effect.ask))
                }

                is Effect.Info -> {
                    effect.message?.run {
                        battleLogListInteractor.add(effect.message)
                    }
                }

                is Effect.EditStock -> {
                    editStockHandler.handleEffect(effect, isHeroPerk)
                }

                is Effect.EditResources -> {
                    editResourcesExecutor.execute(effect, isHeroPerk)
                }
            }
            if (originalEffect.additionalEffects.isNotEmpty()) {
                originalEffect.additionalEffects.forEach { additionalEffect ->
                    val success = when (additionalEffect.successType) {
                        Effect.SuccessType.TOUCH -> {
                            when (originalEffect.target) {
                                Effect.EffectTarget.ENEMY -> enemy!!.wasTouchedByPreviousEffect
                                Effect.EffectTarget.HERO -> hero!!.wasTouchedByPreviousEffect
                                Effect.EffectTarget.ALL -> hero!!.wasTouchedByPreviousEffect || enemy!!.wasTouchedByPreviousEffect
                                Effect.EffectTarget.SELF -> {
                                    if (isHeroPerk) {
                                        hero!!.wasTouchedByPreviousEffect
                                    } else {
                                        enemy!!.wasTouchedByPreviousEffect
                                    }
                                }

                                Effect.EffectTarget.FOE -> {
                                    if (!isHeroPerk) {
                                        hero!!.wasTouchedByPreviousEffect
                                    } else {
                                        enemy!!.wasTouchedByPreviousEffect
                                    }
                                }
                            }
                        }

                        Effect.SuccessType.HIT -> {
                            when (originalEffect.target) {
                                Effect.EffectTarget.ENEMY -> enemy!!.wasHitByPreviousEffect
                                Effect.EffectTarget.HERO -> hero!!.wasHitByPreviousEffect
                                Effect.EffectTarget.ALL -> hero!!.wasHitByPreviousEffect || enemy!!.wasHitByPreviousEffect
                                Effect.EffectTarget.SELF -> {
                                    if (isHeroPerk) {
                                        hero!!.wasHitByPreviousEffect
                                    } else {
                                        enemy!!.wasHitByPreviousEffect
                                    }
                                }

                                Effect.EffectTarget.FOE -> {
                                    if (!isHeroPerk) {
                                        hero!!.wasHitByPreviousEffect
                                    } else {
                                        enemy!!.wasHitByPreviousEffect
                                    }
                                }
                            }
                        }

                        Effect.SuccessType.ANY -> true
                        Effect.SuccessType.FAIL -> false
                    }
                    if (success) {
                        //todo надо тоже проверку условий, зарядов? может просто useEffect
                        /**
                         * сейчас с applyEffect если главный эффект сработал,
                         * то будут запущены все остальные эффекты без проверки условий (но с проверкой вероятности)
                         */
                        applyEffect(additionalEffect, enemy, hero)
                    }
                }
            }

        } else {
            battleLogListInteractor.add("Эффект не сработал. Выпало $numberForCompareWithEffectProbability")
            val additionalEffectsOnFail =
                originalEffect.additionalEffects.filter { it.successType == Effect.SuccessType.FAIL }
            if (additionalEffectsOnFail.isNotEmpty()) {
                battleLogListInteractor.add("Но неудача дала эффекты:")
                additionalEffectsOnFail.forEach {
                    applyEffect(it, enemy, hero)
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
                 * для атак направленных против себя контратака не применяется и нельзя промазать или увернуться
                 */
                val selfTarget = isHeroTarget && isHeroPerk || !isHeroTarget && !isHeroPerk
                if (!selfTarget) {
                    /**
                     * при атаке на персонажа смотрим попала ли атака
                     * влияют [Status.StatusType.EVASION] цели и [Status.StatusType.ACCURACY] источника атаки
                     */
                    val chanceToHit = countHitChance(attack)
                    val numberForCheckHit = Random.nextInt(0, DEFAULT_CHANCE_TO_HIT + 1)
                    /**
                     * 100 всегда больше или равно случайного числа от 0 до 101
                     */
                    if (chanceToHit >= numberForCheckHit) {
                        /**
                         * при атаке персонажа ищем у него активный статус, который позволяет избежать атаки
                         * [Status.StatusType.DODGE] или [Status.StatusType.SMART_DODGE]
                         * todo верно ли, что уклонение ратится против успешной атаки? можно ведь уклоняться и от промаха
                         */
                        if (isHeroTarget) {
                            if (ENABLE_DODGE) {
                                applyAttackOrUseDodge(attack, isHeroTarget, person)
                            } else {
                                applyAttackExecutor.execute(person, attack)
                            }
                        } else {
                            applyAttackOrUseDodge(attack, isHeroTarget, person)
                        }
                    } else {
                        if (chanceToHit < ONE_HUNDRED_PERCENT) {
                            battleLogListInteractor.add("Шанс попадания: $chanceToHit%")
                        }
                        battleLogListInteractor.add(
                            "Но выпало: $numberForCheckHit - Промах!",
                            Gem.LOG_COLOR
                        )
                    }
                    /**
                     * против "помощников" также не применяются контратаки на персонажа
                     */
                    if (!ignoreAnswer && !attack.help && !attack.ignoreCounterAttacks) {
                        /**
                         *  при атаке персонажа ищем у него активный статус, который наносит урон в ответ, типа
                         *  [Status.StatusType.COUNTERATTACK] или [Status.StatusType.HARM]
                         */
                        val answerStatuses =
                            this.statuses.filter { status: Status ->
                                (status.type == Status.StatusType.COUNTERATTACK
                                        || status.type == Status.StatusType.HARM) && status.isWork()
                            }
                        answerStatuses.forEach {
                            //todo добавить новые варианты ответов
                            answerOnAttack(it)
                        }
                    }
                } else {
                    applyAttackExecutor.execute(person, attack, true)
                }
                personInteractor.update(person)
            }
        }
    }

    private fun Person.applyAttackOrUseDodge(
        attack: Effect.Attack,
        isHeroTarget: Boolean,
        person: Person
    ) {
        /**
         * когда персонаж атакован, то он надо посмотреть, собирался ли он использовать умное уклонение против атаки,
         * т.е. проверить порог
         * если нет, то надо проверить
         * может у него есть обычные уклонения
         * если нет, то он получает удар от атаки
         */
        val findSuitableSmartDodgeStatus =
            this.statuses.find { status: Status -> status.type == Status.StatusType.SMART_DODGE && status.isWork() && status.smartValue != null && attack.value > status.smartValue }
        if (findSuitableSmartDodgeStatus != null) {
            dodge(isHeroTarget, findSuitableSmartDodgeStatus)
        } else {
            val dodgeStatus = this.statuses.findActiveStatus(Status.StatusType.DODGE)
            if (dodgeStatus != null) {
                if (attack.ignoreDodge) {
                    applyAttackExecutor.execute(person, attack)
                } else {
                    dodge(isHeroTarget, dodgeStatus)
                }
            } else {
                applyAttackExecutor.execute(person, attack)
            }
        }
    }

    private fun countHitChance(attack: Effect.Attack): Int {
        val sourceOfAttack: Person?
        val targetOfAttack: Person?
        if (isHeroPerk) {
            sourceOfAttack = heroInteractor.value()
            targetOfAttack = enemyInteractor.value()
        } else {
            sourceOfAttack = enemyInteractor.value()
            targetOfAttack = heroInteractor.value()
        }
        var chanceToHit = ONE_HUNDRED_PERCENT
        if (!attack.ignoreEvasion) {
            val evasionStatuses =
                targetOfAttack?.statuses?.findWorkStatuses(Status.StatusType.EVASION)
            evasionStatuses?.forEach { evasionStatus ->
                chanceToHit -= evasionStatus.value
                evasionStatus.decreaseTimes()
            }
        }
        /**
         * при действии помощников не нужно применять "точность" источника
         */
        if (!attack.help && !attack.ignoreAcc) {
            val accuracyStatuses =
                sourceOfAttack?.statuses?.findWorkStatuses(Status.StatusType.ACCURACY)
            accuracyStatuses?.forEach { accuracyStatus ->
                chanceToHit += accuracyStatus.value
                accuracyStatus.decreaseTimes()
            }
        }
        return chanceToHit
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
        when (heal.type) {
            Effect.Heal.Type.CHANGE -> this.increaseHp(heal.value)
            Effect.Heal.Type.SET -> this.setHpValue(heal.value)
        }
        message += if (this.hp + heal.value > this.maxHp) {
            "Здоровье полностью восстановлено"
        } else {
            "(${this.hp}/${this.maxHp})"
        }
        battleLogListInteractor.add(message)
    }

    private fun useFunctionForChangeEffectValue(effect: Effect): Effect {
        val effectForChange = effect.copyEffect()
        /**
         * если есть функция, то она будет использована для изменения значения
         */
        effectForChange.func?.run {
            val func = this
            func.allSegments().forEach { segment ->
                val personInteractor = when (segment.source) {
                    Effect.Source.ENEMY -> enemyInteractor
                    Effect.Source.HERO -> heroInteractor
                    Effect.Source.SELF -> if (isHeroPerk) heroInteractor else enemyInteractor
                    Effect.Source.FOE -> if (!isHeroPerk) heroInteractor else enemyInteractor
                }
                segment.parameter.let {
                    personInteractor.value()?.run {
                        val personParameter = checkConditionExecutor.getParameter(
                            this,
                            Condition(
                                name = segment.name,
                                parameter = segment.parameter,
                                gemType = segment.gemType
                            )
                        )
                        effectForChange.value += (segment.mul * personParameter).toInt()
                    }
                }
            }

            /**
             * бросок кости
             */
            func.dice?.let {
                effectForChange.value += func.rollDice()
            }
        }
        return effectForChange
    }

    private fun useFunctionForChangeEffectRepeats(effect: Effect): Int {
        val effectForChange = effect.copyEffect()
        var repeats = effectForChange.repeats
        effectForChange.rFunc?.run {
            val func = this
            func.allSegments().forEach { segment ->
                val personInteractor = when (segment.source) {
                    Effect.Source.ENEMY -> enemyInteractor
                    Effect.Source.HERO -> heroInteractor
                    Effect.Source.SELF -> if (isHeroPerk) heroInteractor else enemyInteractor
                    Effect.Source.FOE -> if (!isHeroPerk) heroInteractor else enemyInteractor
                }
                segment.parameter.let {
                    personInteractor.value()?.run {
                        val personParameter = checkConditionExecutor.getParameter(
                            this,
                            Condition(
                                name = segment.name,
                                parameter = segment.parameter,
                                gemType = segment.gemType
                            )
                        )
                        repeats += (segment.mul * personParameter).toInt()
                    }
                }
            }

            /**
             * бросок кости
             */
            func.dice?.let {
                repeats += func.rollDice()
            }
        }
        return repeats
    }

    /**
     * изменение силы навыков в зависимости от статусов сражающихся
     */
    private fun changeEffectByPersonsStatuses(effect: Effect): Effect {
        val effectForChange = effect.copyEffect()
        val effectChangedByHeroStatuses = effectChangeByPersonStatuses(effectForChange, true)
        val effectChangedByHeroAndEnemyStatuses =
            effectChangeByPersonStatuses(effectChangedByHeroStatuses, false)
        /**
         * не нужно, чтобы результирующая сила была меньше нуля для:
         * атак
         * защит
         * ..
         */
        if (effectChangedByHeroAndEnemyStatuses is Effect.Attack ||
            effectChangedByHeroAndEnemyStatuses is Effect.Defend
        ) {
            effectChangedByHeroAndEnemyStatuses.value =
                effectChangedByHeroAndEnemyStatuses.value.coerceAtLeast(0)
        }
        return if (effectChangedByHeroAndEnemyStatuses is Effect.Attack) {
            val personInteractor =
                personInteractor(!isHeroPerk)
            val person = personInteractor.value()
            applyResistanceMultiplier(effectChangedByHeroAndEnemyStatuses, person!!)
        } else {
            effectChangedByHeroAndEnemyStatuses
        }
    }

    private fun applyResistanceMultiplier(
        effect: Effect.Attack,
        targetPerson: Person
    ): Effect.Attack {
        val resistanceStatuses = targetPerson.statuses.filter {
            it.isWork() && it.type == Status.StatusType.RESISTANCE
        }

        val totalResistance = resistanceStatuses.sumOf { it.value }
        val multiplier = 1.0 - (totalResistance / 100.0)
        val adjustedValue = (effect.value * multiplier).toInt().coerceAtLeast(0)

        // Обновляем описание — только если есть отклонение от 1.0
        val multiplierStr = if (multiplier != 1.0) "×${"%.2f".format(multiplier)} " else ""
        effectValueForDescriptionInteractor.item =
            multiplierStr + effectValueForDescriptionInteractor.item

        return effect.copy(value = adjustedValue)
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
                    if (status.isWork()) {
                        val isPersonPerk = if (isHeroTarget) {
                            isHeroPerk
                        } else {
                            !isHeroPerk
                        }
                        if (isPersonPerk) {
                            if (status.type == Status.StatusType.WEAK) {
                                when (effect) {
                                    is Effect.Attack -> {
                                        if (!effect.help && !effect.ignoreWeak) {
                                            effect.value =
                                                decreaseAttackEffectValueByStatus(effect, status)
                                            status.decreaseTimes()
                                        }
                                    }

                                    else -> {}
                                }
                            }
                            if (status.type == Status.StatusType.STRONG) {
                                when (effect) {
                                    is Effect.Attack -> {
                                        if (!effect.help && !effect.ignoreStrong) {
                                            effect.value =
                                                increaseEffectValueByStatus(effect, status)
                                            status.decreaseTimes()
                                        }
                                    }

                                    else -> {}
                                }
                            }

                            if (status.type == Status.StatusType.CHANGE_DEFEND) {
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
                            if (status.type == Status.StatusType.VULNERABLE || status.type == Status.StatusType.VUL) {
                                when (effect) {
                                    is Effect.Attack -> {
                                        if (!effect.ignoreVul) {
                                            effect.value =
                                                increaseEffectValueByStatus(effect, status)
                                            status.decreaseTimes()
                                        }
                                    }

                                    else -> {}
                                }
                            }
                            if (status.type == Status.StatusType.ARMOR) {
                                when (effect) {
                                    is Effect.Attack -> {
                                        if (!effect.ignoreArmor) {
                                            effect.value =
                                                decreaseAttackEffectValueByStatus(effect, status)
                                            status.decreaseTimes()
                                        }
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

    /**
     * может быть меньше нуля, потому что здесь указан эффект от конкретного статуса
     * не может быть меньше нуля суммарное значение атаки
     */
    private fun decreaseAttackEffectValueByStatus(
        attack: Effect.Attack,
        status: Status
    ): Int {
        val i = attack.value - status.value
        effectValueForDescriptionInteractor.item += "-${status.value}"
        return i
    }

    private fun increaseEffectValueByStatus(
        attack: Effect.Attack,
        status: Status
    ): Int {
        val i = attack.value + status.value
        effectValueForDescriptionInteractor.item += "+${status.value}"
        return i
    }

    private fun dodge(
        isHeroTarget: Boolean,
        dodgeStatus: Status
    ) {
        if (isHeroTarget) {
            if (ENABLE_DODGE) {
                val message = "Герой уворачивается"
                dodgeStatus.decreaseTimes()
                battleLogListInteractor.add(message, Gem.DODGE_COLOR)
            }
        } else {
            val message = "Противник уворачивается"
            dodgeStatus.decreaseTimes()
            battleLogListInteractor.add(message, Gem.DODGE_COLOR)
        }
    }

    /**
     * сейчас в ответ наносится только урон, но можно делать что-то еще
     */
    private fun Person.answerOnAttack(
        counterAttackStatus: Status,
    ) {
        //todo почему только для контратак? потому
        if (counterAttackStatus.type == Status.StatusType.COUNTERATTACK) {
            counterAttackStatus.decreaseTimes()
        }
        val isHeroTarget = this is Hero
        isHeroPerk = isHeroTarget
        var message = ""
        message += if (isHeroTarget) {
            "У героя "
        } else {
            "У противника "
        }
        message += "срабатывает ${counterAttackStatus.name}(${counterAttackStatus.value})."
        battleLogListInteractor.add(message, Gem.COUNTERATTACK_COLOR)
        val attack = Effect.Attack(
            counterAttackStatus.value,
            Effect.Attack.Type.BOTH,
            target = if (isHeroTarget) Effect.EffectTarget.ENEMY else Effect.EffectTarget.HERO,
            help = counterAttackStatus.type == Status.StatusType.HARM
        )
        effectValueForDescriptionInteractor.item = attack.value.toString()
        //todo здесь надо разграничивать HARM и COUNTERATTACK
        val effectChangeByPersonStatuses = changeEffectByPersonsStatuses(attack)

        val personInteractor = personInteractor(!isHeroTarget)
        personInteractor.value()?.run {
            attackPerson(
                effectChangeByPersonStatuses as Effect.Attack,
                ignoreAnswer = true,
                persons = arrayOf(this)
            )
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

    companion object {
        //todo сделать настраиваемым
        const val DEFAULT_CHANCE_TO_HIT = 100
        const val ONE_HUNDRED_PERCENT = 100

        var ENABLE_DODGE = true
    }
}