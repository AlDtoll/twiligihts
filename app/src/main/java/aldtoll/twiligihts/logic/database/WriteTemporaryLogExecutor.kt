package aldtoll.twiligihts.logic.database

import aldtoll.twiligihts.logic.database.DatabaseInteractor.Companion.PREFIX
import aldtoll.twiligihts.storage.BattleLogListInteractor
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WriteTemporaryLogExecutor @Inject constructor(
    private val logListInteractor: BattleLogListInteractor,
) {

    private val database = Firebase.database

    fun execute() {
        val logReference = database.getReference("$PREFIX/LogTemp")
        logReference.setValue(
            logListInteractor.value()?.map {
                it.message
            }
        )
    }
}