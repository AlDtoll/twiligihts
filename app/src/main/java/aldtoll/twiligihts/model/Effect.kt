package aldtoll.twiligihts.model

sealed class Effect(
    open val name: EffectName = EffectName.ATTACK,
    open val target: EffectTarget = EffectTarget.HERO,
    open val condition: Condition? = null,
    /**
     * костыль, который нужен чтобы для [Enemy]
     * можно было назначать действия окружения
     * если это действие окружения,
     * то к нему не должны применяться бонусные статусы [Enemy]
     */
    open val place: Boolean = false,
) {

    @Suppress("unused")
    constructor() : this(EffectName.ATTACK, EffectTarget.HERO)

    abstract fun copyEffect(): Effect
    data class Attack(
        var value: Int,
        val type: Type = Type.BOTH,
        override val name: EffectName = EffectName.ATTACK,
        override val target: EffectTarget = EffectTarget.HERO,
        override val condition: Condition? = null, override val place: Boolean = false,
    ) : Effect() {

        @Suppress("unused")
        constructor() : this(0)

        enum class Type {
            BOTH,
            HP,
            SP
        }

        override fun copyEffect(): Effect = copy()
    }

    data class Defend(
        var value: Int,
        override val name: EffectName = EffectName.DEFEND,
        override val target: EffectTarget = EffectTarget.HERO,
        override val condition: Condition? = null, override val place: Boolean = false,
    ) : Effect() {

        @Suppress("unused")
        constructor() : this(0)

        override fun copyEffect(): Effect = copy()
    }

    data class EditStatus(
        val status: Status,
        val type: Type = Type.SET,
        override val name: EffectName = EffectName.EDIT_STATUS,
        override val target: EffectTarget = EffectTarget.HERO,
        override val condition: Condition? = null, override val place: Boolean = false,
    ) : Effect() {

        @Suppress("unused")
        constructor() : this(Status())

        //todo напрашивается DURATION
        enum class Type {
            SET,
            CHANGE
        }

        override fun copyEffect(): Effect = copy()
    }

    data class ChangeStock(
        var value: Int,
        val gemType: Int,
        override val name: EffectName = EffectName.CHANGE_STOCK,
        override val target: EffectTarget = EffectTarget.HERO,
        override val condition: Condition? = null, override val place: Boolean = false,
    ) : Effect() {

        @Suppress("unused")
        constructor() : this(0, 0)

        override fun copyEffect(): Effect = copy()
    }

    data class Heal(
        var value: Int,
        override val name: EffectName = EffectName.HEAL,
        override val target: EffectTarget = EffectTarget.HERO,
        override val condition: Condition? = null, override val place: Boolean = false,
    ) : Effect() {

        @Suppress("unused")
        constructor() : this(0)

        override fun copyEffect(): Effect = copy()
    }

    data class FinishBattle(
        override val name: EffectName = EffectName.FINISH,
        override val target: EffectTarget = EffectTarget.HERO,
        override val condition: Condition? = null, override val place: Boolean = false,
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
        CHANGE_STOCK,
        HEAL,

        /**
         * отступление, либо сюжетное действие
         */
        FINISH,
    }

}