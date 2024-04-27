package aldtoll.twiligihts.logic.database

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
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
                    saveStartedData(dataSnapshot)
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

    abstract fun saveStartedData(dataSnapshot: DataSnapshot)
    abstract fun getClazzForDataBase(): Any

    abstract fun getNameForDataBase(): Any
}