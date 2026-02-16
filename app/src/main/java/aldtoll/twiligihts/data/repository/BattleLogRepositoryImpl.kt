package aldtoll.twiligihts.data.repository

import aldtoll.twiligihts.domain.repository.BattleLogRepository
import aldtoll.twiligihts.model.BattleEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Реализация Repository для логов боя
 *
 * Использует MutableStateFlow для хранения и эмита обновлений.
 * Singleton гарантирует единственный источник данных (Single Source of Truth).
 *
 * Принципы реализации:
 * - Иммутабельность: всегда создаем новый список при изменениях
 * - Thread-safety: StateFlow потокобезопасен
 * - Reactive: автоматические обновления через Flow
 */
@Singleton
class BattleLogRepositoryImpl @Inject constructor() : BattleLogRepository {

    /**
     * Внутреннее хранилище логов
     * MutableStateFlow — для изменений внутри Repository
     */
    private val _logs = MutableStateFlow<List<BattleEvent>>(emptyList())

    /**
     * Наблюдать за логами
     *
     * Возвращает read-only Flow для внешних подписчиков.
     * Гарантирует инкапсуляцию — снаружи нельзя изменить состояние напрямую.
     */
    override fun observeLogs(): Flow<List<BattleEvent>> {
        return _logs.asStateFlow()
    }

    /**
     * Добавить запись в лог
     *
     * Создает новый список (иммутабельность) и эмитит его.
     * Thread-safe благодаря StateFlow.
     */
    override fun addEntry(event: BattleEvent) {
        val currentLogs = _logs.value.toMutableList()
        currentLogs.add(event)
        _logs.value = currentLogs.toList()  // Создаем новый иммутабельный список
    }

    /**
     * Очистить логи
     *
     * Эмитит пустой список.
     */
    override fun clear() {
        _logs.value = emptyList()
    }

    /**
     * Получить текущие логи (snapshot)
     *
     * Возвращает текущее значение StateFlow без подписки.
     */
    override fun getCurrentLogs(): List<BattleEvent> {
        return _logs.value
    }
}
