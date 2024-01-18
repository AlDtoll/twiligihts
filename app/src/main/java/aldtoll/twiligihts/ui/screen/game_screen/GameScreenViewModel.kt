package aldtoll.twiligihts.ui.screen.game_screen

import aldtoll.twiligihts.logic.EndTurnExecutor
import aldtoll.twiligihts.logic.FillEnemyExecutor
import aldtoll.twiligihts.logic.FillPersonExecutor
import aldtoll.twiligihts.logic.PerkExecutor
import aldtoll.twiligihts.logic.UpdateStockExecutor
import aldtoll.twiligihts.model.Gem
import aldtoll.twiligihts.model.Perk
import aldtoll.twiligihts.storage.BattleLogListInteractor
import aldtoll.twiligihts.storage.EnemyInteractor
import aldtoll.twiligihts.storage.HandsListInteractor
import aldtoll.twiligihts.storage.HeroInteractor
import aldtoll.twiligihts.storage.StockListInteractor
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GameScreenViewModel @Inject constructor(
    private val fillPersonExecutor: FillPersonExecutor,
    private val fillEnemyExecutor: FillEnemyExecutor,
    private val stockListInteractor: StockListInteractor,
    private val handsListInteractor: HandsListInteractor,
    private val updateStockExecutor: UpdateStockExecutor,
    private val heroInteractor: HeroInteractor,
    private val enemyInteractor: EnemyInteractor,
    private val perkExecutor: PerkExecutor,
    private val endTurnExecutor: EndTurnExecutor,
    private val battleLogListInteractor: BattleLogListInteractor,
) : ViewModel() {

    fun crushGems(removedGems: MutableList<Gem>) {
        updateStockExecutor.addValueFromCrushedGems(removedGems)
    }

    fun initPerson() {
        fillPersonExecutor.execute()
    }

    fun initEnemy() {
        fillEnemyExecutor.execute()
    }

    override fun onCleared() {
        battleLogListInteractor.update(arrayListOf())
        handsListInteractor.update(arrayListOf())
        super.onCleared()
    }

    fun stockData() = stockListInteractor.get()
    fun handsData() = handsListInteractor.get()

    fun personData() = heroInteractor.get()
    fun enemyData() = enemyInteractor.get()
    fun logData() = battleLogListInteractor.get()
    fun clickPerk(perk: Perk) {
        perkExecutor.execute(perk, true)
    }

    fun endTurn() {
        endTurnExecutor.execute()
    }
}