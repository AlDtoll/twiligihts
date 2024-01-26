package aldtoll.twiligihts.model

data class Hand(
    val name: String,
    val gemType: Int,
    val perks: ArrayList<Perk>,
) {

    // Add a no-argument constructor
    @Suppress("unused")
    constructor() : this("", 0, arrayListOf())
}
