package aldtoll.twiligihts.logic

import aldtoll.twiligihts.storage.HeroHandsListInteractor
import aldtoll.twiligihts.storage.HeroInteractor
import aldtoll.twiligihts.storage.HeroStockListInteractor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FillHeroExecutor @Inject constructor(
    private val heroStockListInteractor: HeroStockListInteractor,
    private val heroHandsListInteractor: HeroHandsListInteractor,
    private val heroInteractor: HeroInteractor,
) {

    fun execute() {
        heroHandsListInteractor.init()
        heroStockListInteractor.init()
        heroInteractor.init()
    }
}