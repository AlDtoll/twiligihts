package aldtoll.twiligihts.logic.database

import aldtoll.twiligihts.model.State
import aldtoll.twiligihts.model.Status
import aldtoll.twiligihts.model.effects.Effect
import com.google.firebase.database.DataSnapshot
import java.lang.reflect.Field

fun List<Status>.fillStatusReactionEffects(
    dataSnapshot: DataSnapshot
) {
    dataSnapshot.children.forEach { statusSnapshot ->
        val statusName = statusSnapshot.child("name").getValue(String::class.java)
        val findStatus = this.find { status -> status.name == statusName }
        findStatus?.run {
            val reactionEffectSnapshot = statusSnapshot.child("reactionEffect")
            if (reactionEffectSnapshot.exists()) {
                val reactionEffect = parseEffect(reactionEffectSnapshot)
                reactionEffect?.let {
                    setReactionEffect(this, it.copyEffect())
                }
            }
        }
    }
}

fun List<State>.fillStateReactionEffects(
    dataSnapshot: DataSnapshot
) {
    dataSnapshot.children.forEach { stateSnapshot ->
        val stateName = stateSnapshot.child("name").getValue(String::class.java)
        val findState = this.find { state -> state.name == stateName }
        findState?.run {
            val statusSnapshot = stateSnapshot.child("status")
            val reactionEffectSnapshot = statusSnapshot.child("reactionEffect")
            if (reactionEffectSnapshot.exists()) {
                val reactionEffect = parseEffect(reactionEffectSnapshot)
                reactionEffect?.let {
                    setReactionEffect(findState.status, it.copyEffect())
                }
            }
        }
    }
}

private fun parseEffect(effectSnapshot: DataSnapshot): Effect? {
    val effectName = effectSnapshot.child("command").getValue(Effect.EffectName::class.java)
    val effect = when (effectName) {
        Effect.EffectName.ATTACK -> effectSnapshot.getValue(Effect.Attack::class.java)
        Effect.EffectName.DEFEND -> effectSnapshot.getValue(Effect.Defend::class.java)
        Effect.EffectName.EDIT_STATUS -> {
            effectSnapshot.getValue(Effect.EditStatus::class.java)
        }

        Effect.EffectName.EDIT_STOCK -> effectSnapshot.getValue(Effect.EditStock::class.java)
        Effect.EffectName.HEAL -> effectSnapshot.getValue(Effect.Heal::class.java)
        Effect.EffectName.FINISH -> effectSnapshot.getValue(Effect.FinishBattle::class.java)
        Effect.EffectName.INFO -> effectSnapshot.getValue(Effect.Info::class.java)
        Effect.EffectName.EDIT_RES -> effectSnapshot.getValue(Effect.EditResources::class.java)
        else -> null
    }
    effect?.additionalEffects = parseEffects(effectSnapshot.child("additionalEffects"))
    return effect
}

private fun parseEffects(effectsSnapshot: DataSnapshot): ArrayList<Effect> {
    val effects = ArrayList<Effect>()
    for (effectSnapshot in effectsSnapshot.children) {
        val effect = parseEffect(effectSnapshot)
        effect?.let { effects.add(it) }
    }
    return effects
}

private fun setReactionEffect(status: Status, effect: Effect) {
    try {
        val field: Field = Status::class.java.getDeclaredField("reactionEffect")
        field.isAccessible = true
        field.set(status, effect)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

