package aldtoll.twiligihts.logic

import aldtoll.twiligihts.model.Hand
import aldtoll.twiligihts.model.Perk
import aldtoll.twiligihts.storage.EnemyHandsListInteractor
import aldtoll.twiligihts.storage.EnemyInteractor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FillEnemyExecutor @Inject constructor(
    private val enemyInteractor: EnemyInteractor,
    private val enemyHandsListInteractor: EnemyHandsListInteractor,
) {

    fun execute() {
        enemyInteractor.init()
        enemyHandsListInteractor.update(
            arrayListOf(
                Hand(
                    "Основная лапа",
                    1,
                    arrayListOf(
                        Perk(
                            name = "Малый удар",
                            effects = arrayListOf(
                                Perk.Effect(
                                    10,
                                    Perk.Effect.EffectType.ATTACK,
                                    Perk.Effect.EffectTarget.HERO
                                )
                            ),
                            prices = arrayListOf(
                                Perk.Price(10, 1)
                            ),
                            description = "10 урона герою"
                        ),
                        Perk(
                            name = "Удар",
                            effects = arrayListOf(
                                Perk.Effect(
                                    20,
                                    Perk.Effect.EffectType.ATTACK,
                                    Perk.Effect.EffectTarget.HERO
                                )
                            ),
                            prices = arrayListOf(
                                Perk.Price(10, 1)
                            ),
                            description = "20 урона герою"
                        )
                    )
                ),
                Hand(
                    "Хвост",
                    2,
                    arrayListOf(
                        Perk(
                            name = "Защита",
                            effects = arrayListOf(
                                Perk.Effect(
                                    10,
                                    Perk.Effect.EffectType.DEFEND,
                                    Perk.Effect.EffectTarget.ENEMY
                                )
                            ),
                            prices = arrayListOf(
                                Perk.Price(0, 2)
                            ),
                            description = "10 щита врагу"
                        )
                    )
                )
            )
        )
    }
}