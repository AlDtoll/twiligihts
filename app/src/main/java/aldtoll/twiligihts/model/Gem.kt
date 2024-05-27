package aldtoll.twiligihts.model

import aldtoll.twiligihts.R
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import kotlin.random.Random

/**
 * это отображение гема на доске
 */
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
                12 -> R.color.gem_color_12
                13 -> R.color.gem_color_13
                DODGE_COLOR -> R.color.dodge_color
                LOG_COLOR -> R.color.log_color
                GRAY_LOG_COLOR -> R.color.gray_log_color
                COUNTERATTACK_COLOR -> R.color.counter_attack_color
                STATUS_COLOR -> R.color.status_color
                else -> R.color.default_color
            }
        }

        const val LOG_COLOR = -101
        const val DODGE_COLOR = -102
        const val GRAY_LOG_COLOR = -103
        const val COUNTERATTACK_COLOR = -104
        const val STATUS_COLOR = -105
        const val GEM_FULL_VALUE = 10
        const val GEM_HALF_VALUE = GEM_FULL_VALUE / 2
        const val GEM_BONUS_VALUE = 2
        const val GEM_HALF_PROBABILITY = 25
        const val GEM_BONUS_PROBABILITY = 10
        var GEM_TYPE_NUMBER = 4
        var GEM_BONUS_TYPE: Int? = null

        fun generateNewGem(): Gem {
            val numberForCompareWithBonusProbability = Random.nextInt(0, 101)
            val until = GEM_TYPE_NUMBER + 1
            val gemType = Random.nextInt(1, until)
            val gemTypeAsInSettings = gemType.toString()
            val gem =
                if (numberForCompareWithBonusProbability > (GEM_MAP[gemTypeAsInSettings]?.bonusProbability
                        ?: GEM_BONUS_PROBABILITY)
                ) {
                    Gem(gemType)
                } else {
                    val bonusType = Random.nextInt(1, until)
                    Gem(gemType, GEM_BONUS_TYPE ?: bonusType)
                }
            val numberForCompareWithHalfProbability = Random.nextInt(0, 101)
            if (numberForCompareWithHalfProbability < (GEM_MAP[gemTypeAsInSettings]?.halfProbability
                    ?: GEM_HALF_PROBABILITY)
            ) {
                gem.half = true
            }
            return gem
        }

        var GEM_MAP = hashMapOf<String, BattleSettings.GemSettings>()

        fun getIconUri(gemType: Int): String {
            if (gemType == 0) {
                return ""
            }
            return GEM_MAP[gemType.toString()]?.uri ?: ""
        }

        fun getName(gemType: Int): String {
            return when (gemType) {
                1 -> GEM_MAP[gemType.toString()]?.displayName ?: "атаки"
                2 -> GEM_MAP[gemType.toString()]?.displayName ?: "защиты"
                3 -> GEM_MAP[gemType.toString()]?.displayName ?: "движения"
                4 -> GEM_MAP[gemType.toString()]?.displayName ?: "отряда/местности"
                5 -> GEM_MAP[gemType.toString()]?.displayName ?: "концентрации/магии"
                else -> ""
            }
        }

        @DrawableRes
        fun getPlaceHolder(type: Int): Int {
            return when (type) {
                0 -> 0
                1 -> R.drawable.ic_strong
                2 -> R.drawable.ic_shield
                3 -> R.drawable.ic_dodge
                4 -> R.drawable.ic_spikes
                5 -> R.drawable.ic_info
                else -> R.drawable.ic_hand
            }
        }

    }
}
