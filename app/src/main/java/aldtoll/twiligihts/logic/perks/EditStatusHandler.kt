package aldtoll.twiligihts.logic.perks

import aldtoll.twiligihts.domain.usecase.battlelog.AddBattleLogEntryUseCase
import aldtoll.twiligihts.model.Gem
import aldtoll.twiligihts.model.Status
import aldtoll.twiligihts.model.characters.Person
import aldtoll.twiligihts.model.effects.Effect
import aldtoll.twiligihts.storage.PersonInteractor
import aldtoll.twiligihts.storage.enemy.EnemyInteractor
import aldtoll.twiligihts.storage.hero.HeroInteractor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EditStatusHandler @Inject constructor(
    private val addBattleLogEntryUseCase: AddBattleLogEntryUseCase,
    private val heroInteractor: HeroInteractor,
    private val enemyInteractor: EnemyInteractor
) {

    fun handleEffect(effect: Effect.EditStatus, isHeroPerk: Boolean) {
        when (effect.target) {
            Effect.EffectTarget.ENEMY -> {
                editPersonStatus(effect, false)
            }

            Effect.EffectTarget.HERO -> {
                editPersonStatus(effect, true)
            }

            Effect.EffectTarget.ALL -> {
                editPersonStatus(effect, false)
                editPersonStatus(effect, true)
            }

            Effect.EffectTarget.SELF -> {
                editPersonStatus(effect, isHeroPerk)
            }

            Effect.EffectTarget.FOE -> {
                editPersonStatus(effect, !isHeroPerk)
            }
        }
    }

    private fun editPersonStatus(effect: Effect.EditStatus, isHeroTarget: Boolean) {
        val personInteractor = personInteractor(isHeroTarget)
        val person = personInteractor.value() ?: return

        val newPerson = person.recreate()
        val effectStatus = effect.status
        val effectValue = effect.value

        val existingStatus = newPerson.statuses.find { it.name == effectStatus.name }
        val isUpdate = existingStatus != null

        if (isUpdate) {
            updateExistingStatus(existingStatus!!, effectStatus, effectValue, effect.type)
        } else {
            addNewStatus(newPerson, effectStatus, effectValue)
        }

        val finalStatus = newPerson.statuses.find { it.name == effectStatus.name }
        val shouldSkipLog = shouldSkipStatusLog(effectStatus, finalStatus, effectValue)

        if (!shouldSkipLog) {
            logStatusChange(isHeroTarget, isUpdate, effectStatus)
        }

        personInteractor.update(newPerson)
    }

    private fun updateExistingStatus(
        status: Status,
        effectStatus: Status,
        effectValue: Int,
        type: Effect.EditStatus.Type
    ) {
        status.duration = effectStatus.duration

        when (type) {
            Effect.EditStatus.Type.SET -> {
                status.value = effectValue
                status.times = effectStatus.times
            }

            Effect.EditStatus.Type.CHANGE -> {
                status.value += effectValue
            }

            Effect.EditStatus.Type.TIMES -> {
                effectStatus.times?.let { timesToAdd ->
                    status.value = effectValue
                    status.times = status.times?.plus(timesToAdd)
                }
            }
        }
    }

    private fun addNewStatus(
        person: Person,
        effectStatus: Status,
        effectValue: Int
    ) {
        val newStatus = effectStatus.copy()
        newStatus.value = effectValue
        person.statuses.add(newStatus)
    }

    private fun shouldSkipStatusLog(
        effectStatus: Status,
        finalStatus: Status?,
        effectValue: Int
    ): Boolean {
        if (!effectStatus.skipZero) return false

        val finalValue = finalStatus?.value ?: effectValue
        return finalValue == 0
    }

    private fun logStatusChange(isHeroTarget: Boolean, isUpdate: Boolean, effectStatus: Status) {
        val who = if (isHeroTarget) "Герой" else "Противник"
        val action = if (isUpdate) "обновляет" else "получает"
        addBattleLogEntryUseCase(
            "$who $action статус: ${effectStatus.name}",
            Gem.STATUS_COLOR,
            effectStatus.log
        )
    }

    private fun personInteractor(isHeroTarget: Boolean): PersonInteractor {
        val personInteractor = if (isHeroTarget) {
            heroInteractor
        } else {
            enemyInteractor
        }
        return personInteractor
    }
}