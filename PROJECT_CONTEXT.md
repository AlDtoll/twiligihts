# Контекст проекта Twilights

## Обзор проекта

**Twilights** — это игра в жанре "3 в ряд" (match-3), которая используется как вспомогательный
инструмент для настольной ролевой игры. Вместо традиционных бросков кубиков, игроки используют
механику совмещения гемов на игровом поле для определения результатов действий в сценах.

### Основная концепция

- **Сцены** (сражения или испытания) описываются через JSON-файлы, которые загружаются из Firebase
- Каждая сцена определяет условия, возможные действия и правила игры
- Игрок управляет героем, который сражается или взаимодействует с противником/ситуацией
- Результаты действий определяются через механику "3 в ряд" вместо случайных бросков кубиков

## Архитектура проекта

Проект построен на **Android** с использованием следующих технологий:

- **Язык**: Kotlin
- **Архитектура**: MVVM с использованием LiveData
- **Dependency Injection**: Hilt (Dagger)
- **UI**: Jetpack Compose + View Binding для старых экранов
- **База данных**: Firebase Realtime Database
- **Хранилище**: Firebase Storage (для изображений и анимаций)
- **Навигация**: Navigation Component

## Структура пакетов

### `ui/` — Пользовательский интерфейс

#### `MainActivity.kt`

- Главная активность приложения
- Инициализация Firebase Messaging
- Управление разрешениями

#### `screen/start_screen/` — Стартовый экран

- **StartScreen.kt**: Экран выбора сцены/противника
- **StartScreenViewModel.kt**: Логика загрузки доступных сцен
- Позволяет начать новую игру или продолжить существующую

#### `screen/game_screen/` — Игровой экран

- **GameScreen.kt**: Основной экран игры с игровым полем
- **GameScreenViewModel.kt**: Управление состоянием игры
- **GameBoardAdapter.kt**: Адаптер для отображения игрового поля (сетка гемов)
- **HandsAdapter.kt**: Отображение доступных навыков (рук) героя
- **PerksAdapter.kt**: Отображение навыков противника
- **StatusAdapter.kt**: Отображение статусов персонажей
- **StockAdapter.kt**: Отображение накопленных ресурсов
- **LogAdapter.kt**: Лог событий боя

#### `screen/editor_screen/` — Редактор сцен

- **EditorFragment.kt**: Экран для редактирования JSON-сцен
- Позволяет создавать и редактировать сцены прямо в приложении

#### `screen/final_screen/` — Экран завершения

- **FinalScreen.kt**: Экран результатов после завершения сцены

### `model/` — Модели данных

#### Персонажи

- **`characters/Hero.kt`**: Модель героя (HP, щиты, статусы, раны)
- **`characters/Enemy.kt`**: Модель противника (HP, щиты, статусы, описание)
- **`characters/Person.kt`**: Базовый интерфейс для героя и противника

#### Игровая механика

- **`GameBoard.kt`**: Игровое поле (сетка гемов)
    - Поиск совпадений (3 в ряд)
    - Проверка возможных ходов
    - Логика падения гемов
- **`Gem.kt`**: Модель гема (тип, бонусы, дополнительные цвета)
- **`Hand.kt`**: Рука — группа навыков одного типа ресурса
- **`Perk.kt`**: Навык — действие, которое можно использовать
- **`effects/Effect.kt`**: Эффекты навыков (ATTACK, DEFEND, HEAL, EDIT_STATUS, EDIT_STOCK, INFO,
  FINISH)
- **`Status.kt`**: Статус персонажа (урон, лечение, баффы, дебаффы)
- **`Stock.kt`**: Ресурс — накопленные очки определенного типа
- **`Resource.kt`**: Настройки ресурса (максимальное значение, тип)
- **`MatchRule.kt`**: Правило для автоматических эффектов при совпадении гемов
- **`Sector.kt`**: Сектор игрового поля с особыми эффектами
- **`BattleSettings.kt`**: Настройки сцены (количество типов гемов, анимации, поведение противника)

### `logic/` — Игровая логика

#### Исполнители (Executors)

- **`PerkExecutor.kt`**: Основной исполнитель навыков
    - Применяет эффекты навыков
    - Управляет перезарядкой и зарядами
    - Обрабатывает условия использования
- **`ApplyAttackExecutor.kt`**: Применение атак
    - Расчет урона с учетом щитов, брони, уязвимостей
    - Обработка типов атак (обычная, по щитам, по HP)
