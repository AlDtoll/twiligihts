package aldtoll.twiligihts.model

data class BattleSettings(
    val types: Int = 4,
    val gemSettings: ArrayList<GemSettings> = arrayListOf(),
    val bonusType: Int? = null,
    val stopGenerate: Boolean = false,
    val animateEnemy: Boolean = false,
    /**
     * позволяет противнику совершать действия
     * пока автоматические случайные
     */
    val makeEnemyMove: Boolean = false,
    /**
     * для новых гемов использовать уменьшенное значение
     */
    val useHalfForNewGems: Boolean = false,
    val showHeroAnimation: Boolean = false,
) {
    @Suppress("unused")
    constructor() : this(4)

    data class GemSettings(
        val type: String = "",
        val name: String = "",
        var uri: String = "",
        val fullValue: Int = Gem.GEM_FULL_VALUE,
        val halfValue: Int = Gem.GEM_HALF_VALUE,
        /**
         * вероятность, что гем данного цвета будет иметь частичное значение
         */
        val halfProbability: Int = Gem.GEM_HALF_PROBABILITY,
        /**
         * если этот цвет используется для бонуса - сколько очков он дает
         */
        val bonusValue: Int = Gem.GEM_BONUS_VALUE,
        /**
         * вероятность того, что данный гем имеет бонус. Не является им
         */
        val bonusProbability: Int = Gem.GEM_BONUS_PROBABILITY,
        /**
         * вероятность, что гем данного цвета будет иметь дополнительный цвет
         */
        val extraProbability: Int = Gem.GEM_EXTRA_PROBABILITY,
        /**
         * указать в процентах сколько очков должно сохранить после хода
         */
        val turnKeepStrategy: Int = DEFAULT_TURN_KEEP_STRATEGY,
        /**
         * указать в процентах сколько очков должно сохранить после получения урона
         */
        val damageKeepStrategy: Int = DEFAULT_DAMAGE_KEEP_STRATEGY,
        val displayName: String? = null
    )

    companion object {
        var SHOW_HERO_ANIMATION: Boolean = false
        var STOP_GENERATE = false
        var ANIMATE_ENEMY_ACTIONS = false
        var MAKE_ENEMY_MOVE = false
        const val DEFAULT_TURN_KEEP_STRATEGY = 50
        const val DEFAULT_DAMAGE_KEEP_STRATEGY = 100
        var GOD_MODE = false
    }
}
