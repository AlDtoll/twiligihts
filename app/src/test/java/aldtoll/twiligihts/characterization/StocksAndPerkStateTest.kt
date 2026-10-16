package aldtoll.twiligihts.characterization

import aldtoll.twiligihts.characterization.Fixture.Companion.perkOf
import aldtoll.twiligihts.model.Condition
import aldtoll.twiligihts.model.Hand
import aldtoll.twiligihts.model.Perk
import aldtoll.twiligihts.model.Resource
import aldtoll.twiligihts.model.Stock
import aldtoll.twiligihts.model.MatchGroupInfo
import aldtoll.twiligihts.model.MatchOrientation
import aldtoll.twiligihts.model.MatchRule
import aldtoll.twiligihts.model.State
import aldtoll.twiligihts.model.Status
import aldtoll.twiligihts.model.StockPerk
import aldtoll.twiligihts.model.TimePerk
import aldtoll.twiligihts.storage.enemy.EnemyTimePerksInteractor
import aldtoll.twiligihts.storage.hero.HeroStatesInteractor
import aldtoll.twiligihts.storage.hero.HeroStatusesInteractor
import aldtoll.twiligihts.model.effects.Effect
import aldtoll.twiligihts.model.effects.Effect.EffectTarget
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Rule
import org.junit.Before
import org.junit.Test

