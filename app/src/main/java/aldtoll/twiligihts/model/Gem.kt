package aldtoll.twiligihts.model

import aldtoll.twiligihts.R
import androidx.annotation.ColorRes
import kotlin.random.Random

data class Gem(
    val type: Int,
    val bonusType: Int = type,
    var half: Boolean = false
) {

    @ColorRes
    fun getGemColor(): Int {
        return getColor(type)
    }

    @ColorRes
    fun getGemBonusColor(): Int {
        return getColor(bonusType)
    }

    fun getIconUri(): String {
        return getIconUri(type)
    }

    companion object {

        @ColorRes
        fun getColor(gemType: Int): Int {
            return when (gemType) {
                1 -> R.color.gem_color_1
                2 -> R.color.gem_color_2
                3 -> R.color.gem_color_3
                4 -> R.color.gem_color_4
                5 -> R.color.gem_color_5
                6 -> R.color.gem_color_6
                7 -> R.color.gem_color_7
                8 -> R.color.gem_color_8
                9 -> R.color.gem_color_9
                10 -> R.color.gem_color_10
                11 -> R.color.gem_color_11
                else -> R.color.default_color
            }
        }

        var GEM_TYPE_NUMBER = 5
        var GEM_FULL_VALUE = 10
        var GEM_HALF_PROBABILITY = 10
        var GEM_BONUS_VALUE = 2
        var GEM_BONUS_PROBABILITY = 10

        fun generateNewGem(): Gem {
            val nextInt = Random.nextInt(0, 101)
            val gem = if (nextInt > GEM_BONUS_PROBABILITY) {
                Gem(Random.nextInt(1, GEM_TYPE_NUMBER))
            } else {
                Gem(Random.nextInt(1, GEM_TYPE_NUMBER), Random.nextInt(1, GEM_TYPE_NUMBER))
            }
            val nextInt1 = Random.nextInt(0, 101)
            if (nextInt1 < GEM_HALF_PROBABILITY) {
                gem.half = true
            }
            return gem
        }

        var GEM_MAP = hashMapOf<String, String>()

        fun getIconUri(gemType: Int): String {
            if (gemType == 0) {
                return ""
            }
            return GEM_MAP[(gemType - 1).toString()] ?: ""
        }

    }
}
