package aldtoll.twiligihts.logic

import aldtoll.twiligihts.model.TimePerk
import aldtoll.twiligihts.storage.enemy.EnemyTimePerksInteractor
import aldtoll.twiligihts.storage.hero.HeroTimePerksInteractor
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Исполнитель таймерных перков (TimePerk): на конкретной секунде автоматически применяет перки,
 * учитывая их conditionsForDisplay.
 */
@Singleton
class TimePerkExecutor @Inject constructor(
    private val heroTimePerksInteractor: HeroTimePerksInteractor,
    private val enemyTimePerksInteractor: EnemyTimePerksInteractor,
    private val perkExecutor: PerkExecutor,
    private val checkConditionExecutor: CheckConditionExecutor,
) {

    /**
     * Проверяет все таймерные перки и применяет те из них,
     * у которых time совпадает с текущим значением таймера
     * и выполнены conditionsForDisplay (если заданы).
     */
    fun checkAndApplyTimePerks(currentTimeSeconds: Int) {
        applyForList(heroTimePerksInteractor.value(), currentTimeSeconds, isHero = true)
        applyForList(enemyTimePerksInteractor.value(), currentTimeSeconds, isHero = false)
    }

    private fun applyForList(
        list: ArrayList<TimePerk>?,
        currentTimeSeconds: Int,
        isHero: Boolean,
    ) {
        list
            ?.filter { it.time == currentTimeSeconds }
            ?.forEach { timePerk ->
                val perk = timePerk.perk
                val conditions = perk.conditionsForDisplay
                val canShow = conditions.isNullOrEmpty() || conditions.all {
                    checkConditionExecutor.execute(it, isHero)
                }
                if (canShow) {
                    perkExecutor.execute(perk, isHero)
                }
            }
    }
}

