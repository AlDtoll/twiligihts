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

Поддерживается строковый DSL для локальных overrides эффектов в массивах `effects` и
`additionalEffects`:

- `ref_name|probability=5|charges=2`
- `ref_name|conditions+=hero_no_ladron_wounded`

Правило merge: если поле в дефе является массивом (или используется `+=`), значения добавляются
(append), а скалярные/объектные поля переопределяются.

Подробное описание формата _base.json и правил для ИИ-агентов — в **docs/BASE_SCENE_FORMAT.md**.
