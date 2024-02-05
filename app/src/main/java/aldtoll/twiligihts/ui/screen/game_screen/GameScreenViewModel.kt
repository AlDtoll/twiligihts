package aldtoll.twiligihts.ui.screen.game_screen

import aldtoll.twiligihts.logic.EndTurnExecutor
import aldtoll.twiligihts.logic.FillEnemyExecutor
import aldtoll.twiligihts.logic.FillHeroExecutor
import aldtoll.twiligihts.logic.InitSettingsExecutor
import aldtoll.twiligihts.logic.PerkExecutor
import aldtoll.twiligihts.logic.UpdateStockExecutor
import aldtoll.twiligihts.logic.database.FinishBattleExecutor
import aldtoll.twiligihts.model.Gem
import aldtoll.twiligihts.model.Perk
import aldtoll.twiligihts.storage.BattleLogListInteractor
import aldtoll.twiligihts.storage.EnemyHandsListInteractor
import aldtoll.twiligihts.storage.EnemyInteractor
import aldtoll.twiligihts.storage.HeroHandsListInteractor
import aldtoll.twiligihts.storage.HeroInteractor
import aldtoll.twiligihts.storage.HeroStockListInteractor
import aldtoll.twiligihts.storage.TurnNumberInteractor
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GameScreenViewModel @Inject constructor(
    private val fillHeroExecutor: FillHeroExecutor,
    private val fillEnemyExecutor: FillEnemyExecutor,
    private val heroStockListInteractor: HeroStockListInteractor,
    private val heroHandsListInteractor: HeroHandsListInteractor,
    private val enemyHandsListInteractor: EnemyHandsListInteractor,
    private val updateStockExecutor: UpdateStockExecutor,
    private val heroInteractor: HeroInteractor,
    private val enemyInteractor: EnemyInteractor,
    private val perkExecutor: PerkExecutor,
    private val endTurnExecutor: EndTurnExecutor,
    private val battleLogListInteractor: BattleLogListInteractor,
    private val initSettingsExecutor: InitSettingsExecutor,
    private val finishBattleExecutor: FinishBattleExecutor,
    private val turnNumberInteractor: TurnNumberInteractor
) : ViewModel() {

    fun crushGems(removedGems: MutableList<Gem>) {
        updateStockExecutor.addValueFromCrushedGems(removedGems)
    }

    override fun onCleared() {
        battleLogListInteractor.update(arrayListOf())
        heroHandsListInteractor.update(arrayListOf())
        super.onCleared()
    }

    fun stockData() = heroStockListInteractor.get()
    fun heroHandsData() = heroHandsListInteractor.get()
    fun enemyHandsData() = enemyHandsListInteractor.get()

    fun personData() = heroInteractor.get()
    fun enemyData() = enemyInteractor.get()
    fun logData() = battleLogListInteractor.get()
    fun clickPerk(perk: Perk) {
        perkExecutor.execute(perk, true)
    }

    fun endTurn() {
        endTurnExecutor.execute()
    }

    fun initBattle() {
        fillHeroExecutor.execute()
        fillEnemyExecutor.execute()
        turnNumberInteractor.init()
        battleLogListInteractor.add("Ход ${turnNumberInteractor.value()}")
    }

    fun finishBattle() {
        finishBattleExecutor.execute()
    }

    fun updatePerksState() {
        updateStockExecutor.updatePerksState()
    }
}