/** RULE-100..RULE-109: EditStock (эффект и исполнитель), доступность/видимость перков, TimePerk. */
class StocksAndPerkStateTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var f: Fixture

    @Before
    fun createFixture() {
        f = Fixture()
    }

    private fun heroUses(vararg effects: Effect) = f.perk.execute(perkOf("p", *effects), true)

    // RULE-100: EDIT_STOCK CHANGE/SET — не ниже 0
    @Test
    fun editStock_changeAndSet_floorAtZero() {
        f.heroStocks(Stock(5, 1), Stock(5, 2))
        heroUses(Effect.EditStock(-10, arrayListOf(1)))
        assertEquals(0, f.heroStock(1))
        heroUses(Effect.EditStock(7, arrayListOf(1, 2)))
        assertEquals(7, f.heroStock(1)); assertEquals(12, f.heroStock(2))
        heroUses(Effect.EditStock(-3, arrayListOf(2), type = Effect.EditStock.Type.SET))
        assertEquals(0, f.heroStock(2))
    }

    // RULE-100: CHANGE не учитывает maxValue стока
    @Test
    fun editStock_change_ignoresMaxValue() {
        f.heroStocks(Stock(5, 1, maxValue = 10))
        heroUses(Effect.EditStock(20, arrayListOf(1)))
        assertEquals(25, f.heroStock(1))
    }

    // RULE-101: цели EDIT_STOCK; SUSPECT-17: ALL меняет только героя
    @Test
    fun editStock_targets() {
        f.heroStocks(Stock(5, 1)); f.enemyStocks(Stock(5, 1))
        heroUses(Effect.EditStock(1, arrayListOf(1), target = EffectTarget.FOE))
        assertEquals(6, f.enemyStock(1))
        heroUses(Effect.EditStock(1, arrayListOf(1), target = EffectTarget.ALL))
        assertEquals(6, f.heroStock(1)); assertEquals(6, f.enemyStock(1))
    }

    // RULE-102, решение 10 (SUSPECT-18): ADD добавляет шкалу только если её НЕТ; существующая не дублируется
    @Test
    fun editStock_add_addsOnlyMissing() {
        f.heroStocks(Stock(5, 1))
        heroUses(Effect.EditStock(7, arrayListOf(1, 2), type = Effect.EditStock.Type.ADD))
        val list = f.heroStockI.value()!!
        assertEquals(listOf(Stock(5, 1), Stock(7, 2)), list.toList())
    }

    // RULE-102: ADD при пустом списке создаёт шкалы
    @Test
    fun editStock_add_onEmptyList_creates() {
        heroUses(Effect.EditStock(7, arrayListOf(1, 2), type = Effect.EditStock.Type.ADD))
        assertEquals(listOf(Stock(7, 1), Stock(7, 2)), f.heroStockI.value()!!.toList())
    }

    // RULE-103: REMOVE удаляет шкалу (только у героя)
    @Test
    fun editStock_remove() {
        f.heroStocks(Stock(5, 1), Stock(5, 2))
        heroUses(Effect.EditStock(0, arrayListOf(1), type = Effect.EditStock.Type.REMOVE))
        assertEquals(listOf(Stock(5, 2)), f.heroStockI.value()!!.toList())
    }

    // RULE-104: доступность (enable) перка в руке
    @Test
    fun enableStatus() {
        f.heroStocks(Stock(5, 1))
        val cheap = perkOf("cheap", price = 5)
        val pricey = perkOf("pricey", price = 6)
        val noStock = perkOf("noStock", price = 6, gemType = 9)
        val reloading = Perk(name = "rel", effects = arrayListOf(), coolDown = 1)
        val needCond = Perk(name = "cond", effects = arrayListOf(), conditionsForEnable = arrayListOf(
            Condition(100, EffectTarget.HERO, Condition.Parameter.HP, symbol = Condition.Symbol.MORE)))
        val needRes = Perk(name = "res", effects = arrayListOf(), resources = arrayListOf(Resource(2, "Поиск")))
        f.heroRes(Resource(1, "Поиск"))
        f.heroHandsI.update(arrayListOf(Hand("h", perks = arrayListOf(cheap, pricey, noStock, reloading, needCond, needRes)).apply { init(true) }))
        reloading.reload = 0
        f.updatePerks.updateEnableStatus()
        assertTrue(cheap.enable)
        assertFalse(pricey.enable)
        // SUSPECT-14: если у героя нет шкалы нужного цвета — перк считается доступным
        assertTrue(noStock.enable)
        assertFalse(reloading.enable)
        assertFalse(needCond.enable)
        assertFalse(needRes.enable)
    }

    // RULE-104: перк без цен (не прошедший init) всегда enable, даже на перезарядке
    @Test
    fun enableStatus_noPrices_alwaysEnabled() {
        val p = Perk(name = "raw", effects = arrayListOf(), coolDown = 2).apply { reload = 0 }
        f.heroHandsI.update(arrayListOf(Hand("h", perks = arrayListOf(p))))
        f.updatePerks.updateEnableStatus()
        assertTrue(p.enable)
    }

    // RULE-105: видимость рук и перков
    @Test
    fun showStatus() {
        val never = Condition(100, EffectTarget.HERO, Condition.Parameter.HP, symbol = Condition.Symbol.MORE)
        val hiddenHandPerk = perkOf("a")
        val hiddenHand = Hand("hid", perks = arrayListOf(hiddenHandPerk), conditionsForDisplay = arrayListOf(never))
        val noCharges = Perk(name = "nc", effects = arrayListOf(), charges = 0)
        val condPerk = Perk(name = "cp", effects = arrayListOf(), conditionsForDisplay = arrayListOf(never))
        val shown = Hand("sh", perks = arrayListOf(noCharges, condPerk))
        listOf(hiddenHand, shown).forEach { it.init(true) }
        f.heroHandsI.update(arrayListOf(hiddenHand, shown))
        f.updatePerks.updateShowStatus()
        assertFalse(hiddenHand.show)
        assertTrue(hiddenHandPerk.show) // перки скрытой руки не пересчитываются
        assertTrue(shown.show)
        assertFalse(noCharges.show)
        assertFalse(condPerk.show)
    }

    // RULE-106: TimePerk — на совпадающей секунде, с проверкой conditionsForDisplay и доступности
    @Test
    fun timePerks() {
        f.heroTimePerksI.update(arrayListOf(
            TimePerk(1, 5, perkOf("t5", Effect.Attack(2))),
            TimePerk(2, 6, perkOf("t6", Effect.Attack(3))),
        ))
        f.timePerk.checkAndApplyTimePerks(5)
        assertEquals(28, f.enemy.hp)
        f.timePerk.checkAndApplyTimePerks(7)
        assertEquals(28, f.enemy.hp)
    }

    // RULE-013, решение 19 (SUSPECT-2): заряды эффектов TimePerk работают после init интерактора
    @Test
    fun timePerk_effectCharges_workAfterInteractorInit() {
        val perk = perkOf("t5", Effect.Attack(2, charges = 1))
        f.heroTimePerksI.startData = arrayListOf(TimePerk(1, 5, perk))
        f.heroTimePerksI.init()
        f.timePerk.checkAndApplyTimePerks(5)
        assertEquals(28, f.enemy.hp)
        f.timePerk.checkAndApplyTimePerks(5)
        assertEquals(28, f.enemy.hp) // заряд эффекта кончился
    }

    // RULE-013, решение 19: то же для TimePerk врага
    @Test
    fun enemyTimePerk_effectCharges_workAfterInteractorInit() {
        val interactor = EnemyTimePerksInteractor()
        interactor.startData = arrayListOf(TimePerk(1, 5, perkOf("e5", Effect.Attack(2, charges = 1))))
        interactor.init()
        assertEquals(1, interactor.value()!![0].perk.effects[0].currentCharges)
    }

    // RULE-013, решение 19: заряды эффектов StockPerk
    @Test
    fun stockPerk_effectCharges_workAfterInteractorInit() {
        f.heroStocks(Stock(0, 1))
        f.heroStockPerksI.startData = arrayListOf(StockPerk(1, 1, 5, perkOf("Ярость", Effect.Attack(4, charges = 1))))
        f.heroStockPerksI.init()
        f.stockPerk.rememberInitialStocks(f.heroStockI.value()!!, isHero = true)
        f.heroStocks(Stock(6, 1))
        f.stockPerk.onStocksChanged(isHero = true) // 0 -> 6 пересекает порог 5
        assertEquals(26, f.enemy.hp)
        f.heroStocks(Stock(0, 1))
        f.stockPerk.onStocksChanged(isHero = true)
        f.heroStocks(Stock(6, 1))
        f.stockPerk.onStocksChanged(isHero = true) // порог пересечён снова, но заряд эффекта кончился
        assertEquals(26, f.enemy.hp)
    }

    // RULE-013, решение 19: заряды эффектов правил (HeroRules)
    @Test
    fun heroRules_effectCharges_workAfterInteractorInit() {
        f.heroRulesI.startData = arrayListOf(MatchRule("r", perk = perkOf("rp", Effect.Attack(1, charges = 1))))
        f.heroRulesI.init()
        val groups = listOf(MatchGroupInfo(1, MatchOrientation.HORIZONTAL, 3))
        f.matchPerk.execute(groups, true)
        assertEquals(29, f.enemy.hp)
        f.matchPerk.execute(groups, true)
        assertEquals(29, f.enemy.hp)
    }

    // RULE-013, решение 19: заряды эффектов реакций (статус REACTION и состояние со статусом REACTION)
    @Test
    fun reactionPerk_effectCharges_initOnStatusesAndStates() {
        val reaction = Status(
            name = "Реакция", value = 1, type = Status.StatusType.REACTION,
            reactionPerk = Perk(name = "Ответ", effects = arrayListOf(Effect.Attack(2, charges = 2)))
        )
        val statuses = HeroStatusesInteractor()
        statuses.startData = arrayListOf(reaction)
        statuses.init()
        assertEquals(2, statuses.value()!![0].reactionPerk!!.effects[0].currentCharges)

        val stateReaction = reaction.copy(reactionPerk = Perk(name = "О", effects = arrayListOf(Effect.Attack(2, charges = 3))))
        val states = HeroStatesInteractor()
        states.startData = arrayListOf(State(Condition(), arrayListOf(), stateReaction, "s"))
        states.init()
        assertEquals(3, states.value()!![0].status.reactionPerk!!.effects[0].currentCharges)
    }

    // RULE-013, решение 19: статус с реакцией, выданный эффектом EDIT_STATUS, получает свежие заряды
    @Test
    fun reactionPerk_effectCharges_initOnEditStatus() {
        val reaction = Status(
            name = "Реакция", value = 1, type = Status.StatusType.REACTION,
            reactionPerk = Perk(name = "Ответ", effects = arrayListOf(Effect.Attack(2, charges = 2)))
        )
        heroUses(Effect.EditStatus(reaction, value = 1, target = EffectTarget.HERO))
        val given = f.hero.statuses.first { it.name == "Реакция" }
        assertEquals(2, given.reactionPerk!!.effects[0].currentCharges)
    }

    // RULE-107: payPriceForPerk не ограничивает снизу; updatePersonStocksAfterDamage для врага
    @Test
    fun payPrice_andDamageKeep() {
        f.heroStocks(Stock(3, 1))
        f.editStock.payPriceForPerk(perkOf("x", price = 5), true)
        assertEquals(-2, f.heroStock(1))
        f.enemyStocks(Stock(9, 1))
        f.settingsI.update(aldtoll.twiligihts.model.BattleSettings(gemSettings = arrayListOf(
            aldtoll.twiligihts.model.BattleSettings.GemSettings(type = "1", damageKeepStrategy = 50))))
        f.editStock.updatePersonStocksAfterDamage(false)
        assertEquals(4, f.enemyStock(1))
    }
}
