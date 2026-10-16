package aldtoll.twiligihts.characterization

import aldtoll.twiligihts.characterization.Fixture.Companion.status
import aldtoll.twiligihts.model.BattleSettings
import aldtoll.twiligihts.model.Status.StatusType
import aldtoll.twiligihts.model.Stock
import aldtoll.twiligihts.model.effects.Effect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Rule
import org.junit.Before
import org.junit.Test

/** RULE-040..RULE-046: ApplyAttackExecutor (урон после всех модификаторов). */
class ApplyAttackExecutorTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var f: Fixture

    @Before
    fun createFixture() {
        f = Fixture()
    }

    // RULE-040: BOTH — сначала щиты, остаток в HP
    @Test
    fun both_shieldsFirstThenHp() {
        f.enemy = f.enemy.copy(shield = 4)
        f.applyAttack.execute(f.enemy, Effect.Attack(10))
        assertEquals(0, f.enemy.shield)
        assertEquals(24, f.enemy.hp)
        assertEquals(1, f.enemy.touches)
        assertEquals(1, f.enemy.hits)
        assertTrue(f.logs().contains("Щиты противника блокируют 4 урона. Щиты противника уничтожены."))
    }

    // RULE-040: щит больше урона
    @Test
    fun both_shieldAbsorbsAll_noHit() {
        f.enemy = f.enemy.copy(shield = 15)
        f.applyAttack.execute(f.enemy, Effect.Attack(10))
        assertEquals(5, f.enemy.shield)
        assertEquals(30, f.enemy.hp)
        assertEquals(1, f.enemy.touches)
        assertEquals(0, f.enemy.hits)
        assertTrue(f.logs().contains("Щиты противника блокируют 10 урона.(5)"))
    }

    // RULE-041: HP — мимо щитов; SP — только щиты
    @Test
    fun hpAndSpTypes() {
        f.enemy = f.enemy.copy(shield = 4)
        f.applyAttack.execute(f.enemy, Effect.Attack(6, Effect.Attack.Type.HP))
        assertEquals(4, f.enemy.shield); assertEquals(24, f.enemy.hp)
        f.applyAttack.execute(f.enemy, Effect.Attack(10, Effect.Attack.Type.SP))
        assertEquals(0, f.enemy.shield); assertEquals(24, f.enemy.hp)
    }

    // RULE-042: урон от статуса / по себе — без касания и попадания
    @Test
    fun fromStatus_noTouchNoHit() {
        f.applyAttack.execute(f.enemy, Effect.Attack(5), fromStatusOrSelfAttack = true)
        assertEquals(25, f.enemy.hp)
        assertEquals(0, f.enemy.touches)
        assertEquals(0, f.enemy.hits)
    }

    // RULE-043, SUSPECT-26: смертельный удар не засчитывается как попадание
    @Test
    fun killingBlow_notCountedAsHit() {
        f.applyAttack.execute(f.enemy, Effect.Attack(50))
        assertEquals(0, f.enemy.hp)
        assertEquals(0, f.enemy.hits)
        assertEquals(1, f.enemy.touches)
    }

    // RULE-044, решение 1 (SUSPECT-6): очки режутся у того, кто получил урон (здесь враг), герой не затронут
    @Test
    fun damageKeepStrategy_appliedToDamagedPerson_enemyHit() {
        f.settingsI.update(BattleSettings(gemSettings = arrayListOf(
            BattleSettings.GemSettings(type = "1", damageKeepStrategy = 50))))
        f.heroStocks(Stock(10, 1))
        f.enemyStocks(Stock(10, 1))
        f.applyAttack.execute(f.enemy, Effect.Attack(3))
        assertEquals(10, f.heroStock(1))
        assertEquals(5, f.enemyStock(1))
    }

    // RULE-044, решение 1: герой получил урон -> режутся очки героя, враг не затронут (общая стратегия)
    @Test
    fun damageKeepStrategy_appliedToDamagedPerson_heroHit() {
        f.settingsI.update(BattleSettings(gemSettings = arrayListOf(
            BattleSettings.GemSettings(type = "1", damageKeepStrategy = 50))))
        f.heroStocks(Stock(10, 1))
        f.enemyStocks(Stock(10, 1))
        f.applyAttack.execute(f.hero, Effect.Attack(3))
        assertEquals(5, f.heroStock(1))
        assertEquals(10, f.enemyStock(1))
    }

    // RULE-044: урон от статуса стоки не трогает
    @Test
    fun damageKeepStrategy_notAppliedForStatusDamage() {
        f.settingsI.update(BattleSettings(gemSettings = arrayListOf(
            BattleSettings.GemSettings(type = "1", damageKeepStrategy = 50))))
        f.heroStocks(Stock(10, 1))
        f.applyAttack.execute(f.hero, Effect.Attack(3), true)
        assertEquals(10, f.heroStock(1))
    }

    // RULE-045, решение 2 (SUSPECT-7): VAMP ищется у АТАКУЮЩЕГО и лечит атакующего; у цели не срабатывает
    @Test
    fun vamp_healsAttacker_notTarget() {
        f.hero = f.hero.copy(hp = 10)
        f.hero.statuses.add(status("Вампиризм", StatusType.VAMP, 50))
        f.enemy = f.enemy.copy(hp = 10)
        f.enemy.statuses.add(status("Вампиризм", StatusType.VAMP, 50))
        // герой бьёт врага на 6
        f.applyAttack.execute(f.enemy, Effect.Attack(6))
        // враг: 10-6 = 4, VAMP цели не работает; герой: +6*50/100 = 3 -> 13
        assertEquals(4, f.enemy.hp)
        assertEquals(13, f.hero.hp)
        assertEquals("Герой  высасывает 3 здоровья (Вампиризм). (13/20)", f.logs().last())
    }

    // RULE-045, решение 2: враг бьёт героя -> лечится враг, лог от имени противника
    @Test
    fun vamp_enemyAttacker_healsEnemy() {
        f.enemy = f.enemy.copy(hp = 10)
        f.enemy.statuses.add(status("Вампиризм", StatusType.VAMP, 50))
        f.applyAttack.execute(f.hero, Effect.Attack(6))
        assertEquals(14, f.hero.hp)
        assertEquals(13, f.enemy.hp)
        assertEquals("Противник  высасывает 3 здоровья (Вампиризм). (13/30)", f.logs().last())
    }

    // RULE-045, решение 18 (SUSPECT-11): «полностью восстановлено» только если после лечения hp == maxHp
    @Test
    fun vamp_fullyRestoredMessage_onlyWhenHpReachesMax() {
        f.hero = f.hero.copy(hp = 19)
        f.hero.statuses.add(status("Вампиризм", StatusType.VAMP, 50))
        f.applyAttack.execute(f.enemy, Effect.Attack(6))
        assertEquals(20, f.hero.hp)
        assertEquals("Герой  высасывает 3 здоровья (Вампиризм). Здоровье полностью восстановлено", f.logs().last())
    }

    // RULE-045: help-атака не даёт вампиризма
    @Test
    fun vamp_notForHelpAttack() {
        f.enemy = f.enemy.copy(hp = 10)
        f.enemy.statuses.add(status("Вампиризм", StatusType.VAMP, 50))
        f.applyAttack.execute(f.enemy, Effect.Attack(6, help = true))
        assertEquals(4, f.enemy.hp)
    }

    // RULE-046: лог атаки начинается с атакующего (цель не Hero -> "Герой")
    @Test
    fun log_attackerNameDerivedFromTarget() {
        f.applyAttack.execute(f.enemy, Effect.Attack(2))
        assertEquals("Герой Наносит 2 урона  противнику", f.logs()[0])
        assertEquals("Противник получает 2 урона. (28/30)", f.logs()[1])
    }
}
