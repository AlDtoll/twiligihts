package aldtoll.twiligihts.model.characters

import aldtoll.twiligihts.model.Debuff
import aldtoll.twiligihts.model.Status

data class Enemy(
    val name: String? = null,
    override var hp: Int,
    override val maxHp: Int,
    override var wounds: Int,
    var maxWounds: Int,
    override var shield: Int,
    override var statuses: ArrayList<Status> = arrayListOf(),
    override val debuffes: ArrayList<Debuff> = arrayListOf(),
    val info: String? = null,
) : Person {

    override fun recreate(): Enemy {
        val copy = this.copy()
        val statuses: ArrayList<Status> = ArrayList(this.statuses.map { status -> status.copy() })
        copy.statuses = statuses
        return copy
    }

    @Suppress("unused")
    constructor() : this(null, 0, 0, 0, 0, 0, arrayListOf())
}