package aldtoll.twiligihts.logic.database

import aldtoll.twiligihts.model.Effect
import aldtoll.twiligihts.model.Hand
import com.google.firebase.database.DataSnapshot

fun List<Hand>.fillEffects(
    dataSnapshot: DataSnapshot
) {
    dataSnapshot.children.forEach { enemyHandSnapshot ->
        val handName = enemyHandSnapshot.child("name").getValue(String::class.java)
        val findHand = this.find { hand -> hand.name == handName }
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