package aldtoll.twiligihts.storage

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnemyMoveEventInteractor @Inject constructor() {

    private val flow = MutableSharedFlow<Unit>(0, 1, BufferOverflow.DROP_OLDEST)

    fun update(item: Unit) {
        flow.tryEmit(item)
    }

    fun get() = flow
}