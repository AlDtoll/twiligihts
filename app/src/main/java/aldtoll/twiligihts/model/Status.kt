package aldtoll.twiligihts.model

import aldtoll.twiligihts.model.Status.Companion.INFINITY
import aldtoll.twiligihts.model.Status.EffectType

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
     * используется вместе с [EffectType.SMART_DODGE]
     */
    val smartValue: Int? = null
) {
    @Suppress("unused")
    constructor() : this("", null, 0, EffectType.DODGE, 1)

    fun isActive(): Boolean {
        if (this.duration == INFINITY) {
            return this.value > 0
        }
        return this.duration > 0 && this.value > 0
    }

    fun isInfinity(): Boolean = duration == INFINITY

    enum class EffectType {
        /**
         * меняют значение после действия
         * для [SMART_DODGE] уклонение сработает, только если урон больше
         * [Status.smartValue]
         */
        DODGE,
        SMART_DODGE,
        GAIN,
        REDUCE,

        /**
         * действуют в течении всего раудна
         */
        WEAK,
        STRONG,
        VULNERABLE,
        ARMOR,

        /**
         * действуют при атаке персонажа
         * [HARM] - усиляется только уязвивымыми статусами атакующего
         * [COUNTERATTACK] - усиляется и статусами владельца
         * todo сейчас работают одинаково
         */
        COUNTERATTACK,
        HARM,

        /**
         * маркерный статус
         * может быть использован
         * для выполнения условия [Condition.Parameter.STATUS]
         */
        @Suppress("unused")
        INFO,

        /**
         * статус наносящий вред персонажу
         * игнорирует механики атаки, т.е. броню и т.д.
         */
        DAMAGE,

        /**
         * статус восстанавливающий здоровье
         */
        HEAL,

        /**
         * статус генерирующий очки
         * нуэен [Status.gemType], чтобы указать какие очки генерировать
         */
        GENERATE
    }

    companion object {
        private const val INFINITY = -1
    }

    fun decreaseValue() {
        if (this.value > 0) {
            this.value = this.value - 1
        }
    }

}
