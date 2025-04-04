package aldtoll.twiligihts.model

fun List<Status>.findActiveStatuses(name: Status.StatusType): List<Status> {
    return this.filter { it.type == name && it.isActive() }
}

fun List<Status>.findActiveStatus(name: Status.StatusType): Status? {
    return this.find { it.type == name && it.isActive() }
}