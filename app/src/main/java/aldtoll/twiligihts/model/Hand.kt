package aldtoll.twiligihts.model

data class Hand(
    val name: String,
    val gemType: Int,
    val perks: ArrayList<Perk>,
) {

    constructor() : this("", 0, arrayListOf())
}
