package aldtoll.twiligihts.logic.perks

import aldtoll.twiligihts.model.effects.Effect
import aldtoll.twiligihts.storage.BattleLogListInteractor
import aldtoll.twiligihts.storage.enemy.EnemyInteractor
import aldtoll.twiligihts.storage.hero.HeroInteractor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefendEffectHandler @Inject constructor(
    private val battleLogListInteractor: BattleLogListInteractor,
    private val heroInteractor: HeroInteractor,
    private val enemyInteractor: EnemyInteractor
) {
    fun handleDefendEffect(effect: Effect.Defend, isHeroPerk: Boolean) {
        when (effect.target) {
            Effect.EffectTarget.ENEMY -> {
                defendPerson(effect, false)
            }

            Effect.EffectTarget.HERO -> {
                defendPerson(effect, true)
            }

            Effect.EffectTarget.ALL -> {
                defendPerson(effect, false)
                defendPerson(effect, true)
            }

            Effect.EffectTarget.SELF -> {
                defendPerson(effect, isHeroPerk)
            }

            Effect.EffectTarget.FOE -> {
                defendPerson(effect, !isHeroPerk)
            }
        }
    }

    private fun defendPerson(defend: Effect.Defend, isHeroTarget: Boolean) {
        val personInteractor = if (isHeroTarget) heroInteractor else enemyInteractor
        val person = personInteractor.value()

        person?.run {
            when (defend.type) {
                Effect.Defend.Type.CHANGE -> this.shield += defend.value
                Effect.Defend.Type.SET -> this.shield = defend.value
            }
            personInteractor.update(this)

            val who = if (isHeroTarget) "Герой" else "Противник"
            battleLogListInteractor.add("$who получает ${defend.value} щитов. (${this.shield})")
        }
    }

}
