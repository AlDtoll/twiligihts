package aldtoll.twiligihts

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {

    companion object {
        const val MYTAG = "MY"
        const val APP_PREFS = "app_prefs"
        lateinit var instance: App

        fun getPrefs() =
            instance.applicationContext.getSharedPreferences(APP_PREFS, Context.MODE_PRIVATE)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}