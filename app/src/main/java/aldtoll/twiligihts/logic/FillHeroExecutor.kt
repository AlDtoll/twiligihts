package aldtoll.twiligihts.logic

import aldtoll.twiligihts.storage.hero.HeroHandsListInteractor
import aldtoll.twiligihts.storage.hero.HeroInteractor
import aldtoll.twiligihts.storage.hero.HeroStatesInteractor
import aldtoll.twiligihts.storage.hero.HeroStockListInteractor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FillHeroExecutor @Inject constructor(
    private val heroStockListInteractor: HeroStockListInteractor,
    private val heroHandsListInteractor: HeroHandsListInteractor,
    private val heroInteractor: HeroInteractor,
    private val heroStatesInteractor: HeroStatesInteractor,
) {

    fun execute() {
        heroHandsListInteractor.init()
        heroStockListInteractor.init()
        heroInteractor.init()
        heroStatesInteractor.init()
    }
}