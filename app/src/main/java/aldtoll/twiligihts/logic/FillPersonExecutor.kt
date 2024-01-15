package aldtoll.twiligihts.logic

import aldtoll.twiligihts.model.Hand
import aldtoll.twiligihts.model.Person
import aldtoll.twiligihts.model.Stock
import aldtoll.twiligihts.storage.HandsListInteractor
import aldtoll.twiligihts.storage.PersonInteractor
import aldtoll.twiligihts.storage.StockListInteractor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FillPersonExecutor @Inject constructor(
    private val stockListInteractor: StockListInteractor,
    private val handsListInteractor: HandsListInteractor,
    private val personInteractor: PersonInteractor,
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
                                5,
                                Hand.Perk.Effect.EffectType.ATTACK,
                                Hand.Perk.Effect.EffectTarget.ENEMY
                            )
                        ),
                        description = "Нанести 5 урона врагу"
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
                                3,
                                Hand.Perk.Effect.EffectType.DEFEND,
                                Hand.Perk.Effect.EffectTarget.PERSON
                            )
                        ),
                        description = "Дать 3 щита себе"
                    )
                )
            )
        )
        hands.add(
            Hand(
                3,
                arrayListOf(
                    Hand.Perk(
                        prices = arrayListOf(
                            Hand.Perk.Price(
                                30, 3
                            ),
                            Hand.Perk.Price(
                                30, 1
                            )
                        ),
                        effects = arrayListOf(
                            Hand.Perk.Effect(
                                3,
                                Hand.Perk.Effect.EffectType.ATTACK,
                                Hand.Perk.Effect.EffectTarget.ALL
                            )
                        ),
                        description = "Нанести 3 урона всем"
                    )
                )
            )
        )
        handsListInteractor.update(hands)

        val person = Person(
            100,
            100,
            0,
            0
        )
        personInteractor.update(person)
    }
}