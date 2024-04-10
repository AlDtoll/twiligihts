package aldtoll.twiligihts.model

sealed class Effect(
    open val name: EffectName = EffectName.ATTACK,
    open val target: EffectTarget = EffectTarget.HERO,
    open val condition: Condition? = null,
    //todo random
    //todo charges?
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
        val type: Type = Type.CHANGE
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
    ) : Effect() {

        enum class Type {
            SET,
            CHANGE
        }

        @Suppress("unused")
        constructor() : this(0, 0)

        override fun copyEffect(): Effect = copy()
    }

    data class ChangeStock(
        var value: Int,
        val gemType: Int,
        val gemTypes: ArrayList<Int> = arrayListOf(),
        override val name: EffectName = EffectName.CHANGE_STOCK,
        override val target: EffectTarget = EffectTarget.HERO,
        override val condition: Condition? = null,
    ) : Effect() {

        @Suppress("unused")
        constructor() : this(0, 0)

        override fun copyEffect(): Effect = copy()
    }

    data class SetStock(
        var value: Int,
        val gemType: Int,
        val gemTypes: ArrayList<Int> = arrayListOf(),
        override val name: EffectName = EffectName.SET_STOCK,
        override val target: EffectTarget = EffectTarget.HERO,
        override val condition: Condition? = null,
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
        val type: Type = Type.CHANGE
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
        INFO
    }

    fun getDescription(): String {
        val effectDescription = when (this) {
            is Attack -> {
                "Наносит ${value} урона" + if (type == Attack.Type.HP) {
                    ". неблокируемоего"
                } else {
                    ""
                }
            }

            is ChangeStock -> {
                var name = Gem.getName(gemType)
                gemTypes.forEach {
                    name += "${Gem.getName(it)};"
                }
                "Дает ${value} очков $name"
            }

            is SetStock -> {
                var name = Gem.getName(gemType)
                gemTypes.forEach {
                    name += "${Gem.getName(it)};"
                }
                "Устанавливает ${value} очков $name"
            }

            is Defend -> {
                "Дает ${value} щитов"
            }

            is EditStatus -> {
                //todo порабоать
                "Дает статус ${status.name}"
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
                val s = if (type == EditStock.Type.CHANGE) {
                    "Дает"
                } else {
                    "Устанавливает"
                }
                "$s ${value} очков $name"
            }
        }
        if (condition != null) {
            return "$effectDescription.*"
        }
        return effectDescription
    }
}