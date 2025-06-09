package aldtoll.twiligihts.logic.perks

import aldtoll.twiligihts.model.Gem
import aldtoll.twiligihts.model.effects.Effect
import aldtoll.twiligihts.storage.BattleLogListInteractor
import aldtoll.twiligihts.storage.PersonInteractor
import aldtoll.twiligihts.storage.enemy.EnemyInteractor
import aldtoll.twiligihts.storage.hero.HeroInteractor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EditStatusHandler @Inject constructor(
    private val battleLogListInteractor: BattleLogListInteractor,
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
        val person = personInteractor.value()
        person?.run {
            val newPerson = this.recreate()
            var what = ""
            effect.status.let { effectStatus ->
                val statusForChange =
                    newPerson.statuses.find { personStatus -> personStatus.name == effectStatus.name }
                val effectValue = effect.value
                if (statusForChange != null) {
                    what = "обновляет"
                    statusForChange.duration = effectStatus.duration

                    when (effect.type) {
                        Effect.EditStatus.Type.SET -> {
                            statusForChange.value = effectValue
                            statusForChange.times = effectStatus.times
                        }

                        Effect.EditStatus.Type.CHANGE -> {
                            statusForChange.value += effectValue
                        }

                        Effect.EditStatus.Type.TIMES -> {
                            effectStatus.times?.run {
                                statusForChange.value = effectValue
                                statusForChange.times = statusForChange.times?.plus(this)
                            }
                        }
                    }
                } else {
                    what = "получает"
                    val status = effectStatus.copy()
                    status.value = effectValue
                    newPerson.statuses.add(status)
                }
            }
            val who = if (isHeroTarget) {
                "Герой"
            } else {
                "Противник"
            }
            battleLogListInteractor.add(
                "$who $what статус: ${effect.status.name}",
                Gem.STATUS_COLOR
            )
            personInteractor.update(newPerson)
        }
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