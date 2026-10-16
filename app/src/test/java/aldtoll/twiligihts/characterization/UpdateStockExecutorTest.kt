package aldtoll.twiligihts.characterization

import aldtoll.twiligihts.characterization.Fixture.Companion.perkOf
import aldtoll.twiligihts.characterization.Fixture.Companion.status
import aldtoll.twiligihts.model.BattleSettings
import aldtoll.twiligihts.model.CellState
import aldtoll.twiligihts.model.CellType
import aldtoll.twiligihts.model.CrushedCell
import aldtoll.twiligihts.model.Gem
import aldtoll.twiligihts.model.MatchGroupInfo
import aldtoll.twiligihts.model.MatchOrientation
import aldtoll.twiligihts.model.MatchRule
import aldtoll.twiligihts.model.Status.StatusType
import aldtoll.twiligihts.model.Stock
import aldtoll.twiligihts.model.StockPerk
import aldtoll.twiligihts.model.effects.Effect
import org.junit.Assert.assertEquals
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Rule
import org.junit.Before
import org.junit.Test

/** RULE-080..RULE-089: очки с доски (UpdateStockExecutor), HeroRules (MatchPerkExecutor), StockPerk. */
class UpdateStockExecutorTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var f: Fixture

    @Before
    fun createFixture() {
        f = Fixture()
    }

    private fun cell(gem: Gem, type: CellType = CellType.NONE, mod: Float = 0f, row: Int = 0, col: Int = 0,
                     trigger: aldtoll.twiligihts.model.Perk? = null) =
        CrushedCell(gem, row, col, CellState(row, col, type, mod, trigger))

    private fun crush(vararg cells: CrushedCell, heroTurn: Boolean = true, groups: List<MatchGroupInfo> = emptyList()) =
        f.updateStock.addValueFromCrushedGems(cells.toList(), groups, heroTurn)

    // RULE-080: обычный гем = GEM_FULL_VALUE (10)
    @Test
    fun plainGems() {
        f.heroStocks(Stock(0, 1))
        crush(cell(Gem(1)), cell(Gem(1)), cell(Gem(1)))
        assertEquals(30, f.heroStock(1))
    }

    // RULE-081: "половинчатый" гем (extraType = EMPTY_COLOR) даёт 50%; extra-цвет делит очки 50/50
    @Test
    fun halfAndExtraGems() {
        f.heroStocks(Stock(0, 1), Stock(0, 2))
        crush(cell(Gem(1, extraType = Gem.EMPTY_COLOR)))
        assertEquals(5, f.heroStock(1))
        crush(cell(Gem(1, extraType = 2)))
        assertEquals(10, f.heroStock(1))
        assertEquals(5, f.heroStock(2))
    }

    // RULE-082: бонус даёт GEM_BONUS_VALUE (2) бонусному цвету
    @Test
    fun bonus() {
        f.heroStocks(Stock(0, 1), Stock(0, 3))
        crush(cell(Gem(1, bonusType = 3)))
        assertEquals(10, f.heroStock(1))
        assertEquals(2, f.heroStock(3))
    }

    // RULE-083: модификаторы клеток; результат клетки >= 0; сумма по типу усекается toInt()
    @Test
    fun cellModifiers() {
        f.heroStocks(Stock(0, 1))
        crush(cell(Gem(1), CellType.MULTIPLIER, 1.5f))
        assertEquals(15, f.heroStock(1))
        crush(cell(Gem(1), CellType.ADDITIVE, -12f))
        assertEquals(15, f.heroStock(1))
        crush(cell(Gem(1), CellType.TRIGGER, 2f))
        assertEquals(27, f.heroStock(1))
        crush(cell(Gem(1, extraType = Gem.EMPTY_COLOR), CellType.MULTIPLIER, 1.5f)) // 7.5 -> 7
        assertEquals(34, f.heroStock(1))
    }

    // RULE-084: GemSettings.fullValue / bonusValue и статус CHANGE_STOCK
    @Test
    fun gemSettingsAndChangeStockStatus() {
        Gem.GEM_MAP["1"] = BattleSettings.GemSettings(type = "1", fullValue = 20, bonusValue = 5)
        f.heroStocks(Stock(0, 1))
        f.hero.statuses.add(status("Жадность", StatusType.CHANGE_STOCK, 2, gemTypes = arrayListOf(1)))
        crush(cell(Gem(1)), cell(Gem(2, bonusType = 1)))
        // 20+2 за основной, бонус 5 (бонусу статус не добавляет)
        assertEquals(27, f.heroStock(1))
    }

    // RULE-085: maxValue стока
    @Test
    fun maxValueCap() {
        f.heroStocks(Stock(25, 1, maxValue = 30))
        crush(cell(Gem(1)))
        assertEquals(30, f.heroStock(1))
    }

    // RULE-086: в ход врага очки идут врагу, HeroRules не исполняются
    @Test
    fun enemyTurn_enemyStocks_noHeroRules() {
        f.heroStocks(Stock(0, 1)); f.enemyStocks(Stock(0, 1))
        f.heroRulesI.update(arrayListOf(MatchRule("r", perk = perkOf("rp", Effect.Attack(1)))))
        val g = listOf(MatchGroupInfo(1, MatchOrientation.HORIZONTAL, 3))
        crush(cell(Gem(1)), heroTurn = false, groups = g)
        assertEquals(0, f.heroStock(1))
        assertEquals(10, f.enemyStock(1))
        assertEquals(30, f.enemy.hp)
        assertEquals(20, f.hero.hp)
    }

    // RULE-087: HeroRules: фильтр orientation/gemType/minSize; каждое подходящее правило на каждую группу
    @Test
    fun heroRules_matching() {
        f.heroStocks(Stock(0, 1), Stock(0, 2))
        f.heroRulesI.update(arrayListOf(
            MatchRule("любая", perk = perkOf("a", Effect.Attack(1))),
            MatchRule("синяя", gemType = 2, perk = perkOf("b", Effect.Attack(1))),
            MatchRule("гор4", orientation = MatchOrientation.HORIZONTAL, minSize = 4, perk = perkOf("c", Effect.Attack(1))),
        ))
        val groups = listOf(
            MatchGroupInfo(1, MatchOrientation.HORIZONTAL, 3),
            MatchGroupInfo(2, MatchOrientation.VERTICAL, 4),
        )
        crush(cell(Gem(1)), groups = groups)
        assertEquals(27, f.enemy.hp)
    }

    // RULE-087, SUSPECT-15: перки HeroRules исполняются ДО начисления очков (комментарий говорит "после")
    @Test
    fun heroRules_runBeforeStocksAreAdded() {
        f.heroStocks(Stock(0, 1))
        val cond = aldtoll.twiligihts.model.Condition(5, Effect.EffectTarget.HERO,
            aldtoll.twiligihts.model.Condition.Parameter.STOCK, symbol = aldtoll.twiligihts.model.Condition.Symbol.MORE, gemType = 1)
        f.heroRulesI.update(arrayListOf(MatchRule("r", perk = perkOf("rp", Effect.Attack(1, conditions = arrayListOf(cond))))))
        crush(cell(Gem(1)), groups = listOf(MatchGroupInfo(1, MatchOrientation.HORIZONTAL, 3)))
        assertEquals(10, f.heroStock(1))
        assertEquals(30, f.enemy.hp) // условие "сток > 5" проверялось при стоке 0
    }

    // RULE-088: TRIGGER-клетка запускает triggerPerk один раз на клетку
    @Test
    fun triggerCell() {
        f.heroStocks(Stock(0, 1))
        val trap = perkOf("Ловушка", Effect.Attack(2))
        val c = cell(Gem(1), CellType.TRIGGER, 0f, 2, 3, trap)
        crush(c, c)
        assertEquals(28, f.enemy.hp)
    }

    // RULE-089: StockPerk — при пересечении порога снизу вверх
    @Test
    fun stockPerk_thresholdCrossing() {
        f.heroStocks(Stock(15, 1))
        f.heroStockPerksI.update(arrayListOf(StockPerk(1, 1, 20, perkOf("Ярость", Effect.Attack(4)))))
        crush(cell(Gem(1)))
        assertEquals(26, f.enemy.hp)
        crush(cell(Gem(1)))
        assertEquals(26, f.enemy.hp)
    }

    // RULE-089, решение 14 (SUSPECT-16): в начале боя запоминаются исходные значения шкал —
    // сток, который уже выше порога, не вызывает StockPerk при изменении другого цвета
    @Test
    fun stockPerk_initialStocksRemembered_noFalseTriggerOnFirstChange() {
        f.heroStocks(Stock(50, 1))
        f.heroStockPerksI.update(arrayListOf(StockPerk(1, 1, 20, perkOf("Ярость", Effect.Attack(4)))))
        f.stockPerk.rememberInitialStocks(f.heroStockI.value()!!, isHero = true)
        crush(cell(Gem(2)))
        assertEquals(30, f.enemy.hp)
    }

    // RULE-089, решение 14: память сбрасывается на исходные значения нового боя (Singleton живёт между боями)
    @Test
    fun stockPerk_newBattleOverwritesMemoryOfPreviousBattle() {
        f.heroStocks(Stock(50, 1))
        f.heroStockPerksI.update(arrayListOf(StockPerk(1, 1, 20, perkOf("Ярость", Effect.Attack(4)))))
        f.stockPerk.rememberInitialStocks(arrayListOf(Stock(50, 1)), isHero = true) // бой 1: память = 50
        f.heroStocks(Stock(0, 1))
        f.stockPerk.rememberInitialStocks(arrayListOf(Stock(0, 1)), isHero = true) // бой 2: память = 0
        f.heroStocks(Stock(25, 1))
        f.stockPerk.onStocksChanged(isHero = true)       // 0 -> 25 пересекает порог 20
        assertEquals(26, f.enemy.hp)
    }

    // RULE-089, решение 14: то же для шкал врага
    @Test
    fun stockPerk_initialStocksRemembered_enemySide() {
        f.enemyStocks(Stock(50, 1))
        f.enemyStockPerksI.update(arrayListOf(StockPerk(1, 1, 20, perkOf("Ярость", Effect.Attack(4)))))
        f.stockPerk.rememberInitialStocks(f.enemyStockI.value()!!, isHero = false)
        f.stockPerk.onStocksChanged(isHero = false)
        assertEquals(20, f.hero.hp)
    }

    // RULE-089, решение 14: память берётся из переданного списка, а не из value() интерактора, которое после
    // init() (postValue) на устройстве ещё старое: здесь value() = 0 (старое), в списке из init() = 50
    @Test
    fun stockPerk_rememberUsesPassedList_notStaleInteractorValue() {
        f.heroStocks(Stock(0, 1)) // «старое» value() интерактора
        f.heroStockPerksI.update(arrayListOf(StockPerk(1, 1, 20, perkOf("Ярость", Effect.Attack(4)))))
        f.stockPerk.rememberInitialStocks(arrayListOf(Stock(50, 1)), isHero = true)
        f.heroStocks(Stock(50, 1))
        f.stockPerk.onStocksChanged(isHero = true)
        assertEquals(30, f.enemy.hp) // при чтении старого value() память = 0 и перк сработал бы
    }

    // RULE-089, решение 14: init() интерактора шкал возвращает собранный список (с копиями и GOD_MODE)
    @Test
    fun stockListInteractor_init_returnsBuiltList() {
        f.heroStockI.startedValue = arrayListOf(Stock(7, 1))
        f.enemyStockI.startedValue = arrayListOf(Stock(8, 2))
        assertEquals(listOf(Stock(7, 1)), f.heroStockI.init().toList())
        assertEquals(listOf(Stock(8, 2)), f.enemyStockI.init().toList())
    }
}
