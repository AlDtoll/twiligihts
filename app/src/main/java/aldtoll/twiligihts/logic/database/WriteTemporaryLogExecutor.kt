package aldtoll.twiligihts.logic.database

import aldtoll.twiligihts.domain.repository.BattleLogRepository
import aldtoll.twiligihts.logic.database.DatabaseInteractor.Companion.PREFIX
import com.google.firebase.database.FirebaseDatabase
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class WriteTemporaryLogExecutor @Inject constructor(
    private val battleLogRepository: BattleLogRepository,
    private val databaseProvider: Provider<FirebaseDatabase>,
) {

    private val database get() = databaseProvider.get()

    fun execute() {
        val logReference = database.getReference("$PREFIX/LogTemp")
        logReference.setValue(
            battleLogRepository.getCurrentLogs().map { it.message }
        )
    }
}