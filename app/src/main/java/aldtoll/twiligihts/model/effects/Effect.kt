package aldtoll.twiligihts.model.effects

import aldtoll.twiligihts.model.Condition
import aldtoll.twiligihts.model.Condition.Parameter
import aldtoll.twiligihts.model.Gem
import aldtoll.twiligihts.model.Status
import com.google.firebase.database.Exclude
import kotlin.math.absoluteValue
import kotlin.random.Random

sealed class Effect(
    open val name: EffectName = EffectName.ATTACK,
    open val target: EffectTarget = EffectTarget.HERO,
    open val conditions: ArrayList<Condition> = arrayListOf(),
    /**
     * есть смысл использовать вероятность для схваток, либо каких-то побочных эффектов
     * todo нужно добавить сообщение для успеха
     */
    open val probability: Int = 100,
    open val pFunc: Func? = null,
    /**
     * планируется использовать для инфо эффектов, чтобы оживить бой
     * todo плохо работает вместе с вероятностью, т.к. заряд тратися, а эффекта не было
     */
    open val charges: Int? = null,
    var currentCharges: Int? = charges,
    open val repeats: Int = 1,
    /**
     * функция для повтора
     * будет добавлена к статичному количеству повторов, а он 1 по умолчанию
     */
    open val rFunc: Func? = null,
    @get:Exclude open var additionalEffects: ArrayList<Effect> = arrayListOf(),
    /**
     * когда должны сработать дополнительные эффекты?
     * на касание или повреждение
     */
    open val successType: SuccessType = SuccessType.ANY,
    /**
     * действие эффекта кроме прямого значения [value]
     * будет еще какой-то функцией от параметра или со случайным значеним кубика
     */
    open val func: Func? = null,
    open var value: Int = 0
) {

    enum class SuccessType {
        TOUCH,
        HIT,
        ANY,

        /**
         * если не сработал основной эффект из-за вероятности
         */
        FAIL
    }

    @Suppress("unused")
    constructor() : this(EffectName.ATTACK, EffectTarget.HERO)

    abstract fun copyEffect(): Effect
    data class Attack(
        override var value: Int,
        val type: Type = Type.BOTH,
        override val name: EffectName = EffectName.ATTACK,
        override val target: EffectTarget = EffectTarget.FOE,
        override val conditions: ArrayList<Condition> = arrayListOf(),
        /**
         * игнорирует усиления и ослабления атак, которые есть на атакующем персонаже
         * нужно например для действий "помощников"
         */
        val help: Boolean = false,
        val ignoreCounterAttacks: Boolean = false,
        /**
         * игнорирует уклонение на цели
         * нужно скорее, чтобы отразить какое-то действие,
         * которое ухудшает позицию или защиту, чем реальное свойство атаки
         */
        val ignoreDodge: Boolean = false,
        /**
         * игнорирует "броню" на цели атаки
         * нужно для атак ядом, магией и т.д.
         */
        val ignoreArmor: Boolean = false,
        /**
         * игнорирует "уязвимости" на цели атаки
         * нужно для атак ядом, магией и т.д.
         */
        val ignoreVul: Boolean = false,
        /**
         * игнорирует "уклонение" на цели атаки
         * нужно для атак ядом, магией и т.д.
         */
        val ignoreEvasion: Boolean = false,
        val ignoreStrong: Boolean = false,
        val ignoreWeak: Boolean = false,
        val ignoreAcc: Boolean = false,
        //todo ignoreStun
        override val probability: Int = 100,
        override val pFunc: Func? = null,
        override val charges: Int? = null,
        override val repeats: Int = 1,
        override val rFunc: Func? = null,
        //todo сделать опцию, чтобы дополнительные эффекты не давали касаний
        //todo сделать опцию, чтобы не получали эффектов статусов?
        @get:Exclude override var additionalEffects: ArrayList<Effect> = arrayListOf(),
        override val successType: SuccessType = SuccessType.HIT,
        override val func: Func? = null
    ) : Effect() {

        @Suppress("unused")
        constructor() : this(0, Type.BOTH, EffectName.ATTACK)

        enum class Type {
            BOTH,

            /**
             * это урон мимо щитов, но он корректируется статусами брони и уязвимости
             */
            HP, SP
        }

        override fun copyEffect(): Effect = copy()
    }

    data class Defend(
        override var value: Int,
        override val name: EffectName = EffectName.DEFEND,
        override val target: EffectTarget = EffectTarget.SELF,
        override val conditions: ArrayList<Condition> = arrayListOf(),
        val type: Type = Type.CHANGE,
        override val probability: Int = 100,
        override val pFunc: Func? = null,
        override val charges: Int? = null,
        override val repeats: Int = 1,
        override val rFunc: Func? = null,
        override val func: Func? = null
        //todo добавить ignoreChange
    ) : Effect() {

        @Suppress("unused")
        constructor() : this(0)

        override fun copyEffect(): Effect = copy()
        enum class Type {
            CHANGE, SET
        }

    }

    data class EditStatus(
        val status: Status = Status(),
        val type: Type = Type.SET,
        override val name: EffectName = EffectName.EDIT_STATUS,
        //todo не работает здесь, разрешается в fillHands
        override var target: EffectTarget =
            when (status.type.color) {
                Status.GOOD_STATUS -> EffectTarget.SELF
                Status.BAD_STATUS -> EffectTarget.FOE
                else -> {
                    if (status.value > 0) {
                        EffectTarget.SELF
                    } else {
                        EffectTarget.FOE
                    }
                }
            },
        override val conditions: ArrayList<Condition> = arrayListOf(),
        override val probability: Int = 100,
        override val pFunc: Func? = null,
        override val charges: Int? = null,
        override val repeats: Int = 1,
        override val rFunc: Func? = null,
        //todo не работает, т.е. берется значение из статуса
        override val func: Func? = null
    ) : Effect() {

        @Suppress("unused")
        constructor() : this(Status(), Type.SET)

        //todo напрашивается DURATION
        enum class Type {
            SET,
            CHANGE,

            /**
             * используется для статусов, которые уменьша.т при действиях
             * todo может имело смысл оставить CHANGE, просто менять для таких статусов не value а times
             */
            TIMES,
        }

        override fun copyEffect(): Effect = copy()
    }

    data class EditStock(
        override var value: Int,
        val gemTypes: ArrayList<Int> = arrayListOf(),
        override val name: EffectName = EffectName.EDIT_STOCK,
        val type: Type = Type.CHANGE,
        override val target: EffectTarget = EffectTarget.HERO,
        override val conditions: ArrayList<Condition> = arrayListOf(),
        override val probability: Int = 100,
        override val pFunc: Func? = null,
        override val charges: Int? = null,
        override val repeats: Int = 1,
        override val rFunc: Func? = null,
        override val func: Func? = null
    ) : Effect() {

        enum class Type {
            SET,
            CHANGE,
            ADD,
            REMOVE
        }

        @Suppress("unused")
        constructor() : this(0, arrayListOf())

        override fun copyEffect(): Effect = copy()
    }

    data class EditResources(
        override var value: Int,
        val resName: String = "",
        val type: Type = Type.CHANGE,
        override val name: EffectName = EffectName.EDIT_RES,
        override val target: EffectTarget = EffectTarget.HERO,
        override val conditions: ArrayList<Condition> = arrayListOf(),
        override val probability: Int = 100,
        override val pFunc: Func? = null,
        override val charges: Int? = null,
        override val repeats: Int = 1,
        override val rFunc: Func? = null,
        override val func: Func? = null
    ) : Effect() {

        enum class Type {
            SET, CHANGE
        }

        @Suppress("unused")
        constructor() : this(0)

        override fun copyEffect(): Effect = copy()
    }

    /**
     * может быть отрицательным. Зачем? тогда на него не срабатывают статусы, как на атаку
     */
    data class Heal(
        override var value: Int,
        override val name: EffectName = EffectName.HEAL,
        override val target: EffectTarget = EffectTarget.SELF,
        override val conditions: ArrayList<Condition> = arrayListOf(),
        val type: Type = Type.CHANGE,
        override val probability: Int = 100,
        override val pFunc: Func? = null,
        override val charges: Int? = null,
        override val repeats: Int = 1,
        override val rFunc: Func? = null,
        override val func: Func? = null
    ) : Effect() {

        enum class Type {
            CHANGE, SET
        }

        @Suppress("unused")
        constructor() : this(0)

        override fun copyEffect(): Effect = copy()
    }

    data class FinishBattle(
        override val name: EffectName = EffectName.FINISH,
        //todo разделить показ и подтверждение
        val ask: Boolean = false,
        override val target: EffectTarget = EffectTarget.HERO,
        override val conditions: ArrayList<Condition> = arrayListOf(),
        override val probability: Int = 100,
        override val pFunc: Func? = null,
        override val charges: Int? = null
    ) : Effect() {

        @Suppress("unused")
        constructor() : this(EffectName.FINISH)

        override fun copyEffect(): Effect = copy()
    }

    data class Info(
        override val name: EffectName = EffectName.INFO,
        override val target: EffectTarget = EffectTarget.SELF,
        val title: String? = null,
        val message: String? = null,
        override val conditions: ArrayList<Condition> = arrayListOf(),
        override val probability: Int = 100,
        override val pFunc: Func? = null,
        override val charges: Int? = null,
        override val repeats: Int = 1,
        override val rFunc: Func? = null,
        override val func: Func? = null,
        @get:Exclude override var additionalEffects: ArrayList<Effect> = arrayListOf(),
        override val successType: SuccessType = SuccessType.ANY,
    ) : Effect() {

        @Suppress("unused")
        constructor() : this(EffectName.FINISH)

        override fun copyEffect(): Effect = copy()
    }

    enum class EffectTarget {
        ENEMY,
        HERO,
        ALL,

        /**
         * на себя
         *  от героя на героя
         *  от врага на врага
         */
        SELF,

        /**
         * противоположная сторона
         * от героя во врага
         * от врага в героя
         */
        FOE
    }

    enum class Source {
        ENEMY,
        HERO,

        /**
         * на себя
         *  от героя на героя
         *  от врага на врага
         */
        SELF,

        /**
         * противоположная сторона
         * от героя во врага
         * от врага в героя
         */
        FOE
    }

    enum class EffectName {
        /**
         * атака - обычное нанесение урона
         */
        ATTACK,
        DEFEND,
        EDIT_STATUS,

        /**
         * изменение очков для действий
         */
        EDIT_STOCK,
        HEAL,

        /**
         * отступление, либо сюжетное действие
         */
        FINISH,
        INFO,
        EDIT_RES,
    }

    data class Func(
        @Deprecated("Use segments ")
        val parameter: Parameter? = null,
        @Deprecated("Use segments")
        val mulP: Float = 1f,
        @Deprecated("Use segments")
        val source: Source = Source.SELF,
        /**
         * должен быть, если [parameter]=[Condition.Parameter.STATUS]
         */
        @Deprecated("Use segments")
        val name: String? = null,
        /**
         * используется только с [Parameter.STOCK]
         */
        @Deprecated("Use segments")
        val gemType: Int? = null,
        val segments: List<Segment> = emptyList(),
        /**
         * Общий параметр для всей функции (например, dice 6 означает грани 1-6)
         */
        val dice: Int? = null
    ) {
        @Suppress("unused")
        constructor() : this(null)

        data class Segment(
            val parameter: Parameter,
            val mul: Float = 1f,
            val source: Source = Source.SELF,
            /**
             * Обязателен при [Parameter.STATUS]
             */
            val name: String? = null,
            /**
             * Обязателен при [Parameter.STOCK]
             * Важно! За навык сначала платится цена, потом исполняются его эффекты
             */
            val gemType: Int? = null
        ) {

            @Suppress("unused")
            constructor() : this(Parameter.SP)

            init {
                require(parameter != Parameter.STATUS || name != null) {
                    "Для STATUS-параметра должен быть указан statusName"
                }
                require(parameter != Parameter.STOCK || gemType != null) {
                    "Для STOCK-параметра должен быть указан gemType"
                }
            }
        }

        // Для обратной совместимости
        fun allSegments(): List<Segment> {
            val mainSegment = parameter?.let {
                Segment(
                    parameter = it,
                    mul = mulP,
                    source = source,
                    name = name,
                    gemType = if (it == Parameter.STOCK) gemType else null
                )
            }
            return listOfNotNull(mainSegment) + segments
        }

        fun rollDice(): Int {
            return if (dice != null) {
                Random.nextInt(1, dice + 1)
            } else {
                0
            }
        }
    }

    /**
     * это описание, которое будет использовано для отображение в навыке
     * (если навык не перетерт собственным описанием)
     */
    fun getDisplayDescription(): String {
        val valueForDescription = if (func == null) {
            "$value"
        } else {
            descriptionForFunc(value)
        }
        var effectDescription = when (this) {
            is Attack -> {
                val type = when (type) {
                    Attack.Type.BOTH -> ""
                    Attack.Type.HP -> ".неблокируемоего"
                    Attack.Type.SP -> " щитам"
                }
                "Наносит $valueForDescription урона $type"
            }

            is Defend -> {
                val type = when (type) {
                    Defend.Type.CHANGE -> "Дает"
                    Defend.Type.SET -> "Устанавливает"
                }
                "$type $valueForDescription щитов"
            }

            is EditStatus -> {
                val type = when (type) {
                    EditStatus.Type.SET -> if (status.value == 0) {
                        "Обнуляет"
                    } else {
                        "Устанавливает"
                    }

                    EditStatus.Type.CHANGE -> "Изменяет"
                    EditStatus.Type.TIMES -> "Меняет"
                }
                "$type статус \"${status.name}\" на ${status.value}"
            }

            is FinishBattle -> {
                "Закончить бой"
            }

            is Heal -> {
                "Дает ${value} здоровья"
            }

            is Info -> {
                ""
            }

            is EditStock -> {
                var name = ""
                gemTypes.forEach {
                    name += "${Gem.getName(it)};"
                }
                val s = when (type) {
                    EditStock.Type.SET -> "Устанавливает"
                    EditStock.Type.CHANGE -> {
                        if (value > 0) {
                            "Дает"
                        } else {
                            "Отнимает"
                        }
                    }

                    EditStock.Type.ADD -> {
                        "Дает шкалу с"
                    }

                    EditStock.Type.REMOVE -> {
                        "Забирает шкалу"
                    }
                }
                "$s ${value.absoluteValue} очков $name"
            }

            is EditResources -> {
                val s = if (type == EditResources.Type.CHANGE) {
                    if (value > 0) {
                        "Дает"
                    } else {
                        "Отнимает"
                    }
                } else {
                    "Устанавливает"
                }
                "$s ${value.absoluteValue} $resName"
            }
        }
        val target = if (this is Info) {
            ""
        } else {
            when (target) {
                EffectTarget.ENEMY -> "врагу"
                EffectTarget.HERO -> "герою"
                EffectTarget.ALL -> "всем"
                EffectTarget.SELF -> "себе"
                EffectTarget.FOE -> "противнику"
            }
        }
        effectDescription = "$effectDescription $target"
        //todo функция может зависеть от повторений
        if (effectDescription.isNotBlank() && probability < 100) {
            effectDescription = "$effectDescription $probability%"
        }
        if (effectDescription.isNotBlank() && conditions.isNotEmpty()) {
            effectDescription = "$effectDescription.*"
        }
        if (additionalEffects.isNotEmpty()) {
            effectDescription += "\n"
            additionalEffects.forEach {
                effectDescription += when (it.successType) {
                    SuccessType.TOUCH -> "При касании:"
                    SuccessType.HIT -> "При повреждении:"
                    SuccessType.ANY -> "Дополнительные эффекты:"
                    SuccessType.FAIL -> "При неудаче:"
                }
                effectDescription += "${it.getDisplayDescription()}\n"
            }
            effectDescription += "\n"
        }
        return effectDescription
    }

    /**
     * это описание, которое будет использовано для отображение в логе
     * todo работает только для атак
     */
    fun getLogDescription(prefix: String = ""): String {
        val valueForDescription = if (func == null) {
            "$value"
        } else {
            descriptionForFunc(value)
        }
        var effectDescription = when (this) {
            is Attack -> {
                val type = when (type) {
                    Attack.Type.BOTH -> ""
                    Attack.Type.HP -> ".неблокируемоего"
                    Attack.Type.SP -> " щитам"
                }
                val s = if (prefix.isNotBlank()) {
                    prefix + "=${valueForDescription}"
                } else {
                    valueForDescription
                }
                "Наносит ${s} урона $type"
            }


            is Defend -> {
                val type = when (type) {
                    Defend.Type.CHANGE -> "Дает"
                    Defend.Type.SET -> "Устанавливает"
                }
                "$type ${value} щитов"
            }

            is EditStatus -> {
                val type = when (type) {
                    EditStatus.Type.SET -> if (status.value == 0) {
                        "Обнуляет"
                    } else {
                        "Устанавливает"
                    }

                    EditStatus.Type.CHANGE -> "Изменяет"
                    EditStatus.Type.TIMES -> "Меняет"
                }
                "$type статус \"${status.name}\" на ${status.value}"
            }

            is FinishBattle -> {
                "Закончить бой"
            }

            is Heal -> {
                "Дает ${value} здоровья"
            }

            is Info -> {
                ""
            }

            is EditStock -> {
                var name = ""
                gemTypes.forEach {
                    name += "${Gem.getName(it)};"
                }
                val s = when (type) {
                    EditStock.Type.SET -> "Устанавливает"
                    EditStock.Type.CHANGE -> {
                        if (value > 0) {
                            "Дает"
                        } else {
                            "Отнимает"
                        }
                    }

                    EditStock.Type.ADD -> {
                        "Дает шкалу с"
                    }

                    EditStock.Type.REMOVE -> {
                        "Забирает шкалу"
                    }
                }
                "$s ${value.absoluteValue} очков $name"
            }

            is EditResources -> {
                val s = if (type == EditResources.Type.CHANGE) {
                    if (value > 0) {
                        "Дает"
                    } else {
                        "Отнимает"
                    }
                } else {
                    "Устанавливает"
                }
                "$s ${value.absoluteValue} $resName"
            }
        }
        val target = if (this is Info) {
            ""
        } else {
            when (target) {
                EffectTarget.ENEMY -> "противнику"
                EffectTarget.HERO -> "герою"
                EffectTarget.ALL -> "всем"
                EffectTarget.SELF -> "себе"
                EffectTarget.FOE -> "противнику"
            }
        }
        effectDescription = "$effectDescription $target"
        if (effectDescription.isNotBlank() && probability < 100) {
            effectDescription = "$effectDescription $probability%"
        }
        if (effectDescription.isNotBlank() && conditions.isNotEmpty()) {
            effectDescription = "$effectDescription.*"
        }
        return effectDescription
    }

    private fun descriptionForFunc(value: Int): String {
        val function = func!!
        var description = "$value"
        function.dice?.let {
            description = "${value}-${value + it}"
        }
        function.allSegments().forEach { segment ->
            val statusName = if (segment.name != null) {
                "(${segment.name})"
            } else {
                ""
            }
            val prefix = if (description.isEmpty() || description == "0") {
                ""
            } else {
                " и "
            }
            val mul = if (segment.mul == 1f) {
                ""
            } else {
                "${segment.mul}*"
            }
            description += "$prefix$mul${segment.parameter}$statusName:${segment.source}"
        }
        return description
    }

    fun decreaseCharges() {
        if (this.currentCharges != null) {
            if (this.currentCharges!! > 0) {
                this.currentCharges = this.currentCharges!! - 1
            }
        }
    }

    fun init() {
        currentCharges = charges
    }
}