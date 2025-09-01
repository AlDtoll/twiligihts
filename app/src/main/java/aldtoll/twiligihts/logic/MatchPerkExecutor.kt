package aldtoll.twiligihts.logic

import aldtoll.twiligihts.model.MatchGroupInfo
import aldtoll.twiligihts.model.MatchRule
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MatchPerkExecutor @Inject constructor(
    private val perkExecutor: PerkExecutor,
) {

    //    private val rules = matchRules()
    private val rules = emptyList<MatchRule>()

    fun execute(groups: List<MatchGroupInfo>, heroTurn: Boolean) {
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


