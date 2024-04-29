package aldtoll.twiligihts.logic

import aldtoll.twiligihts.model.Effect
import aldtoll.twiligihts.model.Perk
import aldtoll.twiligihts.storage.enemy.EnemyResourcesInteractor
import aldtoll.twiligihts.storage.hero.HeroResourcesInteractor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EditResourcesExecutor @Inject constructor(
    private val heroResourcesInteractor: HeroResourcesInteractor,
    private val enemyResourcesInteractor: EnemyResourcesInteractor
) {

    fun execute(effect: Effect.EditResources) {
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
        }
    }

    /**
     * изменение может дать отрицательный результат
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
