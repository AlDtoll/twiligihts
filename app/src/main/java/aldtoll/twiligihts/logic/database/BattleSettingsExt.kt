package aldtoll.twiligihts.logic.database

import aldtoll.twiligihts.model.BattleSettings
import aldtoll.twiligihts.model.effects.Effect
import com.google.firebase.database.DataSnapshot

/**
 * Заполняет effects у triggerPerk в настройках ячеек, так как Perk.effects помечен @Exclude.
 */
fun BattleSettings.fillCellTriggerPerkEffects(
    settingsSnapshot: DataSnapshot
): BattleSettings {
    if (cells.isEmpty()) return this

    val updatedCells = ArrayList(cells.map { it.copy() })
    val cellsSnapshot = settingsSnapshot.child("cells")

    updatedCells.forEachIndexed { index, cellConfig ->
        val perk = cellConfig.triggerPerk ?: return@forEachIndexed

        val snapshotByIndex = cellsSnapshot.child(index.toString())
        if (!snapshotByIndex.exists()) return@forEachIndexed

        val perkSnapshot = snapshotByIndex.child("triggerPerk")
        if (!perkSnapshot.exists()) return@forEachIndexed

        val effects = parseEffects(perkSnapshot.child("effects"))
        if (effects.isNotEmpty()) {
            updatedCells[index] = cellConfig.copy(
                triggerPerk = perk.copy(
                    effects = ArrayList(effects.map { effect -> effect.copyEffect() })
                )
            )
        }
    }

    return copy(cells = updatedCells)
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
        Effect.EffectName.EDIT_STATUS -> effectSnapshot.getValue(Effect.EditStatus::class.java)
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

