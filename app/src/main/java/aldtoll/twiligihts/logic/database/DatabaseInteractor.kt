package aldtoll.twiligihts.logic.database

import aldtoll.twiligihts.model.BattleResult
import aldtoll.twiligihts.model.BattleSettings
import aldtoll.twiligihts.model.Effect
import aldtoll.twiligihts.model.Hand
import aldtoll.twiligihts.model.Resource
import aldtoll.twiligihts.model.State
import aldtoll.twiligihts.model.Status
import aldtoll.twiligihts.model.Stock
import aldtoll.twiligihts.model.characters.Enemy
import aldtoll.twiligihts.model.characters.Hero
import aldtoll.twiligihts.storage.BattleResultInteractor
import aldtoll.twiligihts.storage.BattleSettingsInteractor
import aldtoll.twiligihts.storage.PlaceHandsListInteractor
import aldtoll.twiligihts.storage.enemy.EnemyHandsListInteractor
import aldtoll.twiligihts.storage.enemy.EnemyInteractor
import aldtoll.twiligihts.storage.enemy.EnemyResourcesInteractor
import aldtoll.twiligihts.storage.enemy.EnemyStatesInteractor
import aldtoll.twiligihts.storage.enemy.EnemyStatusesInteractor
import aldtoll.twiligihts.storage.hero.HeroHandsListInteractor
import aldtoll.twiligihts.storage.hero.HeroInteractor
import aldtoll.twiligihts.storage.hero.HeroResourcesInteractor
import aldtoll.twiligihts.storage.hero.HeroStatesInteractor
import aldtoll.twiligihts.storage.hero.HeroStatusesInteractor
import aldtoll.twiligihts.storage.hero.HeroStockListInteractor
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
    private val heroInteractor: HeroInteractor,
    private val enemyInteractor: EnemyInteractor,
    private val heroStockListInteractor: HeroStockListInteractor,
    private val heroHandsListInteractor: HeroHandsListInteractor,
    private val enemyHandsListInteractor: EnemyHandsListInteractor,
    private val placeHandsListInteractor: PlaceHandsListInteractor,
    private val battleSettingsInteractor: BattleSettingsInteractor,
    private val battleResultInteractor: BattleResultInteractor,
    private val enemyStatesInteractor: EnemyStatesInteractor,
    private val heroStatesInteractor: HeroStatesInteractor,
    private val enemyStatusesInteractor: EnemyStatusesInteractor,
    private val heroStatusesInteractor: HeroStatusesInteractor,
    private val enemyResourcesInteractor: EnemyResourcesInteractor,
    private val heroResourcesInteractor: HeroResourcesInteractor,
) {

    private val database = Firebase.database
    fun observeRealtimeDatabase() {
        val heroReference = database.getReference("$PREFIX/Hero")
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

        val enemyReference = database.getReference("$PREFIX/Enemy")
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

        val heroStocksReference = database.getReference("$PREFIX/HeroStocks")
        heroStocksReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val stocks = dataSnapshot.children.mapNotNull { it.getValue(Stock::class.java) }
                stocks.run {
                    heroStockListInteractor.startedValue = ArrayList(this)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("TAG", "Failed to read value.", error.toException())
            }
        })

        val heroStatesReference = database.getReference("$PREFIX/HeroStates")
        heroStatesReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val states = dataSnapshot.children.mapNotNull { it.getValue(State::class.java) }
                states.run {
                    heroStatesInteractor.startData = ArrayList(this)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("TAG", "Failed to read value.", error.toException())
            }
        })

        val enemyStatesReference = database.getReference("$PREFIX/EnemyStates")
        enemyStatesReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val states = dataSnapshot.children.mapNotNull { it.getValue(State::class.java) }
                states.run {
                    enemyStatesInteractor.startData = ArrayList(this)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("TAG", "Failed to read value.", error.toException())
            }
        })

        val enemyStatusesReference = database.getReference("$PREFIX/EnemyStatuses")
        enemyStatusesReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val states = dataSnapshot.children.mapNotNull { it.getValue(Status::class.java) }
                states.run {
                    enemyStatusesInteractor.startData = ArrayList(this)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("TAG", "Failed to read value.", error.toException())
            }
        })

        val heroStatusesReference = database.getReference("$PREFIX/HeroStatuses")
        heroStatusesReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val states = dataSnapshot.children.mapNotNull { it.getValue(Status::class.java) }
                states.run {
                    heroStatusesInteractor.startData = ArrayList(this)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("TAG", "Failed to read value.", error.toException())
            }
        })

        val enemyResourcesReference = database.getReference("$PREFIX/EnemyResources")
        enemyResourcesReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val resources =
                    dataSnapshot.children.mapNotNull { it.getValue(Resource::class.java) }
                resources.run {
                    enemyResourcesInteractor.startData = ArrayList(this)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("TAG", "Failed to read value.", error.toException())
            }
        })

        val heroResourcesReference = database.getReference("$PREFIX/HeroResources")
        heroResourcesReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val resources =
                    dataSnapshot.children.mapNotNull { it.getValue(Resource::class.java) }
                resources.run {
                    heroResourcesInteractor.startData = ArrayList(this)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("TAG", "Failed to read value.", error.toException())
            }
        })

        val heroHandsReference = database.getReference("$PREFIX/HeroHands")
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

        val enemyHandsReference = database.getReference("$PREFIX/EnemyHands")
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

        val placeHandsReference = database.getReference("$PREFIX/PlaceHands")
        placeHandsReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val hands = dataSnapshot.children.mapNotNull { it.getValue(Hand::class.java) }
                fillEffects(hands, dataSnapshot)
                hands.run {
                    placeHandsListInteractor.startData = ArrayList(this)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("TAG", "Failed to read value.", error.toException())
            }
        })

        val settingsReference = database.getReference("$PREFIX/Settings")
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

        val resultReference = database.getReference("$PREFIX/Result")
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

                                Effect.EffectName.EDIT_STATUS -> {
                                    effectSnapshot.getValue(Effect.EditStatus::class.java)
                                }

                                Effect.EffectName.EDIT_STOCK -> {
                                    effectSnapshot.getValue(Effect.EditStock::class.java)
                                }

                                Effect.EffectName.CHANGE_STOCK -> {
                                    effectSnapshot.getValue(Effect.ChangeStock::class.java)
                                }

                                Effect.EffectName.SET_STOCK -> {
                                    effectSnapshot.getValue(Effect.SetStock::class.java)
                                }

                                Effect.EffectName.HEAL -> {
                                    effectSnapshot.getValue(Effect.Heal::class.java)
                                }

                                Effect.EffectName.FINISH -> {
                                    effectSnapshot.getValue(Effect.FinishBattle::class.java)
                                }

                                Effect.EffectName.INFO -> {
                                    effectSnapshot.getValue(Effect.Info::class.java)
                                }

                                Effect.EffectName.EDIT_RES -> {
                                    effectSnapshot.getValue(Effect.EditResources::class.java)
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
        const val PREFIX = "Battle"
    }
}