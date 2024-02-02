package aldtoll.twiligihts.logic.database

import aldtoll.twiligihts.model.BattleResult
import aldtoll.twiligihts.model.BattleSettings
import aldtoll.twiligihts.model.Effect
import aldtoll.twiligihts.model.Enemy
import aldtoll.twiligihts.model.Hand
import aldtoll.twiligihts.model.Hero
import aldtoll.twiligihts.model.Stock
import aldtoll.twiligihts.storage.BattleResultInteractor
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
    private val battleResultInteractor: BattleResultInteractor,
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
                    heroInteractor.startedValue = this.recreate()
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
                fillEffects(hands, dataSnapshot)
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
                fillEffects(hands, dataSnapshot)
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
                    battleSettingsInteractor.init()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("TAG", "Failed to read value.", error.toException())
            }
        })

        val resultReference = database.getReference("Result")
        resultReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val battleResult = dataSnapshot.getValue(BattleResult::class.java)
                battleResult?.run {
                    battleResultInteractor.update(battleResult)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("TAG", "Failed to read value.", error.toException())
            }
        })
    }

    private fun fillEffects(
        hands: List<Hand>,
        dataSnapshot: DataSnapshot
    ) {
        dataSnapshot.children.forEach { enemyHandSnapshot ->
            val handName = enemyHandSnapshot.child("name").getValue(String::class.java)
            val findHand = hands.find { hand -> hand.name == handName }
            findHand?.run {
                val perksSnapshot = enemyHandSnapshot.child("perks").children
                perksSnapshot.forEach { perkSnapshot ->
                    val perkName =
                        perkSnapshot.child("name").getValue(String::class.java)
                    val findPerk = findHand.perks.find { perk -> perk.name == perkName }
                    findPerk?.run {
                        val effects = ArrayList<Effect>()
                        for (effectSnapshot in perkSnapshot.child("effects").children) {
                            val effect = when (effectSnapshot.child("name")
                                .getValue(Effect.EffectName::class.java)) {
                                Effect.EffectName.ATTACK -> {
                                    effectSnapshot.getValue(Effect.Attack::class.java)
                                }

                                Effect.EffectName.DEFEND -> {
                                    effectSnapshot.getValue(Effect.Defend::class.java)
                                }

                                Effect.EffectName.CHANGE_STATUS -> {
                                    effectSnapshot.getValue(Effect.ChangeStatus::class.java)
                                }

                                Effect.EffectName.CHANGE_STOCK -> {
                                    effectSnapshot.getValue(Effect.ChangeStock::class.java)
                                }

                                else -> null
                            }
                            effect?.let { effects.add(it) }
                        }
                        findPerk.effects =
                            ArrayList(effects.map { hand -> hand.copyEffect() })
                    }
                }
            }
        }
    }
}