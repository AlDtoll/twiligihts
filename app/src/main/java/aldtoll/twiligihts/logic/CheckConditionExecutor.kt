package aldtoll.twiligihts.logic

import aldtoll.twiligihts.model.Condition
import aldtoll.twiligihts.model.Effect
import aldtoll.twiligihts.model.characters.Person
import aldtoll.twiligihts.storage.TurnNumberInteractor
import aldtoll.twiligihts.storage.enemy.EnemyInteractor
import aldtoll.twiligihts.storage.hero.HeroInteractor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CheckConditionExecutor @Inject constructor(
    private val heroInteractor: HeroInteractor,
    private val enemyInteractory: EnemyInteractor,
    private val turnNumberInteractor: TurnNumberInteractor

) {

    fun execute(condition: Condition): Boolean {
        val hero = heroInteractor.value()
        val enemy = enemyInteractory.value()
        return when (condition.target) {
            Effect.EffectTarget.ENEMY -> {
                return enemy!!.checkConditionForPerson(condition, turnNumberInteractor)
            }

            Effect.EffectTarget.HERO -> hero!!.checkConditionForPerson(
                condition,
                turnNumberInteractor
            )

            Effect.EffectTarget.ALL -> {
                return enemy!!.checkConditionForPerson(condition, turnNumberInteractor)
                        && hero!!.checkConditionForPerson(condition, turnNumberInteractor)
            }
        }
    }

    fun Person.checkConditionForPerson(
        condition: Condition,
        turnNumberInteractor: TurnNumberInteractor
    ): Boolean {
        val valueForCompare = when (condition.parameter) {
            Condition.Parameter.HP -> this.hp
            Condition.Parameter.SP -> this.shield
            //todo почему то здесь статус оказывается зануленым
            Condition.Parameter.STATUS -> this.statuses.find { it.name == condition.name }?.value
                ?: 0

            Condition.Parameter.TURN -> turnNumberInteractor.value() ?: 0
            Condition.Parameter.HP_P -> this.hp * 100 / maxHp
            Condition.Parameter.HITS -> this.hits
            Condition.Parameter.TOUCHES -> this.touches
        }
        return when (condition.symbol) {
            Condition.Symbol.MORE -> valueForCompare > condition.value
            Condition.Symbol.LESS -> valueForCompare < condition.value
            Condition.Symbol.EQUALS -> valueForCompare == condition.value
            Condition.Symbol.HAVE -> valueForCompare > 0
            Condition.Symbol.EMPTY -> valueForCompare == 0
        }
    }
}