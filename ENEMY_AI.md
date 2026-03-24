# AI противника в Twilights

## Текущая реализация

### Местоположение кода

`aldtoll.twiligihts.logic.EndTurnExecutor.enemyActions()` (строки 302-322)

### Алгоритм

```kotlin
private fun enemyActions() {
    val enemyHands = enemyHandsListInteractor.value()
    enemyHands?.run {
        this.forEach { hand ->
            // Если рука видна (show = true)
            if (hand.show) {
                hand.perks.forEach { perk: Perk ->
                    // Если навык виден и доступен
                    if (perk.show && perk.enable) {
                        perkExecutor.messageAboutUsedPerk(perk, false)
                        perkExecutor.execute(perk)
                    }
                }
            }
        }
    }
}
```

### Принцип работы

Противник **просто перебирает все свои руки сверху вниз**, и для каждой руки перебирает все навыки
сверху вниз:

1. Берет список рук противника (`EnemyHandsListInteractor`)
2. Для каждой руки проверяет `hand.show`
3. Для каждого навыка в руке проверяет:
    - `perk.show` — навык виден (не скрыт условиями `conditionsForDisplay`)
    - `perk.enable` — навык доступен (достаточно ресурсов, перезарядка закончена, выполнены
      условия `conditionsForEnable`)
4. Если навык доступен — **сразу использует его**
5. Переходит к следующему навыку

### Что определяет доступность навыка (`perk.enable`)?

Флаг `perk.enable` вычисляется в `UpdatePerksStateExecutor.updateEnableStatus()`:

#### Проверки:

1. **Ресурсы (`prices`)**: Достаточно ли очков каждого типа для оплаты навыка?
2. **Перезарядка (`coolDown` / `reload`)**: Закончилась ли перезарядка? (`reload >= coolDown`)
3. **Заряды (`currentCharges`)**: Остались ли заряды для использования?
4. **Условия (`conditionsForEnable`)**: Выполнены ли все условия доступности?
    - Примеры условий:
        - `TURN_NUMBER` — номер хода >= N
        - `HERO_HP` — здоровье героя <= X
        - `ENEMY_HP` — здоровье противника <= Y
        - `HAS_STATUS` — есть ли определенный статус
        - `STOCK_VALUE` — значение ресурса >= Z
        - `TIME_SECONDS` — прошло времени >= T
5. **Ресурсы для навыка (`resources`)**: Выполнены ли специальные требования к ресурсам?

---

## Характеристики текущего AI

### ✅ Преимущества:

1. **Простота и предсказуемость**
    - Легко понять, что сделает противник
    - Простая отладка

2. **Настраиваемость через JSON**
    - Порядок навыков определяет приоритет
    - Условия (`conditionsForEnable`) позволяют контролировать, когда навык доступен
    - Можно задавать сложные условия через Firebase

3. **Декларативное поведение**
    - Поведение описывается данными, а не кодом
    - Легко создавать новых противников без изменения кода

4. **Поддержка сценариев**
    - Можно создать "скриптовое" поведение через условия
    - Например: "Если HP < 30%, использовать лечение"

### ❌ Недостатки:

1. **Нет принятия решений**
    - Противник не выбирает между навыками — он использует первый доступный
    - Не может пропустить неэффективный навык в пользу более полезного

2. **Нет оценки ситуации**
    - Не анализирует:
        - Здоровье героя (для агрессивной тактики)
        - Свое здоровье (для защитной тактики)
        - Доступные ресурсы (для оптимального использования)
        - Статусы и дебаффы
        - Эффективность навыков в текущей ситуации

3. **Предсказуемость**
    - Противник всегда действует одинаково в одной и той же ситуации
    - Игрок быстро понимает паттерн и может злоупотреблять им

4. **Негибкое поведение**
    - Нельзя реализовать:
        - Адаптивную тактику (менять стратегию в зависимости от ситуации)
        - Приоритизацию (лечение важнее атаки при низком HP)
        - Риск/награда расчеты
        - Комбо-действия (использовать навыки в определенной последовательности для синергии)

5. **Сложность настройки через условия**
    - Чтобы сделать "умное" поведение, нужно создавать множество навыков с разными условиями
    - Дублирование навыков с разными `conditionsForEnable`
    - Сложно поддерживать

---

## Примеры поведения

