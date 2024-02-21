package aldtoll.twiligihts.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Hero(
    val name: String? = null,
    override var hp: Int,
    override val maxHp: Int,
    override var wounds: Int,
    var maxWounds: Int,
    override var shield: Int,
    override var statuses: ArrayList<Status> = arrayListOf(),
    override val debuffes: ArrayList<Debuff> = arrayListOf(),
) : Person {

    override fun recreate(): Hero {
        val copy = this.copy()
        val statuses: ArrayList<Status> = ArrayList(this.statuses.map { status -> status.copy() })
        copy.statuses = statuses
        return copy
    }

    // Add a no-argument constructor
    @Suppress("unused")
    constructor() : this("", 0, 0, 0, 0, 0, arrayListOf())
}

