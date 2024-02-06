package aldtoll.twiligihts.model

data class Hand(
    val name: String,
    val description: String? = null,
    val gemType: Int,
    val perks: ArrayList<Perk>,
) {

    // Add a no-argument constructor
    @Suppress("unused")
    constructor() : this("", null, 0, arrayListOf())
}
