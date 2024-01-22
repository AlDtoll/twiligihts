package aldtoll.twiligihts.logic

import aldtoll.twiligihts.model.Hand
import aldtoll.twiligihts.model.Hero
import aldtoll.twiligihts.model.Perk
import aldtoll.twiligihts.model.Status
import aldtoll.twiligihts.model.Stock
import aldtoll.twiligihts.storage.HeroHandsListInteractor
import aldtoll.twiligihts.storage.HeroInteractor
import aldtoll.twiligihts.storage.StockListInteractor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FillHeroExecutor @Inject constructor(
    private val stockListInteractor: StockListInteractor,
    private val heroHandsListInteractor: HeroHandsListInteractor,
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
                "Основная рука",
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
                    ),
                    Perk(
                        name = "Парирование",
                        prices = arrayListOf(
                            Perk.Price(
                                30, 1
                            )
                        ),
                        effects = arrayListOf(
                            Perk.Effect(
                                10,
                                Perk.Effect.EffectType.DEFEND,
                                Perk.Effect.EffectTarget.HERO
                            )
                        ),
                        description = "10 защиты герою"
                    )
                )
            )
        )
        hands.add(
            Hand(
                "Вторая рука",
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
                "Корпус",
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
                    ),
                    Perk(
                        name = "Уклонение",
                        prices = arrayListOf(
                            Perk.Price(
                                30, 3
                            )
                        ),
                        effects = arrayListOf(
                            Perk.Effect(
                                10,
                                Perk.Effect.EffectType.ADD_STATUS,
                                Perk.Effect.EffectTarget.HERO,
                                Status(
                                    "Уклонение",
                                    1,
                                    Status.EffectType.DODGE
                                )
                            )
                        ),
                        description = "Приготовиться увернуться"
                    )
                )
            )
        )
        heroHandsListInteractor.update(hands)

        val hero = Hero(
            100,
            100,
            0,
            3,
            0
        )
//        heroInteractor.update(hero)
    }
}