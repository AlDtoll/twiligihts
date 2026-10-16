package aldtoll.twiligihts.logic.database

import aldtoll.twiligihts.App
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.FirebaseDatabase

/**
 * Состояние debug-режима «локальная сцена» + изолированная офлайн-база.
 *
 * Изоляция от боевой базы: отдельный FirebaseApp "local" с ПОДДЕЛЬНЫМИ options
 * (другой projectId/databaseUrl), persistence выключен, goOffline() сразу после создания.
 * Любая запись в этот экземпляр остаётся в памяти процесса и до боевой базы дойти не может.
 */
object LocalScenes {
    const val TAG = "LocalScenes"
    private const val APP_NAME = "local"
    private const val FAKE_DATABASE_URL = "https://local-scenes-offline.firebaseio.com"

    /** true, пока текущий PREFIX — локальная сцена. Выставляется только из DatabaseInteractor. */
    @Volatile
    var active = false

    private var database: FirebaseDatabase? = null

    @Synchronized
    fun localDatabase(): FirebaseDatabase {
        database?.let { return it }
        val context = App.instance.applicationContext
        val app = try {
            FirebaseApp.getInstance(APP_NAME)
        } catch (e: IllegalStateException) {
            // приложения "local" ещё нет — создаём с фейковыми параметрами
            val options = FirebaseOptions.Builder()
                .setApplicationId("1:000000000000:android:0000000000000000")
                .setApiKey("fake-local-scenes-api-key")
                .setProjectId("local-scenes-offline")
                .setDatabaseUrl(FAKE_DATABASE_URL)
                .build()
            FirebaseApp.initializeApp(context, options, APP_NAME)
        }
        val created = FirebaseDatabase.getInstance(app)
        // порядок важен: оба вызова до первого обращения к базе
        created.setPersistenceEnabled(false)
        created.goOffline()
        database = created
        return created
    }
}
