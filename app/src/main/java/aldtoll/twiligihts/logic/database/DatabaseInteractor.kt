package aldtoll.twiligihts.logic.database

import aldtoll.twiligihts.logic.database.enemy.EnemyDownLoadInteractor
import aldtoll.twiligihts.logic.database.enemy.EnemyHandsDownLoadInteractor
import aldtoll.twiligihts.logic.database.enemy.EnemyResourcesDownloadExecutor
import aldtoll.twiligihts.logic.database.enemy.EnemySectorsDownLoadInteractor
import aldtoll.twiligihts.logic.database.enemy.EnemyStatesDownLoadInteractor
import aldtoll.twiligihts.logic.database.enemy.EnemyStatusesDownLoadInteractor
import aldtoll.twiligihts.logic.database.enemy.EnemyStocksDownLoadInteractor
import aldtoll.twiligihts.logic.database.hero.HeroDownLoadInteractor
import aldtoll.twiligihts.logic.database.hero.HeroHandsDownLoadInteractor
import aldtoll.twiligihts.logic.database.hero.HeroResourcesDownloadExecutor
import aldtoll.twiligihts.logic.database.hero.HeroRulesDownLoadInteractor
import aldtoll.twiligihts.logic.database.hero.HeroStatesDownLoadInteractor
import aldtoll.twiligihts.logic.database.hero.HeroStatusesDownLoadInteractor
import aldtoll.twiligihts.logic.database.hero.HeroStocksDownLoadInteractor
import aldtoll.twiligihts.model.Hand
import aldtoll.twiligihts.storage.PlaceHandsListInteractor
import aldtoll.twiligihts.ui.screen.start_screen.StartScreen.Companion.STARTED
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseInteractor @Inject constructor(
    private val heroDownLoadInteractor: HeroDownLoadInteractor,
    private val enemyDownLoadInteractor: EnemyDownLoadInteractor,
    private val heroStocksDownLoadInteractor: HeroStocksDownLoadInteractor,
    private val heroHandsDownLoadInteractor: HeroHandsDownLoadInteractor,
    private val heroStatesDownLoadInteractor: HeroStatesDownLoadInteractor,
    private val heroRulesDownLoadInteractor: HeroRulesDownLoadInteractor,
    private val enemyStocksDownLoadInteractor: EnemyStocksDownLoadInteractor,
    private val enemyStatesDownLoadInteractor: EnemyStatesDownLoadInteractor,
    private val enemyHandsDownLoadInteractor: EnemyHandsDownLoadInteractor,
    private val heroStatusesDownLoadInteractor: HeroStatusesDownLoadInteractor,
    private val enemyStatusesDownLoadInteractor: EnemyStatusesDownLoadInteractor,
    private val enemySectorsDownLoadInteractor: EnemySectorsDownLoadInteractor,
    private val placeHandsListInteractor: PlaceHandsListInteractor,
    private val battleSettingsDowloadExecutor: BattleSettingsDowloadExecutor,
    private val battleResultDownloadExecutor: BattleResultDownloadExecutor,
    private val enemyResourcesDownloadExecutor: EnemyResourcesDownloadExecutor,
    private val heroResourcesDownloadExecutor: HeroResourcesDownloadExecutor,
) {

    private val database = Firebase.database

    fun observeRealtimeDatabase(prefix: String = "") {
        STARTED = false
        if (prefix.isNotBlank()) {
            PREFIX = prefix
        }
        heroDownLoadInteractor.downloadFromDatabase(database)
        enemyDownLoadInteractor.downloadFromDatabase(database)
        heroStocksDownLoadInteractor.downloadFromDatabase(database)
        enemyStocksDownLoadInteractor.downloadFromDatabase(database)
        heroStatesDownLoadInteractor.downloadFromDatabase(database)
        heroRulesDownLoadInteractor.downloadFromDatabase(database)
        enemyStatesDownLoadInteractor.downloadFromDatabase(database)
        heroStatusesDownLoadInteractor.downloadFromDatabase(database)
        enemyStatusesDownLoadInteractor.downloadFromDatabase(database)
        enemySectorsDownLoadInteractor.downloadFromDatabase(database)
        enemyResourcesDownloadExecutor.downloadFromDatabase(database)
        heroResourcesDownloadExecutor.downloadFromDatabase(database)
        heroHandsDownLoadInteractor.downloadFromDatabase(database)
        enemyHandsDownLoadInteractor.downloadFromDatabase(database)
        battleSettingsDowloadExecutor.downloadFromDatabase(database)
        battleResultDownloadExecutor.downloadFromDatabase(database)

        val placeHandsReference = database.getReference("$PREFIX/PlaceHands")
        placeHandsReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val hands = dataSnapshot.children.mapNotNull { it.getValue(Hand::class.java) }
                hands.fillEffects(dataSnapshot)
                hands.run {
                    placeHandsListInteractor.startData = ArrayList(this)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("TAG", "Failed to read value.", error.toException())
            }
        })
    }

    fun addToken(token: String) {
        val tokensReference = database.reference.child("tokens")
        tokensReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val tokensList =
                    dataSnapshot.getValue(object : GenericTypeIndicator<MutableList<String>>() {})

                // Create a new list if it doesn't exist
                val updatedList = tokensList ?: mutableListOf()

                // Add a new token to the list
                if (!updatedList.contains(token)) {
                    updatedList.add(token)
                }

                // Update the list in the Firebase database
                tokensReference.setValue(updatedList)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle cancelation
            }
        })
    }

    companion object {
        var PREFIX = "Battle"
    }
}