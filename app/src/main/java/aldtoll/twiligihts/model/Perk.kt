package aldtoll.twiligihts.model

import aldtoll.twiligihts.logic.database.DatabaseInteractor
import aldtoll.twiligihts.model.Perk.ReloadType
import com.google.firebase.database.Exclude

data class Perk(
    val name: String,
    val prices: ArrayList<Price> = arrayListOf(),
    @get:Exclude
    /**
     * [Effect] - это sealed class, поэтому он эксклудится,
     * т.к. ему нужно писать парсер см. [DatabaseInteractor.fillEffects]
     */
    var effects: ArrayList<Effect>,
    var description: String? = null,
    var enable: Boolean = false,
    val icon: String? = null,
    /**
     * условие для показа навыка
     */
    val conditionForDisplay: Condition? = null,
    val conditionsForDisplay: ArrayList<Condition> = arrayListOf(),
    var show: Boolean = true,
    /**
     * сколько зарядов навыка есть изначально
     */
    //todo напрашиваются общие ресурсы для навыков
    var charges: Int? = null,
    var currentCharges: Int? = charges,
    /**
     * тип перезарядки - по ходу или по действию
     */
    //todo напрашивается несколько использований до перезарядки
    val reloadType: ReloadType = ReloadType.TURN,
    /**
     * сколько перезарадяок должно пройти после использования навыка
     * 1 - это значит, что в этом ходу, если [ReloadType.TURN], можно использовать только раз
     * в таком виде имеет значение только для героя, т.к. у противника все навыки автоматические
     */
    val coolDown: Int? = null,
    var startReload: Int? = null,
    /**
     * при использовании навыка принимает значение 0
     * затем каждый ход увеличивается до [coolDown]
     */
    var reload: Int = coolDown ?: 0,
    /**
     * есть смысл использовать вероятность для схваток, либо каких-то побочных эффектов
     */
    val probability: Int = 100,
    /**
     * используется для навыков не противника, а окружения
     */
    val place: Boolean = false
    //todo usage - применений за ход
) {

    enum class ReloadType {
        /**
         * обновляется ходом
         */
        TURN,

        /**
         * обновляется действием
         * в настоящий момент имеет значение только для героя
         */
        PERK,

        /**
         * обновляется действием
         * в конце хода сбрасывает свое значение до 0
         */
        COMBO
    }

    fun isReloading(): Boolean {
        return if (coolDown == null) {
            false
        } else {
            reload < coolDown
        }
    }

    companion object {
        const val EMPTY = "empty"
        const val LAST = "last"
        var PERK_MAP = hashMapOf<String, String>()

        /**
         * пустой перк для зануления анимации
         */
        val EMPTY_PERK = Perk(
            name = EMPTY,
            arrayListOf(),
            arrayListOf()
        )
    }

    constructor() : this("", arrayListOf(), arrayListOf())

    data class Price(
        val value: Int,
        val gemType: Int
    ) {
        @Suppress("unused")
        constructor() : this(0, 0)
    }

    fun decreaseCharges() {
        if (this.currentCharges != null) {
            if (this.currentCharges!! > 0) {
                this.currentCharges = this.currentCharges!! - 1
            }
        }
    }

    fun init() {
        currentCharges = charges
        if (startReload == null) {
            startReload = if (reloadType == ReloadType.COMBO) 0 else coolDown
        }
        reload = startReload ?: 0
        if (description == null) {
            description = defaultDescription(effects)
        }
        effects.forEach {
            it.init()
        }
    }

    private fun defaultDescription(effects: ArrayList<Effect>): String {
        var description = ""
        effects.forEach {
            if (it.getDescription().isNotBlank()) {
                description += "${it.getDescription()}\n"
            }
        }
        return description.substringBeforeLast("\n")
    }

    fun reloadPerkAfterUse() {
        when (reloadType) {
            ReloadType.TURN -> {}
            ReloadType.PERK -> {
                if (show && isReloading()) {
                    reload += 1
                }
            }

            ReloadType.COMBO -> {
                if (show && isReloading()) {
                    reload += 1
                }
            }
        }
    }

    fun isSame(perk: Perk?): Boolean {
        return perk?.name == name && perk.prices == prices && perk.effects == effects
    }

    fun nameForDisplay(): String {
        if (conditionsForDisplay.isNotEmpty() || conditionForDisplay != null) {
            return "$name.*"
        }
        return name
    }

}