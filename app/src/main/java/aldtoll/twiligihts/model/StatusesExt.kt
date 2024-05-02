package aldtoll.twiligihts.model

fun List<Status>.findActiveStatuses(name: Status.EffectType): List<Status> {
    return this.filter { it.type == name && it.isActive() }
}

fun List<Status>.findActiveStatus(name: Status.EffectType): Status? {
    return this.find { it.type == name && it.isActive() }
}