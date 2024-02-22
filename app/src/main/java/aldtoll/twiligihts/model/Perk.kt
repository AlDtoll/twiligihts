package aldtoll.twiligihts.model

import com.google.firebase.database.Exclude

data class Perk(
    val name: String,
    val prices: ArrayList<Price> = arrayListOf(),
    @get:Exclude
    var effects: ArrayList<Effect>,
    val description: String? = effects.toString(),
    var enable: Boolean = false,
    val icon: String? = null,
    val conditionForDisplay: Condition? = null,
    var show: Boolean = true,
) {

    companion object {
        var PERK_MAP = hashMapOf<String, String>()
    }

    constructor() : this("", arrayListOf(), arrayListOf())

    data class Price(
        val value: Int,
        val gemType: Int
    ) {
        @Suppress("unused")
        constructor() : this(0, 0)
    }

}