package aldtoll.twiligihts.characterization

import aldtoll.twiligihts.characterization.Fixture.Companion.status
import aldtoll.twiligihts.model.Condition
import aldtoll.twiligihts.model.Condition.Parameter
import aldtoll.twiligihts.model.Condition.Symbol
import aldtoll.twiligihts.model.Resource
import aldtoll.twiligihts.model.Status.StatusType
import aldtoll.twiligihts.model.Stock
import aldtoll.twiligihts.model.effects.Effect
import aldtoll.twiligihts.model.effects.Effect.EffectTarget
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** RULE-030..RULE-036: CheckConditionExecutor + ApplyFunctionExecutor. */
class CheckConditionExecutorTest {

    private val f = Fixture()

    private fun cond(
        p: Parameter, s: Symbol, v: Int = 0, t: EffectTarget = EffectTarget.HERO,
        name: String? = null, gemType: Int? = null,
    ) = Condition(value = v, target = t, parameter = p, name = name, symbol = s, gemType = gemType)

    // RULE-030: символы сравнения
    @Test
    fun symbols() {
        f.hero = f.hero.copy(hp = 10)
        assertTrue(f.check.execute(cond(Parameter.HP, Symbol.MORE, 9), true))
        assertFalse(f.check.execute(cond(Parameter.HP, Symbol.MORE, 10), true))
        assertTrue(f.check.execute(cond(Parameter.HP, Symbol.LESS, 11), true))
        assertTrue(f.check.execute(cond(Parameter.HP, Symbol.EQUALS, 10), true))
        assertTrue(f.check.execute(cond(Parameter.HP, Symbol.NOT, 3), true))
        assertTrue(f.check.execute(cond(Parameter.HP, Symbol.HAVE), true))
        assertTrue(f.check.execute(cond(Parameter.HP, Symbol.EXIST), true))
        assertFalse(f.check.execute(cond(Parameter.HP, Symbol.EMPTY), true))
    }

    // RULE-030: HAVE = >0, EXIST = !=0 (отличаются на отрицательных)
    @Test
    fun haveVsExist_onNegativeNeutralStatus() {
        f.hero.statuses.add(status("Точность", StatusType.ACCURACY, -5))
        val have = cond(Parameter.STATUS, Symbol.HAVE, name = "Точность")
        val exist = cond(Parameter.STATUS, Symbol.EXIST, name = "Точность")
        assertFalse(f.check.execute(have, true))
        assertTrue(f.check.execute(exist, true))
    }

    // RULE-031: STATUS берёт value первого АКТИВНОГО статуса с таким именем, иначе 0
    @Test
    fun statusParameter_onlyActive() {
        f.hero.statuses.add(status("Горит", StatusType.DAMAGE, 3, duration = 0))
        assertTrue(f.check.execute(cond(Parameter.STATUS, Symbol.EMPTY, name = "Горит"), true))
        f.hero.statuses.add(status("Горит", StatusType.DAMAGE, 4, duration = 2))
        assertTrue(f.check.execute(cond(Parameter.STATUS, Symbol.EQUALS, 4, name = "Горит"), true))
    }

    // RULE-032: HP_P — целочисленное деление
    @Test
    fun hpPercent_truncates() {
        f.hero = f.hero.copy(hp = 2, maxHp = 3)
        assertEquals(66, f.check.getParameter(f.hero, cond(Parameter.HP_P, Symbol.EQUALS)))
    }

    // RULE-033: TURN/TIME/ATTEMPT/RES/STOCK/SP/HITS/TOUCHES/TOUCHED/HIT
    @Test
    fun otherParameters() {
        f.turnI.update(7); f.timeI.update(12); f.attemptI.update(3)
        f.heroRes(Resource(4, "Скрытность"))
        f.heroStocks(Stock(15, 2))
        f.enemyStocks(Stock(6, 2))
        f.hero = f.hero.copy(shield = 5, hits = 2, touches = 3, wasTouchedByPreviousEffect = true)
        val h = f.hero
        fun p(c: Condition) = f.check.getParameter(h, c)
        assertEquals(7, p(cond(Parameter.TURN, Symbol.EQUALS)))
        assertEquals(12, p(cond(Parameter.TIME, Symbol.EQUALS)))
        assertEquals(3, p(cond(Parameter.ATTEMPT, Symbol.EQUALS)))
        assertEquals(4, p(cond(Parameter.RES, Symbol.EQUALS, name = "Скрытность")))
        assertEquals(0, p(cond(Parameter.RES, Symbol.EQUALS, name = "нет")))
        assertEquals(15, p(cond(Parameter.STOCK, Symbol.EQUALS, gemType = 2)))
        assertEquals(6, f.check.getParameter(f.enemy, cond(Parameter.STOCK, Symbol.EQUALS, gemType = 2)))
        assertEquals(5, p(cond(Parameter.SP, Symbol.EQUALS)))
        assertEquals(2, p(cond(Parameter.HITS, Symbol.EQUALS)))
        assertEquals(3, p(cond(Parameter.TOUCHES, Symbol.EQUALS)))
        assertEquals(1, p(cond(Parameter.TOUCHED, Symbol.EQUALS)))
        assertEquals(0, p(cond(Parameter.HIT, Symbol.EQUALS)))
    }

    // RULE-034: target ENEMY/HERO/ALL/SELF
    @Test
    fun targets() {
        f.hero = f.hero.copy(hp = 5)
        f.enemy = f.enemy.copy(hp = 25)
        assertTrue(f.check.execute(cond(Parameter.HP, Symbol.MORE, 20, EffectTarget.ENEMY), true))
        assertFalse(f.check.execute(cond(Parameter.HP, Symbol.MORE, 20, EffectTarget.ALL), true))
        assertTrue(f.check.execute(cond(Parameter.HP, Symbol.MORE, 1, EffectTarget.ALL), true))
        assertTrue(f.check.execute(cond(Parameter.HP, Symbol.EQUALS, 5, EffectTarget.SELF), true))
        assertTrue(f.check.execute(cond(Parameter.HP, Symbol.EQUALS, 25, EffectTarget.SELF), false))
    }

    // RULE-034, SUSPECT-5: FOE проверяет ту же сторону, что и SELF
    @Test
    fun foeTarget_checksSameSideAsSelf() {
        f.hero = f.hero.copy(hp = 5)
        f.enemy = f.enemy.copy(hp = 25)
        assertTrue(f.check.execute(cond(Parameter.HP, Symbol.EQUALS, 5, EffectTarget.FOE), true))
        assertTrue(f.check.execute(cond(Parameter.HP, Symbol.EQUALS, 25, EffectTarget.FOE), false))
    }

    // RULE-035: функция = сумма (mul * параметр).toInt() по сегментам
    @Test
    fun applyFunction_sumsSegments() {
        f.hero = f.hero.copy(hp = 7, shield = 3)
        f.enemy = f.enemy.copy(hp = 25)
        val func = Effect.Func(segments = listOf(
            Effect.Func.Segment(Parameter.HP, 0.5f, Effect.Source.SELF),
            Effect.Func.Segment(Parameter.SP, 2f, Effect.Source.HERO),
            Effect.Func.Segment(Parameter.HP, 0.1f, Effect.Source.FOE),
        ))
        // 7*0.5=3.5->3 ; 3*2=6 ; 25*0.1=2.5->2
        assertEquals(11, f.applyFunc.execute(func, isHeroPerk = true))
        // для врага SELF=враг, FOE=герой: 25*0.5=12 ; 6 ; 7*0.1=0.7->0
        assertEquals(18, f.applyFunc.execute(func, isHeroPerk = false))
    }
}
