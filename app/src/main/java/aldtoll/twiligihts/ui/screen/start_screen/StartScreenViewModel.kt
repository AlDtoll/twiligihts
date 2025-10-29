package aldtoll.twiligihts.ui.screen.start_screen

import aldtoll.twiligihts.FCMHelper
import aldtoll.twiligihts.logic.database.DatabaseInteractor
import aldtoll.twiligihts.logic.database.DatabaseInteractor.Companion.PREFIX
import aldtoll.twiligihts.model.BattleEvent
import aldtoll.twiligihts.model.BattleSettings
import aldtoll.twiligihts.storage.AttemptCounterInteractor
import aldtoll.twiligihts.storage.BattleResultInteractor
import aldtoll.twiligihts.storage.BattleSettingsInteractor
import aldtoll.twiligihts.storage.common.RemoteMessageInteractor
import aldtoll.twiligihts.storage.enemy.EnemyInteractor
import aldtoll.twiligihts.ui.screen.start_screen.StartScreen.Companion.STARTED
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class StartScreenViewModel @Inject constructor(
    private val battleResultInteractor: BattleResultInteractor,
    private val settingsInteractor: BattleSettingsInteractor,
    private val attemptCounterInteractor: AttemptCounterInteractor,
    private val enemyInteractor: EnemyInteractor,
    private val remoteMessageInteractor: RemoteMessageInteractor,
    private val databaseInteractor: DatabaseInteractor,
) : ViewModel() {

    private val database = Firebase.database

    fun resultData() = battleResultInteractor.get()

    fun battleName() = enemyInteractor.startedValue?.name
    fun enemyIcon() = enemyInteractor.startedValue?.preview

    fun settingsData() = settingsInteractor.get()
    fun newAttempt() {
        //todo сделать подсчет попыток от базы
        attemptCounterInteractor.increment()
        FCMHelper.sendPushNotification(
            "$PREFIX: ${enemyInteractor.startedValue?.name}",
            (attemptCounterInteractor.value() ?: 0).toString()
        )
    }

    fun activateGodMode() {
        BattleSettings.GOD_MODE = !BattleSettings.GOD_MODE
    }

    fun startBattleAgain() {
        startBattle()
        val resultFinishedReference =
            database.getReference("$PREFIX/Result/finished")
        resultFinishedReference.setValue(
            false
        )
    }

    fun startBattle() {
        STARTED = true
        val resultStartedReference =
            database.getReference("$PREFIX/Result/started")
        resultStartedReference.setValue(
            true
        )
    }

    fun showDice(dice: Int, maxDiceValue: Int) {
        FCMHelper.sendPushNotification("Кость$maxDiceValue", dice.toString())
    }

    fun showDices(dices: List<Int>, maxDiceValue: Int) {
        val title = if (dices.size > 1) {
            "Кость$maxDiceValue x${dices.size}"
        } else {
            "Кость$maxDiceValue"
        }
        val body = dices.joinToString(", ")
        FCMHelper.sendPushNotification(title, body)
    }

    var logData = arrayListOf<BattleEvent>()
    fun getLogData() {
        val logReference = database.getReference("$PREFIX/Log")
        logReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val events = dataSnapshot.children.mapNotNull { it.getValue(String::class.java) }
                logData = ArrayList(events.map { BattleEvent(it) })
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("TAG", "Failed to read value.", error.toException())
            }
        })
    }

    var diceData = MutableStateFlow(0)
    fun getDiceData() {
        val logReference = database.getReference("Dice")
        logReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val i = dataSnapshot.getValue(Int::class.java)
                i?.run {
                    diceData.tryEmit(this)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("TAG", "Failed to read value.", error.toException())
            }
        })
    }

    fun diceData() = diceData

    var dicesCountData = MutableStateFlow(1)
    fun getDicesCountData() {
        val ref = database.getReference("Dices")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val i = dataSnapshot.getValue(Int::class.java)
                dicesCountData.tryEmit(i ?: 1)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("TAG", "Failed to read value.", error.toException())
            }
        })
    }

    fun dicesCountData() = dicesCountData

    var masterTokenData = MutableStateFlow("")

    fun getMasterTokenData() {
        val masterTokenReference = database.getReference("masterToken")
        masterTokenReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val i = dataSnapshot.getValue(String::class.java)
                i?.run {
                    masterTokenData.tryEmit(this)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("TAG", "Failed to read value.", error.toException())
            }
        })
    }

    fun masterTokenData() = masterTokenData
    fun pushData() = remoteMessageInteractor.get()
    fun changePrefixAndLoadNewData(enemyName: String) {
        databaseInteractor.observeRealtimeDatabase(enemyName)
    }
}