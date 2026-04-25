# CLAUDE.md — Twilights

## Что такое Twilights

**Twilights — это инструмент для настольной ролевой игры**, а не самостоятельная игра.
Вместо бросков кубиков игрок использует механику match-3: совмещает гемы → получает ресурсы → тратит
их на навыки → навыки применяют эффекты (атака, лечение, статусы и т.д.).

Каждая **сцена** — это JSON-конфиг, описывающий персонажей, навыки, условия и правила. Сцены
загружаются из **Firebase Realtime Database**. После прохождения сцены генерируется **battle log**,
по которому пишется нарративный результат.

Задачи для Claude: создание и отладка сцен (JSON) + разработка Android-приложения.

---

## Мир

**Дарк лоу фэнтези.** Действие разворачивается в **Пустошах** — землях «сумрачной крови» к югу от
враждебных Королевств. Обитатели: не только люди, но и гоблины, орки, тролли и другие расы — живут
преимущественно кочевым укладом.

### Главный персонаж

**Рук** — вождь орков и эмиссар тёмных сил. Обладает эфирной связью со своими **агентами** — людьми,
которых он воспитал и наделил силой. Сцены разворачиваются как от лица Рука, так и с участием его
агентов.

### Известные агенты и персонажи

- **Ребелд** (`rebeld/`) — агент, специализируется на скрытности и разведке. Сцены: проникновение,
  переговоры, социальные испытания.
- Другие персонажи появляются в подпапках `example/` (ala, fangs, hombro, joven, papel, perro, rook
  и др.)

---

## Типы сцен

| Тип                          | Описание                                                                                                   |
|------------------------------|------------------------------------------------------------------------------------------------------------|
| **Боевые**                   | Короткие столкновения. HP противника = здоровье врага. Цель — убить.                                       |
| **Социальные / приключения** | Переговоры, проникновение, испытания. HP противника = метрика прогресса (репутация, изученность, доверие). |

---

## Технический стек

- **Язык:** Kotlin
- **UI:** Jetpack Compose + View Binding (старые экраны)
- **Архитектура:** MVVM + LiveData
- **DI:** Hilt (Dagger)
- **Backend:** Firebase Realtime Database (данные сцен) + Firebase Storage (изображения/анимации)
- **Навигация:** Navigation Component
- **Изображения:** Glide, Coil
- **Push:** Firebase Cloud Messaging

### Ключевые пакеты

```
ui/          — экраны (start, game, editor, final)
model/       — модели данных (Hero, Enemy, Perk, Effect, Status, Stock, MatchRule)
logic/       — игровая логика (Executors: PerkExecutor, ApplyAttackExecutor, EndTurnExecutor и др.)
storage/     — in-memory состояние игры через Interactor'ы
di/          — Hilt модули
data/        — репозитории
```

Состояние игры живёт **в памяти** через Interactor'ы. Firebase — только для загрузки конфигурации
сцен, не для сохранения прогресса.

---

## Структура сцены (JSON)

Сцены хранятся в `example/`. Структура файла:

```json
{
  "Hero": {
    "hp",
    "maxHp",
    "name",
    "shield"
  },
  "Enemy": {
    "hp",
    "maxHp",
    "name",
    "info",
    "shield"
  },
  "HeroHands": [
    {
      "gemType",
      "name",
      "perks": [
        ...
      ]
    }
  ],
  "EnemyHands": [
    {
      "gemType",
      "name",
      "perks": [
        ...
      ]
    }
  ],
  "HeroStocks": [
    {
      "gemType",
      "maxValue",
      "value"
    }
  ],
  "EnemyStocks": [
    ...
  ],
  "HeroStatuses": [
    {
      "name",
      "type",
      "value",
      "duration"
    }
  ],
  "EnemyStatuses": [
    ...
  ],
  "HeroStates": [
    {
      "name",
      "conditions": [
        ...
      ],
      "status": {
        ...
      }
    }
  ],
  "EnemyStates": [
    ...
  ],
  "HeroRules": [
    {
      "name",
      "gemType",
      "minSize",
      "orientation",
      "perk": {
        ...
      }
    }
  ],
  "EnemySectors": [
    {
      "id",
      "name",
      "perk": {
        ...
      }
    }
  ],
  "Settings": {
    "types",
    "stopGenerate",
    "makeEnemyMove",
    "gemSettings": [
      ...
    ]
  }
}
```

