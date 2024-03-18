package aldtoll.twiligihts.model

sealed class Effect(
    open val name: EffectName = EffectName.ATTACK,
    open val target: EffectTarget = EffectTarget.HERO,
    open val condition: Condition? = null,
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
        constructor() : this(0)

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

    data class ChangeStock(
        var value: Int,
        val gemType: Int,
        override val name: EffectName = EffectName.CHANGE_STOCK,
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
    ) : Effect() {

        @Suppress("unused")
        constructor() : this(0)

        override fun copyEffect(): Effect = copy()
    }

    data class FinishBattle(
        override val name: EffectName = EffectName.FINISH,
        val ask: Boolean = false,
        override val target: EffectTarget = EffectTarget.HERO,
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
        CHANGE_STOCK,
        HEAL,

        /**
         * отступление, либо сюжетное действие
         */
        FINISH,
        //todo добавить INFO действие
    }

    fun getDescription(): String {
        return when (this) {
            is Attack -> {
                "Наносит ${value} урона"
            }

            is ChangeStock -> {
                "Дает ${value} очков"
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
        }
    }
}