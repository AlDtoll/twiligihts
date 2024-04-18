package aldtoll.twiligihts.logic

import aldtoll.twiligihts.storage.hero.HeroHandsListInteractor
import aldtoll.twiligihts.storage.hero.HeroInteractor
import aldtoll.twiligihts.storage.hero.HeroResourcesInteractor
import aldtoll.twiligihts.storage.hero.HeroStatesInteractor
import aldtoll.twiligihts.storage.hero.HeroStatusesInteractor
import aldtoll.twiligihts.storage.hero.HeroStockListInteractor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FillHeroExecutor @Inject constructor(
    private val heroStockListInteractor: HeroStockListInteractor,
    private val heroHandsListInteractor: HeroHandsListInteractor,
    private val heroInteractor: HeroInteractor,
    private val heroStatesInteractor: HeroStatesInteractor,
    private val heroStatusesInteractor: HeroStatusesInteractor,
    private val heroResourcesInteractor: HeroResourcesInteractor,
) {

    fun execute() {
        heroHandsListInteractor.init()
        heroStockListInteractor.init()
        heroInteractor.init()
        heroStatesInteractor.init()
        heroStatusesInteractor.init()
        heroResourcesInteractor.init()
        //todo пока здесь, чтобы не убирать Person.statuses
        heroStatusesInteractor.value()?.run {
            heroInteractor.value()?.statuses = this
        }
    }
}