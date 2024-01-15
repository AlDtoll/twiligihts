package aldtoll.twiligihts.model

data class Enemy(
    override var hp: Int,
    val maxHp: Int,
    var wounds: Int,
    override var shield: Int,
    val perks: ArrayList<Perk>
) : Person {

}