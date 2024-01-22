package aldtoll.twiligihts.logic

import aldtoll.twiligihts.storage.HeroHandsListInteractor
import aldtoll.twiligihts.storage.HeroInteractor
import aldtoll.twiligihts.storage.StockListInteractor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FillHeroExecutor @Inject constructor(
    private val stockListInteractor: StockListInteractor,
    private val heroHandsListInteractor: HeroHandsListInteractor,
    private val heroInteractor: HeroInteractor,
) {

    fun execute() {
        stockListInteractor.init()
        heroInteractor.init()
        heroHandsListInteractor.init()
    }
}