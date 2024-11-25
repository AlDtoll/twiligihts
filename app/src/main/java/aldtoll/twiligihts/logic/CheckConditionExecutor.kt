package aldtoll.twiligihts.logic

import aldtoll.twiligihts.model.Condition
import aldtoll.twiligihts.model.Effect
import aldtoll.twiligihts.model.characters.Hero
import aldtoll.twiligihts.model.characters.Person
import aldtoll.twiligihts.storage.TimeSecondsInteractor
import aldtoll.twiligihts.storage.TurnNumberInteractor
import aldtoll.twiligihts.storage.enemy.EnemyInteractor
import aldtoll.twiligihts.storage.enemy.EnemyResourcesInteractor
import aldtoll.twiligihts.storage.enemy.EnemyStockListInteractor
import aldtoll.twiligihts.storage.hero.HeroInteractor
import aldtoll.twiligihts.storage.hero.HeroResourcesInteractor
import aldtoll.twiligihts.storage.hero.HeroStockListInteractor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CheckConditionExecutor @Inject constructor(
    private val heroInteractor: HeroInteractor,
    private val enemyInteractory: EnemyInteractor,
    private val turnNumberInteractor: TurnNumberInteractor,
    private val enemyResourcesInteractor: EnemyResourcesInteractor,
    private val heroResourcesInteractor: HeroResourcesInteractor,
    private val heroStockListInteractor: HeroStockListInteractor,
    private val enemyStockListInteractor: EnemyStockListInteractor,
    private val timeSecondsInteractor: TimeSecondsInteractor,
) {

    fun execute(condition: Condition): Boolean {
        val hero = heroInteractor.value()
        val enemy = enemyInteractory.value()
        return when (condition.target) {
            Effect.EffectTarget.ENEMY -> {
                return enemy!!.checkConditionForPerson(condition)
            }

            Effect.EffectTarget.HERO -> hero!!.checkConditionForPerson(
                condition
            )

            Effect.EffectTarget.ALL -> {
                return enemy!!.checkConditionForPerson(condition)
                        && hero!!.checkConditionForPerson(condition)
            }
        }
    }

    private fun Person.checkConditionForPerson(
        condition: Condition
    ): Boolean {
        val valueForCompare = getPersonParameter(condition)
        return when (condition.symbol) {
            /**
             * нейтральные статусы могут иметь и отрицательные значения
             * это может быть важно для условия
             */
            Condition.Symbol.MORE -> valueForCompare > condition.value
            Condition.Symbol.LESS -> valueForCompare < condition.value
            Condition.Symbol.EQUALS -> valueForCompare == condition.value
            Condition.Symbol.HAVE -> valueForCompare > 0
            Condition.Symbol.EXIST -> valueForCompare != 0
            Condition.Symbol.EMPTY -> valueForCompare == 0
        }
    }

    fun getParameter(person: Person, condition: Condition) = person.getPersonParameter(condition)

    private fun Person.getPersonParameter(condition: Condition) =
        when (condition.parameter) {
            Condition.Parameter.HP -> this.hp
            Condition.Parameter.SP -> this.shield
            Condition.Parameter.STATUS -> this.statuses.find { it.name == condition.name && it.isActive() }?.value
                ?: 0

            Condition.Parameter.TURN -> turnNumberInteractor.value() ?: 0
            Condition.Parameter.HP_P -> this.hp * 100 / this.maxHp
            Condition.Parameter.HITS -> this.hits
            Condition.Parameter.TOUCHES -> this.touches
            Condition.Parameter.RES -> {
                if (this is Hero) {
                    heroResourcesInteractor.value()?.find { it.name == condition.name }?.amount
                } else {
                    enemyResourcesInteractor.value()?.find { it.name == condition.name }?.amount
                } ?: 0
            }

            Condition.Parameter.TOUCHED -> if (this.wasTouchedByPreviousEffect) 1 else 0
            Condition.Parameter.HIT -> if (this.wasHitByPreviousEffect) 1 else 0
            Condition.Parameter.STOCK -> {
                if (this is Hero) {
                    heroStockListInteractor.value()
                        ?.find { it.gemType == condition.gemType }?.value
                        ?: 0
                } else {
                    enemyStockListInteractor.value()
                        ?.find { it.gemType == condition.gemType }?.value
                        ?: 0
                }
            }

            Condition.Parameter.TIME -> {
                timeSecondsInteractor.value() ?: 0
            }
        }
}