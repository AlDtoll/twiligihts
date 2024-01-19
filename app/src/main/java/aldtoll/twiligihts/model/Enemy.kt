package aldtoll.twiligihts.model

data class Enemy(
    override var hp: Int,
    val maxHp: Int,
    override var wounds: Int,
    var maxWounds: Int,
    override var shield: Int,
    val perks: ArrayList<Perk>,
    override var statuses: ArrayList<Status> = arrayListOf()
) : Person {

}