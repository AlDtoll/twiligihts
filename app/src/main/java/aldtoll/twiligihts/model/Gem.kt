package aldtoll.twiligihts.model

import aldtoll.twiligihts.R
import androidx.annotation.ColorRes

data class Gem(
    val type: Int
) {

    @ColorRes
    fun getGemColor(): Int {
        return when (type) {
            1 -> R.color.gem_color_1
            2 -> R.color.gem_color_2
            3 -> R.color.gem_color_3
            4 -> R.color.gem_color_4
            else -> R.color.default_color
        }
    }
}
