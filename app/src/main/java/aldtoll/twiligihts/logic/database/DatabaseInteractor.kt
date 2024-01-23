package aldtoll.twiligihts.logic.database

import aldtoll.twiligihts.model.BattleSettings
import aldtoll.twiligihts.model.Enemy
import aldtoll.twiligihts.model.Hand
import aldtoll.twiligihts.model.Hero
import aldtoll.twiligihts.model.Stock
import aldtoll.twiligihts.storage.BattleSettingsInteractor
import aldtoll.twiligihts.storage.EnemyHandsListInteractor
import aldtoll.twiligihts.storage.EnemyInteractor
import aldtoll.twiligihts.storage.HeroHandsListInteractor
import aldtoll.twiligihts.storage.HeroInteractor
import aldtoll.twiligihts.storage.StockListInteractor
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseInteractor @Inject constructor(
    private val heroInteractor: HeroInteractor,
    private val enemyInteractor: EnemyInteractor,
    private val stockListInteractor: StockListInteractor,
    private val heroHandsListInteractor: HeroHandsListInteractor,
    private val enemyHandsListInteractor: EnemyHandsListInteractor,
    private val battleSettingsInteractor: BattleSettingsInteractor,
) {

    private val database = Firebase.database
    fun observeRealtimeDatabase() {
        val heroReference = database.getReference("Hero")
        heroReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val hero = dataSnapshot.getValue(Hero::class.java)
                hero?.run {
                    heroInteractor.startedValue = hero
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("TAG", "Failed to read value.", error.toException())
            }
        })

        val enemyReference = database.getReference("Enemy")
        enemyReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val enemy = dataSnapshot.getValue(Enemy::class.java)
                enemy?.run {
                    enemyInteractor.startedValue = enemy
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("TAG", "Failed to read value.", error.toException())
            }
        })

        val stocksReference = database.getReference("Stocks")
        stocksReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val stocks = dataSnapshot.children.mapNotNull { it.getValue(Stock::class.java) }
                stocks.run {
                    stockListInteractor.startedValue = ArrayList(this)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("TAG", "Failed to read value.", error.toException())
            }
        })

        val heroHandsReference = database.getReference("HeroHands")
        heroHandsReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val hands = dataSnapshot.children.mapNotNull { it.getValue(Hand::class.java) }
                hands.run {
                    heroHandsListInteractor.startData = ArrayList(this)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("TAG", "Failed to read value.", error.toException())
            }
        })

        val enemyHandsReference = database.getReference("EnemyHands")
        enemyHandsReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val hands = dataSnapshot.children.mapNotNull { it.getValue(Hand::class.java) }
                hands.run {
                    enemyHandsListInteractor.startData = ArrayList(this)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("TAG", "Failed to read value.", error.toException())
            }
        })

        val settingsReference = database.getReference("Settings")
        settingsReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val settings = dataSnapshot.getValue(BattleSettings::class.java)
                settings.run {
                    battleSettingsInteractor.startData = settings
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("TAG", "Failed to read value.", error.toException())
            }
        })
    }
}