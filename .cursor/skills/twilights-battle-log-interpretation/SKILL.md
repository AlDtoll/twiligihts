---
name: twilights-battle-log-interpretation
description: >-
  Interprets Twilights battle logs (battle_log_*.txt) into design summaries (*_results.md),
  narrative (*_story.md), and structured stats (*_stats.md). Use when analyzing playthrough
  logs, scene outcomes, or when the user mentions RESULT_INTERPRETATION_GUIDE, лог боя,
  результаты сцены, story, сводка по прохождению.
---

# Twilights — интерпретация результатов сцены

## Цель

По пошаговому логу боя собрать краткую сводку для дизайна, художественное повествование и при
необходимости машиночитаемую статистику — в форматах и структуре, описанных в гайде.

## Источник правды

Прочитай (Read): **`RESULT_INTERPRETATION_GUIDE.md`** в корне workspace.

Там: что даёт лог, имена выходных файлов, структура разделов results/story/stats, что выделять как
важное.

## Связанное

- Таблица совмещения и агрегаты `*_stats.md` — skill **`twilights-battle-log-stats`** (
  эталон: `example/rebeld/10/9_sneak/rebeld_9_sneak_stats.md`).
- Как устроены поля сцены и эффекты в JSON — **`SCENE_CREATION_GUIDE.md`** (секциями).
- Контекст приложения — **`PROJECT_CONTEXT.md`**.
