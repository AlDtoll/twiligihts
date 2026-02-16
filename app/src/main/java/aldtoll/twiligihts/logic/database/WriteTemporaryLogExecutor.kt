package aldtoll.twiligihts.logic.database

import aldtoll.twiligihts.domain.repository.BattleLogRepository
import aldtoll.twiligihts.logic.database.DatabaseInteractor.Companion.PREFIX
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WriteTemporaryLogExecutor @Inject constructor(
    private val battleLogRepository: BattleLogRepository,
) {

    private val database = Firebase.database

    fun execute() {
        val logReference = database.getReference("$PREFIX/LogTemp")
        logReference.setValue(
            battleLogRepository.getCurrentLogs().map { it.message }
        )
    }
}