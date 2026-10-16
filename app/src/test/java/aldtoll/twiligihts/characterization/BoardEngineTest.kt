package aldtoll.twiligihts.characterization

import aldtoll.twiligihts.logic.board.BoardEngine
import aldtoll.twiligihts.logic.board.CascadeStep
import aldtoll.twiligihts.logic.board.GemMove
import aldtoll.twiligihts.model.BattleSettings
import aldtoll.twiligihts.model.GameBoard
import aldtoll.twiligihts.model.Gem
import aldtoll.twiligihts.model.MatchGroupInfo
import aldtoll.twiligihts.model.MatchOrientation
import aldtoll.twiligihts.storage.BattleSettingsInteractor
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.random.Random

/**
 * Characterization BoardEngine: логика доски, вынесенная из GameBoardAdapter.
 * Фиксирует поведение КАК ЕСТЬ, включая странности (помечены QUIRK).
 */
class BoardEngineTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var settings: BattleSettingsInteractor

    @Before
    fun setUp() {
        Fixture.resetGlobals()
        settings = BattleSettingsInteractor()
    }

    /** Заполнить доску по шаблону строк (цифра = type, 0 = пустая клетка). */
    private fun board(vararg rows: String): GameBoard {
        val b = GameBoard(rows.size, rows[0].length, settings)
        rows.forEachIndexed { r, line -> line.forEachIndexed { c, ch -> b[r, c] = Gem(ch.digitToInt()) } }
        return b
    }

    /** Доска строками из type (extra/bonus не показываются). */
    private fun snapshot(b: GameBoard): List<String> =
        (0 until b.rowSize).map { r -> (0 until b.columnSize).joinToString("") { c -> b[r, c].type.toString() } }

    private fun engine(b: GameBoard, stopGenerate: Boolean = false, seed: Int = 1) =
        BoardEngine(b, stopGenerate, Random(seed))

    private fun p(r: Int, c: Int) = Pair(r, c)

    /**
     * Прогон каскада до остановки как в адаптере: Remove/Generate -> гравитация -> следующий шаг.
     * Возвращает все шаги и все перемещения гравитации.
     */
    private class CascadeRun(val steps: List<CascadeStep>, val moves: List<List<GemMove>>)

    private fun runCascade(e: BoardEngine, limit: Int = 100): CascadeRun {
        val steps = mutableListOf<CascadeStep>()
        val moves = mutableListOf<List<GemMove>>()
        repeat(limit) {
            val step = e.nextStep()
            steps.add(step)
            when (step) {
                is CascadeStep.Remove, is CascadeStep.Generate -> moves.add(e.applyGravity())
                is CascadeStep.Stop, CascadeStep.Stalled -> return CascadeRun(steps, moves)
            }
        }
        throw AssertionError("cascade did not stabilize in $limit steps")
    }

    // ---------- areAdjacent / swap ----------

    @Test
    fun areAdjacent_onlyOrthogonalNeighbours() {
        assertTrue(BoardEngine.areAdjacent(p(2, 2), p(2, 3)))
        assertTrue(BoardEngine.areAdjacent(p(2, 2), p(2, 1)))
        assertTrue(BoardEngine.areAdjacent(p(2, 2), p(1, 2)))
        assertTrue(BoardEngine.areAdjacent(p(2, 2), p(3, 2)))
        assertFalse(BoardEngine.areAdjacent(p(2, 2), p(3, 3))) // диагональ
        assertFalse(BoardEngine.areAdjacent(p(2, 2), p(2, 4))) // через клетку
        assertFalse(BoardEngine.areAdjacent(p(2, 2), p(2, 2))) // сама с собой
    }

    @Test
    fun swap_exchangesGemsWithoutAdjacencyCheck() {
        val b = board("12", "34")
        val e = engine(b)
        e.swap(p(0, 0), p(1, 1)) // QUIRK: движок не проверяет соседство, это делает вызывающий
        assertEquals(listOf("42", "31"), snapshot(b))
    }

    // ---------- findMatches ----------

    @Test
    fun findMatches_horizontalTriple() {
        val b = board("11123", "23414", "34231", "42342")
        assertEquals(listOf(p(0, 0), p(0, 1), p(0, 2)), engine(b).findMatches())
    }

    @Test
    fun findMatches_verticalTriple() {
        val b = board("1234", "1342", "1423", "2134")
        assertEquals(listOf(p(0, 0), p(1, 0), p(2, 0)), engine(b).findMatches())
    }

    @Test
    fun findMatches_horizontalsBeforeVerticals() {
        // горизонтальная тройка типа 2 внизу и вертикальная тройка типа 1 слева
        val b = board("1234", "1342", "1423", "3222")
        // QUIRK: вертикальная 1 найдена, а (3,1),(3,2),(3,3) - горизонтальная; порядок: сначала строки
        assertEquals(
            listOf(p(3, 1), p(3, 2), p(3, 3), p(0, 0), p(1, 0), p(2, 0)),
            engine(b).findMatches()
        )
    }

    @Test
    fun findMatches_four_duplicatesOverlappingPositions() {
        val b = board("11112", "23413", "34234", "42341")
        // QUIRK: четвёрка = две пересекающиеся тройки, средние клетки в списке дважды
        assertEquals(
            listOf(p(0, 0), p(0, 1), p(0, 2), p(0, 1), p(0, 2), p(0, 3)),
            engine(b).findMatches()
        )
    }

    @Test
    fun findMatches_five_threeOverlappingTriples() {
        val b = board("11111", "23423", "34234", "42342")
        val m = engine(b).findMatches()
        assertEquals(9, m.size)
        assertEquals(5, m.toSet().size)
        // QUIRK: клетки на пересечении троек повторяются: края 1 раз, (0,1) и (0,3) - 2 раза, (0,2) - 3 раза
        assertEquals(listOf(1, 2, 3, 2, 1), (0..4).map { c -> m.count { it == p(0, c) } })
    }

    @Test
    fun findMatches_noMatches_emptyList() {
        val b = board("1234", "3412", "2341", "4123")
        assertTrue(engine(b).findMatches().isEmpty())
    }

    @Test
    fun findMatches_emptyCellsAreNotMatches() {
        val b = board("000", "123", "231")
        assertTrue(engine(b).findMatches().isEmpty())
    }

    // ---------- computeMatchGroups ----------

    private fun groupsOf(b: GameBoard): List<MatchGroupInfo> {
        val e = engine(b)
        return e.computeMatchGroups(e.findMatches().toMutableSet())
    }

    @Test
    fun groups_horizontalTriple() {
        val b = board("11123", "23414", "34231", "42342")
        assertEquals(listOf(MatchGroupInfo(1, MatchOrientation.HORIZONTAL, 3)), groupsOf(b))
    }

    @Test
    fun groups_verticalTriple() {
        val b = board("1234", "1342", "1423", "2134")
        assertEquals(listOf(MatchGroupInfo(1, MatchOrientation.VERTICAL, 3)), groupsOf(b))
    }

    @Test
    fun groups_four_isOneGroupOfFour() {
        val b = board("11112", "23413", "34234", "42341")
        assertEquals(listOf(MatchGroupInfo(1, MatchOrientation.HORIZONTAL, 4)), groupsOf(b))
    }

    @Test
    fun groups_five_isOneGroupOfFive() {
        val b = board("11111", "23423", "34234", "42342")
        assertEquals(listOf(MatchGroupInfo(1, MatchOrientation.HORIZONTAL, 5)), groupsOf(b))
    }

    @Test
    fun groups_tShape() {
        val b = board("11123", "21434", "31241", "42312")
        assertEquals(listOf(MatchGroupInfo(1, MatchOrientation.T_SHAPE, 5)), groupsOf(b))
    }

    @Test
    fun groups_lShape_isLShape() {
        // изменено: L теперь угол. Раньше настоящая "Г" (3+3 в углу) давала T_SHAPE
        val b = board("11123", "13434", "14241", "22312")
        assertEquals(listOf(MatchGroupInfo(1, MatchOrientation.L_SHAPE, 5)), groupsOf(b))
    }

    @Test
    fun groups_lShape_cornerAtBottomRight() {
        val b = board("23421", "34231", "42111", "23423", "34234")
        assertEquals(listOf(MatchGroupInfo(1, MatchOrientation.L_SHAPE, 5)), groupsOf(b))
    }

    @Test
    fun groups_lShape_longArm() {
        // линия 4 + линия 3 в общем углу
        val b = board("11112", "13434", "14243", "23324")
        assertEquals(listOf(MatchGroupInfo(1, MatchOrientation.L_SHAPE, 6)), groupsOf(b))
    }

    @Test
    fun groups_cross_isTShape() {
        val b = board("23134", "31112", "42143", "23424")
        assertEquals(listOf(MatchGroupInfo(1, MatchOrientation.T_SHAPE, 5)), groupsOf(b))
    }

    @Test
    fun groups_sidewaysT_isTShape() {
        // вертикаль 3, горизонталь отходит от её середины
        val b = board("12342", "11134", "13423", "24234")
        assertEquals(listOf(MatchGroupInfo(1, MatchOrientation.T_SHAPE, 5)), groupsOf(b))
    }

    @Test
    fun groups_twoStackedTriples_isOther() {
        // изменено: L теперь угол. Блок 2x3 (две соседние параллельные тройки) - не L, а OTHER
        val b = board("11123", "11134", "23441", "34212")
        assertEquals(listOf(MatchGroupInfo(1, MatchOrientation.OTHER, 6)), groupsOf(b))
    }

    @Test
    fun groups_verticalBlock3x2_isOther() {
        val b = board("11234", "11342", "11423", "23234")
        assertEquals(listOf(MatchGroupInfo(1, MatchOrientation.OTHER, 6)), groupsOf(b))
    }

    @Test
    fun groups_twoSeparateGroups_inFindOrder() {
        val b = board("1234", "1342", "1423", "3222")
        assertEquals(
            listOf(
                MatchGroupInfo(2, MatchOrientation.HORIZONTAL, 3),
                MatchGroupInfo(1, MatchOrientation.VERTICAL, 3),
            ),
            groupsOf(b)
        )
    }

    // ---------- двухцветные гемы ----------

    @Test
    fun twoColorGems_commonColorViaExtra_isMatch() {
        val b = board("12341", "23412", "34123", "41234")
        b[0, 0] = Gem(1, extraType = 2) // [1+2, 2, 3] - нет
        assertTrue(engine(b).findMatches().isEmpty())
        b[0, 2] = Gem(2) // [1+2, 2, 2] - общий цвет 2
        assertEquals(listOf(p(0, 0), p(0, 1), p(0, 2)), engine(b).findMatches())
    }

    @Test
    fun twoColorGems_groupSplitsByMainType() {
        val b = board("12341", "23412", "34123", "41234")
        b[0, 0] = Gem(1, extraType = 2)
        b[0, 2] = Gem(2)
        // QUIRK: в матч-линию входят три клетки, но BFS групп сравнивает только gem.type,
        // поэтому тройка [1+2, 2, 2] распадается на две группы: OTHER(1) и OTHER(2)
        assertEquals(
            listOf(
                MatchGroupInfo(1, MatchOrientation.OTHER, 1),
                MatchGroupInfo(2, MatchOrientation.OTHER, 2),
            ),
            groupsOf(b)
        )
    }

    @Test
    fun twoColorGems_sharedExtraColor_isMatch() {
        val b = board("12341", "23412", "34123", "41234")
        b[0, 0] = Gem(1, extraType = 3)
        b[0, 1] = Gem(2, extraType = 3)
        b[0, 2] = Gem(4, extraType = 3)
        assertEquals(listOf(p(0, 0), p(0, 1), p(0, 2)), engine(b).findMatches())
    }

    @Test
    fun twoColorGems_halfGemExtraEmptyColorDoesNotCount() {
        val b = board("12341", "23412", "34123", "41234")
        b[0, 0] = Gem(1, extraType = Gem.EMPTY_COLOR)
        b[0, 1] = Gem(2, extraType = Gem.EMPTY_COLOR)
        b[0, 2] = Gem(3, extraType = Gem.EMPTY_COLOR)
        assertTrue(engine(b).findMatches().isEmpty())
        // но по основному цвету "половинки" совпадают
        b[0, 0] = Gem(1, extraType = Gem.EMPTY_COLOR)
        b[0, 1] = Gem(1, extraType = Gem.EMPTY_COLOR)
        b[0, 2] = Gem(1)
        assertEquals(3, engine(b).findMatches().size)
    }

    // ---------- removeMatches ----------

    @Test
    fun removeMatches_clearsCellsAndReportsCrushed() {
        val b = board("11123", "23414", "34231", "42342")
        val e = engine(b)
        val removal = e.removeMatches(e.findMatches())
        assertEquals(listOf("00023", "23414", "34231", "42342"), snapshot(b))
        assertEquals(listOf(p(0, 0), p(0, 1), p(0, 2)), removal.positions)
        assertEquals(listOf(Gem(1), Gem(1), Gem(1)), removal.crushedCells.map { it.gem })
        assertEquals(listOf(p(0, 0), p(0, 1), p(0, 2)), removal.crushedCells.map { it.row to it.col })
        assertEquals(mapOf(1 to 3), removal.removedByType)
        assertEquals(listOf(MatchGroupInfo(1, MatchOrientation.HORIZONTAL, 3)), removal.groups)
    }

    @Test
    fun removeMatches_four_duplicatesNotInCrushedCells() {
        val b = board("11112", "23413", "34234", "42341")
        val e = engine(b)
        val removal = e.removeMatches(e.findMatches())
        // изменено: убраны дубли. positions по-прежнему 6 записей (анимация), но
        // crushedCells и removedByType - по одной записи на клетку, без Gem(0)
        assertEquals(6, removal.positions.size)
        assertEquals(4, removal.crushedCells.size)
        assertEquals(listOf(Gem(1), Gem(1), Gem(1), Gem(1)), removal.crushedCells.map { it.gem })
        assertEquals(
            listOf(p(0, 0), p(0, 1), p(0, 2), p(0, 3)),
            removal.crushedCells.map { it.row to it.col }
        )
        assertEquals(mapOf(1 to 4), removal.removedByType)
        assertEquals(listOf(MatchGroupInfo(1, MatchOrientation.HORIZONTAL, 4)), removal.groups)
        assertEquals("00002", snapshot(b)[0])
    }

    @Test
    fun removeMatches_cross_eachCellOnceAndNoEmptyGems() {
        val b = board("23134", "31112", "42143", "23424")
        val e = engine(b)
        val removal = e.removeMatches(e.findMatches())
        // изменено: убраны дубли. Центр креста входит в две линии, но в crushedCells один раз
        assertEquals(6, removal.positions.size)
        assertEquals(5, removal.crushedCells.size)
        assertTrue(removal.crushedCells.all { it.gem == Gem(1) })
        assertEquals(5, removal.crushedCells.map { it.row to it.col }.toSet().size)
        assertEquals(mapOf(1 to 5), removal.removedByType)
        assertEquals(listOf(MatchGroupInfo(1, MatchOrientation.T_SHAPE, 5)), removal.groups)
        assertEquals(listOf("23034", "30002", "42043", "23424"), snapshot(b))
    }

    // ---------- гравитация ----------

    @Test
    fun gravity_withHoles_dropsGemsToBottom() {
        val b = board("123", "040", "300", "002")
        val moves = engine(b).applyGravity()
        assertEquals(listOf("000", "000", "123", "342"), snapshot(b))
        // колонки справа налево, в колонке снизу вверх
        assertEquals(
            listOf(
                GemMove(p(0, 2), p(2, 2)),
                GemMove(p(1, 1), p(3, 1)),
                GemMove(p(0, 1), p(2, 1)),
                GemMove(p(2, 0), p(3, 0)),
                GemMove(p(0, 0), p(2, 0)),
            ),
            moves
        )
    }

    @Test
    fun gravity_noHoles_noMoves() {
        val b = board("123", "231", "312")
        assertTrue(engine(b).applyGravity().isEmpty())
        assertEquals(listOf("123", "231", "312"), snapshot(b))
    }

    @Test
    fun gravity_keepsExtraAndBonusOfFallingGem() {
        val b = board("100", "000", "000")
        val gem = Gem(1, bonusType = 2, extraType = 3)
        b[0, 0] = gem
        engine(b).applyGravity()
        assertEquals(gem, b[2, 0])
        assertEquals(Gem(0), b[0, 0])
    }

    @Test
    fun gravity_emptyColumn_noMoves() {
        val b = board("000", "000")
        assertTrue(engine(b).applyGravity().isEmpty())
    }

    // ---------- генерация ----------

    @Test
    fun generate_fillsOnlyEmptyTopRowCells() {
        val b = board("101", "232", "323")
        val gens = engine(b, seed = 42).generateNewGems()
        assertEquals(listOf(p(0, 1)), gens.map { it.position })
        assertTrue(b[0, 1].type in 1..4)
        assertEquals(b[0, 1], gens[0].gem)
        // остальные клетки не тронуты
        assertEquals(Gem(1), b[0, 0])
        assertEquals(Gem(1), b[0, 2])
    }

    @Test
    fun generate_emptyCellsLowerDown_notFilledByGenerate() {
        val b = board("123", "000", "321")
        val gens = engine(b, seed = 42).generateNewGems()
        assertTrue(gens.isEmpty())
        assertEquals(listOf("123", "000", "321"), snapshot(b))
    }

    @Test
    fun generate_sameSeed_sameGems_differentFromOtherSeedIsPossible() {
        fun gen(seed: Int): List<Gem> {
            val b = board("00000000", "12341234")
            return engine(b, seed = seed).generateNewGems().map { it.gem }
        }
        assertEquals(gen(42), gen(42))
        assertEquals(8, gen(42).size)
        // порядок слева направо, каждый гем = Gem.generateNewGem(random) подряд
        val random = Random(42)
        assertEquals(List(8) { Gem.generateNewGem(random) }, gen(42))
    }

    @Test
    fun generate_seed42_exactTypes() {
        val b = board("0000", "1234")
        val gems = engine(b, seed = 42).generateNewGems().map { it.gem }
        assertEquals(EXPECTED_SEED_42, gems)
    }

    // ---------- nextStep: простые случаи ----------

    @Test
    fun nextStep_noMatchesNoEmpty_stopsImmediately() {
        val b = board("1234", "3412", "2341", "4123")
        val step = engine(b).nextStep()
        assertTrue(step is CascadeStep.Stop)
        assertFalse((step as CascadeStep.Stop).finishBattleIfNoMatches)
        assertEquals(b.checkPossibleMoves(), step.hasPossibleMoves)
    }

    @Test
    fun nextStep_emptyCellsNoMatches_generateFirst_notStop() {
        val b = board("1034", "3412", "2341", "4123")
        val step = engine(b, seed = 5).nextStep()
        assertTrue(step is CascadeStep.Generate)
        assertEquals(listOf(p(0, 1)), (step as CascadeStep.Generate).newGems.map { it.position })
    }

    @Test
    fun nextStep_stopGenerate_emptyCellsNoMatches_stopsWithFinishFlag() {
        val b = board("1034", "3412", "2341", "4123")
        val step = engine(b, stopGenerate = true).nextStep()
        assertTrue(step is CascadeStep.Stop)
        assertTrue((step as CascadeStep.Stop).finishBattleIfNoMatches)
        assertEquals(Gem(0), b[0, 1]) // ничего не сгенерировано
    }

    @Test
    fun nextStep_matches_removeFirst() {
        val b = board("11123", "23414", "34231", "42342")
        val step = engine(b).nextStep()
        assertTrue(step is CascadeStep.Remove)
        assertEquals("00023", snapshot(b)[0])
    }

    @Test
    fun nextStep_possibleMovesFlag_trueAndFalse() {
        // есть ход: поменять (0,2) и (1,2) даёт тройку 1 в строке 0 -> нужны [1 1 x] и 1 под x
        val withMove = board("1123", "2314", "3421", "4231")
        // (0,2)=2 <-> (1,2)=1 -> строка 0 = 1 1 1
        val s1 = engine(withMove).nextStep() as CascadeStep.Stop
        assertTrue(s1.hasPossibleMoves)
        // доска-паттерн без ходов
        val noMove = board("1234", "3412", "2341", "4123")
        val s2 = engine(noMove).nextStep() as CascadeStep.Stop
        assertEquals(noMove.checkPossibleMoves(), s2.hasPossibleMoves)
    }

    // ---------- каскад ----------

    // Колонка 0: тройка 1 (строки 1-3). После падения (0,0)=2 встаёт в строку 3 рядом с "22" -> вторая тройка.
    private fun cascadeBoard() = board("234", "143", "134", "122")

    @Test
    fun cascade_stopGenerate_multiStepUntilStable() {
        val b = cascadeBoard()
        val e = engine(b, stopGenerate = true)
        val run = runCascade(e)

        val kinds = run.steps.map { it::class.simpleName }
        assertEquals(listOf("Remove", "Remove", "Stop"), kinds)

        val r1 = (run.steps[0] as CascadeStep.Remove).removal
        assertEquals(listOf(p(1, 0), p(2, 0), p(3, 0)), r1.positions)
        assertEquals(listOf(MatchGroupInfo(1, MatchOrientation.VERTICAL, 3)), r1.groups)
        assertEquals(listOf(GemMove(p(0, 0), p(3, 0))), run.moves[0])

        val r2 = (run.steps[1] as CascadeStep.Remove).removal
        assertEquals(listOf(p(3, 0), p(3, 1), p(3, 2)), r2.positions)
        assertEquals(listOf(MatchGroupInfo(2, MatchOrientation.HORIZONTAL, 3)), r2.groups)
        assertEquals(
            listOf(
                GemMove(p(2, 2), p(3, 2)),
                GemMove(p(1, 2), p(2, 2)),
                GemMove(p(0, 2), p(1, 2)),
                GemMove(p(2, 1), p(3, 1)),
                GemMove(p(1, 1), p(2, 1)),
                GemMove(p(0, 1), p(1, 1)),
            ),
            run.moves[1]
        )

        // пустые клетки остаются: генерация выключена
        val stop = run.steps[2] as CascadeStep.Stop
        assertTrue(stop.finishBattleIfNoMatches)
        assertEquals(listOf("000", "034", "043", "034"), snapshot(b))
    }

    @Test
    fun cascade_withGeneration_seeded_stabilizesWithFullBoardAndNoMatches() {
        val b = cascadeBoard()
        val e = engine(b, seed = 7)
        val run = runCascade(e)

        // первые два шага те же: удаление, удаление; дальше - фаза падения/генерации
        assertTrue(run.steps[0] is CascadeStep.Remove)
        assertTrue(run.steps[1] is CascadeStep.Remove)
        assertTrue(run.steps.last() is CascadeStep.Stop)
        assertTrue(run.steps.any { it is CascadeStep.Generate })
        assertFalse((run.steps.last() as CascadeStep.Stop).finishBattleIfNoMatches)

        assertFalse(b.hasEmptyGems())
        assertFalse(b.hasMatches())
        assertEquals(EXPECTED_CASCADE_SEED_7, snapshot(b))
        assertEquals(EXPECTED_CASCADE_STEPS_SEED_7, run.steps.map { it::class.simpleName })
    }

    @Test
    fun cascade_withGeneration_sameSeed_isReproducible() {
        fun run(): List<String> {
            val b = cascadeBoard()
            runCascade(engine(b, seed = 7))
            return snapshot(b)
        }
        assertEquals(run(), run())
    }

    @Test
    fun cascade_generatePhase_matchesFormedWhileFallingWaitUntilNoEmptyCells() {
        // QUIRK: в фазе падения/генерации, пока есть пустые клетки, существующие совпадения
        // не удаляются: nextStep снова возвращает Generate, а не Remove.
        val b = board("1034", "3412", "2341", "4123")
        val e = engine(b, seed = 5)
        assertTrue(e.nextStep() is CascadeStep.Generate) // фаза началась
        // руками: в нижней строке тройка, и наверху снова дыра
        b[0, 0] = Gem(0)
        b[3, 0] = Gem(2)
        b[3, 1] = Gem(2)
        b[3, 2] = Gem(2)
        assertTrue(e.findMatches().isNotEmpty())
        val step = e.nextStep()
        assertTrue(step is CascadeStep.Generate)
        assertEquals(listOf(p(0, 0)), (step as CascadeStep.Generate).newGems.map { it.position })
        // дыр больше нет -> фаза закончена, теперь совпадения удаляются
        assertTrue(e.nextStep() is CascadeStep.Remove)
    }

    private companion object {
        // Gem.generateNewGem(Random(42)) для 4 клеток подряд (GEM_TYPE_NUMBER = 4, дефолтные вероятности)
        val EXPECTED_SEED_42: List<Gem> = listOf(
            Gem(type = 1, bonusType = 4, extraType = null),
            Gem(type = 1, bonusType = null, extraType = null),
            Gem(type = 1, bonusType = 3, extraType = Gem.EMPTY_COLOR),
            Gem(type = 1, bonusType = 3, extraType = 1),
        )
        val EXPECTED_CASCADE_SEED_7: List<String> = listOf("321", "134", "443", "434")
        val EXPECTED_CASCADE_STEPS_SEED_7: List<String> =
            listOf("Remove", "Remove", "Generate", "Generate", "Generate", "Generate", "Stop")
    }
}
