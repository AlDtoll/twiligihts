package aldtoll.twiligihts.model

data class Hand(
    val name: String,
    val description: String? = null,
    val gemType: Int = 1,
    val perks: ArrayList<Perk>,
    var show: Boolean = true,
    val conditionsForDisplay: ArrayList<Condition> = arrayListOf(),
    //todo общие эффекты
) {
    fun init(heroHand: Boolean = false) {
        perks.forEach {
            it.init(heroHand, gemType)
        }
    }

    // Add a no-argument constructor
    @Suppress("unused")
    constructor() : this("", null, 1, arrayListOf())
}
