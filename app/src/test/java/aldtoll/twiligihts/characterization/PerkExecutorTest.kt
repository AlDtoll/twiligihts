package aldtoll.twiligihts.characterization

import aldtoll.twiligihts.characterization.Fixture.Companion.perkOf
import aldtoll.twiligihts.characterization.Fixture.Companion.status
import aldtoll.twiligihts.model.Condition
import aldtoll.twiligihts.model.Perk
import aldtoll.twiligihts.model.Resource
import aldtoll.twiligihts.model.State
import aldtoll.twiligihts.model.Status
import aldtoll.twiligihts.model.Status.StatusType
import aldtoll.twiligihts.model.Stock
import aldtoll.twiligihts.model.effects.Effect
import aldtoll.twiligihts.model.effects.Effect.EffectTarget
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Rule
import org.junit.Before
import org.junit.Test

/** RULE-050..RULE-079: PerkExecutor (+ handlers DEFEND/EDIT_STATUS/HEAL/INFO/FINISH). */
class PerkExecutorTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var f: Fixture

    @Before
    fun createFixture() {
        f = Fixture()
    }

    private fun heroUses(perk: Perk) = f.perk.execute(perk, true)

    // RULE-050: цена списывается со стока, эффект атакует противника
    @Test
    fun heroPerk_paysPrice_andAttacksFoe() {
        f.heroStocks(Stock(10, 1))
        val p = perkOf("Удар", Effect.Attack(5), price = 4)
        f.heroHand(p)
        heroUses(p)
        assertEquals(6, f.heroStock(1))
        assertEquals(25, f.enemy.hp)
    }

    // RULE-050: execute не проверяет хватает ли очков — сток уходит в минус
    @Test
    fun execute_doesNotCheckPrice_stockCanGoNegative() {
        f.heroStocks(Stock(2, 1))
        val p = perkOf("Удар", Effect.Attack(1), price = 5)
        f.heroHand(p)
        heroUses(p)
        assertEquals(-3, f.heroStock(1))
    }

    // RULE-051, SUSPECT-10: при оглушении навык всё равно оплачивается, эффекты не применяются
    @Test
    fun stun_blocksEffects_butPriceIsPaid() {
        f.heroStocks(Stock(10, 1))
        f.hero.statuses.add(status("Оглушение", StatusType.STUN, 1, times = 2))
        val p = perkOf("Удар", Effect.Attack(5), price = 4)
        f.heroHand(p)
        heroUses(p)
        assertEquals(6, f.heroStock(1))
        assertEquals(30, f.enemy.hp)
        assertTrue(f.logs().contains("Оглушение не позволило применить навык"))
        assertEquals(1, f.hero.statuses.first { it.name == "Оглушение" }.times)
    }

    // RULE-052: модификаторы атаки: STRONG/WEAK атакующего, ARMOR/VUL цели, затем RESISTANCE цели (%)
    @Test
    fun attackModifiers_order() {
        f.hero.statuses.add(status("Сила", StatusType.STRONG, 2))
        f.enemy.statuses.add(status("Броня", StatusType.ARMOR, 1))
        f.enemy.statuses.add(status("Стойкость", StatusType.RESISTANCE, 50))
        heroUses(perkOf("Удар", Effect.Attack(5)))
        // (5 + 2 - 1) * (1 - 0.5) = 3
        assertEquals(27, f.enemy.hp)
    }

    // RULE-052, решение 24 (SUSPECT-22): RESISTANCE берётся у реальной цели; для ALL у каждой цели своё
    @Test
    fun resistance_attackAll_eachTargetOwnResistance() {
        f.hero.statuses.add(status("Стойкость героя", StatusType.RESISTANCE, 20))
        f.enemy.statuses.add(status("Стойкость врага", StatusType.RESISTANCE, 50))
        heroUses(perkOf("Взрыв", Effect.Attack(10, target = EffectTarget.ALL)))
        assertEquals(25, f.enemy.hp) // 10 * 0.5 = 5
        assertEquals(12, f.hero.hp)  // 10 * 0.8 = 8
    }

    // RULE-052, решение 24: сопротивление противника не режет урон по герою (атака врага по герою)
    @Test
    fun resistance_enemyAttacksHero_usesHeroResistanceOnly() {
        f.hero.statuses.add(status("Стойкость героя", StatusType.RESISTANCE, 50))
        f.enemy.statuses.add(status("Стойкость врага", StatusType.RESISTANCE, 20))
        f.perk.execute(perkOf("Удар врага", Effect.Attack(10)), false)
        assertEquals(15, f.hero.hp) // 10 * 0.5 = 5, а не 10 * 0.8
        assertEquals(30, f.enemy.hp)
    }

    // RULE-052: VUL цели увеличивает; VUL самого атакующего не влияет на его атаку по врагу
    @Test
    fun vulnerable_onTargetOnly() {
        f.hero.statuses.add(status("Уязв", StatusType.VULNERABLE, 3))
        heroUses(perkOf("Удар", Effect.Attack(5)))
        assertEquals(25, f.enemy.hp)
        f.enemy.statuses.add(status("Уязв", StatusType.VUL, 3))
        heroUses(perkOf("Удар", Effect.Attack(5)))
        assertEquals(17, f.enemy.hp)
    }

    // RULE-053: итоговая сила атаки не меньше 0
    @Test
    fun weak_cannotMakeAttackNegative() {
        f.hero.statuses.add(status("Слабость", StatusType.WEAK, 10))
        heroUses(perkOf("Удар", Effect.Attack(5)))
        assertEquals(30, f.enemy.hp)
    }

    // RULE-054: help/ignore* флаги отключают соответствующие модификаторы
    @Test
    fun ignoreFlags() {
        f.hero.statuses.add(status("Сила", StatusType.STRONG, 2))
        f.enemy.statuses.add(status("Броня", StatusType.ARMOR, 1))
        heroUses(perkOf("Удар", Effect.Attack(5, ignoreStrong = true, ignoreArmor = true)))
        assertEquals(25, f.enemy.hp)
        heroUses(perkOf("Помощь", Effect.Attack(5, help = true)))
        // help игнорирует STRONG атакующего, но не ARMOR цели
        assertEquals(21, f.enemy.hp)
    }

    // RULE-055: STRONG с times тратит заряд при каждой атаке
    @Test
    fun strongWithTimes_consumed() {
        f.hero.statuses.add(status("Сила", StatusType.STRONG, 2, times = 1))
        heroUses(perkOf("Удар", Effect.Attack(5)))
        heroUses(perkOf("Удар", Effect.Attack(5)))
        assertEquals(30 - 7 - 5, f.enemy.hp)
    }

    // RULE-056: атака по себе — без статусов и без касания
    @Test
    fun selfAttack_ignoresStatuses() {
        f.hero.statuses.add(status("Сила", StatusType.STRONG, 2))
        heroUses(perkOf("Жертва", Effect.Attack(5, target = EffectTarget.SELF)))
        assertEquals(15, f.hero.hp)
        assertEquals(0, f.hero.touches)
    }

    // RULE-057: уклонение DODGE тратит times; ignoreDodge пробивает
    @Test
    fun dodge() {
        f.enemy.statuses.add(status("Уклон", StatusType.DODGE, 1, times = 1))
        heroUses(perkOf("Удар", Effect.Attack(5)))
        assertEquals(30, f.enemy.hp)
        assertTrue(f.logs().contains("Противник уворачивается"))
        assertEquals(0, f.enemy.statuses[0].times)
        heroUses(perkOf("Удар", Effect.Attack(5)))
        assertEquals(25, f.enemy.hp)

        f.enemy.statuses.add(status("Уклон2", StatusType.DODGE, 1))
        heroUses(perkOf("Удар", Effect.Attack(5, ignoreDodge = true)))
        assertEquals(20, f.enemy.hp)
    }

    // RULE-058: SMART_DODGE — только если урон строго больше smartValue (сравнивается сырой урон)
    @Test
    fun smartDodge_threshold() {
        f.enemy.statuses.add(status("Умный", StatusType.SMART_DODGE, 1, smartValue = 3))
        heroUses(perkOf("Удар", Effect.Attack(3)))
        assertEquals(27, f.enemy.hp)
        heroUses(perkOf("Удар", Effect.Attack(4)))
        assertEquals(27, f.enemy.hp)
    }

    // RULE-059: шанс попадания = 100 - EVASION цели + ACCURACY атакующего
    @Test
    fun evasionAndAccuracy() {
        f.enemy.statuses.add(status("Ловкость", StatusType.EVASION, 150))
        heroUses(perkOf("Удар", Effect.Attack(5)))
        assertEquals(30, f.enemy.hp)
        assertTrue(f.logs().contains("Шанс попадания: -50%"))
        f.hero.statuses.add(status("Меткость", StatusType.ACCURACY, 150))
        heroUses(perkOf("Удар", Effect.Attack(5)))
        assertEquals(25, f.enemy.hp)
        heroUses(perkOf("Удар", Effect.Attack(5, ignoreEvasion = true, ignoreAcc = true)))
        assertEquals(20, f.enemy.hp)
    }

    // RULE-060: контратака бьёт атакующего
    @Test
    fun counterattack_hitsAttacker() {
        f.enemy.statuses.add(status("Шипы", StatusType.COUNTERATTACK, 3))
        heroUses(perkOf("Удар", Effect.Attack(5)))
        assertEquals(25, f.enemy.hp)
        assertEquals(17, f.hero.hp)
        assertTrue(f.logs().contains("У противника срабатывает Шипы(3)."))
    }

    // RULE-060: контратака срабатывает даже при уклонении (проверка после попадания/промаха)
    @Test
    fun counterattack_evenWhenDodged() {
        f.enemy.statuses.add(status("Уклон", StatusType.DODGE, 1))
        f.enemy.statuses.add(status("Шипы", StatusType.COUNTERATTACK, 3))
        heroUses(perkOf("Удар", Effect.Attack(5)))
        assertEquals(30, f.enemy.hp)
        assertEquals(17, f.hero.hp)
    }

    // RULE-060, решение 3 (SUSPECT-8): после контратаки isHeroPerk восстанавливается -> следующий эффект
    // того же навыка героя (Defend SELF) остаётся у героя
    @Test
    fun counterattack_keepsOriginalSideForRemainingEffects() {
        f.enemy.statuses.add(status("Шипы", StatusType.COUNTERATTACK, 3))
        heroUses(perkOf("Удар и щит", Effect.Attack(5), Effect.Defend(4)))
        assertEquals(4, f.hero.shield)
        assertEquals(0, f.enemy.shield)
        assertEquals(17, f.hero.hp)
    }

    // RULE-060, решение 3: то же для статуса REACTION (исполняется как навык врага через execute)
    @Test
    fun reaction_keepsOriginalSideForRemainingEffects() {
        val reaction = Status(
            name = "Реакция", value = 1, type = StatusType.REACTION,
            reactionPerk = Perk(name = "Ответ", effects = arrayListOf(Effect.Attack(2)))
        )
        f.enemy.statuses.add(reaction)
        heroUses(perkOf("Удар и щит", Effect.Attack(5), Effect.Defend(4)))
        assertEquals(18, f.hero.hp)
        assertEquals(4, f.hero.shield)
        assertEquals(0, f.enemy.shield)
    }

    // RULE-061: ignoreCounterAttacks
    @Test
    fun ignoreCounterAttacks() {
        f.enemy.statuses.add(status("Шипы", StatusType.COUNTERATTACK, 3))
        heroUses(perkOf("Удар", Effect.Attack(5, ignoreCounterAttacks = true)))
        assertEquals(20, f.hero.hp)
    }

    // RULE-062, решение 18 (SUSPECT-11): «полностью восстановлено» только если после лечения hp == maxHp
    @Test
    fun heal_capsAtMax_fullMessageOnlyWhenFull() {
        f.hero = f.hero.copy(hp = 5)
        heroUses(perkOf("Лечение", Effect.Heal(10)))
        assertEquals(15, f.hero.hp)
        assertTrue(f.logs().contains("Герой восстанавливает 10 здоровья.(15/20)"))
        assertFalse(f.logs().contains("Герой восстанавливает 10 здоровья.Здоровье полностью восстановлено"))
        heroUses(perkOf("Лечение", Effect.Heal(10)))
        assertEquals(20, f.hero.hp)
        assertTrue(f.logs().contains("Герой восстанавливает 10 здоровья.Здоровье полностью восстановлено"))
        heroUses(perkOf("Сет", Effect.Heal(3, type = Effect.Heal.Type.SET)))
        assertEquals(3, f.hero.hp)
        assertTrue(f.logs().contains("Герой восстанавливает 3 здоровья.(3/20)"))
    }

    // RULE-063: DEFEND CHANGE/SET; цели
    @Test
    fun defend() {
        heroUses(perkOf("Щит", Effect.Defend(4)))
        heroUses(perkOf("Щит", Effect.Defend(3)))
        assertEquals(7, f.hero.shield)
        heroUses(perkOf("Сет", Effect.Defend(2, type = Effect.Defend.Type.SET)))
        assertEquals(2, f.hero.shield)
        heroUses(perkOf("Всем", Effect.Defend(1, target = EffectTarget.ALL)))
        assertEquals(3, f.hero.shield)
        assertEquals(1, f.enemy.shield)
    }

    // RULE-064: CHANGE_DEFEND статус увеличивает щиты от своего навыка
    @Test
    fun changeDefendStatus() {
        f.hero.statuses.add(status("Стена", StatusType.CHANGE_DEFEND, 2))
        heroUses(perkOf("Щит", Effect.Defend(4)))
        assertEquals(6, f.hero.shield)
    }

    // RULE-065: EDIT_STATUS: новый статус / CHANGE / SET
    @Test
    fun editStatus() {
        val st = status("Ярость", StatusType.STRONG, 0, duration = 2)
        heroUses(perkOf("Ярость", Effect.EditStatus(st, Effect.EditStatus.Type.CHANGE, target = EffectTarget.SELF, value = 2)))
        heroUses(perkOf("Ярость", Effect.EditStatus(st, Effect.EditStatus.Type.CHANGE, target = EffectTarget.SELF, value = 2)))
        val s = f.hero.statuses.first { it.name == "Ярость" }
        assertEquals(4, s.value)
        assertEquals(2, s.duration)
        assertTrue(f.logs().contains("Герой получает статус: Ярость (2)"))
        assertTrue(f.logs().contains("Герой обновляет статус: Ярость (2 → 4)"))
        heroUses(perkOf("Сброс", Effect.EditStatus(st, Effect.EditStatus.Type.SET, target = EffectTarget.SELF, value = 0)))
        assertEquals(0, f.hero.statuses.first { it.name == "Ярость" }.value)
    }

    // RULE-065: TIMES не создаёт times у статуса, где times был null
    @Test
    fun editStatus_times_onNullTimes_staysNull() {
        f.hero.statuses.add(status("Щит", StatusType.DODGE, 1))
        val st = status("Щит", StatusType.DODGE, 1, times = 2)
        heroUses(perkOf("t", Effect.EditStatus(st, Effect.EditStatus.Type.TIMES, target = EffectTarget.SELF, value = 5)))
        val s = f.hero.statuses.first { it.name == "Щит" }
        assertEquals(5, s.value)
        assertNull(s.times)
    }

    // RULE-066: условия эффекта проверяются на каждый повтор; repeats; rFunc
    @Test
    fun conditionsAndRepeats() {
        heroUses(perkOf("Трижды", Effect.Attack(2, repeats = 3)))
        assertEquals(24, f.enemy.hp)
        val lowHp = Condition(10, EffectTarget.HERO, Condition.Parameter.HP, symbol = Condition.Symbol.LESS)
        heroUses(perkOf("Отчаяние", Effect.Attack(5, conditions = arrayListOf(lowHp))))
        assertEquals(24, f.enemy.hp)
        val rf = Effect.Func(segments = listOf(Effect.Func.Segment(Condition.Parameter.TURN)))
        f.turnI.update(2)
        heroUses(perkOf("Серия", Effect.Attack(1, rFunc = rf)))
        assertEquals(21, f.enemy.hp)
    }

    // RULE-067: func добавляется к value атаки
    @Test
    fun valueFunction() {
        f.hero = f.hero.copy(shield = 4)
        val fn = Effect.Func(segments = listOf(Effect.Func.Segment(Condition.Parameter.SP)))
        heroUses(perkOf("Таран", Effect.Attack(1, func = fn)))
        assertEquals(25, f.enemy.hp)
    }

    // RULE-068: заряды эффекта (после init через руку)
    @Test
    fun effectCharges() {
        val p = perkOf("Раз", Effect.Attack(5, charges = 1))
        f.heroHand(p)
        heroUses(p); heroUses(p)
        assertEquals(25, f.enemy.hp)
    }

    // RULE-069: доп. эффекты по HIT/TOUCH/ANY
    @Test
    fun additionalEffects_successTypes() {
        val onHit = Effect.Info(message = "hit!", successType = Effect.SuccessType.HIT)
        val onTouch = Effect.Info(message = "touch!", successType = Effect.SuccessType.TOUCH)
        val always = Effect.Info(message = "any!", successType = Effect.SuccessType.ANY)
        val onFail = Effect.Info(message = "fail!", successType = Effect.SuccessType.FAIL)
        f.enemy = f.enemy.copy(shield = 50)
        heroUses(perkOf("Удар", Effect.Attack(5, additionalEffects = arrayListOf(onHit, onTouch, always, onFail))))
        val logs = f.logs()
        assertFalse(logs.contains("hit!"))
        assertTrue(logs.contains("touch!"))
        assertTrue(logs.contains("any!"))
        assertFalse(logs.contains("fail!"))
    }

    // RULE-070: неудача по вероятности запускает только FAIL доп. эффекты
    @Test
    fun failedProbability_runsFailEffects() {
        val onFail = Effect.Info(message = "fail!", successType = Effect.SuccessType.FAIL)
        val always = Effect.Info(message = "any!", successType = Effect.SuccessType.ANY)
        heroUses(perkOf("Удар", Effect.Attack(5, probability = -1, additionalEffects = arrayListOf(onFail, always))))
        val logs = f.logs()
        assertEquals(30, f.enemy.hp)
        assertTrue(logs.any { it.startsWith("Эффект не сработал. Выпало ") })
        assertTrue(logs.contains("Но неудача дала эффекты:"))
        assertTrue(logs.contains("fail!"))
        assertFalse(logs.contains("any!"))
    }

    // RULE-071: FINISH
    @Test
    fun finishBattle() {
        heroUses(perkOf("Бегство", Effect.FinishBattle(ask = true)))
        assertEquals(Pair(true, true), f.goToFinishI.get().value)
    }

    // RULE-072: категория — использование одного обнуляет перезарядку всех перков категории
    @Test
    fun category_reloadsWholeCategory() {
        val a = Perk(name = "A", effects = arrayListOf(), category = "стойка", coolDown = 1)
        val b = Perk(name = "B", effects = arrayListOf(), category = "стойка", coolDown = 1)
        val c = Perk(name = "C", effects = arrayListOf(), coolDown = 1)
        val hand = aldtoll.twiligihts.model.Hand("h", perks = arrayListOf(a, b, c)).apply { init(true) }
        f.heroHandsI.update(arrayListOf(hand))
        f.updatePerks.updateEnableStatus()
        assertTrue(a.enable && b.enable && c.enable)
        heroUses(a)
        assertEquals(0, a.reload); assertEquals(0, b.reload); assertEquals(1, c.reload)
        assertFalse(a.enable); assertFalse(b.enable); assertTrue(c.enable)
    }

    // RULE-073: executeIfAvailable проверяет цену/ресурсы/условия/перезарядку/заряды
    @Test
    fun executeIfAvailable_checks() {
        f.heroStocks(Stock(3, 1))
        val p = perkOf("Авто", Effect.Attack(5), price = 5)
        assertFalse(f.perk.executeIfAvailable(p, true))
        f.heroStocks(Stock(5, 1))
        assertTrue(f.perk.executeIfAvailable(p, true))
        assertEquals(0, f.heroStock(1))
        assertEquals(25, f.enemy.hp)

        val needRes = Perk(name = "Р", effects = arrayListOf(), resources = arrayListOf(Resource(0, "Поиск")))
        f.heroRes(Resource(0, "Поиск"))
        assertFalse(f.perk.executeIfAvailable(needRes, true)) // amount 0 => нужно > 0
        f.heroRes(Resource(1, "Поиск"))
        assertTrue(f.perk.executeIfAvailable(needRes, true))

        val charged = Perk(name = "З", effects = arrayListOf(), charges = 1, currentCharges = 1)
        assertTrue(f.perk.executeIfAvailable(charged, true))
        assertFalse(f.perk.executeIfAvailable(charged, true))
    }

    // RULE-073, решение 6 (SUSPECT-9, НЕ баг): в реальных сценах навыку передаётся КОПИЯ перка из руки;
    // Perk — data class, currentCharges участвует в equals, поэтому списание одно
    @Test
    fun executeIfAvailable_handPerkCopy_singleChargeDecrease() {
        val p = Perk(name = "Двойной", effects = arrayListOf(), charges = 2)
        f.heroHand(p)
        val copy = p.copy()
        assertTrue(f.perk.executeIfAvailable(copy, true))
        // списана копия, после этого она уже не равна перку из руки, и перк в руке не списывается повторно
        assertEquals(1, copy.currentCharges)
        assertEquals(2, p.currentCharges)
    }

    // RULE-073, решение 6: обычный запуск навыка из руки (execute с копией) списывает заряд в руке один раз
    @Test
    fun execute_handPerkCopy_singleChargeDecrease() {
        val p = Perk(name = "Двойной", effects = arrayListOf(), charges = 2)
        f.heroHand(p)
        f.perk.execute(p.copy(), true)
        assertEquals(1, p.currentCharges)
    }

    // RULE-074: ресурсы перка тратятся (не ниже 0)
    @Test
    fun resourcesSpent() {
        f.heroRes(Resource(3, "Поиск"))
        heroUses(Perk(name = "Р", effects = arrayListOf(), resources = arrayListOf(Resource(5, "Поиск"))))
        assertEquals(0, f.heroResI.value()!![0].amount)
    }

    // RULE-075: HeroStates — статус добавляется при выполнении условия и снимается иначе
    @Test
    fun states_addAndRemove() {
        val wounded = status("Ранен", StatusType.WEAK, 1, duration = -1)
        val lowHp = Condition(10, EffectTarget.HERO, Condition.Parameter.HP, symbol = Condition.Symbol.LESS)
        f.heroStatesI.update(arrayListOf(State(lowHp, arrayListOf(), wounded, "Ранен")))
        f.enemyStatesI.update(arrayListOf())
        f.hero = f.hero.copy(hp = 5)
        f.perk.updatePersonsStates()
        assertTrue(f.hero.statuses.any { it.name == "Ранен" })
        assertTrue(f.logs().contains("Герой получает Ранен 1"))
        f.hero = f.hero.copy(hp = 15)
        f.perk.updatePersonsStates()
        assertFalse(f.hero.statuses.any { it.name == "Ранен" })
    }

    // RULE-076: сообщение о применении навыка; пустая строка между навыками; place-перк без префикса
    @Test
    fun messageAboutUsedPerk() {
        f.perk.resetPerkLogSpacing()
        f.perk.messageAboutUsedPerk(perkOf("Удар", gemType = 2), true)
        f.perk.messageAboutUsedPerk(Perk(name = "Обвал", effects = arrayListOf(), place = true), false)
        assertEquals(listOf("Герой применяет Удар", "", "Обвал"), f.logs())
        assertEquals(2, f.logRepo.getCurrentLogs()[0].gemType)
    }

    // RULE-077: INFO с message пишет в лог
    @Test
    fun infoEffect() {
        heroUses(perkOf("i", Effect.Info(message = "Тишина")))
        assertTrue(f.logs().contains("Тишина"))
    }

    // RULE-078: EDIT_RES SET/CHANGE, результат может быть отрицательным
    @Test
    fun editResources() {
        f.heroRes(Resource(3, "Поиск"))
        heroUses(perkOf("r", Effect.EditResources(-5, "Поиск")))
        assertEquals(-2, f.heroResI.value()!![0].amount)
        heroUses(perkOf("r", Effect.EditResources(7, "Поиск", Effect.EditResources.Type.SET)))
        assertEquals(7, f.heroResI.value()!![0].amount)
    }

    // RULE-078, решение 12 (SUSPECT-12): EDIT_RES с target ALL меняет и героя, и врага
    @Test
    fun editResources_allTarget_heroAndEnemy() {
        f.heroRes(Resource(3, "Поиск"))
        f.enemyResI.update(arrayListOf(Resource(3, "Поиск")))
        heroUses(perkOf("r", Effect.EditResources(1, "Поиск", target = EffectTarget.ALL)))
        assertEquals(4, f.heroResI.value()!![0].amount)
        assertEquals(4, f.enemyResI.value()!![0].amount)
    }

    // RULE-007, решение 15 (SUSPECT-1): probability перка = 0 — никогда, 100 — всегда (executeIfAvailable)
    @Test
    fun perkProbability_zeroNever_hundredAlways() {
        val never = Perk(name = "Никогда", effects = arrayListOf(), probability = 0)
        val always = Perk(name = "Всегда", effects = arrayListOf(), probability = 100)
        repeat(2000) { assertFalse(f.perk.executeIfAvailable(never, true)) }
        repeat(2000) { assertTrue(f.perk.executeIfAvailable(always, true)) }
    }

    // RULE-007, решение 15: probability эффекта = 0 — эффект никогда не применяется
    @Test
    fun effectProbability_zeroNever() {
        val p = perkOf("Нулевой шанс", Effect.Attack(1, probability = 0))
        repeat(500) { heroUses(p) }
        assertEquals(30, f.enemy.hp)
    }

    // RULE-079: атака ALL бьёт обоих
    @Test
    fun attackAll() {
        heroUses(perkOf("Взрыв", Effect.Attack(3, target = EffectTarget.ALL)))
        assertEquals(17, f.hero.hp)
        assertEquals(27, f.enemy.hp)
    }
}
