package aldtoll.twiligihts.model

data class Status(
    val name: String,
    var value: Int,
    val effectType: EffectType
) {

    fun isActive(): Boolean = this.value > 0

    enum class EffectType {
        DODGE,
    }

}
