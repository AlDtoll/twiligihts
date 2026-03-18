package aldtoll.twiligihts.logic.database

import aldtoll.twiligihts.model.TimePerk
import aldtoll.twiligihts.model.effects.Effect
import com.google.firebase.database.DataSnapshot

/**
 * Заполняет эффекты перков внутри таймерных навыков (TimePerk) из снапшота Firebase.
 * Структура аналогична тому, как это сделано для секторов.
 */
fun List<TimePerk>.fillTimePerkEffects(
    dataSnapshot: DataSnapshot
) {
    dataSnapshot.children.forEach { TimePerksnapshot ->
        val timeStateId = TimePerksnapshot.child("id").getValue(Int::class.java)
        val findState = this.find { state -> state.id == timeStateId }
        findState?.run {
            val perkSnapshot = TimePerksnapshot.child("perk")
            val perkName = perkSnapshot.child("name").getValue(String::class.java)
            if (perkName == findState.perk.name) {
                val effects = parseEffects(perkSnapshot.child("effects"))
                findState.perk.effects = ArrayList(effects.map { effect -> effect.copyEffect() })
            }
        }
    }
}

private fun parseEffects(effectsSnapshot: DataSnapshot): ArrayList<Effect> {
    val effects = ArrayList<Effect>()
    for (effectSnapshot in effectsSnapshot.children) {
        val effect = parseEffect(effectSnapshot)
        effect?.let { effects.add(it) }
    }
    return effects
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

