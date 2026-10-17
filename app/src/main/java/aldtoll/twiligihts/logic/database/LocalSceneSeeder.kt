package aldtoll.twiligihts.logic.database

import aldtoll.twiligihts.App
import aldtoll.twiligihts.BuildConfig
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import org.json.JSONObject

/**
 * Debug-only: загружает сцену из assets (debug sourceSet = папка example/) в локальную
 * офлайн-базу, чтобы существующие слушатели и *Ext.kt-парсеры работали без изменений.
 *
 * Имя сцены: "local:<путь к json внутри example, без .json>",
 * например "local:ala/10/6_adapt/ala_6_adapt".
 */
object LocalSceneSeeder {
    const val NAME_PREFIX = "local:"

    // запрещённые символы ключей Firebase + управляющие
    private val illegalKeyChars = Regex("""[./#${'$'}\[\]\u0000-\u001F\u007F]""")

    /** В release BuildConfig.LOCAL_SCENES == false -> всегда false. */
    fun isLocalName(name: String): Boolean =
        BuildConfig.LOCAL_SCENES && name.startsWith(NAME_PREFIX)

    /** Путь к json в assets: без "local:", без ведущего '/', с расширением .json. */
    fun assetPath(name: String): String {
        val path = name.removePrefix(NAME_PREFIX).trim().trimStart('/')
        return if (path.endsWith(".json")) path else "$path.json"
    }

    /** PREFIX для локальной сцены: путь без "local:" и .json, запрещённые символы -> '_'. */
    fun prefixFor(name: String): String {
        val path = assetPath(name).removeSuffix(".json")
        return illegalKeyChars.replace(path, "_").ifBlank { "LocalScene" }
    }

    /**
     * Читает сцену и кладёт её в локальную базу по пути [prefix].
     * Ошибки (нет файла / битый JSON / нелегальные ключи) логируются и показываются Toast,
     * исключение наружу не выбрасывается: сцена остаётся пустой, как для неизвестного префикса.
     */
    fun seed(name: String, prefix: String): Boolean {
        val path = assetPath(name)
        return try {
            require(!path.split('/').contains("..")) { "недопустимый путь: $path" }
            val text = App.instance.assets.open(path).bufferedReader(Charsets.UTF_8)
                .use { it.readText() }
            val map = JsonToMap.toMap(JSONObject(text))
            LocalScenes.localDatabase().getReference(prefix).setValue(map)
            Log.i(LocalScenes.TAG, "Loaded scene $path as /$prefix, keys=${map.size}")
            true
        } catch (e: Exception) {
            Log.e(LocalScenes.TAG, "Failed to load local scene $path", e)
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(
                    App.instance,
                    "Локальная сцена не загружена: $path (${e.javaClass.simpleName})",
                    Toast.LENGTH_LONG
                ).show()
            }
            false
        }
    }
}
