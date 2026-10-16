package aldtoll.twiligihts.logic.database

import org.json.JSONArray
import org.json.JSONObject

/**
 * org.json -> Map/List для DatabaseReference.setValue.
 * Целые числа -> Long (как отдаёт Firebase), дробные -> Double. null-значения опускаются
 * (в Firebase null = отсутствие ключа), как и при реальной загрузке JSON.
 */
object JsonToMap {

    fun toMap(json: JSONObject): Map<String, Any?> {
        val result = LinkedHashMap<String, Any?>()
        val keys = json.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val value = convert(json.get(key))
            if (value != null) {
                result[key] = value
            }
        }
        return result
    }

    fun toList(array: JSONArray): List<Any?> =
        (0 until array.length()).map { convert(array.get(it)) }

    private fun convert(value: Any?): Any? = when {
        value == null || value == JSONObject.NULL -> null
        value is JSONObject -> toMap(value)
        value is JSONArray -> toList(value)
        value is Int || value is Short || value is Byte -> (value as Number).toLong()
        value is Long -> value
        value is Number -> value.toDouble()
        value is Boolean || value is String -> value
        else -> value.toString()
    }
}