### Пример 1: Простой противник (Гоблин)

#### Руки противника:

```json
[
  {
    "name": "Атака",
    "gemType": 1,
    "perks": [
      {
        "name": "Удар",
        "prices": [
          {
            "value": 3,
            "gemType": 1
          }
        ],
        "effects": [
          {
            "type": "ATTACK",
            "value": 5,
            "target": "HERO"
          }
        ]
      },
      {
        "name": "Сильный удар",
        "prices": [
          {
            "value": 6,
            "gemType": 1
          }
        ],
        "effects": [
          {
            "type": "ATTACK",
            "value": 10,
            "target": "HERO"
          }
        ]
      }
    ]
  },
  {
    "name": "Защита",
    "gemType": 2,
    "perks": [
      {
        "name": "Блок",
        "prices": [
          {
            "value": 4,
            "gemType": 2
          }
        ],
        "effects": [
          {
            "type": "DEFEND",
            "value": 5,
            "target": "ENEMY"
          }
        ]
      }
    ]
  }
]
```

#### Поведение:

1. Если доступны 3+ очка типа 1 → использует "Удар"
2. Если доступны 6+ очков типа 1 → использует "Сильный удар" (только если "Удар" недоступен из-за
   перезарядки)
3. Если доступны 4+ очка типа 2 → использует "Блок"

**Проблема:** Противник **всегда** будет использовать "Удар" даже если у него достаточно ресурсов
для "Сильного удара" (потому что "Удар" идет первым в списке).

---

### Пример 2: Противник с условиями (Тактический орк)

#### Руки противника:

```json
[
  {
    "name": "Выживание",
    "gemType": 3,
    "perks": [
      {
        "name": "Аварийное лечение",
        "prices": [
          {
            "value": 4,
            "gemType": 3
          }
        ],
        "effects": [
          {
            "type": "HEAL",
            "value": 15,
            "target": "ENEMY"
          }
        ],
        "conditionsForEnable": [
          {
            "type": "ENEMY_HP",
            "operator": "<=",
            "value": 20
          }
        ]
      }
    ]
  },
  {
    "name": "Атака",
    "gemType": 1,
    "perks": [
      {
        "name": "Удар",
        "prices": [
          {
            "value": 3,
            "gemType": 1
          }
        ],
        "effects": [
          {
            "type": "ATTACK",
            "value": 5,
            "target": "HERO"
          }
        ]
      }
    ]
  }
]
```

#### Поведение:

1. Если HP <= 20 и есть 4+ очка типа 3 → использует "Аварийное лечение"
2. Если есть 3+ очка типа 1 → использует "Удар"

**Лучше, но всё ещё проблемы:**

- Если HP = 25, а ресурсов для лечения достаточно — не вылечится (условие не выполнено)
- Если HP = 15, но нет 4 очков типа 3 — будет атаковать вместо защиты или копления ресурсов
- Нет способа "пропустить ход" для накопления ресурсов

---

### Пример 3: Попытка имитировать приоритеты

Чтобы сделать "приоритет" лечения, нужно:

1. **Дублировать навык атаки** с условием "HP > 20"
2. **Поместить лечение первым** в списке

```json
[
  {
    "name": "Выживание",
    "gemType": 3,
    "perks": [
      {
        "name": "Аварийное лечение",
        "conditionsForEnable": [
          {
            "type": "ENEMY_HP",
            "operator": "<=",
            "value": 20
          }
        ]
      }
    ]
  },
  {
    "name": "Атака",
    "gemType": 1,
    "perks": [
      {
        "name": "Удар (только когда здоров)",
        "conditionsForEnable": [
          {
            "type": "ENEMY_HP",
            "operator": ">",
            "value": 20
          }
        ]
      },
      {
        "name": "Удар (в любом случае)",
        "conditionsForEnable": []
      }
    ]
  }
]
```

**Проблемы такого подхода:**

- Дублирование навыков
- Сложность поддержки (изменение одного навыка = изменение всех копий)
- Разрастание JSON файлов
- Нет реальной приоритизации — всё зависит от порядка в списке

---

## Как можно улучшить AI

### Вариант 1: Приоритизация навыков

Добавить поле `priority` к навыкам:

```kotlin
data class Perk(
    // ...
    val priority: Int = 0  // Чем выше, тем важнее
)
```

