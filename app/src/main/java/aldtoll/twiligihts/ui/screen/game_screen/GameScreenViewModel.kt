package aldtoll.twiligihts.ui.screen.game_screen

import aldtoll.twiligihts.logic.EndTurnExecutor
import aldtoll.twiligihts.logic.FillEnemyExecutor
import aldtoll.twiligihts.logic.FillHeroExecutor
import aldtoll.twiligihts.logic.InitSettingsExecutor
import aldtoll.twiligihts.logic.PerkExecutor
import aldtoll.twiligihts.logic.UpdateStockExecutor
import aldtoll.twiligihts.model.ExecutedPerk
import aldtoll.twiligihts.model.Gem
import aldtoll.twiligihts.model.Hand
import aldtoll.twiligihts.model.Perk
import aldtoll.twiligihts.model.Perk.Companion.EMPTY_PERK
import aldtoll.twiligihts.storage.BattleLogListInteractor
import aldtoll.twiligihts.storage.EnemyMoveEventInteractor
import aldtoll.twiligihts.storage.ExecutedPerkInteractor
import aldtoll.twiligihts.storage.GoToFinishScreenInteractor
import aldtoll.twiligihts.storage.StartTimerAgainEventInteractor
import aldtoll.twiligihts.storage.TurnNumberInteractor
import aldtoll.twiligihts.storage.enemy.EnemyHandsListInteractor
import aldtoll.twiligihts.storage.enemy.EnemyInteractor
import aldtoll.twiligihts.storage.hero.HeroHandsListInteractor
import aldtoll.twiligihts.storage.hero.HeroInteractor
import aldtoll.twiligihts.storage.hero.HeroResourcesInteractor
import aldtoll.twiligihts.storage.hero.HeroStockListInteractor
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
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
    private val turnNumberInteractor: TurnNumberInteractor,
    private val goToFinishScreenInteractor: GoToFinishScreenInteractor,
    private val executedPerkInteractor: ExecutedPerkInteractor,
    private val startTimerAgainEventInteractor: StartTimerAgainEventInteractor,
    private val heroResourcesInteractor: HeroResourcesInteractor,
    private val enemyMoveEventInteractor: EnemyMoveEventInteractor,
) : ViewModel() {

    fun crushGems(removedGems: MutableList<Gem>, heroTurn: Boolean) {
        //todo обновление гемов противника
        if (heroTurn) {
            updateStockExecutor.addValueFromCrushedGems(removedGems)
        }
    }

    fun startTurnAgainEventData() = startTimerAgainEventInteractor.get()

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
    fun executePerk(perk: Perk, isHero: Boolean = true) {
        perkExecutor.execute(perk, isHero)
    }

    fun endTurn() {
        endTurnExecutor.execute()
    }

    fun initBattle() {
        initSettingsExecutor.execute()
        fillHeroExecutor.execute()
        fillEnemyExecutor.execute()
        turnNumberInteractor.init()
        executedPerkInteractor.update(
            ExecutedPerk(EMPTY_PERK, Hand())
        )
        battleLogListInteractor.add("Ход ${turnNumberInteractor.value()}")
        battleLogListInteractor.add("Действует ${heroInteractor.value()?.name}")

        perkExecutor.updatePersonsStates()
    }

    fun updatePerksState() {
        updateStockExecutor.updatePerksState()
    }

    fun eventGoToFinishScreen() = goToFinishScreenInteractor.get()
    fun enemySparkData(): LiveData<ExecutedPerk> = executedPerkInteractor.get()
    fun enemyMoveData(): Flow<Unit> = enemyMoveEventInteractor.get()
    fun afterEnemyActions() {
        endTurnExecutor.afterEnemyAction()
        executedPerkInteractor.update(
            ExecutedPerk(EMPTY_PERK, Hand())
        )
    }

    fun callNextPerk(perk: Perk) {
        perkExecutor.callNextPerk(perk)
    }

    fun messageAboutUsedPerk(perk: Perk, isHeroPerk: Boolean) {
        perkExecutor.messageAboutUsedPerk(perk, isHeroPerk)
    }

    fun logPoints(heroTurn: Boolean) {
        //todo подсчет очков за ход
        //todo очки противника
        if (heroTurn) {
            val value = heroStockListInteractor.value()
            var message = "Очков: "
            value?.forEach {
                message += " ${it.value} ${Gem.getName(it.gemType)};"
            }
            battleLogListInteractor.add(message, Gem.LOG_COLOR)
        }
    }

    fun logTime(timeSpentForTurnInSeconds: Long) {
        battleLogListInteractor.add("Время хода:${timeSpentForTurnInSeconds}", Gem.LOG_COLOR)
    }

    fun heroResourcesData() = heroResourcesInteractor.get()

    fun startEnemyTurn() {
        endTurnExecutor.startEnemyTurn()
    }

    fun messageAboutEvaluateMove() {
        battleLogListInteractor.add("Противник думает...")
    }

    fun messageAboutMakeMove() {
        battleLogListInteractor.add("Противник ходит")
    }
}