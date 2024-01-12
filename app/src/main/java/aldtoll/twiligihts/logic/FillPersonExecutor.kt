package aldtoll.twiligihts.logic

import aldtoll.twiligihts.model.Hand
import aldtoll.twiligihts.model.Stock
import aldtoll.twiligihts.storage.HandsListInteractor
import aldtoll.twiligihts.storage.StockListInteractor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FillPersonExecutor @Inject constructor(
    private val stockListInteractor: StockListInteractor,
    private val handsListInteractor: HandsListInteractor,
) {

    fun execute() {
        val list = arrayListOf<Stock>()
        list.add(Stock(0, 1))
        list.add(Stock(0, 2))
        list.add(Stock(0, 3))
        list.add(Stock(0, 4))
        stockListInteractor.update(list)

        val hands = arrayListOf<Hand>()
        hands.add(
            Hand(
                1,
                arrayListOf(
                    Hand.Perk(
                        prices = arrayListOf(
                            Hand.Perk.Price(
                                30, 1
                            )
                        ),
                        effects = arrayListOf(
                            Hand.Perk.Effect(
                                5, Hand.Perk.Effect.EffectType.ATTACK
                            )
                        ),
                        description = null
                    )
                )
            )
        )
        hands.add(
            Hand(
                2,
                arrayListOf(
                    Hand.Perk(
                        prices = arrayListOf(
                            Hand.Perk.Price(
                                30, 2
                            )
                        ),
                        effects = arrayListOf(
                            Hand.Perk.Effect(
                                3, Hand.Perk.Effect.EffectType.DEFEND
                            )
                        ),
                        description = null
                    )
                )
            )
        )
        handsListInteractor.update(hands)
    }
}