Алгоритм:

1. Собрать все доступные навыки
2. Отсортировать по приоритету (по убыванию)
3. Выполнить навык с наивысшим приоритетом

**Плюсы:**

- Простая реализация
- Легко настраивать через JSON

**Минусы:**

- Всё ещё нет оценки ситуации
- Статичные приоритеты не адаптируются к игровой ситуации

---

### Вариант 2: Стратегии поведения

Создать несколько стратегий противника:

#### Интерфейс стратегии:

```kotlin
interface EnemyStrategy {
    /**
     * Выбирает навык для использования на основе текущего состояния игры
     * @return Выбранный навык или null, если нет подходящего
     */
    fun selectPerk(
        availablePerks: List<Perk>,
        enemy: Enemy,
        hero: Hero,
        enemyStocks: List<Stock>,
        turnNumber: Int
    ): Perk?
}
```

#### Реализации:

**1. SimpleStrategy (текущее поведение)**

```kotlin
class SimpleStrategy : EnemyStrategy {
    override fun selectPerk(...): Perk? {
        // Возвращает первый доступный навык
        return availablePerks.firstOrNull()
    }
}
```

**2. PriorityStrategy (приоритеты)**

```kotlin
class PriorityStrategy : EnemyStrategy {
    override fun selectPerk(...): Perk? {
        // Сортирует по priority, возвращает первый
        return availablePerks
            .sortedByDescending { it.priority }
            .firstOrNull()
    }
}
```

**3. AdaptiveStrategy (оценка ситуации)**

```kotlin
class AdaptiveStrategy : EnemyStrategy {
    override fun selectPerk(...): Perk? {
        // Оценивает каждый навык и выбирает лучший
        return availablePerks
            .map { perk -> perk to evaluatePerk(perk, enemy, hero, enemyStocks) }
            .maxByOrNull { it.second }
            ?.first
    }

    private fun evaluatePerk(
        perk: Perk,
        enemy: Enemy,
        hero: Hero,
        stocks: List<Stock>
    ): Float {
        var score = 0f

        perk.effects.forEach { effect ->
            when (effect) {
                is Effect.Attack -> {
                    // Атака более ценна, если герой на низком HP
                    val heroHpPercent = hero.hp.toFloat() / hero.maxHp
                    score += effect.value * (1.5f - heroHpPercent)
                }
                is Effect.Heal -> {
                    // Лечение более ценно, если противник на низком HP
                    val enemyHpPercent = enemy.hp.toFloat() / enemy.maxHp
                    if (enemyHpPercent < 0.5f) {
                        score += effect.value * 2f
                    }
                }
                is Effect.Defend -> {
                    // Защита ценнее, если противник на низком HP
                    val enemyHpPercent = enemy.hp.toFloat() / enemy.maxHp
                    if (enemyHpPercent < 0.3f) {
                        score += effect.value * 1.5f
                    }
                }
            }
        }

        return score
    }
}
```

**4. AggressiveStrategy (фокус на урон)**

```kotlin
class AggressiveStrategy : EnemyStrategy {
    override fun selectPerk(...): Perk? {
        // Приоритизирует атакующие навыки
        return availablePerks
            .sortedByDescending { perk ->
                perk.effects.filterIsInstance<Effect.Attack>().sumOf { it.value }
            }
            .firstOrNull()
    }
}
```

**5. DefensiveStrategy (фокус на защиту/лечение)**

```kotlin
class DefensiveStrategy : EnemyStrategy {
    override fun selectPerk(...): Perk? {
        // Приоритизирует защиту и лечение
        val enemyHpPercent = enemy.hp.toFloat() / enemy.maxHp

        return if (enemyHpPercent < 0.5f) {
            // При низком HP — лечение/защита
            availablePerks
                .filter { perk ->
                    perk.effects.any { it is Effect.Heal || it is Effect.Defend }
                }
                .firstOrNull()
        } else {
            // Иначе — атака
            availablePerks.firstOrNull()
        }
    }
}
```

---

### Вариант 3: Utility-based AI

Каждый навык оценивается по "полезности" (utility) в текущей ситуации:

