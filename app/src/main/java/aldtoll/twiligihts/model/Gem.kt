package aldtoll.twiligihts.model

import aldtoll.twiligihts.R
import androidx.annotation.ColorRes
import kotlin.random.Random

data class Gem(
    val type: Int
) {

    @ColorRes
    fun getGemColor(): Int {
        return getColor(type)
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
                else -> R.color.default_color
            }
        }

        private const val GEM_TYPE_NUMBER = 11

        fun generateNewGem(): Gem {
            return Gem(Random.nextInt(1, GEM_TYPE_NUMBER))
        }

    }
}
