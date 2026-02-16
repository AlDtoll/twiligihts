package aldtoll.twiligihts.domain.usecase.battlelog

import aldtoll.twiligihts.domain.repository.BattleLogRepository
import aldtoll.twiligihts.model.BattleEvent
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use Case для получения логов боя
 *
 * Возвращает Flow с актуальным списком событий.
 * UI может подписаться на этот Flow и получать обновления при добавлении новых событий.
 *
 * Пример использования:
 * ```
 * // В ViewModel
 * val battleLog: StateFlow<List<BattleEvent>> = getBattleLogUseCase()
 *     .stateIn(
 *         scope = viewModelScope,
 *         started = SharingStarted.WhileSubscribed(5000),
 *         initialValue = emptyList()
 *     )
 *
 * // В UI
 * battleLog.collect { logs ->
 *     adapter.updateData(logs)
 * }
 * ```
 */
class GetBattleLogUseCase @Inject constructor(
    private val repository: BattleLogRepository
) {
    /**
     * Получить Flow со списком событий лога
     *
     * @return Flow<List<BattleEvent>> — поток событий, который обновляется при добавлении новых записей
     */
    operator fun invoke(): Flow<List<BattleEvent>> {
        return repository.observeLogs()
    }
}