```kotlin
class UtilityBasedStrategy : EnemyStrategy {
    override fun selectPerk(...): Perk? {
        return availablePerks
            .map { perk ->
                perk to calculateUtility(perk, enemy, hero, enemyStocks, turnNumber)
            }
            .maxByOrNull { it.second }
            ?.first
    }

    private fun calculateUtility(
        perk: Perk,
        enemy: Enemy,
        hero: Hero,
        stocks: List<Stock>,
        turnNumber: Int
    ): Float {
        var utility = 0f

        val enemyHpRatio = enemy.hp.toFloat() / enemy.maxHp
        val heroHpRatio = hero.hp.toFloat() / hero.maxHp

        perk.effects.forEach { effect ->
            when (effect) {
                is Effect.Attack -> {
                    // Урон более полезен, если герой на низком HP
                    utility += effect.value * evaluateDamageCurve(heroHpRatio)
                }

                is Effect.Heal -> {
                    // Лечение полезно, если противник ранен
                    if (enemyHpRatio < 1.0f) {
                        utility += effect.value * evaluateHealCurve(enemyHpRatio)
                    }
                }

                is Effect.Defend -> {
                    // Защита полезна при низком HP или если герой агрессивен
                    val heroThreatLevel = evaluateHeroThreat(hero)
                    utility += effect.value * heroThreatLevel * evaluateDefendCurve(enemyHpRatio)
                }

                is Effect.EditStatus -> {
                    // Оценка бафов/дебафов
                    utility += evaluateStatusUtility(effect.status, enemy, hero)
                }
            }
        }

        // Штраф за дорогие навыки, если ресурсов мало
        val costPenalty = evaluateResourceCost(perk.prices, stocks)
        utility *= costPenalty

        return utility
    }

    private fun evaluateDamageCurve(heroHpRatio: Float): Float {
        // Урон более ценен, если HP героя низкое (можно добить)
        return when {
            heroHpRatio < 0.2f -> 2.0f  // Добить!
            heroHpRatio < 0.5f -> 1.5f  // Агрессия
            else -> 1.0f               // Нормальный урон
        }
    }

    private fun evaluateHealCurve(enemyHpRatio: Float): Float {
        // Лечение очень ценно при низком HP
        return when {
            enemyHpRatio < 0.2f -> 3.0f  // Критично!
            enemyHpRatio < 0.5f -> 2.0f  // Важно
            enemyHpRatio < 0.8f -> 1.2f  // Полезно
            else -> 0.3f                // Не особо нужно
        }
    }

    private fun evaluateDefendCurve(enemyHpRatio: Float): Float {
        // Защита важнее при низком HP
        return when {
            enemyHpRatio < 0.3f -> 2.0f
            enemyHpRatio < 0.6f -> 1.5f
            else -> 1.0f
        }
    }

    private fun evaluateHeroThreat(hero: Hero): Float {
        // Оценка угрозы от героя (сколько урона может нанести)
        // Можно учитывать статусы (STRONG), ресурсы, доступные навыки
        return 1.0f  // Упрощенная версия
    }

    private fun evaluateStatusUtility(status: Status, enemy: Enemy, hero: Hero): Float {
        // Оценка полезности статуса
        return when (status.statusType) {
            Status.StatusType.DAMAGE -> status.value * 1.5f  // Урон со временем
            Status.StatusType.STRONG -> status.value * 2.0f  // Увеличение урона
            Status.StatusType.WEAK -> status.value * 1.5f    // Ослабление героя
            else -> status.value.toFloat()
        }
    }

    private fun evaluateResourceCost(prices: List<Perk.Price>, stocks: List<Stock>): Float {
        // Снижаем ценность навыка, если он тратит последние ресурсы
        // (возможно, лучше сохранить ресурсы для более важного навыка)
        return 1.0f  // Упрощенная версия
    }
}
```

**Преимущества Utility-based AI:**

- Адаптивное поведение (меняется в зависимости от ситуации)
- Гибкие критерии оценки
- Легко балансировать через параметры

---

## Интеграция в проект

### Шаг 1: Добавить стратегию в BattleSettings

```kotlin
data class BattleSettings(
    // ...
    val enemyStrategy: String = "SIMPLE"  // SIMPLE, PRIORITY, ADAPTIVE, AGGRESSIVE, DEFENSIVE, UTILITY
)
```

### Шаг 2: Создать фабрику стратегий

