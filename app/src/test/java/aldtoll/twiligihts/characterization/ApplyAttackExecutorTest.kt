package aldtoll.twiligihts.characterization

import aldtoll.twiligihts.characterization.Fixture.Companion.status
import aldtoll.twiligihts.model.BattleSettings
import aldtoll.twiligihts.model.Status.StatusType
import aldtoll.twiligihts.model.Stock
import aldtoll.twiligihts.model.effects.Effect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** RULE-040..RULE-046: ApplyAttackExecutor (урон после всех модификаторов). */
class ApplyAttackExecutorTest {

    private val f = Fixture()

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

    // RULE-044, SUSPECT-6: после урона по ЛЮБОЙ цели режутся стоки ГЕРОЯ (damageKeepStrategy)
    @Test
    fun damageKeepStrategy_appliedToHeroStocks_evenWhenEnemyIsHit() {
        f.settingsI.update(BattleSettings(gemSettings = arrayListOf(
            BattleSettings.GemSettings(type = "1", damageKeepStrategy = 50))))
        f.heroStocks(Stock(10, 1))
        f.enemyStocks(Stock(10, 1))
        f.applyAttack.execute(f.enemy, Effect.Attack(3))
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

    // RULE-045, SUSPECT-7: VAMP лечит не атакующего, а ЦЕЛЬ (если у цели есть VAMP)
    @Test
    fun vamp_healsTargetWithVamp_notAttacker() {
        f.hero = f.hero.copy(hp = 10)
        f.hero.statuses.add(status("Вампиризм", StatusType.VAMP, 50))
        f.enemy = f.enemy.copy(hp = 10)
        f.enemy.statuses.add(status("Вампиризм", StatusType.VAMP, 50))
        // герой бьёт врага на 6
        f.applyAttack.execute(f.enemy, Effect.Attack(6))
        // враг: 10-6 = 4, затем +6*50/100 = 3 -> 7
        assertEquals(7, f.enemy.hp)
        assertEquals(10, f.hero.hp)
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
