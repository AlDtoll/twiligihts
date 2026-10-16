package aldtoll.twiligihts.logic.database

import aldtoll.twiligihts.App
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

abstract class IDownloadFromDataBase {

    private var databaseReference: DatabaseReference? = null
    private lateinit var valueEventListener: ValueEventListener
    fun downloadFromDatabase(database: FirebaseDatabase) {
        if (!::valueEventListener.isInitialized) {
            valueEventListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    try {
                        saveStartedData(dataSnapshot)
                    } catch (e: DatabaseException) {
                        // неизвестное значение/тип в данных сцены: не роняем приложение,
                        // узел остаётся незагруженным (как пустой)
                        reportParseError(dataSnapshot, e)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Failed to read value
                    Log.w("TAG", "Failed to read value.", error.toException())
                }
            }
        }
        databaseReference?.removeEventListener(valueEventListener)
        databaseReference =
            database.getReference("${DatabaseInteractor.PREFIX}/${getNameForDataBase()}")
        databaseReference?.addValueEventListener(valueEventListener)
    }

    private fun reportParseError(dataSnapshot: DataSnapshot, e: DatabaseException) {
        val node = dataSnapshot.ref.path.toString()
        Log.e(LocalScenes.TAG, "Scene data parse error at $node: ${e.message}", e)
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(
                App.instance,
                "Ошибка в данных сцены ($node): ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    abstract fun saveStartedData(dataSnapshot: DataSnapshot)
    abstract fun getClazzForDataBase(): Any

    abstract fun getNameForDataBase(): Any
}