```kotlin
object EnemyStrategyFactory {
    fun create(strategyName: String): EnemyStrategy {
        return when (strategyName.uppercase()) {
            "SIMPLE" -> SimpleStrategy()
            "PRIORITY" -> PriorityStrategy()
            "ADAPTIVE" -> AdaptiveStrategy()
            "AGGRESSIVE" -> AggressiveStrategy()
            "DEFENSIVE" -> DefensiveStrategy()
            "UTILITY" -> UtilityBasedStrategy()
            else -> SimpleStrategy()
        }
    }
}
```

### Шаг 3: Использовать в EndTurnExecutor

```kotlin
@Singleton
class EndTurnExecutor @Inject constructor(
    // ... existing dependencies
    private val battleSettingsInteractor: BattleSettingsInteractor,
    private val heroStockListInteractor: HeroStockListInteractor,
    private val enemyStockListInteractor: EnemyStockListInteractor,
) {

    private var enemyStrategy: EnemyStrategy? = null

    fun initStrategy() {
        val settings = battleSettingsInteractor.value()
        enemyStrategy = EnemyStrategyFactory.create(settings?.enemyStrategy ?: "SIMPLE")
    }

    private fun enemyActions() {
        val strategy = enemyStrategy ?: SimpleStrategy()

        // Собираем все доступные навыки
        val availablePerks = mutableListOf<Perk>()
        enemyHandsListInteractor.value()?.forEach { hand ->
            if (hand.show) {
                hand.perks.forEach { perk ->
                    if (perk.show && perk.enable) {
                        availablePerks.add(perk)
                    }
                }
            }
        }

        // Используем стратегию для выбора навыка
        while (availablePerks.isNotEmpty()) {
            val selectedPerk = strategy.selectPerk(
                availablePerks = availablePerks,
                enemy = enemyInteractor.value()!!,
                hero = heroInteractor.value()!!,
                enemyStocks = enemyStockListInteractor.value() ?: emptyList(),
                turnNumber = turnNumberInteractor.value() ?: 1
            )

            if (selectedPerk == null) break

            // Выполняем выбранный навык
            perkExecutor.messageAboutUsedPerk(selectedPerk, false)
            perkExecutor.execute(selectedPerk)

            // Обновляем список доступных навыков (может измениться после использования)
            availablePerks.clear()
            enemyHandsListInteractor.value()?.forEach { hand ->
                if (hand.show) {
                    hand.perks.forEach { perk ->
                        if (perk.show && perk.enable) {
                            availablePerks.add(perk)
                        }
                    }
                }
            }
        }
    }
}
```

---

## Рекомендации

### Для простых противников:

- Использовать **SimpleStrategy** (текущее поведение)
- Настраивать поведение через порядок навыков и условия

### Для средних противников:

- Использовать **PriorityStrategy** с полем `priority` в навыках
- Позволяет легко настроить важность навыков

### Для сложных боссов:

- Использовать **AdaptiveStrategy** или **UtilityBasedStrategy**
- Противник будет адаптироваться к ситуации
- Более интересный геймплей

### Для специализированных противников:

- **AggressiveStrategy** — для врагов, которые фокусируются на атаке
- **DefensiveStrategy** — для врагов, которые защищаются и выживают

---

## Будущие улучшения

1. **Машинное обучение**
    - Обучить AI на основе действий игрока
    - Адаптироваться к стилю игры

2. **Планирование на несколько ходов**
    - AI просчитывает последствия на 2-3 хода вперед
    - Использует комбо-действия

3. **Персонализация противников**
    - Каждый противник имеет свою "личность" (агрессивный, хитрый, защитник)
    - Личность влияет на оценку полезности навыков

4. **Учет вероятностей**
    - AI учитывает вероятность успеха навыков
    - Избегает рискованных действий в критических ситуациях

5. **Синергия навыков**
    - AI понимает, какие навыки работают лучше в комбинации
    - Планирует последовательность использования

---

## Заключение

**Текущая реализация:** Простой перебор навыков сверху вниз без принятия решений.

**Преимущества:** Простота, предсказуемость, настраиваемость через JSON.

**Недостатки:** Отсутствие тактики, предсказуемость, негибкость.

**Рекомендуемое улучшение:** Внедрить паттерн Strategy с несколькими реализациями (Simple, Priority,
Adaptive, Utility-based), позволяющими настраивать поведение противника через BattleSettings.