Подробная документация по всем полям: **`SCENE_CREATION_GUIDE.md`**

---

## Критическое правило: согласованность имён статусов

**Имена статусов (`name`) должны совпадать дословно** во всех местах, где они используются:

- в `conditionsForDisplay` / `conditions`
- в `EDIT_STATUS` эффектах
- в `HeroStatuses` / `EnemyStatuses` (стартовые значения)
- в `HeroStates` / `EnemyStates` (автоматические статусы)

**Пример ошибки:** навык проверяет `"name": "Горит"`, а статус выдан с `"name": "В огне"` → условие
никогда не сработает.

**Перед добавлением нового условия на статус:** убедиться, что точное имя уже используется в сцене.

---

## Типы эффектов (Effects)

| Команда       | Что делает                                       |
|---------------|--------------------------------------------------|
| `ATTACK`      | Наносит урон цели (с учётом щитов и уязвимостей) |
| `DEFEND`      | Добавляет щиты                                   |
| `HEAL`        | Восстанавливает HP                               |
| `EDIT_STATUS` | Устанавливает/изменяет/убирает статус            |
| `EDIT_STOCK`  | Изменяет ресурс (stock)                          |
| `INFO`        | Добавляет сообщение в лог                        |
| `FINISH`      | Завершает сцену                                  |

---

## Типы статусов

Основные: `DAMAGE`, `HEAL`, `DEFEND`, `GENERATE`, `STRONG`, `WEAK`, `VULNERABLE`, `INFO`

Статус `INFO` — маркер состояния (не наносит урон/лечение сам по себе), используется как флаг для
условий.

---

## Условия (Conditions)

Параметры: `HP`, `STOCK` (с `gemType`), `STATUS` (с `name`), `TURN`, `SHIELD`

Символы сравнения: `MORE`, `LESS`, `EQUALS`, `MORE_OR_EQUALS`, `LESS_OR_EQUALS`, `HAVE`, `NOT_HAVE`

---

## Формат сцены: три файла (новые сцены)

Новые сцены разбиваются на три файла вместо одного монолитного `*_base.json`:

| Файл             | Содержимое                                                                              | Меняется                   |
|------------------|-----------------------------------------------------------------------------------------|----------------------------|
| `*_static.json`  | Hero, Enemy, HeroStocks, EnemyStocks, HeroResources, Settings (включая cells)           | Редко                      |
| `*_defs.json`    | conditionDefs, effectDefs, statusDefs                                                   | При добавлении новых дефов |
| `*_dynamic.json` | HeroHands, EnemyHands, HeroRules, EnemySectors, Statuses, States, TimePerks, StockPerks | Постоянно                  |

`Settings.cells` с `cellType: TRIGGER` (содержат `triggerPerk.effects` → ссылки на effectDefs) живут
в `_static.json` — это редкие правки.

Старые сцены с единым `*_base.json` продолжают работать как есть.

---

## Рабочий процесс со сценами

**Новый формат (3 файла):**

1. Редактировать нужный файл (обычно `*_dynamic.json`)
2. Собрать: `python scripts/compose_scene.py <директория>`  → создаёт `*_base.json`
3. Проверить: `python scripts/validate_scene.py <путь_к_base.json>`
4. Раскрыть: `python scripts/expand_scene.py <путь_к_base.json>` → создаётся `*.json`
5. Загрузить в Firebase → пройти → написать results/story

**Старый формат (единый _base.json):**

1. Редактировать `*_base.json`
2. `validate_scene.py` → `expand_scene.py`

---

## Инструменты для работы со сценами

### `scripts/compose_scene.py` — сборка из трёх файлов

```bash
python scripts/compose_scene.py example/palabrot/10/1_investigate/
# → создаёт palabrot_1_investigate_base.json
```

