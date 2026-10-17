package aldtoll.twiligihts.storage

import aldtoll.twiligihts.model.Hand
import aldtoll.twiligihts.model.Perk
import aldtoll.twiligihts.model.Resource
import aldtoll.twiligihts.model.Sector
import aldtoll.twiligihts.model.Stock
import aldtoll.twiligihts.model.StockPerk
import aldtoll.twiligihts.model.TimePerk

/**
 * Снимки для публикации в LiveData: интерактор отдаёт логике рабочий объект (value()),
 * а наблюдателям (UI/Compose) — независимую копию, которую логика уже не меняет.
 * Поэтому новое значение всегда отличается от предыдущего по equals, если что-то изменилось.
 * Effect внутри Perk не копируется: его currentCharges не входит в equals и в UI не показывается.
 * Снимок Hero/Enemy делается через recreate(): он копирует Status, но не вложенные reactionPerk/gemTypes,
 * поэтому смена зарядов reactionPerk не делает опубликованного персонажа другим.
 */
fun <T, R> ArrayList<T>.snapList(snap: (T) -> R): ArrayList<R> = ArrayList(map(snap))

fun Resource.snap(): Resource = copy()

fun Stock.snap(): Stock = copy()

fun Perk.snap(): Perk = copy(
    prices = ArrayList(prices),
    effects = ArrayList(effects),
    resources = resources.snapList { it.snap() },
)

fun Hand.snap(): Hand = copy(
    perks = perks.snapList { it.snap() },
    conditionsForDisplay = ArrayList(conditionsForDisplay),
)

fun StockPerk.snap(): StockPerk = copy(perk = perk.snap())

fun TimePerk.snap(): TimePerk = copy(perk = perk.snap())

fun Sector.snap(): Sector = copy(perk = perk.snap())
