package aldtoll.twiligihts

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {

    companion object {

    }

    override fun onCreate() {
        super.onCreate()
    }
}