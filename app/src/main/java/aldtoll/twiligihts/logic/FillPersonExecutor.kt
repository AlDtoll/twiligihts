package aldtoll.twiligihts.logic

import aldtoll.twiligihts.model.Hand
import aldtoll.twiligihts.model.Hero
import aldtoll.twiligihts.model.Perk
import aldtoll.twiligihts.model.Stock
import aldtoll.twiligihts.storage.HandsListInteractor
import aldtoll.twiligihts.storage.HeroInteractor
import aldtoll.twiligihts.storage.StockListInteractor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FillPersonExecutor @Inject constructor(
    private val stockListInteractor: StockListInteractor,
    private val handsListInteractor: HandsListInteractor,
    private val heroInteractor: HeroInteractor,
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
                    Perk(
                        name = "Удар",
                        prices = arrayListOf(
                            Perk.Price(
                                30, 1
                            )
                        ),
                        effects = arrayListOf(
                            Perk.Effect(
                                30,
                                Perk.Effect.EffectType.ATTACK,
                                Perk.Effect.EffectTarget.ENEMY
                            )
                        ),
                        description = "30 урона врагу"
                    )
                )
            )
        )
        hands.add(
            Hand(
                2,
                arrayListOf(
                    Perk(
                        name = "Защита",
                        prices = arrayListOf(
                            Perk.Price(
                                30, 2
                            )
                        ),
                        effects = arrayListOf(
                            Perk.Effect(
                                10,
                                Perk.Effect.EffectType.DEFEND,
                                Perk.Effect.EffectTarget.HERO
                            )
                        ),
                        description = "10 щита себе"
                    )
                )
            )
        )
        hands.add(
            Hand(
                3,
                arrayListOf(
                    Perk(
                        name = "Взрыв",
                        prices = arrayListOf(
                            Perk.Price(
                                30, 3
                            ),
                            Perk.Price(
                                30, 1
                            )
                        ),
                        effects = arrayListOf(
                            Perk.Effect(
                                10,
                                Perk.Effect.EffectType.ATTACK,
                                Perk.Effect.EffectTarget.ALL
                            )
                        ),
                        description = "10 урона всем"
                    )
                )
            )
        )
        handsListInteractor.update(hands)

        val hero = Hero(
            100,
            100,
            0,
            3,
            0
        )
        heroInteractor.update(hero)
    }
}