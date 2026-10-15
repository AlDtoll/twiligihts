package aldtoll.twiligihts.characterization

import aldtoll.twiligihts.characterization.Fixture.Companion.status
import aldtoll.twiligihts.model.Condition
import aldtoll.twiligihts.model.Gem
import aldtoll.twiligihts.model.Hand
import aldtoll.twiligihts.model.Perk
import aldtoll.twiligihts.model.Resource
import aldtoll.twiligihts.model.Status
import aldtoll.twiligihts.model.Status.StatusType
import aldtoll.twiligihts.model.Stock
import aldtoll.twiligihts.model.characters.Enemy
import aldtoll.twiligihts.model.characters.Hero
import aldtoll.twiligihts.model.effects.Effect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Rule
import org.junit.Test

/** RULE-001..RULE-019: модели (Stock, Resource, Person, Status, Perk, Effect). */
class ModelRulesTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun reset() = Fixture.resetGlobals()

    // RULE-001
    @Test
    fun stock_increase_isCappedByMaxValue() {
        val s = Stock(8, 1, maxValue = 10)
        s.increaseStock(5)
        assertEquals(10, s.value)
        val unlimited = Stock(8, 1)
        unlimited.increaseStock(500)
        assertEquals(508, unlimited.value)
    }

    // RULE-001: отрицательное добавление не ограничено снизу
    @Test
    fun stock_increase_withNegative_canGoBelowZero() {
        val s = Stock(3, 1, maxValue = 10)
        s.increaseStock(-5)
        assertEquals(-2, s.value)
    }

    // RULE-002
    @Test
    fun resource_decrease_floorsAtZero() {
        val r = Resource(5, "Поиск")
        r.decreaseValue(7)
        assertEquals(0, r.amount)
        val r2 = Resource(5, "Поиск")
        r2.decreaseValue(2)
        assertEquals(3, r2.amount)
    }

    // RULE-003
    @Test
    fun person_hp_decreaseFloorsAtZero_increaseCapsAtMax_setCapsAtMax() {
        val h = Hero(hp = 10, maxHp = 20, shield = 0, wounds = 0, maxWounds = 0)
        h.decreaseHp(15)
        assertEquals(0, h.hp)
        h.increaseHp(25)
        assertEquals(20, h.hp)
        h.setHpValue(50)
        assertEquals(20, h.hp)
        h.setHpValue(-3)
        assertEquals(-3, h.hp) // setHpValue не ограничивает снизу
    }

    // RULE-004
    @Test
    fun person_hitTouchUndoClear() {
        val e = Enemy(hp = 10, maxHp = 10, wounds = 0, maxWounds = 0, shield = 0)
        e.hit(); e.touch(); e.touch()
        assertEquals(1, e.hits); assertEquals(2, e.touches)
        assertTrue(e.wasHitByPreviousEffect); assertTrue(e.wasTouchedByPreviousEffect)
        e.undo()
        assertFalse(e.wasHitByPreviousEffect); assertFalse(e.wasTouchedByPreviousEffect)
        assertEquals(1, e.hits) // undo не трогает счётчики
        e.clearHitsAndTouches()
        assertEquals(0, e.hits); assertEquals(0, e.touches)
    }

    // RULE-005
    @Test
    fun person_recreate_deepCopiesStatusesOnly() {
        val h = Hero(hp = 10, maxHp = 20, shield = 0, wounds = 0, maxWounds = 0,
            statuses = arrayListOf(status("Сила", StatusType.STRONG, 2)))
        val copy = h.recreate()
        assertNotSame(h.statuses[0], copy.statuses[0])
        copy.statuses[0].value = 99
        assertEquals(2, h.statuses[0].value)
    }

    // RULE-006: активность статуса
    @Test
    fun status_isActive_rules() {
        assertTrue(status("a", StatusType.STRONG, 1, duration = 1).isActive())
        assertFalse(status("a", StatusType.STRONG, 0, duration = 1).isActive())
        assertFalse(status("a", StatusType.STRONG, -1, duration = 1).isActive()) // GOOD: только > 0
        assertTrue(status("a", StatusType.ACCURACY, -5, duration = 1).isActive()) // NEUTRAL: != 0
        assertFalse(status("a", StatusType.STRONG, 1, duration = 0).isActive())
        assertTrue(status("a", StatusType.STRONG, 1, duration = -1).isActive()) // -1 = бесконечно
        assertFalse(status("a", StatusType.STRONG, 1, duration = -2).isActive())
        assertFalse(status("a", StatusType.STRONG, 1, times = 0).isActive())
        assertTrue(status("a", StatusType.STRONG, 1, times = 1).isActive())
    }

    // RULE-006: VULNERABLE окрашен как BAD -> активен только при value > 0
    @Test
    fun status_vulnerable_negativeValue_isInactive() {
        assertFalse(status("v", StatusType.VULNERABLE, -2).isActive())
        assertTrue(status("v", StatusType.CHANGE_STOCK, -2).isActive())
    }

    // RULE-007, SUSPECT-1: probability=0 всё равно срабатывает ~1/101 (roll 0..100, сравнение <=)
    @Test
    fun status_isWork_probabilityZero_stillSometimesWorks() {
        val s = status("p", StatusType.STRONG, 1, probability = 0)
        var worked = 0
        repeat(5000) { if (s.isWork()) worked++ }
        assertTrue("expected some successes, got $worked", worked > 0)
        assertTrue(worked < 200)
    }

    // RULE-008
    @Test
    fun status_decreaseValueAndTimes_floorAtZero() {
        val s = status("a", StatusType.STRONG, 1, times = 1)
        s.decreaseValue(); s.decreaseValue()
        assertEquals(0, s.value)
        s.decreaseTimes(); s.decreaseTimes()
        assertEquals(0, s.times)
        val noTimes = status("a", StatusType.STRONG, 1)
        noTimes.decreaseTimes()
        assertNull(noTimes.times)
    }

    // RULE-009: init перка в руке
    @Test
    fun perk_init_defaults() {
        val p = Perk(name = "Удар", effects = arrayListOf(Effect.Attack(3)), coolDown = 2, charges = 3)
        p.init(heroHand = true, gemType = 4)
        assertEquals(arrayListOf(Perk.Price(0, 4)), p.prices)
        assertEquals(3, p.currentCharges)
        assertEquals(2, p.startReload)
        assertEquals(2, p.reload)
        assertFalse(p.isReloading())
        assertFalse(p.hideDescription)
        assertEquals("Наносит 3 урона  противнику", p.description)

        val combo = Perk(name = "c", effects = arrayListOf(), coolDown = 2, reloadType = Perk.ReloadType.COMBO)
        combo.init(heroHand = false, gemType = 1)
        assertEquals(0, combo.reload)
        assertTrue(combo.isReloading())
        assertTrue(combo.hideDescription)
    }

    // RULE-010
    @Test
    fun perk_reloadAfterUse_onlyPerkAndComboAdvance() {
        fun mk(t: Perk.ReloadType) = Perk(name = "x", effects = arrayListOf(), coolDown = 2, reloadType = t).apply { reload = 0 }
        val turn = mk(Perk.ReloadType.TURN); turn.reloadPerkAfterUse()
        val perk = mk(Perk.ReloadType.PERK); perk.reloadPerkAfterUse()
        val combo = mk(Perk.ReloadType.COMBO); combo.reloadPerkAfterUse()
        assertEquals(0, turn.reload)
        assertEquals(1, perk.reload)
        assertEquals(1, combo.reload)
        val hidden = mk(Perk.ReloadType.PERK).apply { show = false }
        hidden.reloadPerkAfterUse()
        assertEquals(0, hidden.reload)
    }

    // RULE-011
    @Test
    fun perk_charges_decreaseFloorsAtZero_nullIsUnlimited() {
        val p = Perk(name = "x", effects = arrayListOf(), charges = 1)
        p.decreaseCharges(); p.decreaseCharges()
        assertEquals(0, p.currentCharges)
        val u = Perk(name = "x", effects = arrayListOf())
        u.decreaseCharges()
        assertNull(u.currentCharges)
    }

    // RULE-012
    @Test
    fun perk_nameForDisplay() {
        val p = Perk(name = "Удар", effects = arrayListOf(), probability = 50,
            conditionsForDisplay = arrayListOf(Condition()), category = "атака")
        assertEquals("Удар 50%*(атака)", p.nameForDisplay())
    }

    // RULE-013, SUSPECT-2: currentCharges эффекта инициализируется null, пока не вызван init()
    @Test
    fun effect_currentCharges_nullUntilInit() {
        val a = Effect.Attack(5, charges = 2)
        assertNull(a.currentCharges)
        a.init()
        assertEquals(2, a.currentCharges)
    }

    // RULE-014
    @Test
    fun effect_defaultTargets() {
        assertEquals(Effect.EffectTarget.FOE, Effect.Attack(1).target)
        assertEquals(Effect.EffectTarget.SELF, Effect.Defend(1).target)
        assertEquals(Effect.EffectTarget.SELF, Effect.Heal(1).target)
        assertEquals(Effect.EffectTarget.HERO, Effect.EditStock(1).target)
        assertEquals(Effect.Attack.Type.BOTH, Effect.Attack(1).type)
        assertEquals(Effect.SuccessType.HIT, Effect.Attack(1).successType)
        assertEquals(Effect.SuccessType.ANY, Effect.Defend(1).successType)
    }

    // RULE-014: цель EditStatus по умолчанию зависит от "цвета" статуса
    @Test
    fun editStatus_defaultTarget_byStatusColor() {
        assertEquals(Effect.EffectTarget.SELF, Effect.EditStatus(status("s", StatusType.STRONG, 1), value = 1).target)
        assertEquals(Effect.EffectTarget.FOE, Effect.EditStatus(status("w", StatusType.WEAK, 1), value = 1).target)
        assertEquals(Effect.EffectTarget.SELF, Effect.EditStatus(status("n", StatusType.INFO, 1), value = 1).target)
        assertEquals(Effect.EffectTarget.FOE, Effect.EditStatus(status("n", StatusType.INFO, 0), value = 1).target)
        assertEquals(Effect.EffectTarget.FOE, Effect.EditStatus(status("v", StatusType.VULNERABLE, 1), value = 1).target)
    }

    // RULE-015
    @Test
    fun func_allSegments_legacyParameterFirst() {
        val f = Effect.Func(
            parameter = Condition.Parameter.HP, mulP = 0.5f,
            segments = listOf(Effect.Func.Segment(Condition.Parameter.SP, 2f))
        )
        val segs = f.allSegments()
        assertEquals(2, segs.size)
        assertEquals(Condition.Parameter.HP, segs[0].parameter)
        assertEquals(0.5f, segs[0].mul)
        assertEquals(0, Effect.Func().rollDice())
    }

    // RULE-015
    @Test(expected = IllegalArgumentException::class)
    fun func_segment_statusWithoutName_throws() {
        Effect.Func.Segment(Condition.Parameter.STATUS)
    }

    @Test(expected = IllegalArgumentException::class)
    fun func_segment_stockWithoutGemType_throws() {
        Effect.Func.Segment(Condition.Parameter.STOCK)
    }

    // RULE-015
    @Test
    fun func_rollDice_inRange() {
        val f = Effect.Func(dice = 6)
        repeat(300) { assertTrue(f.rollDice() in 1..6) }
    }

    // RULE-016: тексты описаний (используются в UI и логе)
    @Test
    fun effect_descriptions() {
        assertEquals("Дает 4 щитов себе", Effect.Defend(4).getDisplayDescription())
        assertEquals("Наносит 3 урона .неблокируемоего противнику 50%",
            Effect.Attack(3, Effect.Attack.Type.HP, probability = 50).getDisplayDescription())
        assertEquals("Отнимает 2 очков атаки;защиты; герою",
            Effect.EditStock(-2, arrayListOf(1, 2)).getDisplayDescription())
        assertEquals("Наносит 1-6 урона  противнику",
            Effect.Attack(0, func = Effect.Func(dice = 6)).getDisplayDescription())
        assertEquals("Наносит X=3 урона  противнику", Effect.Attack(3).getLogDescription("X"))
        // SUSPECT-3: в логе ENEMY -> "противнику", в описании ENEMY -> "врагу"
        assertEquals("Дает 2 щитов врагу", Effect.Defend(2, target = Effect.EffectTarget.ENEMY).getDisplayDescription())
        assertEquals("Дает 2 щитов противнику", Effect.Defend(2, target = Effect.EffectTarget.ENEMY).getLogDescription())
    }

    // RULE-017
    @Test
    fun gem_names_and_fallbacks() {
        assertEquals("атаки", Gem.getName(1))
        assertEquals("концентрации/магии", Gem.getName(5))
        assertEquals("", Gem.getName(6))
        assertEquals("", Gem.getIconUri(0))
    }

    // RULE-018
    @Test
    fun gem_generateNewGem_typesInRange() {
        Gem.GEM_TYPE_NUMBER = 4
        repeat(500) {
            val g = Gem.generateNewGem()
            assertTrue(g.type in 1..4)
            g.bonusType?.let { b -> assertTrue(b in 1..4 && b != g.type) }
            g.extraType?.let { e -> assertTrue(e in 1..4 || e == Gem.EMPTY_COLOR) }
        }
    }

    // RULE-019
    @Test
    fun hand_init_initsAllPerksWithHandColor() {
        val p = Perk(name = "x", effects = arrayListOf())
        Hand("h", gemType = 3, perks = arrayListOf(p)).init(heroHand = true)
        assertEquals(3, p.prices[0].gemType)
    }
}
