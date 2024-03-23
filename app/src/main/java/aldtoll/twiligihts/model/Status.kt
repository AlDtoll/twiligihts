package aldtoll.twiligihts.model

import aldtoll.twiligihts.R
import aldtoll.twiligihts.model.Status.Companion.INFINITY
import aldtoll.twiligihts.model.Status.EffectType
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes

data class Status(
    val name: String,
    val description: String? = null,
    var value: Int,
    val type: EffectType,
    /**
     * -1 будет означать бесконечность [INFINITY]
     */
    var duration: Int = 1,
    /**
     * используется вместе с [EffectType.GENERATE]
     */
    val gemType: Int? = null,
    /**
     * используется вместе с [EffectType.GENERATE]
     */
    val gemTypes: ArrayList<Int> = arrayListOf(),
    /**
     * используется вместе с [EffectType.SMART_DODGE]
     */
    val smartValue: Int? = null,
    /**
     * сколько раз статус действует
     * актуально для разовых статусов
     * [Status.EffectType.REDUCE]
     * [Status.EffectType.GAIN]
     * [Status.EffectType.DODGE]
     */
    var times: Int? = null
) {
    @Suppress("unused")
    constructor() : this("", null, 0, EffectType.DODGE, 1)

    fun isActive(): Boolean {
        val haveTimes = if (times == null) {
            this.value > 0
        } else {
            this.value > 0 && this.times!! > 0
        }
        if (this.duration == INFINITY) {
            return haveTimes
        }
        return this.duration > 0 && haveTimes
    }

    fun isInfinity(): Boolean = duration == INFINITY

    enum class EffectType(
        @DrawableRes val image: Int,
        @ColorRes val color: Int
    ) {
        /**
         * меняют значение [Status.times] после действия
         * для [SMART_DODGE] уклонение сработает, только если урон больше
         * [Status.smartValue]
         */
        DODGE(R.drawable.ic_dodge, R.color.light_green_background_color),
        SMART_DODGE(R.drawable.ic_dodge, R.color.light_green_background_color),
        GAIN(R.drawable.ic_gain, R.color.light_green_background_color),
        REDUCE(R.drawable.ic_reduce, R.color.light_red_background_color),

        /**
         * действуют в течении всего раудна
         */
        WEAK(R.drawable.ic_weak, R.color.light_red_background_color),
        STRONG(R.drawable.ic_strong, R.color.light_green_background_color),
        STRONG_DEFEND(R.drawable.ic_shield_plus, R.color.light_green_background_color),
        VULNERABLE(R.drawable.ic_vul, R.color.light_red_background_color),
        ARMOR(R.drawable.ic_armor, R.color.light_green_background_color),

        /**
         * действуют при атаке персонажа
         * [HARM] - усиляется только уязвивымыми статусами атакующего
         * [COUNTERATTACK] - усиляется и статусами владельца
         * todo сейчас работают одинаково
         */
        COUNTERATTACK(R.drawable.ic_counterattack, R.color.light_green_background_color),
        HARM(R.drawable.ic_spikes, R.color.light_green_background_color),

        /**
         * маркерный статус
         * может быть использован
         * для выполнения условия [Condition.Parameter.STATUS]
         */
        @Suppress("unused")
        INFO(R.drawable.ic_info, R.color.light_blue_background_color),

        /**
         * статус наносящий вред персонажу игнорирует механики атаки, т.е. броню и т.д.
         * [DAMAGE_HP] - наносит урон напрямую здоровью
         * [DAMAGE] - наносит урон щитам, потом здоровью
         */
        DAMAGE(R.drawable.ic_damage, R.color.light_blue_background_color),
        DAMAGE_HP(R.drawable.ic_damage, R.color.light_blue_background_color),

        /**
         * статус восстанавливающий здоровье
         */
        HEAL(R.drawable.ic_heal, R.color.light_green_background_color),

        /**
         * статус генерирующий очки
         * нуэен [Status.gemType], чтобы указать какие очки генерировать
         * при этом после сработает обновление очков в конце хода - надо это учитывать
         * и по дефолту больше очков в два раза давать
         */
        GENERATE(R.drawable.ic_generate, R.color.light_blue_background_color),

        /**
         * статус генерирующий очки щита
         */
        DEFEND(R.drawable.ic_shield, R.color.light_green_background_color),
    }

    companion object {
        private const val INFINITY = -1
    }

    fun decreaseValue() {
        if (this.value > 0) {
            this.value = this.value - 1
        }
    }

    fun decreaseTimes() {
        if (this.times != null) {
            if (this.times!! > 0) {
                this.times = this.times!! - 1
            }
        }
    }

}
