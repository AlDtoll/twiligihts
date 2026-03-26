# Формат _base.json сцен (для ИИ-агентов и редакторов)

Документ описывает, как составлять файлы сцен с суффиксом **`_base.json`**: в них объявляются
именованные условия и эффекты, а в теле сцены используются **ссылки по имени**. Готовый JSON для
игры получается запуском скрипта `scripts/expand_scene.py`, который раскрывает ссылки и убирает
блоки определений. Код приложения менять не нужно.

## Назначение

- **conditionDefs** — один раз описать условие, много раз использовать по имени
  в `conditions`, `conditionsForDisplay`, `conditionsForEnable`.
- **effectDefs** — один раз описать эффект (или цепочку через `additionalEffects`), много раз
  использовать по имени в `effects`, `additionalEffects`.

Итог: меньше дублирования, меньше размер итогового JSON, проще править сцену.

## Структура файла _base.json

Файл — валидный JSON. На верхнем уровне обязательно присутствуют (могут быть пустыми):

- **conditionDefs** — объект: ключ = имя ссылки, значение = один объект условия (как в
  SCENE_CREATION_GUIDE).
- **effectDefs** — объект: ключ = имя ссылки, значение = один объект эффекта (как в
  SCENE_CREATION_GUIDE).

Остальные ключи — как в обычном JSON сцены (Hero, Enemy, HeroHands, EnemyHands, EnemySectors,
HeroStates, BattleSettings и т.д.). В любом месте, где в обычной сцене стоит массив условий или
эффектов, в _base можно подставлять **строку** — имя из соответствующего справочника.

```json
{
  "conditionDefs": {
    "enemy_has_center": {
      "parameter": "STATUS",
      "symbol": "HAVE",
      "target": "ENEMY",
      "name": "Цель: Центр"
    },
    "turn_before_5": {
      "parameter": "TURN",
      "symbol": "LESS",
      "value": 5
    }
  },
  "effectDefs": {
    "add_tension_1": {
      "command": "EDIT_STATUS",
      "type": "CHANGE",
      "value": 1,
      "target": "ENEMY",
      "status": {
        "name": "Напряжение",
        "type": "INFO",
        "value": 1,
        "duration": 1
      }
    }
  },
  "Hero": {
    ...
  },
  "HeroHands": [
    {
      "gemType": 1,
      "name": "Поиск",
      "perks": [
        {
          "name": "Поиск: Обыскать палатки",
          "effects": [
            "add_tension_1",
            {
              "command": "ATTACK",
              "target": "ENEMY",
              "value": 5,
              "conditions": [
                "enemy_has_center"
              ]
            }
          ],
          "prices": [
            {
              "gemType": 1,
              "value": 24
            }
          ]
        }
      ]
    }
  ]
}
```

После запуска скрипта в итоговом файле не будет `conditionDefs` и `effectDefs`, а все строки в
массивах условий/эффектов будут заменены на полные объекты.

## Где можно использовать ссылки

### Условия (строки из conditionDefs)

- **conditions** — в эффектах (когда эффект срабатывает при выполнении условия).
- **conditionsForDisplay** — у руки (hand) и у навыка (perk): когда показывать руку/навык.
- **conditionsForEnable** — у навыка (perk): когда навык доступен (помимо ресурсов и перезарядки).
- **conditions** в HeroStates — условия автоматического наложения статуса.

В этих полях в массиве можно писать:

- **строка** — подставляется объект из `conditionDefs` с таким именем;
- **объект** — как в обычной сцене (условие «инлайн»).

Смешивать можно: `["enemy_has_center", { "parameter": "TURN", "symbol": "EQUALS", "value": 3 }]`.

Практика: для **вражеских** перков чаще удобнее управлять доступностью через `conditionsForDisplay`
(и через темп `probability`/`coolDown`/`charges`), а `conditionsForEnable` использовать редко.

### Эффекты (строки из effectDefs)

- **effects** — у навыка (perk), у сектора (EnemySectors[].perk.effects), у PlaceHands и т.д.
- **additionalEffects** — внутри эффекта (дополнительные эффекты при успехе/неудаче).

В этих полях в массиве можно писать:

- **строка** — подставляется объект из `effectDefs` с таким именем (внутри него тоже раскрываются
  условия и эффекты);
- **объект** — как в обычной сцене.

Также поддержан строковый DSL для локальных переопределений ссылки:

- `ref_name|key=value` — переопределить scalar/object поле;
- `ref_name|arrayKey+=item` — добавить элемент в массив;
- токены разделяются символом `|`.

Примеры:

```json
"effects": [
"strike_ladron|probability=5",
"strike_ladron|conditions+=hero_no_ladron_wounded",
"strike_ladron|additionalEffects+=set_hero_ladron_wounded",
"strike_ladron|probability=10|charges=2|conditions+=hero_no_ladron_immobilized"
]
```

Правило merge для DSL:

- если поле в базовом effectDef является массивом, новые значения **добавляются** (append);
- при операторе `+=` всегда выполняется append;
- scalar/object поля переопределяются значением из `key=value`.

## Правила для имён

- Имена в **conditionDefs** и **effectDefs** — уникальные ключи объекта (строка). Удобно: латиница,
  цифры, подчёркивание, например `enemy_has_center`, `add_tension_1`, `reaction_hero_if_center`.
- Ссылка в сцене — строка, **точно совпадающая** с ключом в соответствующем справочнике. Опечатка
  приведёт к ошибке при запуске скрипта.

## Формат одного условия (conditionDefs)

Как в SCENE_CREATION_GUIDE, раздел «Условия (Conditions)»:

- **parameter** — HP, STATUS, TURN, STOCK, HITS, TOUCHES и т.д.
- **symbol** — HAVE, EMPTY, MORE, LESS, EQUALS, EXIST, NOT.
- **target** — HERO, ENEMY и т.д. (при необходимости).
- **value** — число для сравнения (для MORE/LESS/EQUALS).
- **name** — имя статуса (для parameter: STATUS).
- **gemType** — тип ресурса (для parameter: STOCK).

Примеры:

```json
"enemy_has_center": {
"parameter": "STATUS",
"symbol": "HAVE",
"target": "ENEMY",
"name": "Цель: Центр"
},
"hero_stock2_low": {
"parameter": "STOCK",
"gemType": 2,
"symbol": "LESS",
"target": "HERO",
"value": 40
}
```

## Формат одного эффекта (effectDefs)

Как в SCENE_CREATION_GUIDE, раздел «Эффекты (Effects)»: полный объект эффекта с
полями `command`, `target`, `value`, `conditions`, `additionalEffects`, `status`, `func`, `pFunc` и
т.д. Внутри эффекта в `conditions` и `additionalEffects` тоже можно использовать строки-ссылки —
скрипт раскрывает их рекурсивно.

Пример:

```json
"reaction_hero_if_center": {
"command": "ATTACK",
"target": "HERO",
"value": 2,
"probability": 25,
"conditions": ["enemy_has_center"],
"additionalEffects": [
{
"successType": "FAIL",
"command": "EDIT_STOCK",
"type": "CHANGE",
"target": "HERO",
"gemTypes": [2],
"value": -5
}
]
}
```

Если в **effectDefs** один эффект ссылается на другой по имени, в массиве `effects`
или `additionalEffects` можно указать строку имени — подставится полное тело эффекта.

## Запуск скрипта

```bash
python scripts/expand_scene.py <путь к _base.json>
```

Пример:

```bash
python scripts/expand_scene.py example/rebeld/10/9_sneak/rebeld_9_sneak_base.json
```

- Входной файл: имя должно содержать `_base` и расширение `.json`.
- Выходной файл: тот же каталог, имя получается заменой `_base` на пустую строку (
  например, `rebeld_9_sneak_base.json` → `rebeld_9_sneak.json`).

Скрипт проверяет, что все использованные имена есть в `conditionDefs`/`effectDefs`; при отсутствии
имени завершается с ошибкой.

## Рекомендации для ИИ-агентов

1. При создании или правке сцены в формате _base:
    - В **conditionDefs** и **effectDefs** выносить **только повторяющиеся** условия и эффекты (
      используются в двух и более местах). Разовые условия и эффекты лучше оставлять **в месте
      применения** (инлайн в массивах `conditions` / `effects` у соответствующего перка или
      эффекта) — так сцена не разрастается справочниками и проще читать контекст.
    - В теле сцены в
      массивах `conditions`, `conditionsForDisplay`, `conditionsForEnable`, `effects`, `additionalEffects`
      использовать строки-имена только для ссылок на defs; остальное — полные объекты инлайн.

2. Имена defs выбирать осмысленно и единообразно (например, `enemy_has_*` для «у противника есть
   статус», `add_tension_*` для добавления Напряжения).

3. После изменений _base файла запускать `expand_scene.py` и проверять, что итоговый JSON валиден и
   при необходимости загружать его в Firebase/использовать в игре.

4. Справочник по полной структуре сцены и по формату условий/эффектов — **SCENE_CREATION_GUIDE.md**;
   структура данных приложения — **PROJECT_CONTEXT.md**.

5. **Settings.gemSettings** — не сокращать до `type`, `name`, `displayName`. Для каждого типа гема
   обязательно указывать полный набор полей: **bonusProbability**, **bonusValue**, **fullValue**, *
   *halfProbability**, **turnKeepStrategy**, **damageKeepStrategy**, **name**, **type**, *
   *displayName**. Числовые значения можно копировать из другой сцены; **displayName** задавать под
   тематику сцены (например: «поиска», «скрытности», «наблюдения», «обстановки», «концентрации»).
   Шаблон — любая уже готовая сцена с полным gemSettings.
