package aldtoll.twiligihts.model

import kotlin.math.absoluteValue

sealed class Effect(
    open val name: EffectName = EffectName.ATTACK,
    open val target: EffectTarget = EffectTarget.HERO,
    open val condition: Condition? = null,
    /**
     * есть смысл использовать вероятность для схваток, либо каких-то побочных эффектов
     * todo нужно добавить сообщение для успеха
     */
    open val probability: Int = 100,
    /**
     * планируется использовать для инфо эффектов, чтобы оживить бой
     */
    open val charges: Int? = null,
    var currentCharges: Int? = charges,
    //todo нужна привязка, что предыдущий эффект сработал
) {

    @Suppress("unused")
    constructor() : this(EffectName.ATTACK, EffectTarget.HERO)

    abstract fun copyEffect(): Effect
    data class Attack(
        var value: Int,
        val type: Type = Type.BOTH,
        override val name: EffectName = EffectName.ATTACK,
        override val target: EffectTarget = EffectTarget.HERO,
        override val condition: Condition? = null,
        /**
         * игнорирует усиления и ослабления атак, которые есть на персонаже
         * нужно например для действий "помощников"
         */
        val ignoreStatusesAndCounterAttacks: Boolean = false,
        override val probability: Int = 100,
    ) : Effect() {

        @Suppress("unused")
        constructor() : this(0, Type.BOTH, EffectName.ATTACK, EffectTarget.ENEMY)

        enum class Type {
            BOTH,

            //todo нужно, чтобы был прямой урон здоровью еще
            HP,
            SP
        }

        override fun copyEffect(): Effect = copy()
    }

    data class Defend(
        var value: Int,
        override val name: EffectName = EffectName.DEFEND,
        override val target: EffectTarget = EffectTarget.HERO,
        override val condition: Condition? = null,
        val type: Type = Type.CHANGE,
        override val probability: Int = 100,
    ) : Effect() {

        @Suppress("unused")
        constructor() : this(0)

        override fun copyEffect(): Effect = copy()
        enum class Type {
            CHANGE,
            SET
        }

    }

    data class EditStatus(
        val status: Status,
        val type: Type = Type.SET,
        override val name: EffectName = EffectName.EDIT_STATUS,
        override val target: EffectTarget = EffectTarget.HERO,
        override val condition: Condition? = null,
        override val probability: Int = 100,
    ) : Effect() {

        @Suppress("unused")
        constructor() : this(Status())

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
        var value: Int,
        val gemType: Int,
        val gemTypes: ArrayList<Int> = arrayListOf(),
        override val name: EffectName = EffectName.EDIT_STOCK,
        val type: Type = Type.CHANGE,
        override val target: EffectTarget = EffectTarget.HERO,
        override val condition: Condition? = null,
        override val probability: Int = 100,
    ) : Effect() {

        enum class Type {
            SET,
            CHANGE,
            ADD,
            REMOVE
        }

        @Suppress("unused")
        constructor() : this(0, 0)

        override fun copyEffect(): Effect = copy()
    }

    data class EditResources(
        var value: Int,
        val resName: String = "",
        val type: Type = Type.CHANGE,
        override val name: EffectName = EffectName.EDIT_RES,
        override val target: EffectTarget = EffectTarget.HERO,
        override val condition: Condition? = null,
        override val probability: Int = 100,
    ) : Effect() {

        enum class Type {
            SET,
            CHANGE
        }

        @Suppress("unused")
        constructor() : this(0)

        override fun copyEffect(): Effect = copy()
    }

    @Deprecated("use EditStock")
    data class ChangeStock(
        var value: Int,
        val gemType: Int,
        val gemTypes: ArrayList<Int> = arrayListOf(),
        override val name: EffectName = EffectName.CHANGE_STOCK,
        override val target: EffectTarget = EffectTarget.HERO,
        override val condition: Condition? = null,
        override val probability: Int = 100,
    ) : Effect() {

        @Suppress("unused")
        constructor() : this(0, 0)

        override fun copyEffect(): Effect = copy()
    }

    @Deprecated("use EditStock")
    data class SetStock(
        var value: Int,
        val gemType: Int,
        val gemTypes: ArrayList<Int> = arrayListOf(),
        override val name: EffectName = EffectName.SET_STOCK,
        override val target: EffectTarget = EffectTarget.HERO,
        override val condition: Condition? = null,
        override val probability: Int = 100,
    ) : Effect() {

        @Suppress("unused")
        constructor() : this(0, 0)

        override fun copyEffect(): Effect = copy()
    }

    data class Heal(
        var value: Int,
        override val name: EffectName = EffectName.HEAL,
        override val target: EffectTarget = EffectTarget.HERO,
        override val condition: Condition? = null,
        val type: Type = Type.CHANGE,
        override val probability: Int = 100,
    ) : Effect() {

        enum class Type {
            CHANGE,
            SET
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
        override val condition: Condition? = null,
        override val probability: Int = 100,
    ) : Effect() {

        @Suppress("unused")
        constructor() : this(EffectName.FINISH)

        override fun copyEffect(): Effect = copy()
    }

    data class Info(
        override val name: EffectName = EffectName.INFO,
        override val target: EffectTarget = EffectTarget.HERO,
        val message: String? = null,
        override val condition: Condition? = null,
        override val probability: Int = 100,
    ) : Effect() {

        @Suppress("unused")
        constructor() : this(EffectName.FINISH)

        override fun copyEffect(): Effect = copy()
    }

    enum class EffectTarget {
        ENEMY,
        HERO,
        ALL
    }

    enum class EffectName {
        ATTACK,
        DEFEND,
        EDIT_STATUS,

        //надо EDIT
        EDIT_STOCK,
        CHANGE_STOCK,
        SET_STOCK,
        HEAL,

        /**
         * отступление, либо сюжетное действие
         */
        FINISH,
        INFO,
        EDIT_RES,
    }

    fun getDescription(): String {
        var effectDescription = when (this) {
            is Attack -> {
                val type = when (type) {
                    Attack.Type.BOTH -> ""
                    Attack.Type.HP -> ".неблокируемоего"
                    Attack.Type.SP -> " щитам"
                }
                "Наносит ${value} урона $type"
            }

            is ChangeStock -> {
                var name = Gem.getName(gemType)
                gemTypes.forEach {
                    name += "${Gem.getName(it)};"
                }
                val prefix = if (value > 0) {
                    "Дает"
                } else {
                    "Отнимает"
                }
                "$prefix ${value.absoluteValue} очков $name"
            }

            is SetStock -> {
                var name = Gem.getName(gemType)
                gemTypes.forEach {
                    name += "${Gem.getName(it)};"
                }
                "Устанавливает ${value} очков $name"
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
                "$type статус \"${status.name}\""
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
                var name = Gem.getName(gemType)
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
            }
        }
        effectDescription = "$effectDescription $target"
        if (effectDescription.isNotBlank() && probability < 100) {
            effectDescription = "$effectDescription $probability%"
        }
        if (effectDescription.isNotBlank() && condition != null) {
            return "$effectDescription.*"
        }
        return effectDescription
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