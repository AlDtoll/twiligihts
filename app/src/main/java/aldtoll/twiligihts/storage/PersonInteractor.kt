package aldtoll.twiligihts.storage

import aldtoll.twiligihts.model.Person

interface PersonInteractor {

    fun value(): Person?

    fun update(item: Person)
}