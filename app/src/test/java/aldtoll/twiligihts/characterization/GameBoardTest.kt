package aldtoll.twiligihts.characterization

import aldtoll.twiligihts.model.BattleSettings
import aldtoll.twiligihts.model.CellType
import aldtoll.twiligihts.model.GameBoard
import aldtoll.twiligihts.model.Gem
import aldtoll.twiligihts.model.Move
import aldtoll.twiligihts.storage.BattleSettingsInteractor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Rule
import org.junit.Test

/** RULE-020..RULE-027: GameBoard. */
class GameBoardTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var settings: BattleSettingsInteractor

    @Before
    fun setUp() {
        Fixture.resetGlobals()
        settings = BattleSettingsInteractor()
    }

    /** Заполнить доску по шаблону строк (цифра = type). */
    private fun board(vararg rows: String): GameBoard {
        val b = GameBoard(rows.size, rows[0].length, settings)
        rows.forEachIndexed { r, line -> line.forEachIndexed { c, ch -> b[r, c] = Gem(ch.digitToInt()) } }
        return b
    }

    // RULE-020
    @Test
    fun initialBoard_hasNoMatches_andTypesInRange() {
        repeat(20) {
            val b = GameBoard(8, 8, settings)
            assertFalse(b.hasMatches())
            for (r in 0 until 8) for (c in 0 until 8) assertTrue(b[r, c].type in 1..4)
        }
    }

    // RULE-021
    @Test
    fun matches_usesMainOrExtraType() {
        assertTrue(GameBoard.matches(Gem(1), Gem(1)))
        assertTrue(GameBoard.matches(Gem(1, extraType = 2), Gem(2)))
        assertTrue(GameBoard.matches(Gem(2), Gem(1, extraType = 2)))
        assertFalse(GameBoard.matches(Gem(1, extraType = 3), Gem(2, extraType = 4)))
        // extra к extra не считается
        assertFalse(GameBoard.matches(Gem(1, extraType = 3), Gem(2, extraType = 3)))
        // бонус не участвует в совпадении
        assertFalse(GameBoard.matches(Gem(1, bonusType = 2), Gem(2)))
    }

    // RULE-022
    @Test
    fun hasMatches_threeInRowOrColumn() {
        assertTrue(board("111", "234", "342").hasMatches())
        assertTrue(board("123", "143", "142").hasMatches())
        assertFalse(board("112", "234", "342").hasMatches())
    }

    // RULE-022: пустая клетка (type 0) не начинает совпадение
    @Test
    fun hasMatches_emptyGemsDoNotMatch() {
        assertFalse(board("000", "123", "231").hasMatches())
        assertTrue(board("111", "000", "232").hasEmptyGems())
    }

    // RULE-022, решение 21 (SUSPECT-4): тройка — линия, только если у всех трёх есть общий цвет;
    // первый гем с extra больше не "склеивает" два разных цвета
    @Test
    fun hasMatches_requiresCommonColor_firstGemExtraDoesNotBridgeTwoColors() {
        val b = board("312", "234", "341")
        b[0, 0] = Gem(1, extraType = 2) // строка 0: [1+2, 1, 2] — общего цвета нет
        assertFalse(b.hasMatches())
    }

    // RULE-022, решение 21: общий цвет может быть extra-цветом
    @Test
    fun hasMatches_commonColorViaExtra() {
        val b = board("312", "234", "341")
        b[0, 0] = Gem(1, extraType = 2) // [1+2, 1, 3+1] — общий цвет 1
        b[0, 2] = Gem(3, extraType = 1)
        assertTrue(b.hasMatches())
    }

    // RULE-022, решение 21: общий extra-цвет у трёх гемов тоже считается
    @Test
    fun hasMatches_sharedExtraColor() {
        val b = board("312", "234", "341")
        b[0, 0] = Gem(1, extraType = 3)
        b[0, 1] = Gem(2, extraType = 3)
        b[0, 2] = Gem(4, extraType = 3)
        assertTrue(b.hasMatches())
    }

    // RULE-022, решение 21: "половинки" (extra = EMPTY_COLOR) цветом не считаются
    @Test
    fun hasMatches_halfGemsDoNotShareEmptyColor() {
        val b = board("312", "234", "341")
        b[0, 0] = Gem(3, extraType = Gem.EMPTY_COLOR)
        b[0, 1] = Gem(1, extraType = Gem.EMPTY_COLOR)
        b[0, 2] = Gem(2, extraType = Gem.EMPTY_COLOR)
        assertFalse(b.hasMatches())
    }

    // RULE-022, решение 21: isLine — прямая проверка тройки
    @Test
    fun isLine_commonColorOfAllThree() {
        assertTrue(GameBoard.isLine(Gem(1), Gem(1), Gem(1)))
        assertTrue(GameBoard.isLine(Gem(1, extraType = 2), Gem(1), Gem(3, extraType = 1)))
        assertFalse(GameBoard.isLine(Gem(1, extraType = 2), Gem(1), Gem(2)))
        assertFalse(GameBoard.isLine(Gem(1), Gem(2), Gem(3)))
    }

    // RULE-023
    @Test
    fun findPossibleMoves_listsSwapsThatCreateMatch_verticalFirst() {
        val b = board(
            "1213",
            "2344",
            "3421",
        )
        val moves = b.findPossibleMoves()
        assertEquals(b.checkPossibleMoves(), moves.isNotEmpty())
        // доска после поиска возвращена в исходное состояние
        assertEquals("1213", (0 until 4).joinToString("") { b[0, it].type.toString() })
        assertEquals("2344", (0 until 4).joinToString("") { b[1, it].type.toString() })
    }

    // RULE-023
    @Test
    fun findPossibleMoves_exactList() {
        val b = board(
            "112",
            "343",
            "434",
        )
        // единственный ход: (1,1)<->(2,1) даёт строку 1 = "333"
        val moves = b.findPossibleMoves()
        assertEquals(listOf(Move(1 to 1, 2 to 1)), moves)
        assertTrue(b.checkPossibleMoves())
    }

    // RULE-023: свап одинаковых не рассматривается
    @Test
    fun noMoves_whenNothingSwappable() {
        val b = board("12", "34")
        assertTrue(b.findPossibleMoves().isEmpty())
        assertFalse(b.checkPossibleMoves())
    }

    // RULE-024
    @Test(expected = IllegalArgumentException::class)
    fun get_outOfBounds_throws() {
        GameBoard(3, 3, settings)[3, 0]
    }

    // RULE-025: клетки берутся из BattleSettings.cells, вне поля — игнорируются
    @Test
    fun cells_fromSettings() {
        settings.update(BattleSettings(cells = arrayListOf(
            BattleSettings.CellConfig(1, 1, CellType.MULTIPLIER, 1.5f),
            BattleSettings.CellConfig(9, 9, CellType.ADDITIVE, 2f),
        )))
        val b = GameBoard(3, 3, settings)
        assertEquals(CellType.MULTIPLIER, b.getCell(1, 1).cellType)
        assertEquals(1.5f, b.getCell(1, 1).modifierValue)
        assertEquals(CellType.NONE, b.getCell(0, 0).cellType)
    }

    // RULE-026: число цветов берётся из Gem.GEM_TYPE_NUMBER
    @Test
    fun gemTypeNumber_limitsColors() {
        Gem.GEM_TYPE_NUMBER = 5
        val seen = HashSet<Int>()
        repeat(10) {
            val b = GameBoard(8, 8, settings)
            for (r in 0 until 8) for (c in 0 until 8) seen.add(b[r, c].type)
        }
        assertEquals(setOf(1, 2, 3, 4, 5), seen)
    }

    // RULE-027
    @Test
    fun sizes() {
        val b = GameBoard(5, 7, settings)
        assertEquals(5, b.rowSize)
        assertEquals(7, b.columnSize)
    }
}
