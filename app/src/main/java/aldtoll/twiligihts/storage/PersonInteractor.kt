package aldtoll.twiligihts.storage

import aldtoll.twiligihts.model.characters.Person

interface PersonInteractor {

    fun value(): Person?

    fun update(item: Person)
}