- **`EndTurnExecutor.kt`**: Логика завершения хода
    - Применение статусов
    - Обновление перезарядки навыков
    - Передача хода между героем и противником
- **`EditStockExecutor.kt`**: Изменение ресурсов
    - Добавление/уменьшение очков
    - Сохранение очков между ходами
- **`UpdateStockExecutor.kt`**: Обновление ресурсов при совпадении гемов
- **`MatchPerkExecutor.kt`**: Применение правил совпадения (HeroRules)
- **`ApplyFunctionExecutor.kt`**: Расчет значений эффектов через функции (dice, segments)
- **`CheckConditionExecutor.kt`**: Проверка условий для навыков и эффектов
- **`InitSettingsExecutor.kt`**: Инициализация настроек игры

#### Обработчики эффектов (`perks/`)

- **`DefendEffectHandler.kt`**: Обработка эффектов защиты (щиты)
- **`EditStatusHandler.kt`**: Обработка изменения статусов
- **`EditStockHandler.kt`**: Обработка изменения ресурсов

#### Загрузка данных (`database/`)

- **`DatabaseInteractor.kt`**: Главный координатор загрузки данных из Firebase
- **`IDownloadFromDataBase.kt`**: Интерфейс для загрузчиков данных

##### Загрузка данных героя (`database/hero/`)

- **`HeroDownLoadInteractor.kt`**: Загрузка данных героя
- **`HeroHandsDownLoadInteractor.kt`**: Загрузка рук (навыков) героя
- **`HeroStocksDownLoadInteractor.kt`**: Загрузка ресурсов героя
- **`HeroStatusesDownLoadInteractor.kt`**: Загрузка статусов героя
- **`HeroStatesDownLoadInteractor.kt`**: Загрузка состояний героя (автоматические статусы)
- **`HeroRulesDownLoadInteractor.kt`**: Загрузка правил совпадения для героя
- **`HeroResourcesDownloadExecutor.kt`**: Загрузка настроек ресурсов героя

##### Загрузка данных противника (`database/enemy/`)

- **`EnemyDownLoadInteractor.kt`**: Загрузка данных противника
- **`EnemyHandsDownLoadInteractor.kt`**: Загрузка рук (навыков) противника
- **`EnemyStocksDownLoadInteractor.kt`**: Загрузка ресурсов противника
- **`EnemyStatusesDownLoadInteractor.kt`**: Загрузка статусов противника
- **`EnemyStatesDownLoadInteractor.kt`**: Загрузка состояний противника
- **`EnemySectorsDownLoadInteractor.kt`**: Загрузка секторов игрового поля
- **`EnemyResourcesDownloadExecutor.kt`**: Загрузка настроек ресурсов противника

##### Другие загрузчики

- **`BattleSettingsDowloadExecutor.kt`**: Загрузка настроек сцены
- **`BattleResultDownloadExecutor.kt`**: Загрузка результатов сцены
- **`FinishBattleExecutor.kt`**: Завершение сцены
- **`WriteTemporaryLogExecutor.kt`**: Запись временного лога

##### Расширения для парсинга

- **`HandsExt.kt`**: Парсинг рук и навыков из Firebase
- **`RulesExt.kt`**: Парсинг правил совпадения
- **`SectorsExt.kt`**: Парсинг секторов
- **`StatusExt.kt`**: Парсинг статусов

### `storage/` — Хранение состояния

Управление состоянием игры через различные Interactor'ы:

#### Персонажи

- **`PersonInteractor.kt`**: Базовый интерфейс для работы с персонажами
- **`hero/HeroInteractor.kt`**: Состояние героя
- **`hero/HeroHandsListInteractor.kt`**: Список рук героя
- **`hero/HeroStockListInteractor.kt`**: Список ресурсов героя
- **`hero/HeroStatusesInteractor.kt`**: Статусы героя
- **`hero/HeroStatesInteractor.kt`**: Состояния героя (автоматические статусы)
- **`hero/HeroResourcesInteractor.kt`**: Настройки ресурсов героя
- **`hero/HeroRulesInteractor.kt`**: Правила совпадения для героя
- **`enemy/EnemyInteractor.kt`**: Состояние противника
- **`enemy/EnemyHandsListInteractor.kt`**: Список рук противника
- **`enemy/EnemyStockListInteractor.kt`**: Список ресурсов противника
- **`enemy/EnemyStatusesInteractor.kt`**: Статусы противника
- **`enemy/EnemyStatesInteractor.kt`**: Состояния противника
- **`enemy/EnemySectorsInteractor.kt`**: Секторы игрового поля
- **`enemy/EnemyResourcesInteractor.kt`**: Настройки ресурсов противника

