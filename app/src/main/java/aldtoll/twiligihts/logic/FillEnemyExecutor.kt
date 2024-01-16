package aldtoll.twiligihts.logic

import aldtoll.twiligihts.model.Enemy
import aldtoll.twiligihts.model.Perk
import aldtoll.twiligihts.storage.EnemyInteractor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FillEnemyExecutor @Inject constructor(
    private val enemyInteractor: EnemyInteractor,
) {

    fun execute() {
        enemyInteractor.update(
            Enemy(
                30,
                30,
                0,
                3,
                10,
                arrayListOf(
                    Perk(
                        effects = arrayListOf(
                            Perk.Effect(
                                20,
                                Perk.Effect.EffectType.ATTACK,
                                Perk.Effect.EffectTarget.HERO
                            )
                        )
                    ),
                    Perk(
                        effects = arrayListOf(
                            Perk.Effect(
                                10,
                                Perk.Effect.EffectType.DEFEND,
                                Perk.Effect.EffectTarget.ENEMY
                            )
                        )
                    )
                )
            )
        )
    }
}