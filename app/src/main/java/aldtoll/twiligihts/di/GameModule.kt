package aldtoll.twiligihts.di

import aldtoll.twiligihts.data.repository.BattleLogRepositoryImpl
import aldtoll.twiligihts.domain.repository.BattleLogRepository
import aldtoll.twiligihts.model.GameBoard
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GameModule {

    @Provides
    @Singleton
    fun provideGameBoard(): GameBoard {
        return GameBoard(8, 8)
    }

    /**
     * Предоставляет BattleLogRepository
     */
    @Provides
    @Singleton
    fun provideBattleLogRepository(
        impl: BattleLogRepositoryImpl
    ): BattleLogRepository = impl
}