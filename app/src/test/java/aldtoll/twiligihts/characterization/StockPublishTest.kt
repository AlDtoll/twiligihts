package aldtoll.twiligihts.characterization

import aldtoll.twiligihts.characterization.Fixture.Companion.perkOf
import aldtoll.twiligihts.model.Stock
import aldtoll.twiligihts.model.StockPerk
import aldtoll.twiligihts.model.effects.Effect
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test

/**
 * Шкалы героя/врага: postValue доставляется отложенно (как на устройстве), а value() актуален сразу.
 * Вместо InstantTaskExecutorRule — ручная очередь главного потока: drain() = "тик main looper".
 */
class StockPublishTest {

    private class ManualMainExecutor : TaskExecutor() {
        val queue = ArrayDeque<Runnable>()
        override fun executeOnDiskIO(runnable: Runnable) = runnable.run()
        override fun postToMainThread(runnable: Runnable) {
            queue.addLast(runnable)
        }
        override fun isMainThread() = true
        fun drain() {
            while (queue.isNotEmpty()) queue.removeFirst().run()
        }
    }

    private val main = ManualMainExecutor()
    private lateinit var f: Fixture

    @Before
    fun createFixture() {
        ArchTaskExecutor.getInstance().setDelegate(main)
        f = Fixture()
        main.drain()
    }

    @After
    fun resetExecutor() {
        ArchTaskExecutor.getInstance().setDelegate(null)
    }

    // value() актуален сразу после update(), опубликованное значение приходит на следующем тике
    @Test
    fun value_isCurrentBeforeDelivery() {
        val list = arrayListOf(Stock(10, 1))
        f.heroStockI.update(list)
        assertSame(list, f.heroStockI.value())
        assertEquals(0, f.heroStockI.get().value!!.size)
        main.drain()
        assertEquals(list, f.heroStockI.get().value)

        val enemyList = arrayListOf(Stock(5, 2))
        f.enemyStockI.update(enemyList)
        assertSame(enemyList, f.enemyStockI.value())
        main.drain()
        assertEquals(enemyList, f.enemyStockI.get().value)
    }

    // опубликованное — независимая копия: правка рабочего списка на месте его не меняет до следующего update()
    @Test
    fun published_isFrozenSnapshot_untilNextUpdate() {
        val list = arrayListOf(Stock(10, 1))
        f.heroStockI.update(list)
        main.drain()
        assertNotSame(list, f.heroStockI.get().value)
        list[0].value = 99
        assertNotEquals(list, f.heroStockI.get().value)
        assertEquals(10, f.heroStockI.get().value!![0].value)
        f.heroStockI.update(list)
        main.drain()
        assertEquals(99, f.heroStockI.get().value!![0].value)
    }

    // наблюдатель видит изменение после правки на месте + update(), предыдущий снимок не меняется
    @Test
    fun observer_seesChange_afterInPlaceMutationAndUpdate() {
        val received = mutableListOf<List<Stock>>()
        f.heroStockI.get().observeForever { received.add(it) }
        main.drain()
        received.clear()

        val list = arrayListOf(Stock(10, 1))
        f.heroStockI.update(list)
        main.drain()
        list[0].value = 25
        f.heroStockI.update(list)
        main.drain()

        assertEquals(2, received.size)
        assertEquals(10, received[0][0].value)
        assertEquals(25, received[1][0].value)
    }

    // два update() подряд: postValue склеивает доставку, наблюдатель получает последний снимок
    @Test
    fun consecutivePostValue_coalesced_observerGetsLatestSnapshot() {
        val received = mutableListOf<List<Stock>>()
        f.heroStockI.get().observeForever { received.add(it) }
        main.drain()
        received.clear()

        val list = arrayListOf(Stock(10, 1))
        f.heroStockI.update(list)
        list[0].value = 25
        f.heroStockI.update(list)
        main.drain()

        assertEquals(1, received.size)
        assertEquals(25, received[0][0].value)
    }

    // updateStocks: value() сразу новое, опубликованное — прежнее до тика
    @Test
    fun editStock_updateStocks_valueCurrentBeforeDelivery() {
        f.heroStocks(Stock(10, 1))
        main.drain()
        f.editStock.updateStocks(Pair(1, 5))
        assertEquals(15, f.heroStockI.value()!![0].value)
        assertEquals(10, f.heroStockI.get().value!![0].value)
        main.drain()
        assertEquals(15, f.heroStockI.get().value!![0].value)
    }

    // ADD на пустом списке: onStocksChanged читает value() и сразу видит созданные шкалы (StockPerk срабатывает)
    @Test
    fun editStockHandler_addOnEmptyList_onStocksChangedSeesNewStocks() {
        f.heroStockPerksI.update(arrayListOf(StockPerk(1, 1, 5, perkOf("Ярость", Effect.Attack(4)))))
        main.drain()
        f.stockPerk.rememberInitialStocks(arrayListOf(), isHero = true)
        f.editStockH.handleEffect(
            Effect.EditStock(7, arrayListOf(1), type = Effect.EditStock.Type.ADD),
            isHeroPerk = true
        )
        assertEquals(26, f.enemy.hp)
    }
}
