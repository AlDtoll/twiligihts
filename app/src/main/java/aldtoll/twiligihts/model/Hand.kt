package aldtoll.twiligihts.model

data class Hand(
    val name: String,
    val description: String? = null,
    val gemType: Int = 1,
    val perks: ArrayList<Perk>,
    //todo тоже добавить conditionsForDisplay
) {
    fun init() {
        perks.forEach {
            it.init()
        }
    }

    // Add a no-argument constructor
    @Suppress("unused")
    constructor() : this("", null, 1, arrayListOf())
}
