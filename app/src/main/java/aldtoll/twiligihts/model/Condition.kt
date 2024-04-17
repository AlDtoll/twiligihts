package aldtoll.twiligihts.model

import aldtoll.twiligihts.model.Condition.Parameter
import aldtoll.twiligihts.model.characters.Enemy
import aldtoll.twiligihts.model.characters.Hero
import aldtoll.twiligihts.storage.TurnNumberInteractor

data class Condition(
    val value: Int = 0,
    val target: Effect.EffectTarget = Effect.EffectTarget.HERO,
    val parameter: Parameter = Parameter.HP,
    /**
     * используется только с [Parameter.STATUS]
     * todo сделать sealed class
     */
    val name: String? = null,
    val symbol: Symbol = Symbol.LESS
) {

    @Suppress("unused")
    constructor() : this(0)

    enum class Parameter {
        HP,

        /**
         *
         */
        HP_P,
        SP,
        STATUS,

        /**
         * количество ходов от начала боя
         */
        TURN,

        //todo сделать количество ходов между, если перезарядка не подойдет
        //todo сделать проверку очков
        /**
         * количество ударов, которые персонаж получил за ход (которые нанесли повреждения)
         */
        HITS,

        /**
         * сколько касаний было - удары, которые были нанесены, нанесли урон или были заблокированы
         */
        TOUCHES
    }

    enum class Symbol {
        MORE,
        LESS,
        EQUALS,
        HAVE,
        EMPTY,
    }

    fun checkConditionIsMet(
        enemy: Enemy,
        hero: Hero,
        turnNumberInteractor: TurnNumberInteractor
    ): Boolean {
        return when (this.target) {
            Effect.EffectTarget.ENEMY -> {
                return enemy.checkConditionForPerson(this, turnNumberInteractor)
            }

            Effect.EffectTarget.HERO -> hero.checkConditionForPerson(this, turnNumberInteractor)
            Effect.EffectTarget.ALL -> {
                return enemy.checkConditionForPerson(this, turnNumberInteractor)
                        && hero.checkConditionForPerson(this, turnNumberInteractor)
            }
        }
    }
}