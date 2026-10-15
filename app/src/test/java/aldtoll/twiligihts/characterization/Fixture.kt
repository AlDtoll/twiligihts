package aldtoll.twiligihts.characterization

import aldtoll.twiligihts.data.repository.BattleLogRepositoryImpl
import aldtoll.twiligihts.domain.usecase.battlelog.AddBattleLogEntryUseCase
import aldtoll.twiligihts.logic.ApplyAttackExecutor
import aldtoll.twiligihts.logic.ApplyFunctionExecutor
import aldtoll.twiligihts.logic.CheckConditionExecutor
import aldtoll.twiligihts.logic.EditResourcesExecutor
import aldtoll.twiligihts.logic.EditStockExecutor
import aldtoll.twiligihts.logic.EndTurnExecutor
import aldtoll.twiligihts.logic.MatchPerkExecutor
import aldtoll.twiligihts.logic.PerkExecutor
import aldtoll.twiligihts.logic.StockPerkExecutor
import aldtoll.twiligihts.logic.TimePerkExecutor
import aldtoll.twiligihts.logic.UpdatePerksStateExecutor
import aldtoll.twiligihts.logic.UpdateStockExecutor
import aldtoll.twiligihts.logic.database.WriteTemporaryLogExecutor
import aldtoll.twiligihts.logic.perks.DefendEffectHandler
import aldtoll.twiligihts.logic.perks.EditStatusHandler
import aldtoll.twiligihts.logic.perks.EditStockHandler
import aldtoll.twiligihts.model.BattleSettings
import aldtoll.twiligihts.model.Gem
import aldtoll.twiligihts.model.Hand
import aldtoll.twiligihts.model.Perk
import aldtoll.twiligihts.model.Resource
import aldtoll.twiligihts.model.Status
import aldtoll.twiligihts.model.Stock
import aldtoll.twiligihts.model.characters.Enemy
import aldtoll.twiligihts.model.characters.Hero
import aldtoll.twiligihts.model.effects.Effect
import aldtoll.twiligihts.storage.AttemptCounterInteractor
import aldtoll.twiligihts.storage.BattleSettingsInteractor
import aldtoll.twiligihts.storage.EffectValueForDescriptionInteractor
import aldtoll.twiligihts.storage.EnemyMoveEventInteractor
import aldtoll.twiligihts.storage.ExecutedPerkInteractor
import aldtoll.twiligihts.storage.GoToFinishScreenInteractor
import aldtoll.twiligihts.storage.PostEnemyTurnLogEventInteractor
import aldtoll.twiligihts.storage.TimeSecondsInteractor
import aldtoll.twiligihts.storage.TurnNumberInteractor
import aldtoll.twiligihts.storage.enemy.EnemyHandsListInteractor
import aldtoll.twiligihts.storage.enemy.EnemyInteractor
import aldtoll.twiligihts.storage.enemy.EnemyResourcesInteractor
import aldtoll.twiligihts.storage.enemy.EnemyStatesInteractor
import aldtoll.twiligihts.storage.enemy.EnemyStockListInteractor
import aldtoll.twiligihts.storage.enemy.EnemyStockPerksInteractor
import aldtoll.twiligihts.storage.enemy.EnemyTimePerksInteractor
import aldtoll.twiligihts.storage.hero.HeroHandsListInteractor
import aldtoll.twiligihts.storage.hero.HeroInteractor
import aldtoll.twiligihts.storage.hero.HeroResourcesInteractor
import aldtoll.twiligihts.storage.hero.HeroRulesInteractor
import aldtoll.twiligihts.storage.hero.HeroStatesInteractor
import aldtoll.twiligihts.storage.hero.HeroStockListInteractor
import aldtoll.twiligihts.storage.hero.HeroStockPerksInteractor
import aldtoll.twiligihts.storage.hero.HeroTimePerksInteractor
import javax.inject.Provider
import org.mockito.Mockito

/**
 * Ручная сборка графа объектов вместо Hilt (как в GameModule / @Inject-конструкторах).
 * Создавать только внутри InstantTaskExecutorRule (в @Before), т.к. init пишет в LiveData.
 * Все тесты используют вероятности 100% — тогда Random в коде не влияет на исход
 * (roll in 0..100 <= 100 всегда истинно).
 */
class Fixture {
    val logRepo = BattleLogRepositoryImpl()
    val addLog = AddBattleLogEntryUseCase(logRepo)

    val heroI = HeroInteractor()
    val enemyI = EnemyInteractor()
    val turnI = TurnNumberInteractor()
    val timeI = TimeSecondsInteractor()
    val attemptI = AttemptCounterInteractor()
    val heroResI = HeroResourcesInteractor()
    val enemyResI = EnemyResourcesInteractor()
    val heroStockI = HeroStockListInteractor()
    val enemyStockI = EnemyStockListInteractor()
    val heroHandsI = HeroHandsListInteractor()
    val enemyHandsI = EnemyHandsListInteractor()
    val heroStatesI = HeroStatesInteractor()
    val enemyStatesI = EnemyStatesInteractor()
    val heroStockPerksI = HeroStockPerksInteractor()
    val enemyStockPerksI = EnemyStockPerksInteractor()
    val heroTimePerksI = HeroTimePerksInteractor()
    val enemyTimePerksI = EnemyTimePerksInteractor()
    val heroRulesI = HeroRulesInteractor()
    val settingsI = BattleSettingsInteractor()
    val goToFinishI = GoToFinishScreenInteractor()
    val executedPerkI = ExecutedPerkInteractor()
    val effDescI = EffectValueForDescriptionInteractor()
    val postEnemyTurnI = PostEnemyTurnLogEventInteractor()
    val enemyMoveI = EnemyMoveEventInteractor()

