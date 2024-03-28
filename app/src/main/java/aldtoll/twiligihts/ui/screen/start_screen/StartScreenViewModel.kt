package aldtoll.twiligihts.ui.screen.start_screen

import aldtoll.twiligihts.FCMHelper
import aldtoll.twiligihts.logic.database.DatabaseInteractor
import aldtoll.twiligihts.model.BattleResult
import aldtoll.twiligihts.model.BattleSettings
import aldtoll.twiligihts.storage.AttemptCounterInteractor
import aldtoll.twiligihts.storage.BattleResultInteractor
import aldtoll.twiligihts.storage.BattleSettingsInteractor
import aldtoll.twiligihts.storage.enemy.EnemyInteractor
import androidx.lifecycle.ViewModel
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StartScreenViewModel @Inject constructor(
    private val battleResultInteractor: BattleResultInteractor,
    private val settingsInteractor: BattleSettingsInteractor,
    private val attemptCounterInteractor: AttemptCounterInteractor,
    private val enemyInteractor: EnemyInteractor,
) : ViewModel() {

    fun resultData() = battleResultInteractor.get()

    fun battleName() = enemyInteractor.startedValue?.name

    fun settingsData() = settingsInteractor.get()
    fun newAttempt() {
        attemptCounterInteractor.increment()
    }

    fun activateGodMode() {
        BattleSettings.GOD_MODE = !BattleSettings.GOD_MODE
    }

    fun startBattleAgain() {
        val resultReference = Firebase.database.getReference("${DatabaseInteractor.PREFIX}/Result")
        resultReference.setValue(
            BattleResult(
                false
            )
        )
    }

    fun showDice(dice: Int) {
        val SENDER_ID =
            "dfweJBGZTeGLvLBUJ4Egs7:APA91bFG-yKxfrvins3Vi1vCnCKYpe--HGzuXsagL8fNAIQPAazXw5_Uwo87JSq-N6Sgrke6k_KF27UjAy2oDY8N9qKxgC9C7lwRyaooJ4oT2VPyN8byAIykbi54vmwSfS_nNi28kvea"
        FCMHelper.sendPushNotification(SENDER_ID, "Кость", dice.toString());
    }
}