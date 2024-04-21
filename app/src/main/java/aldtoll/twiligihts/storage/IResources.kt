package aldtoll.twiligihts.storage

import aldtoll.twiligihts.model.Resource

interface IResources {

    fun value(): ArrayList<Resource>?
    fun refresh()
}