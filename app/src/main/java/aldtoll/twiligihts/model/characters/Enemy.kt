package aldtoll.twiligihts.model.characters

import aldtoll.twiligihts.model.Status

data class Enemy(
    override val name: String? = null,
    override var hp: Int,
    override val maxHp: Int,
    override var wounds: Int?,
    var maxWounds: Int,
    override var shield: Int,
    override var statuses: ArrayList<Status> = arrayListOf(),
    val info: String? = null,
    override var touches: Int = 0,
    override var hits: Int = 0,
    override var blocks: Int = 0,
    override var wasHitByPreviousEffect: Boolean = false,
    override var wasTouchedByPreviousEffect: Boolean = false,
    val preview: String = "test_enemy"
) : Person {

    override fun recreate(): Enemy {
        val copy = this.copy()
        val statuses: ArrayList<Status> = ArrayList(this.statuses.map { status -> status.copy() })
        copy.statuses = statuses
        return copy
    }

    @Suppress("unused")
    constructor() : this(null, 0, 1, 0, 0, 0, arrayListOf())
}