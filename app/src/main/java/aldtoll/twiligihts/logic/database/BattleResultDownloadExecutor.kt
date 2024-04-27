package aldtoll.twiligihts.logic.database

import aldtoll.twiligihts.model.BattleResult
import aldtoll.twiligihts.storage.BattleResultInteractor
import com.google.firebase.database.DataSnapshot
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BattleResultDownloadExecutor @Inject constructor(
    private val interactor: BattleResultInteractor
) : IDownloadFromDataBase() {

    override fun saveStartedData(dataSnapshot: DataSnapshot) {
        val data = dataSnapshot.getValue(getClazzForDataBase())
        data?.run {
            interactor.update(data)
        }
    }

    override fun getClazzForDataBase() = BattleResult::class.java

    //todo заменить
    override fun getNameForDataBase(): String = "Result"
}