#### Игровые системы

- **`BattleSettingsInteractor.kt`**: Настройки текущей сцены
- **`BattleResultInteractor.kt`**: Результаты сцены
- **`BattleLogListInteractor.kt`**: Лог событий боя
- **`PlaceHandsListInteractor.kt`**: Пассивные навыки (события)
- **`ExecutedPerkInteractor.kt`**: Информация о выполняемом навыке (для анимаций)
- **`EnemyMoveEventInteractor.kt`**: Событие хода противника на игровом поле
- **`TurnNumberInteractor.kt`**: Номер текущего хода
- **`TimeSecondsInteractor.kt`**: Таймер сцены
- **`AttemptCounterInteractor.kt`**: Счетчик попыток
- **`AfterPerkInteractor.kt`**: События после применения навыка
- **`StartTimerAgainEventInteractor.kt`**: Перезапуск таймера
- **`GoToFinishScreenInteractor.kt`**: Переход на экран завершения
- **`EffectValueForDescriptionInteractor.kt`**: Значение эффекта для описания

#### Интерфейсы

- **`IStocks.kt`**: Интерфейс для работы с ресурсами
- **`IResources.kt`**: Интерфейс для работы с настройками ресурсов

### `di/` — Dependency Injection

- **`GameModule.kt`**: Модуль Hilt для предоставления зависимостей игровой логики

### `ext/` — Расширения

- **`TextViewExt.kt`**: Расширения для TextView
- **`ViewExt.kt`**: Расширения для View (анимации кликов)

### Другие файлы

- **`App.kt`**: Класс Application, инициализация Firebase
- **`MyService.kt`**: Сервис для обработки Firebase Cloud Messaging
- **`FCMHelper.kt`**: Вспомогательные функции для FCM
- **`MyAppGlideModule.kt`**: Настройка Glide для загрузки изображений
- **`SingleLiveEvent.kt`**: LiveData для одноразовых событий

## Поток данных

### Инициализация игры

1. **StartScreen** → пользователь выбирает сцену
2. **DatabaseInteractor.observeRealtimeDatabase()** → загружает данные из Firebase:
    - Данные героя и противника
    - Навыки (руки)
    - Ресурсы и статусы
    - Настройки сцены
    - Правила совпадения
3. **GameScreen** → инициализирует игровое поле и UI

### Игровой цикл

1. **Ход героя**:
    - Игрок делает ход на игровом поле (совмещает гемы)
    - **GameBoardAdapter.handleMatches()** → находит совпадения
    - **UpdateStockExecutor** → обновляет ресурсы по совпавшим гемам
    - **MatchPerkExecutor** → применяет правила совпадения (HeroRules)
    - Игрок выбирает навык из доступных рук
    - **PerkExecutor.execute()** → применяет навык и его эффекты
    - **EndTurnExecutor.endHeroTurn()** → завершает ход героя

2. **Ход противника**:
    - **EndTurnExecutor.startEnemyTurn()** → начинает ход противника
    - Если `makeEnemyMove: true` → противник делает ход на игровом поле
    - Противник автоматически применяет навыки (сверху вниз)
    - **PerkExecutor.execute()** → применяет навыки противника
    - **EndTurnExecutor.afterEnemyAction()** → завершает ход противника

3. **Применение статусов**:
    - **EndTurnExecutor.applyPersonStatus()** → применяет статусы перед/после хода
    - Статусы могут наносить урон, лечить, генерировать ресурсы и т.д.
    - **EndTurnExecutor.updatePersonStatus()** → обновляет длительность статусов

### Применение эффектов

1. **PerkExecutor.executePerkEffects()** → перебирает эффекты навыка
2. Для каждого эффекта:
    - Проверка условий (`CheckConditionExecutor`)
    - Расчет вероятности (`ApplyFunctionExecutor.useFunctionForChangeEffectProbability()`)
    - Расчет значения (`ApplyFunctionExecutor.useFunctionForChangeEffectValue()`)
    - Применение эффекта:
        - **ATTACK** → `ApplyAttackExecutor`
        - **DEFEND** → `DefendEffectHandler`
        - **HEAL** → прямое восстановление HP
        - **EDIT_STATUS** → `EditStatusHandler`
        - **EDIT_STOCK** → `EditStockHandler`
        - **INFO** → добавление сообщения в лог
        - **FINISH** → завершение сцены

