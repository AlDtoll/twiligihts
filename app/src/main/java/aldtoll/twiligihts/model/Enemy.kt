package aldtoll.twiligihts.model

data class Enemy(
    override var hp: Int,
    val maxHp: Int,
    override var wounds: Int,
    var maxWounds: Int,
    override var shield: Int,
    override var statuses: ArrayList<Status> = arrayListOf()
) : Person {

    override fun recreate() = this.copy()

    @Suppress("unused")
    constructor() : this(0, 0, 0, 0, 0, arrayListOf())
}