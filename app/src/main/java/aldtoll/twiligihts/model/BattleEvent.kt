package aldtoll.twiligihts.model

import com.google.firebase.database.Exclude
import java.util.UUID

data class BattleEvent(
    val message: String,
    @Exclude
    val uuid: UUID = UUID.randomUUID(),
    val gemType: Int = 0,
    /**
     * Сообщение относится к «техническому» слою (например, статус с [Status.log] = true).
     * При включённой настройке «скрывать тех логи» не показывается в списке на экране боя.
     */
    @Exclude
    val isTechnical: Boolean = false,
)
