---
name: twilights-scene-json
description: >-
  Author and validate Twilights scene JSON (hero, enemy, settings, heroRules, conditions,
  effects, perks). Use when editing scene JSON, Firebase scene data, example scene files,
  or when the user mentions SCENE_CREATION_GUIDE, JSON сцены, условия, эффекты, настройки героя.
---

# Twilights — JSON сцены

## Правила

- **`Settings.types` ≥ 4.** Меньше четырёх цветов на доске даёт долгую генерацию стартового поля без
  совпадений и тормозит вход в сцену. Узкий дизайн ресурсов — через руки/стоки, а не через
  урезание `types`.
- JSON **без комментариев** — примеры в гайде валидны как есть; не подмешивать `//` или `/* */`.
- Корневые блоки сцены: `hero`, `enemy`, `settings`, `heroRules` (см. гайд).
- Для рабочих `*_base.json` предпочитай шаблон с `conditionDefs`, `effectDefs`, `statusDefs` и
  строковыми ссылками в `conditions*`, `effects`, `status`, `HeroStatuses`/`EnemyStatuses`.
  Такой файл затем раскрывается через `scripts/expand_scene.py` в финальный `*.json`.
- Порядок верхнеуровневых блоков в `*_base.json` держи стабильным:
    1) `conditionDefs` / `effectDefs` / `statusDefs`,
    2) блоки противника (`Enemy*`),
    3) блоки героя (`Hero*`, `HeroRules`),
    4) поле и общие настройки (`Settings`).
- Для **врага** обычно предпочтительнее `conditionsForDisplay` (скрыть/не рассматривать перк), чем
  `conditionsForEnable` (оставить видимым, но “запретить”): противник действует автоматически, и
  темп/вариативность чаще лучше моделировать
  через `probability`, `coolDown`, `reloadType`, `charges`,
  `category` и ветвления `additionalEffects`.

## Как читать SCENE_CREATION_GUIDE

Файл большой — **не загружать целиком** в контекст без нужды.

1. Определи тему запроса (Hero, Enemy, Settings, Conditions, Effects, Perks и т.д.).
2. Используй поиск по заголовкам / grep по ключевым словам или Read с `offset`/`limit` по известным
   строкам.
3. Полное руководство при необходимости — порциями по разделам.

## Источник правды

- **`SCENE_CREATION_GUIDE.md`** — корень workspace.

## Связанное

- Архитектура приложения и модели — **`PROJECT_CONTEXT.md`** (skill `twilights-project-context`).
- Интерпретация прохождения по логу — **`RESULT_INTERPRETATION_GUIDE.md`**.
