package aldtoll.twiligihts.logic

import aldtoll.twiligihts.model.Perk
import aldtoll.twiligihts.model.effects.Effect
import aldtoll.twiligihts.storage.enemy.EnemyResourcesInteractor
import aldtoll.twiligihts.storage.hero.HeroResourcesInteractor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EditResourcesExecutor @Inject constructor(
    private val heroResourcesInteractor: HeroResourcesInteractor,
    private val enemyResourcesInteractor: EnemyResourcesInteractor
) {

    fun execute(effect: Effect.EditResources, isHeroPerk: Boolean) {
        when (effect.target) {
            Effect.EffectTarget.ENEMY -> {
                editPersonResources(effect, false)
            }

            Effect.EffectTarget.HERO -> {
                editPersonResources(effect, true)
            }

            Effect.EffectTarget.ALL -> {
                editPersonResources(effect, false)
            }

            Effect.EffectTarget.SELF -> {
                editPersonResources(effect, isHeroPerk)
            }

            Effect.EffectTarget.FOE -> {
                editPersonResources(effect, !isHeroPerk)
            }
        }
    }

    /**
     * изменение может дать отрицательный результат
     * todo добавить сообщение в лог
     */
    private fun editPersonResources(effect: Effect.EditResources, isHeroTarget: Boolean) {
        val resourcesIntractor = if (isHeroTarget) {
            heroResourcesInteractor
        } else {
            enemyResourcesInteractor
        }
        val resources = resourcesIntractor.value()
        val foundResource = resources?.find { it.name == effect.resName }
        if (foundResource != null) {
            when (effect.type) {
                Effect.EditResources.Type.SET -> {
                    foundResource.amount = effect.value
                }

                Effect.EditResources.Type.CHANGE -> {
                    foundResource.amount = foundResource.amount + effect.value
                }
            }
            resourcesIntractor.refresh()
        }
    }

    fun spendResourcesForPerk(perk: Perk, isHeroPerk: Boolean) {
        val resourcesIntractor = if (isHeroPerk) {
            heroResourcesInteractor
        } else {
            enemyResourcesInteractor
        }
        perk.run {
            this.resources.forEach { resource ->
                val foundRes =
                    resourcesIntractor.value()?.find { it.name == resource.name }
                foundRes?.decreaseValue(resource.amount)
                resourcesIntractor.refresh()
            }
        }
    }
}
