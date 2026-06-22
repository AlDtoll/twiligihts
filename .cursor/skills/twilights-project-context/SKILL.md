---
name: twilights-project-context
description: >-
  Twilights Android match-3 RPG: Kotlin, MVVM, Hilt, Compose, Firebase Realtime DB/Storage,
  scene JSON from Firebase. Explains app architecture, packages, Hero/Enemy/GameBoard models,
  game turn loop (read GAMEPLAY.md first for port/web), and how scenes tie into the app. Use for
  architecture questions, code navigation, domain overview, or when the user mentions
  PROJECT_CONTEXT, GAMEPLAY, структура проекта, архитектура Twilights, игровой цикл.
---

# Twilights — контекст проекта

## Когда применять

Вопросы об устройстве приложения, пакетах, моделях, экранах, Firebase и связи игровой механики со
сценами — не выдумывать детали, а опираться на гайд.

**Портируешь клиент (веб и т.д.) или спрашивают про ход / совмещение / каскад** — сначала
**`GAMEPLAY.md`**, потом код.

## Источник правды

1. **Игровой цикл и правила хода** — **`GAMEPLAY.md`** (обязательно при порте, вебе, вопросах «как
   работает ход / совмещение / каскад»).
2. **Архитектура приложения** — **`PROJECT_CONTEXT.md`** в корне workspace.

## Связанные гайды

- Схема JSON сцены, условия, эффекты — skill `twilights-scene-json` и **`SCENE_CREATION_GUIDE.md`
  ** (по необходимости, секциями).
- AI противника — **`ENEMY_AI.md`**.
- Разбор логов боя — skill `twilights-battle-log-interpretation` и *
  *`RESULT_INTERPRETATION_GUIDE.md`**.
