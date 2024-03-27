package aldtoll.twiligihts.logic

import aldtoll.twiligihts.model.BattleSettings
import aldtoll.twiligihts.model.Gem
import aldtoll.twiligihts.storage.BattleSettingsInteractor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InitSettingsExecutor @Inject constructor(
    private val battleSettingsInteractor: BattleSettingsInteractor
) {

    fun execute() {
        battleSettingsInteractor.init()
        val value = battleSettingsInteractor.value()
        value?.run {
            Gem.GEM_TYPE_NUMBER = this.types
            Gem.GEM_BONUS_TYPE = this.bonusType
            this.gemSettings.forEach {
                Gem.GEM_MAP[it.type] = it
            }
            BattleSettings.STOP_GENERATE = this.stopGenerate
            BattleSettings.ANIMATE_ENEMY_ACTIONS = this.animateEnemy
        }
    }
}