    val check = CheckConditionExecutor(
        heroI, enemyI, turnI, enemyResI, heroResI, heroStockI, enemyStockI, timeI, attemptI
    )
    val updatePerks = UpdatePerksStateExecutor(
        heroHandsI, enemyHandsI, heroStockI, enemyStockI, heroResI, enemyResI, check
    )
    private var perkExecutorRef: PerkExecutor? = null
    val stockPerk = StockPerkExecutor(
        heroStockPerksI, enemyStockPerksI, heroStockI, enemyStockI,
        Provider { perkExecutorRef!! }, check
    )
    val editStock = EditStockExecutor(
        heroStockI, enemyStockI, settingsI, updatePerks, heroI, enemyI, stockPerk
    )
    val applyAttack = ApplyAttackExecutor(addLog, editStock, enemyI, heroI, effDescI, check)
    val editRes = EditResourcesExecutor(heroResI, enemyResI)
    val applyFunc = ApplyFunctionExecutor(heroI, enemyI, check)
    val defendH = DefendEffectHandler(addLog, heroI, enemyI)
    val editStatusH = EditStatusHandler(addLog, heroI, enemyI)
    val editStockH = EditStockHandler(editStock, heroStockI, enemyStockI, stockPerk)
    val perk = PerkExecutor(
        editStock, heroI, enemyI, addLog, heroHandsI, enemyHandsI, heroStockI, enemyStockI,
        heroResI, enemyResI, goToFinishI, enemyStatesI, heroStatesI, executedPerkI, check,
        editRes, applyAttack, updatePerks, effDescI, defendH, editStatusH, editStockH, applyFunc
    ).also { perkExecutorRef = it }
    val matchPerk = MatchPerkExecutor(perk, heroRulesI)
    val updateStock = UpdateStockExecutor(
        heroStockI, heroI, enemyI, enemyStockI, updatePerks, matchPerk, stockPerk, perk, check
    )
    /** Реальный класс в конструкторе обращается к Firebase — подменяем mock'ом (mockito-inline: класс final). */
    val writeTempLog: WriteTemporaryLogExecutor = Mockito.mock(WriteTemporaryLogExecutor::class.java)
    val endTurn = EndTurnExecutor(
        perk, enemyI, heroHandsI, enemyHandsI, heroI, addLog, editStock, updatePerks, turnI,
        executedPerkI, postEnemyTurnI, enemyMoveI, applyAttack, writeTempLog
    )
    val timePerk = TimePerkExecutor(heroTimePerksI, enemyTimePerksI, perk, check)

    init {
        resetGlobals()
        heroI.update(Hero(name = "Hero", hp = 20, maxHp = 20, shield = 0, wounds = 0, maxWounds = 0))
        enemyI.update(Enemy(name = "Enemy", hp = 30, maxHp = 30, wounds = 0, maxWounds = 0, shield = 0))
        heroHandsI.update(arrayListOf())
        enemyHandsI.update(arrayListOf())
        heroStockI.update(arrayListOf())
        enemyStockI.update(arrayListOf())
        heroResI.update(arrayListOf())
        enemyResI.update(arrayListOf())
        settingsI.update(BattleSettings())
    }

    var hero: Hero
        get() = heroI.value()!!
        set(v) = heroI.update(v)
    var enemy: Enemy
        get() = enemyI.value()!!
        set(v) = enemyI.update(v)

    fun heroStocks(vararg s: Stock) = heroStockI.update(arrayListOf(*s))
    fun enemyStocks(vararg s: Stock) = enemyStockI.update(arrayListOf(*s))
    fun heroStock(gemType: Int) = heroStockI.value()!!.first { it.gemType == gemType }.value
    fun enemyStock(gemType: Int) = enemyStockI.value()!!.first { it.gemType == gemType }.value
    fun heroRes(vararg r: Resource) = heroResI.update(arrayListOf(*r))

    /** Положить перки в руку героя/врага (с init, как HandsListInteractor.init). */
    fun heroHand(vararg perks: Perk, gemType: Int = 1): Hand {
        val hand = Hand("hand", gemType = gemType, perks = arrayListOf(*perks))
        hand.init(heroHand = true)
        heroHandsI.update(arrayListOf(hand))
        return hand
    }

    fun enemyHands(vararg hands: Hand) {
        hands.forEach { it.init(false) }
        enemyHandsI.update(arrayListOf(*hands))
    }

    fun logs(): List<String> = logRepo.getCurrentLogs().map { it.message }

    companion object {
        fun resetGlobals() {
            BattleSettings.ANIMATE_ENEMY_ACTIONS = false
            BattleSettings.MAKE_ENEMY_MOVE = false
            BattleSettings.STOP_GENERATE = false
            BattleSettings.GOD_MODE = false
            PerkExecutor.ENABLE_DODGE = true
            Gem.GEM_MAP = hashMapOf()
            Gem.GEM_TYPE_NUMBER = 4
        }

        fun status(
            name: String,
            type: Status.StatusType,
            value: Int,
            duration: Int = 1,
            times: Int? = null,
            end: Boolean = false,
            gemTypes: ArrayList<Int> = arrayListOf(),
            smartValue: Int? = null,
            probability: Int = 100,
        ) = Status(
            name = name, value = value, type = type, duration = duration, times = times,
            end = end, gemTypes = gemTypes, smartValue = smartValue, probability = probability
        )

        fun perkOf(name: String, vararg effects: Effect, price: Int = 0, gemType: Int = 1) =
            Perk(name = name, prices = arrayListOf(Perk.Price(price, gemType)), effects = arrayListOf(*effects))
    }
}