## Ключевые механики

### Игровое поле (Match-3)

- **GameBoard**: Сетка гемов (обычно 8x8)
- **Поиск совпадений**: Горизонтальные и вертикальные линии из 3+ одинаковых гемов
- **Падение гемов**: После удаления совпадений, гемы падают вниз
- **Генерация новых гемов**: Новые гемы появляются сверху (если `stopGenerate: false`)

### Ресурсы (Stocks)

- Каждый тип гема дает очки определенного типа ресурса
- Ресурсы тратятся на использование навыков
- Можно настроить сохранение ресурсов между ходами (`turnKeepStrategy`)
- Можно настроить сохранение ресурсов после урона (`damageKeepStrategy`)

### Навыки (Perks)

- Навыки группируются в **руки** (Hands) по типу ресурса
- Каждый навык имеет:
    - Стоимость (prices) — сколько ресурсов нужно потратить
    - Эффекты — что происходит при использовании
    - Условия видимости/доступности
    - Перезарядку и заряды
    - Вероятность срабатывания

### Статусы

- Статусы применяются автоматически каждый ход
- Могут применяться **перед ходом** (`end: false`) или **после хода** (`end: true`)
- Типы статусов:
    - **DAMAGE**: Урон каждый ход
    - **HEAL**: Лечение каждый ход
    - **DEFEND**: Генерация щитов
    - **GENERATE**: Генерация ресурсов при совпадении гемов
    - **STRONG/WEAK**: Изменение наносимого урона
    - **VULNERABLE**: Изменение получаемого урона
    - И многие другие...

### Правила совпадения (HeroRules)

- Автоматически применяются при совпадении гемов определенного типа/размера/ориентации
- Позволяют создавать комбо-эффекты
- Например: "При совпадении 4+ гемов типа 1, получить статус генерации очков типа 1"

## Работа с Firebase

### Структура данных в Firebase

```
/{prefix}/
  Hero/              - данные героя
  Enemy/             - данные противника
  HeroHands/         - руки (навыки) героя
  EnemyHands/        - руки (навыки) противника
  HeroStocks/        - ресурсы героя
  EnemyStocks/       - ресурсы противника
  HeroStatuses/      - статусы героя
  EnemyStatuses/     - статусы противника
  HeroStates/        - состояния героя (автоматические статусы)
  EnemyStates/       - состояния противника
  HeroRules/         - правила совпадения для героя
  EnemySectors/      - секторы игрового поля
  BattleSettings/    - настройки сцены
  BattleResult/      - результаты сцены
  PlaceHands/        - пассивные навыки (события)
```

### Загрузка данных

- Все данные загружаются через **ValueEventListener** из Firebase Realtime Database
- Данные парсятся в Kotlin data classes через Firebase SDK
- Сложные структуры (эффекты, условия) парсятся вручную через расширения (`HandsExt`, `RulesExt`)

## Дополнительные возможности

### Редактор сцен

- **EditorScreen** позволяет создавать и редактировать сцены прямо в приложении
- Изменения сохраняются в Firebase

### Логирование

- Все события боя записываются в **BattleLogListInteractor**
- Лог можно просмотреть в игре через **LogBottomSheetDialog**

### Анимации

- Анимации действий противника (летающие иконки навыков)
- Анимации совпадения и падения гемов
- Анимации персонажей (через GIF из Firebase Storage)

## Зависимости проекта

- **AndroidX**: Core, AppCompat, Lifecycle, Navigation, Compose
- **Firebase**: Realtime Database, Storage, Messaging
- **Hilt**: Dependency Injection
- **Glide**: Загрузка изображений
- **Lottie**: Анимации (JSON)
- **Coil**: Загрузка изображений в Compose

## Полезные файлы документации

- **`SCENE_CREATION_GUIDE.md`**: Подробное руководство по созданию сцен через JSON
- **`example/`**: Примеры JSON-файлов сцен для различных героев и ситуаций

## Примечания для разработчиков

1. **Состояние игры** хранится в памяти через Interactor'ы, а не в базе данных
2. **Firebase** используется только для загрузки конфигурации сцен, не для сохранения прогресса
3. **Игровая логика** полностью детерминирована (кроме случайных значений в функциях)
4. **UI обновления** происходят через LiveData и наблюдатели
5. **Анимации** синхронизированы с игровой логикой через Handler'ы и задержки



