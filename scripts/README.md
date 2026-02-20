# Скрипты сборки сцен

## expand_scene.py

Превращает файл сцены в формате `_base.json` в готовый JSON для игры: раскрывает ссылки
из `conditionDefs` и `effectDefs` в теле сцены и убирает блоки определений.

**Запуск:**

```bash
python scripts/expand_scene.py <путь к _base.json>
```

Пример: `python scripts/expand_scene.py example/rebeld/10/9_sneak/rebeld_9_sneak_base.json` →
создаётся `rebeld_9_sneak.json` в том же каталоге.

Подробное описание формата _base.json и правил для ИИ-агентов — в **docs/BASE_SCENE_FORMAT.md**.
