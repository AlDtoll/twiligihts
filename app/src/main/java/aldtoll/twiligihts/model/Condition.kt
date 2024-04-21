package aldtoll.twiligihts.model

import aldtoll.twiligihts.model.Condition.Parameter

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
        TOUCHES,

        //todo RES
    }

    enum class Symbol {
        MORE,
        LESS,
        EQUALS,
        HAVE,
        EMPTY,
    }
}