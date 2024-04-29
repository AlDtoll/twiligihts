package aldtoll.twiligihts.model

fun List<Status>.findActiveStatues(name: Status.EffectType): List<Status> {
    return this.filter { it.type == name && it.isActive() }
}