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
     * если не задать, то статус действует - сколько раундов длится
     * если задавать, то при каждом использовании уменьшается оставшее колчиство раз
     */
    var times: Int? = null,
    //todo добавить вероятность срабатывания статуса
    //todo категория для инфо статусов
) {
    @Suppress("unused")
    constructor() : this("", null, 0, EffectType.DODGE, 1)

    fun isActive(): Boolean {
        val haveTimes = if (times == null) {
            compareValue()
        } else {
            compareValue() && this.times!! > 0
        }
        if (this.duration == INFINITY) {
            return haveTimes
        }
        return this.duration > 0 && haveTimes
    }

    //todo может везде надо сделать статусы с положительным значением?
    private fun compareValue(): Boolean {
        return when (this.type.color) {
            NEUTRAL_STATUS -> this.value != 0
            else -> this.value > 0
        }
    }

    fun isInfinity(): Boolean = duration == INFINITY

    enum class EffectType(
        @DrawableRes val image: Int,
        @ColorRes val color: Int
    ) {
        /**
         * изменяет точность источника атаки
         * может быть как положительной, так и отрицательной
         */
        ACCURACY(R.drawable.ic_accuracy, NEUTRAL_STATUS),

        /**
         * изменяет шанст шанс попадания
         * может быть как положительной, так и отрицательной
         */
        EVASION(R.drawable.ic_percent, NEUTRAL_STATUS),
        DODGE(R.drawable.ic_dodge, GOOD_STATUS),

        /**
         * для [SMART_DODGE] уклонение сработает, только если урон больше
         * [Status.smartValue]
         */
        SMART_DODGE(R.drawable.ic_dodge, GOOD_STATUS),
        WEAK(R.drawable.ic_weak, BAD_STATUS),
        STRONG(R.drawable.ic_strong, GOOD_STATUS),
        CHANGE_DEFEND(R.drawable.ic_shield_plus, NEUTRAL_STATUS),
        VULNERABLE(R.drawable.ic_vul, R.color.light_red_background_color),
        ARMOR(R.drawable.ic_armor, GOOD_STATUS),

        /**
         * действуют при атаке персонажа
         * [HARM] - усиляется только уязвивымыми статусами атакующего
         * [COUNTERATTACK] - усиляется и статусами владельца
         * todo сейчас работают одинаково
         */
        COUNTERATTACK(R.drawable.ic_counterattack, GOOD_STATUS),
        HARM(R.drawable.ic_spikes, GOOD_STATUS),

        /**
         * маркерный статус
         * может быть использован
         * для выполнения условия [Condition.Parameter.STATUS]
         */
        @Suppress("unused")
        INFO(R.drawable.ic_info, NEUTRAL_STATUS),

        /**
         * статус наносящий вред персонажу игнорирует механики атаки, т.е. броню и т.д.
         * [DAMAGE_HP] - наносит урон напрямую здоровью
         * [DAMAGE] - наносит урон щитам, потом здоровью
         */
        DAMAGE(R.drawable.ic_damage, BAD_STATUS),
        DAMAGE_HP(R.drawable.ic_damage, BAD_STATUS),

        /**
         * статус восстанавливающий здоровье
         */
        HEAL(R.drawable.ic_heal, GOOD_STATUS),

        /**
         * статус генерирующий очки
         * нужен [Status.gemType] или [Status.gemTypes], чтобы указать какие очки генерировать
         * при этом после сработает обновление очков в конце хода - надо это учитывать
         * и по дефолту больше очков в два раза давать
         */
        GENERATE(R.drawable.ic_generate, NEUTRAL_STATUS),

        /**
         * статус генерирующий очки щита
         */
        DEFEND(R.drawable.ic_shield, GOOD_STATUS),

        /**
         * нанося урон, будет восстанавливаться здоровье
         */
        VAMP(R.drawable.ic_vamp, GOOD_STATUS),

        /**
         * этот статус не позволяет совершать действия
         */
        STUN(R.drawable.ic_stuned, BAD_STATUS),

        /**
         * изменяет количество очков, получаемое персонажем
         */
        CHANGE_STOCK(R.drawable.ic_generate, NEUTRAL_STATUS),
    }

    companion object {
        private const val INFINITY = -1

        @ColorRes
        val BAD_STATUS = R.color.light_red_background_color

        @ColorRes
        val NEUTRAL_STATUS = R.color.light_blue_background_color

        @ColorRes
        val GOOD_STATUS = R.color.light_green_background_color
    }

    fun decreaseValue() {
        if (this.value > 0) {
            this.value = this.value - 1
        }
    }

    /**
     * если у статуса есть количество приминений - снижаем их количество
     */
    fun decreaseTimes() {
        if (this.times != null) {
            if (this.times!! > 0) {
                this.times = this.times!! - 1
            }
        }
    }

}
