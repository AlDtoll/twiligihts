package aldtoll.twiligihts.domain.usecase.battlelog

import aldtoll.twiligihts.domain.repository.BattleLogRepository
import aldtoll.twiligihts.model.BattleEvent
import javax.inject.Inject

/**
 * Use Case для добавления записи в лог боя
 *
 * Инкапсулирует логику добавления событий в лог.
 * Может быть вызван с готовым BattleEvent или с параметрами для создания нового.
 *
 * Примеры использования:
 * ```
 * // Добавить простое сообщение
 * addBattleLogEntryUseCase("Герой атакует")
 *
 * // Добавить сообщение с типом гема (для цветового выделения)
 * addBattleLogEntryUseCase("Критический удар!", Gem.FIRE)
 *
 * // Добавить готовое событие
 * val event = BattleEvent("Особое событие", gemType = Gem.WATER)
 * addBattleLogEntryUseCase(event)
 * ```
 */
class AddBattleLogEntryUseCase @Inject constructor(
    private val repository: BattleLogRepository
) {
    /**
     * Добавить готовое событие в лог
     */
    operator fun invoke(event: BattleEvent) {
        repository.addEntry(event)
    }

    /**
     * Создать и добавить событие в лог
     *
     * @param message Текст сообщения
     * @param gemType Тип гема для цветового выделения (опционально)
     */
    operator fun invoke(message: String, gemType: Int = 0) {
        val event = BattleEvent(message, gemType = gemType)
        repository.addEntry(event)
    }
}
