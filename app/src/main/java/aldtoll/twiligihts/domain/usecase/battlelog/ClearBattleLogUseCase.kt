package aldtoll.twiligihts.domain.usecase.battlelog

import aldtoll.twiligihts.domain.repository.BattleLogRepository
import javax.inject.Inject

/**
 * Use Case для очистки логов боя
 *
 * Используется при начале нового боя или при сбросе состояния.
 */
class ClearBattleLogUseCase @Inject constructor(
    private val repository: BattleLogRepository
) {
    /**
     * Очистить все логи
     */
    operator fun invoke() {
        repository.clear()
    }
}
