package aldtoll.twiligihts.domain.repository

import aldtoll.twiligihts.model.BattleEvent
import kotlinx.coroutines.flow.Flow

/**
 * Repository для логов боя
 *
 * Определяет контракт для работы с логами.
 * Реализация находится в data слое.
 *
 * Принципы:
 * - Иммутабельность: методы возвращают новые списки, а не изменяют существующие
 * - Reactive: использует Flow для автоматического обновления UI
 * - Single Source of Truth: единственный источник данных о логах
 */
interface BattleLogRepository {
    /**
     * Наблюдать за логами
     *
     * Возвращает Flow который эмитит новый список при каждом изменении.
     * UI подписывается на этот Flow и автоматически обновляется.
     *
     * @return Flow<List<BattleEvent>> — поток с иммутабельным списком событий
     */
    fun observeLogs(): Flow<List<BattleEvent>>

    /**
     * Добавить запись в лог
     *
     * Добавляет событие в конец списка и эмитит обновленный список в Flow.
     *
     * @param event Событие для добавления
     */
    fun addEntry(event: BattleEvent)

    /**
     * Очистить логи
     *
     * Удаляет все события и эмитит пустой список в Flow.
     */
    fun clear()

    /**
     * Получить текущие логи (snapshot)
     *
     * Возвращает текущее состояние логов без подписки на обновления.
     * Полезно для одноразовых проверок или синхронизации.
     *
     * @return List<BattleEvent> — иммутабельный список текущих событий
     */
    fun getCurrentLogs(): List<BattleEvent>
}
