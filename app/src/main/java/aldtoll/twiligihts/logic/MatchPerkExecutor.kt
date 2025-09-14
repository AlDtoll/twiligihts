package aldtoll.twiligihts.logic

import aldtoll.twiligihts.model.MatchGroupInfo
import aldtoll.twiligihts.storage.hero.HeroRulesInteractor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MatchPerkExecutor @Inject constructor(
    private val perkExecutor: PerkExecutor,
    private val heroRulesInteractor: HeroRulesInteractor,
) {

    fun execute(groups: List<MatchGroupInfo>, heroTurn: Boolean) {
        val rules = heroRulesInteractor.value() ?: emptyList()
        for (group in groups) {
            val matched = rules.filter { rule ->
                (rule.orientation == null || rule.orientation == group.orientation) &&
                        (rule.gemType == null || rule.gemType == group.gemType) &&
                        group.size >= rule.minSize
            }
            for (rule in matched) {
                // Здесь вызываем перк согласно правилу
                perkExecutor.execute(rule.perk, heroTurn)
            }
        }
    }
}


