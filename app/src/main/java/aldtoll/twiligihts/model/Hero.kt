package aldtoll.twiligihts.model

data class Hero(
    override var hp: Int,
    val maxHp: Int,
    override var wounds: Int,
    var maxWounds: Int,
    override var shield: Int
) : Person