Автоматически находит `*_static.json`, `*_defs.json`, `*_dynamic.json` в директории. Проверяет
конфликты ключей и неожиданные поля. Порядок ключей в результате фиксированный (defs → Enemy →
Hero → Settings).

---

### `scripts/build.py` — полный пайплайн одной командой

```bash
python scripts/build.py example/palabrot/10/1_investigate/
# = compose (если есть _dynamic.json) → validate → expand

python scripts/build.py example/... --no-validate   # быстро, без проверки
python scripts/build.py example/... --only-compose  # только сборка _base.json
```

---

### `scripts/new_scene.py` — шаблон новой сцены

```bash
python scripts/new_scene.py example/palabrot/10/2_chase/ palabrot_2_chase
# создаёт _static.json, _defs.json, _dynamic.json с правильной структурой

python scripts/new_scene.py example/... prefix --types=6   # нестандартное число типов гемов
python scripts/new_scene.py example/... prefix --force     # перезаписать существующие
```

---

### `scripts/grep_scene.py` — поиск по сцене

```bash
python scripts/grep_scene.py palabrot_1_investigate_base.json "Патруль"
# все места где встречается слово: перки, статусы, условия, эффекты

python scripts/grep_scene.py scene.json "ATTACK" --effect     # только в командах эффектов
python scripts/grep_scene.py scene.json "Ранен" --names-only  # только имена перков
```

Использовать перед переименованием статуса — найти все его упоминания в сцене.

---

### `scripts/scene_outline.py` — оглавление сцены

```bash
python scripts/scene_outline.py example/palabrot/10/1_investigate/palabrot_1_investigate_base.json
```

Выводит структуру сцены без тел эффектов (~150 строк вместо 1000+):

- персонажи (HP, щиты)
- все руки и перки с ценами, кулдаунами, условиями видимости
- список всех имён статусов, встречающихся в сцене
- ключи `conditionDefs`, `effectDefs`, `statusDefs`

**Когда использовать:** в начале задачи по сцене — вместо чтения полного файла. Даёт карту сцены
за ~1KB токенов.

---

### `scripts/validate_scene.py` — проверка согласованности

```bash
python scripts/validate_scene.py example/palabrot/10/1_investigate/palabrot_1_investigate_base.json
```

Проверяет до запуска expand:

- все строковые ссылки в `effects[]` → есть в `effectDefs`
- все строковые ссылки в `conditions*[]` → есть в `conditionDefs`
- все строковые ссылки в `HeroStatuses`/`EnemyStatuses` → есть в `statusDefs`
- похожие имена статусов (возможные опечатки: "Горит" vs "горит")
- рекурсивные ссылки внутри самих `effectDefs`

Выводит список всех статусов с количеством использований.

**Когда использовать:** перед `expand_scene.py` и после любых правок в сцене.

---

### Рекомендуемый порядок работы Claude со сценой

1. `scene_outline` → понять структуру (не читать весь файл)
2. grep по имени нужной руки/перка → загрузить только этот блок
3. Внести правку в `*_base.json`
4. `validate_scene` → проверить согласованность
5. `expand_scene` → получить финальный JSON

---

## Пример эталонной сцены

`example/rebeld/10/9_sneak/rebeld_9_sneak.json` — сцена проникновения в лагерь пустынников.

- Герой: Ребелд, HP=15/20, маскируется под гнолла
- Противник: Лагерь (HP = степень неизученности)
- Ресурсы: Поиск, Скрытность, Наблюдение, Обстановка, Концентрация
- Сектора (выбор зоны): Центр, Палатки, Кузница — переключают условия навыков
- Пассивный враг: периодические проверки личины при высоком Напряжении

---

## Документация

| Файл                             | Содержание                                             |
|----------------------------------|--------------------------------------------------------|
| `SCENE_CREATION_GUIDE.md`        | Полный JSON-референс: все поля, типы, условия, примеры |
| `RESULT_INTERPRETATION_GUIDE.md` | Как читать battle log и писать results.md + story.md   |
| `example/`                       | Готовые сцены — эталон для нового контента             |
| `PROJECT_CONTEXT.md`             | Архитектура приложения                                 |
