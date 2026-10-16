package aldtoll.twiligihts.di

import aldtoll.twiligihts.BuildConfig
import aldtoll.twiligihts.logic.database.LocalScenes
import com.google.firebase.database.FirebaseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Единая точка получения FirebaseDatabase.
 *
 * Без scope: потребители берут экземпляр через Provider<FirebaseDatabase> в момент использования,
 * потому что режим (боевая база / локальная сцена) переключается в рантайме по префиксу.
 * В release BuildConfig.LOCAL_SCENES == false -> всегда стандартный FirebaseDatabase.getInstance()
 * (то же самое, что раньше давал Firebase.database).
 */
@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    fun provideFirebaseDatabase(): FirebaseDatabase =
        if (BuildConfig.LOCAL_SCENES && LocalScenes.active) {
            LocalScenes.localDatabase()
        } else {
            FirebaseDatabase.getInstance()
        }
}
