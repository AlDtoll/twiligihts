package aldtoll.twiligihts.ui.screen.game_screen

import aldtoll.twiligihts.domain.usecase.battlelog.AddBattleLogEntryUseCase
import aldtoll.twiligihts.domain.usecase.battlelog.ClearBattleLogUseCase
import aldtoll.twiligihts.domain.usecase.battlelog.GetBattleLogUseCase
import aldtoll.twiligihts.logic.EndTurnExecutor
import aldtoll.twiligihts.logic.FillEnemyExecutor
import aldtoll.twiligihts.logic.FillHeroExecutor
import aldtoll.twiligihts.logic.InitSettingsExecutor
import aldtoll.twiligihts.logic.PerkExecutor
import aldtoll.twiligihts.logic.TimePerkExecutor
import aldtoll.twiligihts.logic.UpdatePerksStateExecutor
import aldtoll.twiligihts.logic.UpdateStockExecutor
import aldtoll.twiligihts.model.CrushedCell
import aldtoll.twiligihts.model.ExecutedPerk
import aldtoll.twiligihts.model.Gem
import aldtoll.twiligihts.model.MatchGroupInfo
import aldtoll.twiligihts.model.Perk
import aldtoll.twiligihts.storage.EnemyMoveEventInteractor
import aldtoll.twiligihts.storage.ExecutedPerkInteractor
import aldtoll.twiligihts.storage.GoToFinishScreenInteractor
import aldtoll.twiligihts.storage.StartTimerAgainEventInteractor
import aldtoll.twiligihts.storage.TimeSecondsInteractor
import aldtoll.twiligihts.storage.TurnNumberInteractor
import aldtoll.twiligihts.storage.common.RemoteMessageInteractor
import aldtoll.twiligihts.storage.enemy.EnemyHandsListInteractor
import aldtoll.twiligihts.storage.enemy.EnemyInteractor
import aldtoll.twiligihts.storage.enemy.EnemySectorsInteractor
import aldtoll.twiligihts.storage.hero.HeroHandsListInteractor
import aldtoll.twiligihts.storage.hero.HeroInteractor
import aldtoll.twiligihts.storage.hero.HeroResourcesInteractor
import aldtoll.twiligihts.storage.hero.HeroStockListInteractor
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class GameScreenViewModel @Inject constructor(
    private val fillHeroExecutor: FillHeroExecutor,
    private val fillEnemyExecutor: FillEnemyExecutor,
    private val heroStockListInteractor: HeroStockListInteractor,
    private val heroHandsListInteractor: HeroHandsListInteractor,
    private val enemyHandsListInteractor: EnemyHandsListInteractor,
    private val enemySectorsInteractor: EnemySectorsInteractor,
    private val updateStockExecutor: UpdateStockExecutor,
    private val heroInteractor: HeroInteractor,
    private val enemyInteractor: EnemyInteractor,
    private val perkExecutor: PerkExecutor,
    private val endTurnExecutor: EndTurnExecutor,
    private val initSettingsExecutor: InitSettingsExecutor,
    private val turnNumberInteractor: TurnNumberInteractor,
    private val goToFinishScreenInteractor: GoToFinishScreenInteractor,
    private val executedPerkInteractor: ExecutedPerkInteractor,
    private val startTimerAgainEventInteractor: StartTimerAgainEventInteractor,
    private val heroResourcesInteractor: HeroResourcesInteractor,
    private val enemyMoveEventInteractor: EnemyMoveEventInteractor,
    private val updatePerksStateExecutor: UpdatePerksStateExecutor,
    private val remoteMessageInteractor: RemoteMessageInteractor,
    private val coverBoardStateInteractor: CoverBoardStateInteractor,
    private val timeSecondsInteractor: TimeSecondsInteractor,
    private val timePerkExecutor: TimePerkExecutor,

    // Battle Log (рефакторинг логов)
    private val addBattleLogEntryUseCase: AddBattleLogEntryUseCase,
    private val getBattleLogUseCase: GetBattleLogUseCase,
    private val clearBattleLogUseCase: ClearBattleLogUseCase,
) : ViewModel() {

    /**
     * StateFlow с логами боя.
     */
    val battleLog: StateFlow<List<aldtoll.twiligihts.model.BattleEvent>> = getBattleLogUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun crushGems(
        crushedCells: List<CrushedCell>,
        groups: List<MatchGroupInfo>,
        heroTurn: Boolean
    ) {
        updateStockExecutor.addValueFromCrushedGems(crushedCells, groups, heroTurn)
        perkExecutor.updatePersonsStates()
    }

    fun coverBoardData() = coverBoardStateInteractor.get()

    fun startTurnAgainEventData() = startTimerAgainEventInteractor.get()

    override fun onCleared() {
//        heroHandsListInteractor.update(arrayListOf())
        super.onCleared()
    }

    fun stockData() = heroStockListInteractor.get()
    fun heroHandsData() = heroHandsListInteractor.get()
    fun enemyHandsData() = enemyHandsListInteractor.get()
    fun sectorsData() = enemySectorsInteractor.get()

    fun personData() = heroInteractor.get()
    fun enemyData() = enemyInteractor.get()

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
        timeSecondsInteractor.update(0)
        executedPerkInteractor.stopRunning()
        coverBoardStateInteractor.update(View.GONE)
        clearBattleLogUseCase()
        addBattleLogEntryUseCase("Ход ${turnNumberInteractor.value()}")
        addBattleLogEntryUseCase("Действует ${heroInteractor.value()?.name}")

        perkExecutor.updatePersonsStates()
    }

    fun updatePerksState() {
        updatePerksStateExecutor.updateEnableStatus()
    }

    fun eventGoToFinishScreen() = goToFinishScreenInteractor.get()
    fun enemySparkData(): LiveData<ExecutedPerk> = executedPerkInteractor.get()
    fun enemyMoveData(): Flow<Unit> = enemyMoveEventInteractor.get()

    fun afterEnemyActions() {
        endTurnExecutor.afterEnemyAction()
        executedPerkInteractor.stopRunning()
    }

    /**
     * передача текущего навыка, чтобы он был найден в руке и среди навыков
     * от него будет вызван следующий
     */
    fun callNextPerk(perk: Perk) {
        perkExecutor.callNextPerk(perk)
    }

    fun messageAboutUsedPerk(perk: Perk, isHeroPerk: Boolean) {
        perkExecutor.messageAboutUsedPerk(perk, isHeroPerk)
    }

    /**
     * Пишет в лог, сколько очков герой собрал за текущий ход (прирост за ход).
     */
    fun logPoints(heroTurn: Boolean) {
        if (!heroTurn) return
        val current = heroStockListInteractor.value() ?: return
        val atTurnStart = heroStockListInteractor.getStocksAtTurnStart() ?: return
        val atStartByType = atTurnStart.associate { it.gemType to it.value }
        val parts = current.mapNotNull { stock ->
            val startValue = atStartByType[stock.gemType] ?: 0
            val gained = stock.value - startValue
            if (gained > 0) "${Gem.getName(stock.gemType)}: +$gained" else null
        }
        if (parts.isEmpty()) {
            addBattleLogEntryUseCase("За ход очков не собрано", Gem.LOG_COLOR)
        } else {
            addBattleLogEntryUseCase("За ход: ${parts.joinToString(", ")}", Gem.LOG_COLOR)
        }
    }

    fun logTime(timeSpentForTurnInSeconds: Long) {
        addBattleLogEntryUseCase("Время хода:${timeSpentForTurnInSeconds}", Gem.LOG_COLOR)
    }

    fun heroResourcesData() = heroResourcesInteractor.get()

    fun startEnemyTurn() {
        endTurnExecutor.startEnemyTurn()
    }

    fun messageAboutEvaluateMove() {
        addBattleLogEntryUseCase("Противник думает...")
    }

    fun messageAboutMakeMove() {
        addBattleLogEntryUseCase("Противник ходит")
    }

    fun pushData() = remoteMessageInteractor.get()
    fun addMessage(message: String) {
        addBattleLogEntryUseCase(message, Gem.STORY_COLOR)
    }

    fun updateCoverBoard(visible: Int) {
        coverBoardStateInteractor.update(visible)
    }

    /**
     * обновление времени может на статусы повлиять
     */
    fun checkTime(seconds: Int) {
        timeSecondsInteractor.update(seconds)
        perkExecutor.updatePersonsStates()
        timePerkExecutor.checkAndApplyTimePerks(seconds)
    }

    fun timerValue(): Long = (timeSecondsInteractor.value() ?: 0).toLong()
}