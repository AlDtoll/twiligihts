package aldtoll.twiligihts.logic.perks

import aldtoll.twiligihts.logic.EditStockExecutor
import aldtoll.twiligihts.model.Stock
import aldtoll.twiligihts.model.effects.Effect
import aldtoll.twiligihts.storage.BattleLogListInteractor
import aldtoll.twiligihts.storage.enemy.EnemyInteractor
import aldtoll.twiligihts.storage.enemy.EnemyStockListInteractor
import aldtoll.twiligihts.storage.hero.HeroInteractor
import aldtoll.twiligihts.storage.hero.HeroStockListInteractor
import javax.inject.Inject
import javax.inject.Singleton

/**
 * обрабатывает эффект редактировать очки
 */
@Singleton
class EditStockHandler @Inject constructor(
    private val battleLogListInteractor: BattleLogListInteractor,
    private val heroInteractor: HeroInteractor,
    private val enemyInteractor: EnemyInteractor,
    private val editStockExecutor: EditStockExecutor,
    private val heroStockListInteractor: HeroStockListInteractor,
    private val enemyStockListInteractor: EnemyStockListInteractor,
) {

    fun handleEffect(effect: Effect.EditStock, isHeroPerk: Boolean) {
        val isHeroTarget = when (effect.target) {
            Effect.EffectTarget.ENEMY -> false
            Effect.EffectTarget.HERO -> true
            Effect.EffectTarget.ALL -> {
                //todo также надо вызвать эффект для противника
                true
            }

            Effect.EffectTarget.SELF -> isHeroPerk
            Effect.EffectTarget.FOE -> !isHeroPerk
        }
        /**
         * добавляет или отнимает значение или устанавливает, в зависимости от
         * [Effect.EditStock.Type]
         */
        when (effect.type) {
            Effect.EditStock.Type.SET -> {
                effect.gemTypes.forEach {
                    editStockExecutor.setStocks(Pair(it, effect.value), isHeroTarget)
                }
            }

            Effect.EditStock.Type.CHANGE -> {
                effect.gemTypes.forEach {
                    editStockExecutor.updateStocks(Pair(it, effect.value), isHeroTarget)
                }
            }

            Effect.EditStock.Type.ADD -> {
                //todo enemy and all
                val value = heroStockListInteractor.value()
                if (value.isNullOrEmpty()) {
                    val arrayListOf = arrayListOf<Stock>()
                    effect.gemTypes.forEach {
                        arrayListOf.add(Stock(effect.value, it))
                    }
                    heroStockListInteractor.update(arrayListOf)
                } else {
                    effect.gemTypes.forEach { effectGemType ->
                        val foundStock = value.find { it.gemType == effectGemType }
                        if (foundStock != null) {
                            value.add((Stock(effect.value, effectGemType)))
                        }
                        value.run {
                            heroStockListInteractor.update(value)
                        }
                    }
                }
            }

            Effect.EditStock.Type.REMOVE -> {
                //todo enemy and all
                val value = heroStockListInteractor.value()
                if (!value.isNullOrEmpty()) {
                    effect.gemTypes.forEach { effectGemType ->
                        val foundStock = value.find { it.gemType == effectGemType }
                        if (foundStock != null) {
                            value.remove(foundStock)
                        }
                        value.run {
                            heroStockListInteractor.update(value)
                        }
                    }
                }
            }
        }
    }
}