package aldtoll.twiligihts.model

import com.google.firebase.database.Exclude
import java.util.UUID

data class BattleEvent(
    val message: String,
    @Exclude
    val uuid: UUID = UUID.randomUUID(),
    val gemType: Int = 0
)
