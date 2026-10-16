package aldtoll.twiligihts.characterization

import aldtoll.twiligihts.characterization.Fixture.Companion.perkOf
import aldtoll.twiligihts.characterization.Fixture.Companion.status
import aldtoll.twiligihts.model.BattleSettings
import aldtoll.twiligihts.model.Hand
import aldtoll.twiligihts.model.Perk
import aldtoll.twiligihts.model.Status.StatusType
import aldtoll.twiligihts.model.Stock
import aldtoll.twiligihts.model.effects.Effect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Rule
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

/** RULE-090..RULE-099: конец хода героя → ход врага → начало хода героя (EndTurnExecutor). */
class EndTurnExecutorTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var f: Fixture

    @Before
    fun createFixture() {
        f = Fixture()
    }

    // RULE-090: полный цикл: счётчик хода, стоки ×50%, щиты обнулены, лог
    @Test
    fun fullCycle_basics() {
        f.heroStocks(Stock(10, 1)); f.enemyStocks(Stock(10, 1))
        f.hero = f.hero.copy(shield = 4)
        f.enemy = f.enemy.copy(shield = 6)
        f.endTurn.execute()
        assertEquals(2, f.turnI.value())
        assertEquals(5, f.heroStock(1))
        assertEquals(5, f.enemyStock(1))
        assertEquals(0, f.hero.shield)
        assertEquals(0, f.enemy.shield)
        assertEquals(5, f.heroStockI.getStocksAtTurnStart()!![0].value)
        Mockito.verify(f.writeTempLog, Mockito.times(1)).execute()
        assertEquals(
            listOf("Hero закончил действовать", "", "Действует Enemy", "", "Ход 2", "Действует Hero"),
            f.logs()
        )
    }

    // RULE-091: turnKeepStrategy из GemSettings + статус CHANGE_TURN_KEEP_STRATEGY (не ниже 0%)
    @Test
    fun turnKeepStrategy() {
        f.settingsI.update(BattleSettings(gemSettings = arrayListOf(
            BattleSettings.GemSettings(type = "1", turnKeepStrategy = 30))))
        f.heroStocks(Stock(10, 1), Stock(10, 2))
        f.hero.statuses.add(status("Запас", StatusType.CHANGE_TURN_KEEP_STRATEGY, 40, duration = -1, gemTypes = arrayListOf(2)))
        f.endTurn.execute()
        assertEquals(3, f.heroStock(1))  // 30%
        assertEquals(9, f.heroStock(2))  // 50% + 40% = 90%
    }

    // RULE-092: враг применяет ВСЕ видимые и доступные перки по порядку рук; недоступные пропускает
    @Test
    fun enemyUsesAllEnabledPerks() {
        f.enemyStocks(Stock(10, 1))
        f.enemyHands(
            Hand("h1", perks = arrayListOf(perkOf("Укус", Effect.Attack(2)), perkOf("Дорогой", Effect.Attack(9), price = 6))),
            Hand("h2", perks = arrayListOf(perkOf("Рык", Effect.Attack(3)))),
        )
        f.endTurn.execute()
        assertEquals(15, f.hero.hp) // 2 + 3; "Дорогой" (6 > 5 после 50%) пропущен
        assertTrue(f.logs().contains("Противник применяет Укус"))
        assertFalse(f.logs().contains("Противник применяет Дорогой"))
    }

    // RULE-093: статусы тикают: применение в начале хода владельца, duration--, при 0 value=0
    @Test
    fun damageStatus_ticksAndExpires() {
        f.hero.statuses.add(status("Яд", StatusType.DAMAGE, 2, duration = 2))
        f.endTurn.execute()
        assertEquals(18, f.hero.hp)
        assertEquals(1, f.hero.statuses[0].duration)
        f.endTurn.execute()
        assertEquals(16, f.hero.hp)
        assertEquals(0, f.hero.statuses[0].duration)
        assertEquals(0, f.hero.statuses[0].value)
        f.endTurn.execute()
        assertEquals(16, f.hero.hp)
    }

    // RULE-094: DEFEND-статус создаёт щиты до DAMAGE-статуса
    @Test
    fun defendStatusBeforeDamageStatus() {
        f.hero.statuses.add(status("Яд", StatusType.DAMAGE, 2, duration = -1))
        f.hero.statuses.add(status("Стена", StatusType.DEFEND, 3, duration = -1))
        f.endTurn.execute()
        assertEquals(20, f.hero.hp)
        assertEquals(1, f.hero.shield)
    }

    // RULE-095: GENERATE — после урезания стоков; HEAL; DAMAGE_HP мимо щитов
    @Test
    fun generateHealDamageHp() {
        f.heroStocks(Stock(10, 1))
        f.hero = f.hero.copy(hp = 10)
        f.hero.statuses.add(status("Ген", StatusType.GENERATE, 4, gemTypes = arrayListOf(1)))
        f.hero.statuses.add(status("Реген", StatusType.HEAL, 3))
        f.endTurn.execute()
        assertEquals(9, f.heroStock(1))
        assertEquals(13, f.hero.hp)
        // решение 27 (SUSPECT-27): лог лечения статусом — «здоровья», а не «урона»
        assertTrue(f.logs().contains("Реген действует и восстанавливает 3 здоровья"))
    }

    // RULE-096: end=true статусы героя применяются в конце его хода (до хода врага)
    @Test
    fun endStatus_appliedAtEndOfHeroTurn() {
        f.hero.statuses.add(status("Стойка", StatusType.DEFEND, 5, end = true))
        f.enemyHands(Hand("h", perks = arrayListOf(perkOf("Укус", Effect.Attack(4)))))
        f.endTurn.execute()
        assertEquals(20, f.hero.hp) // щит 5 от end-статуса поглотил атаку врага
        assertEquals(0, f.hero.shield) // затем очищен в начале хода героя
    }

    // RULE-097: перезарядка по ходу
    @Test
    fun reloadRules() {
        fun p(n: String, t: Perk.ReloadType) = Perk(name = n, effects = arrayListOf(), coolDown = 2, reloadType = t)
        val turn = p("turn", Perk.ReloadType.TURN)
        val perkT = p("perk", Perk.ReloadType.PERK)
        val combo = p("combo", Perk.ReloadType.COMBO)
        val hidden = p("hidden", Perk.ReloadType.TURN)
        f.heroHandsI.update(arrayListOf(Hand("h", perks = arrayListOf(turn, perkT, combo, hidden)).apply { init(true) }))
        listOf(turn, perkT, combo, hidden).forEach { it.reload = 0 }
        combo.reload = 1
        hidden.show = false
        val eCombo = p("ecombo", Perk.ReloadType.COMBO)
        f.enemyHands(Hand("e", perks = arrayListOf(eCombo)))
        eCombo.reload = 0
        eCombo.show = false // чтобы враг его не применил
        f.endTurn.execute()
        assertEquals(1, turn.reload)
        assertEquals(2, perkT.reload)
        assertEquals(0, combo.reload)
        assertEquals(0, hidden.reload)
        // SUSPECT-13: у врага COMBO не сбрасывается в 0, а сразу заряжается до coolDown
        assertEquals(2, eCombo.reload)
    }

    // RULE-098: MAKE_ENEMY_MOVE — цикл останавливается на событии хода врага по доске
    @Test
    fun makeEnemyMove_stopsBeforeEnemyActions() {
        BattleSettings.MAKE_ENEMY_MOVE = true
        f.enemyHands(Hand("h", perks = arrayListOf(perkOf("Укус", Effect.Attack(4)))))
        f.endTurn.execute()
        assertEquals(20, f.hero.hp)
        assertEquals(1, f.turnI.value())
    }

    // RULE-099: ANIMATE_ENEMY_ACTIONS — первый перк первой видимой руки уходит в ExecutedPerkInteractor
    @Test
    fun animateEnemy_queuesFirstPerk() {
        BattleSettings.ANIMATE_ENEMY_ACTIONS = true
        val first = perkOf("Укус", Effect.Attack(4))
        f.enemyHands(Hand("hidden", perks = arrayListOf(perkOf("x"))).apply { show = false },
            Hand("h", perks = arrayListOf(first)))
        f.enemyHandsI.value()!![0].show = false
        f.endTurn.execute()
        assertEquals(first, f.executedPerkI.value()!!.perk)
        assertEquals(1, f.turnI.value())
    }

    // RULE-099: при анимации следующий перк врага выбирается callNextPerk; последний -> LAST_PERK
    @Test
    fun animateEnemy_callNextPerk_chain() {
        BattleSettings.ANIMATE_ENEMY_ACTIONS = true
        val a = perkOf("a"); val b = perkOf("b"); val c = perkOf("c")
        f.enemyHands(Hand("h1", perks = arrayListOf(a, b)), Hand("h2", perks = arrayListOf(c)))
        f.perk.execute(a, false)
        assertEquals("b", f.executedPerkI.value()!!.perk.name)
        f.perk.execute(b, false)
        assertEquals("c", f.executedPerkI.value()!!.perk.name)
        f.perk.execute(c, false)
        assertEquals(Perk.LAST, f.executedPerkI.value()!!.perk.name)
    }

    // RULE-099: без анимации следующий перк не ставится в очередь
    @Test
    fun noAnimate_noQueue() {
        val a = perkOf("a")
        f.enemyHands(Hand("h1", perks = arrayListOf(a, perkOf("b"))))
        f.perk.execute(a, false)
        assertNull(f.executedPerkI.value())
    }
}
