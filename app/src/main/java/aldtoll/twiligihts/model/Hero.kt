package aldtoll.twiligihts.model

data class Hero(
    override var hp: Int,
    val maxHp: Int,
    var wounds: Int,
    override var shield: Int
) : Person

