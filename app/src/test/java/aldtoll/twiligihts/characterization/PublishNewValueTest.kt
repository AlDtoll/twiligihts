package aldtoll.twiligihts.characterization

import aldtoll.twiligihts.characterization.Fixture.Companion.perkOf
import aldtoll.twiligihts.characterization.Fixture.Companion.status
import aldtoll.twiligihts.model.Hand
import aldtoll.twiligihts.model.Perk
import aldtoll.twiligihts.model.Resource
import aldtoll.twiligihts.model.Sector
import aldtoll.twiligihts.model.Status.StatusType
import aldtoll.twiligihts.model.Stock
import aldtoll.twiligihts.model.StockPerk
import aldtoll.twiligihts.model.TimePerk
import aldtoll.twiligihts.model.effects.Effect
import aldtoll.twiligihts.storage.PlaceHandsListInteractor
import aldtoll.twiligihts.storage.enemy.EnemySectorsInteractor
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Шаг 2 миграции на Compose: каждый update() интерактора публикует НОВЫЙ независимый снимок,
 * поэтому observeAsState (структурное равенство) видит реальные изменения.
 * Интерактор хранит рабочий объект (его меняет логика, value()) и публикует его глубокую копию.
 * HeroStock/EnemyStock тоже хранят рабочий список и публикуют снимок, но доставка остаётся через postValue
 * (решение 17); отложенная доставка проверяется в StockPublishTest.
 */
class PublishNewValueTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var f: Fixture

    @Before
    fun createFixture() {
        f = Fixture()
    }

    private fun heroUses(vararg effects: Effect) = f.perk.execute(perkOf("p", *effects), true)

    // value() возвращает тот же рабочий объект, что передали в update()
    @Test
    fun value_returnsWorkingObject_publishedIsAnotherInstance() {
        val hands = arrayListOf(Hand("h", perks = arrayListOf(perkOf("a"))))
        f.heroHandsI.update(hands)
        assertSame(hands, f.heroHandsI.value())
        assertNotSame(hands, f.heroHandsI.get().value)
        assertNotSame(hands[0], f.heroHandsI.get().value!![0])
        assertNotSame(hands[0].perks[0], f.heroHandsI.get().value!![0].perks[0])
        assertEquals(hands, f.heroHandsI.get().value)

        val hero = f.hero
        f.heroI.update(hero)
        assertSame(hero, f.heroI.value())
        assertNotSame(hero, f.heroI.get().value)
    }

    // повторный update без изменений: новый экземпляр, но равный по содержимому
    @Test
    fun update_withoutChanges_newInstanceEqualContent() {
        val hands = arrayListOf(Hand("h", perks = arrayListOf(perkOf("a"))))
        f.heroHandsI.update(hands)
        val p1 = f.heroHandsI.get().value
        f.heroHandsI.update(hands)
        assertNotSame(p1, f.heroHandsI.get().value)
        assertEquals(p1, f.heroHandsI.get().value)
    }

    // Hand/Perk: опубликованный снимок заморожен, изменение после update видно только после нового update
    @Test
    fun hands_mutationAfterUpdate_previousSnapshotFrozen() {
        val perk = perkOf("a").apply { charges = 3; currentCharges = 3 }
        val hand = Hand("h", perks = arrayListOf(perk))
        f.heroHandsI.update(arrayListOf(hand))
        val p1 = f.heroHandsI.get().value!!

        perk.enable = true
        perk.reload = 2
        perk.decreaseCharges()
        hand.show = false
        // без update снимок не меняется
        assertFalse(p1[0].perks[0].enable)
        assertEquals(3, p1[0].perks[0].currentCharges)
        assertTrue(p1[0].show)

        f.heroHandsI.update(f.heroHandsI.value()!!)
        val p2 = f.heroHandsI.get().value!!
        assertNotEquals(p1, p2)
        assertTrue(p2[0].perks[0].enable)
        assertEquals(2, p2[0].perks[0].reload)
        assertEquals(2, p2[0].perks[0].currentCharges)
        assertFalse(p2[0].show)
        // прежний снимок по-прежнему со старыми значениями
        assertFalse(p1[0].perks[0].enable)
        assertEquals(3, p1[0].perks[0].currentCharges)
    }

    @Test
    fun enemyHands_mutationAfterUpdate_previousSnapshotFrozen() {
        val perk = perkOf("a")
        val hand = Hand("h", perks = arrayListOf(perk))
        f.enemyHands(hand)
        val p1 = f.enemyHandsI.get().value!!
        perk.enable = !perk.enable
        f.enemyHandsI.update(f.enemyHandsI.value()!!)
        assertNotEquals(p1, f.enemyHandsI.get().value)
        assertEquals(!perk.enable, p1[0].perks[0].enable)
    }

    // updateEnableStatus: ставит enable и публикует новое значение, отличное от предыдущего
    @Test
    fun updateEnableStatus_publishesChangedEnable() {
        f.heroStocks(Stock(0, 1))
        f.heroHand(perkOf("p", price = 5))
        f.updatePerks.updateEnableStatus()
        val before = f.heroHandsI.get().value!!
        assertFalse(before[0].perks[0].enable)

        f.heroStockI.value()!![0].value = 10
        f.updatePerks.updateEnableStatus()
        val after = f.heroHandsI.get().value!!
        assertTrue(after[0].perks[0].enable)
        assertFalse(before[0].perks[0].enable)
        assertNotEquals(before, after)
    }

    // updateShowStatus меняет show на месте -> теперь публикует (раньше изменение не доходило до UI)
    @Test
    fun updateShowStatus_publishesShowChange() {
        val perk = Perk(name = "p", effects = arrayListOf(), charges = 1)
        f.heroHand(perk)
        f.updatePerks.updateEnableStatus()
        f.updatePerks.updateShowStatus()
        val before = f.heroHandsI.get().value!!
        assertTrue(before[0].perks[0].show)

        perk.decreaseCharges()
        f.updatePerks.updateShowStatus()
        val after = f.heroHandsI.get().value!!
        assertFalse(after[0].perks[0].show)
        assertTrue(before[0].perks[0].show)
        assertEquals(f.heroHandsI.value(), after)
    }

    @Test
    fun enemyUpdateShowStatus_publishesShowChange() {
        val perk = Perk(name = "p", effects = arrayListOf(), charges = 1)
        f.enemyHands(Hand("e", perks = arrayListOf(perk)))
        f.updatePerks.updateShowStatus()
        val before = f.enemyHandsI.get().value!!
        perk.decreaseCharges()
        f.updatePerks.updateShowStatus()
        assertFalse(f.enemyHandsI.get().value!![0].perks[0].show)
        assertTrue(before[0].perks[0].show)
    }

    // после execute() опубликованные руки и персонажи равны рабочим (нет «мутаций без публикации»)
    @Test
    fun execute_afterwards_publishedEqualsWorking() {
        f.heroStocks(Stock(10, 1))
        f.heroHand(perkOf("a", Effect.Attack(3), price = 2).copy(charges = 2, currentCharges = 2, coolDown = 1))
        f.enemy.statuses.add(status("Вампиризм", StatusType.VAMP, 50))
        val used = f.heroHandsI.value()!![0].perks[0].copy()
        f.perk.execute(used, true)
        assertEquals(f.heroHandsI.value(), f.heroHandsI.get().value)
        assertEquals(f.enemyHandsI.value(), f.enemyHandsI.get().value)
        assertEquals(f.heroI.value(), f.heroI.get().value)
        assertEquals(f.enemyI.value(), f.enemyI.get().value)
        assertEquals(1, f.heroHandsI.get().value!![0].perks[0].currentCharges)
        assertEquals(27, f.enemyI.get().value!!.hp)
    }

    // после полного цикла хода опубликованное равно рабочему
    @Test
    fun endTurn_afterwards_publishedEqualsWorking() {
        f.heroStocks(Stock(10, 1)); f.enemyStocks(Stock(10, 1))
        f.hero.statuses.add(status("Щит", StatusType.DEFEND, 2, duration = 2))
        f.enemy.statuses.add(status("Яд", StatusType.DAMAGE, 3, duration = 2))
        f.heroHand(perkOf("a").copy(coolDown = 2))
        f.endTurn.execute()
        assertEquals(f.heroHandsI.value(), f.heroHandsI.get().value)
        assertEquals(f.enemyHandsI.value(), f.enemyHandsI.get().value)
        assertEquals(f.heroI.value(), f.heroI.get().value)
        assertEquals(f.enemyI.value(), f.enemyI.get().value)
        assertEquals(27, f.enemyI.get().value!!.hp)
    }

    // Hero/Enemy: update(тот же объект) после изменения -> другой снимок, статусы не общие
    @Test
    fun persons_update_publishesIndependentSnapshot() {
        f.hero.statuses.add(status("С", StatusType.DEFEND, 1))
        f.heroI.update(f.hero)
        val p1 = f.heroI.get().value!!

        f.hero.shield += 3
        f.hero.statuses[0].value = 7
        f.heroI.update(f.hero)
        val p2 = f.heroI.get().value!!
        assertNotSame(p1, p2)
        assertNotEquals(p1, p2)
        assertEquals(0, p1.shield)
        assertEquals(1, p1.statuses[0].value)
        assertEquals(3, p2.shield)
        assertNotSame(f.hero.statuses[0], p2.statuses[0])

        f.enemy.shield += 2
        f.enemyI.update(f.enemy)
        assertEquals(2, f.enemyI.get().value!!.shield)
        assertNotSame(f.enemy, f.enemyI.get().value)
    }

    // Defend-эффект (мутация + update того же объекта) доходит до опубликованного значения
    @Test
    fun defendEffect_publishesNewHero() {
        val before = f.heroI.get().value!!
        heroUses(Effect.Defend(4))
        val after = f.heroI.get().value!!
        assertNotSame(before, after)
        assertEquals(0, before.shield)
        assertEquals(4, after.shield)
    }

    // вампиризм меняет здоровье и статус АТАКУЮЩЕГО без update -> теперь публикуется
    @Test
    fun vamp_publishesAttacker() {
        f.hero = f.hero.copy(hp = 10)
        f.hero.statuses.add(status("Вампиризм", StatusType.VAMP, 50))
        val before = f.heroI.get().value!!
        f.applyAttack.execute(f.enemy, Effect.Attack(6))
        val after = f.heroI.get().value!!
        assertEquals(10, before.hp)
        assertEquals(13, after.hp)
        assertEquals(f.heroI.value(), after)
    }

    // Resources: refresh() после изменения amount -> новый снимок, прежний не изменился
    @Test
    fun resources_refresh_publishesNewSnapshot() {
        f.heroRes(Resource(5, "gold"))
        val p1 = f.heroResI.get().value!!
        f.heroResI.value()!![0].amount = 8
        f.heroResI.refresh()
        val p2 = f.heroResI.get().value!!
        assertNotSame(p1, p2)
        assertNotEquals(p1, p2)
        assertEquals(5, p1[0].amount)
        assertEquals(8, p2[0].amount)
        assertNotSame(f.heroResI.value()!![0], p2[0])
    }

    @Test
    fun editResourcesEffect_publishesNewSnapshot() {
        f.heroRes(Resource(5, "gold"))
        val p1 = f.heroResI.get().value!!
        heroUses(Effect.EditResources(3, "gold", target = Effect.EffectTarget.HERO))
        val p2 = f.heroResI.get().value!!
        assertEquals(5, p1[0].amount)
        assertEquals(8, p2[0].amount)
    }

    @Test
    fun enemyResources_snapshotIndependent() {
        f.enemyResI.update(arrayListOf(Resource(1, "r")))
        val p1 = f.enemyResI.get().value!!
        f.enemyResI.value()!![0].amount = 4
        f.enemyResI.refresh()
        assertEquals(1, p1[0].amount)
        assertEquals(4, f.enemyResI.get().value!![0].amount)
    }

    // TimePerk: списание заряда -> переэмит списка с новым значением, прежний снимок не тронут
    @Test
    fun timePerk_chargeSpent_publishesNewSnapshot() {
        val perk = Perk(name = "t", effects = arrayListOf(Effect.Attack(1)), charges = 1)
        f.heroTimePerksI.update(arrayListOf(TimePerk(1, 5, perk)))
        val p1 = f.heroTimePerksI.get().value!!
        f.timePerk.checkAndApplyTimePerks(5)
        val p2 = f.heroTimePerksI.get().value!!
        assertEquals(1, p1[0].perk.currentCharges)
        assertEquals(0, p2[0].perk.currentCharges)
        assertNotEquals(p1, p2)
        assertNotSame(f.heroTimePerksI.value()!![0].perk, p2[0].perk)
    }

    // StockPerk / EnemyStockPerks / EnemyTimePerks / Sectors / PlaceHands: независимые снимки
    @Test
    fun otherListInteractors_publishIndependentSnapshots() {
        val sp = StockPerk(1, 1, 5, perkOf("s"))
        f.heroStockPerksI.update(arrayListOf(sp))
        sp.perk.enable = true
        assertFalse(f.heroStockPerksI.get().value!![0].perk.enable)
        f.heroStockPerksI.update(f.heroStockPerksI.value()!!)
        assertTrue(f.heroStockPerksI.get().value!![0].perk.enable)

        val ep = StockPerk(2, 1, 5, perkOf("s"))
        f.enemyStockPerksI.update(arrayListOf(ep))
        assertNotSame(ep.perk, f.enemyStockPerksI.get().value!![0].perk)

        val tp = TimePerk(3, 1, perkOf("t"))
        f.enemyTimePerksI.update(arrayListOf(tp))
        assertNotSame(tp.perk, f.enemyTimePerksI.get().value!![0].perk)

        val sectorsI = EnemySectorsInteractor()
        val sector = Sector(id = 1, iconRes = 0, perk = perkOf("sec"))
        sectorsI.update(arrayListOf(sector))
        assertSame(sector, sectorsI.value()!![0])
        assertNotSame(sector, sectorsI.get().value!![0])
        assertNotSame(sector.perk, sectorsI.get().value!![0].perk)
        assertEquals(sector, sectorsI.get().value!![0])

        val placeI = PlaceHandsListInteractor()
        val hand = Hand("pl", perks = arrayListOf(perkOf("x")))
        placeI.update(arrayListOf(hand))
        hand.perks[0].enable = true
        assertFalse(placeI.get().value!![0].perks[0].enable)
        assertSame(hand, placeI.value()!![0])
    }

    // init() публикует снимок после Hand.init (цены, перезарядка, описание)
    @Test
    fun handsInit_publishesInitialisedSnapshot() {
        f.heroHandsI.startData = arrayListOf(Hand("h", gemType = 2, perks = arrayListOf(
            Perk(name = "a", effects = arrayListOf(Effect.Attack(1)), coolDown = 3)
        )))
        f.heroHandsI.init()
        val published = f.heroHandsI.get().value!![0].perks[0]
        assertEquals(3, published.reload)
        assertEquals(listOf(Perk.Price(0, 2)), published.prices)
        assertEquals(f.heroHandsI.value(), f.heroHandsI.get().value)
    }

    // стоки HeroStock/EnemyStock: value() — рабочий список, публикуется независимый снимок (доставка postValue)
    @Test
    fun stockInteractors_valueIsWorkingList_publishedIsSnapshot() {
        val list = arrayListOf(Stock(1, 1))
        f.heroStockI.update(list)
        assertSame(list, f.heroStockI.value())
        assertNotSame(list, f.heroStockI.get().value)
        assertNotSame(list[0], f.heroStockI.get().value!![0])
        assertEquals(list, f.heroStockI.get().value)
        f.enemyStockI.update(list)
        assertSame(list, f.enemyStockI.value())
        assertNotSame(list, f.enemyStockI.get().value)
        assertEquals(list, f.enemyStockI.get().value)
    }
}
