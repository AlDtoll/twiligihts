package aldtoll.twiligihts

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {

    companion object {
        const val MYTAG = "MY"
    }

    override fun onCreate() {
        super.onCreate()
    }
}