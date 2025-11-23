package aldtoll.twiligihts.model

import aldtoll.twiligihts.R
import aldtoll.twiligihts.model.Status.Companion.INFINITY
import aldtoll.twiligihts.model.Status.StatusType
import aldtoll.twiligihts.model.effects.Effect
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.google.firebase.database.Exclude
import kotlin.random.Random

//todo одно имя статуса, но много эффектов
data class Status(
    val name: String,
    val description: String? = null,
    var value: Int,
    val type: StatusType,
    /**
     * вероятность срабатывания статуса
     * разы [times] не потратятся, если ноавык не сработал
     */
    val probability: Int = 100,
    /**
     * -1 будет означать бесконечность [INFINITY]
     */
    var duration: Int = 1,
    /**
     * используется вместе с [StatusType.GENERATE]
     */
    val gemTypes: ArrayList<Int> = arrayListOf(),
    /**
     * используется вместе с [StatusType.SMART_DODGE]
     */
    val smartValue: Int? = null,
    /**
     * сколько раз статус действует
     * если не задать, то статус действует - сколько раундов длится
     * если задавать, то при каждом использовании уменьшается оставшее колчиство раз
     * //todo сейчас срабатывает на каждый эффект, а что если на навык
     * если статус не сработал из-за вероятности [probability], то количество раз уменьшено не будет
     */
    var times: Int? = null,
    /**
     * используется вместе с [StatusType.REACTION]
     * эффект, который будет выполнен при срабатывании статуса-реакции
     */
    @get:Exclude
    val reactionEffect: Effect? = null,
    /**
     * если true, статус применяется и обновляется после хода персонажа
     * если false (по умолчанию), статус применяется и обновляется перед ходом персонажа
     */
    val end: Boolean = false,
    //todo добавить вероятность срабатывания статуса
    //todo категория для инфо статусов
) {
    @Suppress("unused")
    constructor() : this("", null, 0, StatusType.DODGE)

    /**
     * статус активный, если:
     * его длительность не закончилась
     * у него есть значение
     * у него "разы" срабатывания (если они были)
     */
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

    /**
     * если у статуса есть вероятность, то он может не сработать
     */
    fun isWork(): Boolean {
        val numberForCompareWithStatusProbability = Random.nextInt(0, 101)

        /**
         * дефолтная вероятность применения статуса 100%
         */
        val isStatusWorked = numberForCompareWithStatusProbability <= this.probability

        return isActive() && isStatusWorked
    }

    //todo может везде надо сделать статусы с положительным значением?
    private fun compareValue(): Boolean {
        return when (this.type.color) {
            NEUTRAL_STATUS -> this.value != 0
            else -> this.value > 0
        }
    }

    fun isInfinity(): Boolean = duration == INFINITY

    enum class StatusType(
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
         * для [SMART_DODGE] уворот сработает, только если урон больше
         * [Status.smartValue]
         * todo сделать также для других эффектов
         */
        SMART_DODGE(R.drawable.ic_dodge, GOOD_STATUS),
        WEAK(R.drawable.ic_weak, BAD_STATUS),
        STRONG(R.drawable.ic_strong, GOOD_STATUS),
        CHANGE_DEFEND(R.drawable.ic_shield_plus, NEUTRAL_STATUS),
        VULNERABLE(R.drawable.ic_vul, R.color.light_red_background_color),
        VUL(R.drawable.ic_vul, R.color.light_red_background_color),
        ARMOR(R.drawable.ic_armor, GOOD_STATUS),
        RESISTANCE(R.drawable.ic_resistance, NEUTRAL_STATUS),

        /**
         * действуют при атаке персонажа
         * [HARM] - усиляется только уязвивымыми статусами атакующего
         * [COUNTERATTACK] - усиляется и статусами владельца
         * todo сейчас работают одинаково
         */
        COUNTERATTACK(R.drawable.ic_counterattack, GOOD_STATUS),
        HARM(R.drawable.ic_spikes, GOOD_STATUS),

        /**
         * реакция на атаку: может запустить любой эффект (не только урон)
         */
        REACTION(R.drawable.ic_counterattack, GOOD_STATUS),

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
         * [DAMAGE] - наносит урон щитам, потом здоровью - только вот щиты пустые перед применением статуса
         */
        //todo добавить учитывание брони и уязвимости
        DAMAGE(R.drawable.ic_damage, BAD_STATUS),
        DAMAGE_HP(R.drawable.ic_damage, BAD_STATUS),

        /**
         * статус восстанавливающий здоровье
         */
        HEAL(R.drawable.ic_heal, GOOD_STATUS),

        /**
         * статус генерирующий очки
         * нужен [Status.gemTypes], чтобы указать какие очки генерировать
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
         * изменяет количество очков, получаемое персонажем за гемы основного цвета и за экстра цвет,
         * бонусы и эффект [Effect.EditStock] бонусов не дает
         * todo сделать, чтобы давал?
         */
        CHANGE_STOCK(R.drawable.ic_generate, NEUTRAL_STATUS),

        /**
         * изменяет в процентах значение, сколько очков данного цвета должно остаться после хода
         * т.е. у очков есть свое правило общее для всех, а это для персонажа
         */
        CHANGE_TURN_KEEP_STRATEGY(R.drawable.ic_backpack, NEUTRAL_STATUS),
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
