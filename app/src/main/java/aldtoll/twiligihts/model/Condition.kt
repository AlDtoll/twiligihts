package aldtoll.twiligihts.model

import aldtoll.twiligihts.model.Condition.Parameter
import aldtoll.twiligihts.model.effects.Effect

data class Condition(
    //todo тоже сделать функцией
    val value: Int = 0,
    //todo передлать на source
    val target: Effect.EffectTarget = Effect.EffectTarget.HERO,
    val parameter: Parameter = Parameter.HP,
    /**
     * используется только с [Parameter.STATUS]
     * todo сделать sealed class
     */
    val name: String? = null,
    val symbol: Symbol = Symbol.LESS,
    /**
     * используется только с [Parameter.STOCK]
     */
    val gemType: Int? = null,
) {

    @Suppress("unused")
    constructor() : this(0)

    enum class Parameter {
        HP,

        /**
         * здоровье в процентах
         */
        HP_P,
        SP,

        /**
         * статус. Используется вместе с [Condition.name]
         */
        STATUS,

        /**
         * количество ходов от начала боя
         */
        TURN,

        //todo сделать количество ходов между, если перезарядка не подойдет
        /**
         * количество ударов, которые персонаж получил за ход (которые нанесли повреждения)
         * сюда входят и конратаки
         */
        HITS,

        /**
         * сколько касаний было - удары, которые были нанесены, нанесли урон или были заблокированы
         */
        TOUCHES,

        /**
         * ресурс. Используется вместе с [Condition.name]
         */
        RES,

        /**
         * для цепочек эффектов. Проверка, что был задет/поврежеден предыдущим эффектом
         */
        TOUCHED,
        HIT,

        /**
         * используется с [gemType]
         * Важно! За навык сначала платится цена, потом исполняются его эффекты
         * todo не правильно работает для state - запоминает значение
         */
        STOCK,
        TIME,
        ATTEMPT
        //todo добавить сложность, рандом
    }

    enum class Symbol {
        MORE,
        LESS,
        EQUALS,
        HAVE,
        EXIST,
        EMPTY,
        NOT
    }
}