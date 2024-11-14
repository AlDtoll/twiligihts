package aldtoll.twiligihts

import android.app.Application
import android.content.Context
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {

    companion object {
        const val MYTAG = "MY"
        const val APP_PREFS = "app_prefs"
        lateinit var instance: App
        var MASTER_TOKEN = ""

        fun getPrefs() =
            instance.applicationContext.getSharedPreferences(APP_PREFS, Context.MODE_PRIVATE)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        val database = FirebaseDatabase.getInstance()

        /**
         * кэширует данные и позволяет оффлайн режим использовать
         */
        database.setPersistenceEnabled(true)
        /**
         * принудительно устанавливает https соединение long-polling вместо Web-Soсket
         */
        database.reference.database.reference.keepSynced(true)
    }
}