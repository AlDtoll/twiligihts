package aldtoll.twiligihts.logic

import aldtoll.twiligihts.model.Condition
import aldtoll.twiligihts.model.effects.Effect
import aldtoll.twiligihts.storage.enemy.EnemyInteractor
import aldtoll.twiligihts.storage.hero.HeroInteractor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApplyFunctionExecutor @Inject constructor(
    private val heroInteractor: HeroInteractor,
    private val enemyInteractor: EnemyInteractor,
    private val checkConditionExecutor: CheckConditionExecutor,
) {

    fun execute(
        func: Effect.Func,
        isHeroPerk: Boolean
    ): Int {
        var value = 0
        func.allSegments().forEach { segment ->
            val personInteractor = when (segment.source) {
                Effect.Source.ENEMY -> enemyInteractor
                Effect.Source.HERO -> heroInteractor
                Effect.Source.SELF -> if (isHeroPerk) heroInteractor else enemyInteractor
                Effect.Source.FOE -> if (!isHeroPerk) heroInteractor else enemyInteractor
            }
            segment.parameter.let {
                personInteractor.value()?.run {
                    val personParameter = checkConditionExecutor.getParameter(
                        this,
                        Condition(
                            name = segment.name,
                            parameter = segment.parameter,
                            gemType = segment.gemType
                        )
                    )
                    value += (segment.mul * personParameter).toInt()
                }
            }
        }

        /**
         * бросок кости
         */
        func.dice?.let {
            value += func.rollDice()
        }

        return value
    }
}