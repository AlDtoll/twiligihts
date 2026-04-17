#!/usr/bin/env python3
"""
Собирает *_base.json из трёх частей:
  *_static.json  — Hero, Enemy, HeroStocks, EnemyStocks, HeroResources, Settings
  *_defs.json    — conditionDefs, effectDefs, statusDefs
  *_dynamic.json — HeroHands, EnemyHands, HeroRules, EnemySectors,
                   HeroStatuses, EnemyStatuses, HeroStates, EnemyStates,
                   HeroTimePerks, EnemyTimePerks, HeroStockPerks, EnemyStockPerks

Использование:
  python scripts/compose_scene.py <директория сцены>
  python scripts/compose_scene.py example/palabrot/10/1_investigate/

Если в директории несколько наборов (разные префиксы), укажи префикс явно:
  python scripts/compose_scene.py example/palabrot/10/1_investigate/ palabrot_1_investigate

Выходной файл: <prefix>_base.json в той же директории.
Существующий _base.json перезаписывается.
"""

import json
import sys
from pathlib import Path

STATIC_SUFFIX = "_static.json"
DEFS_SUFFIX = "_defs.json"
DYNAMIC_SUFFIX = "_dynamic.json"

EXPECTED_STATIC_KEYS = {
    "Hero", "Enemy", "HeroStocks", "EnemyStocks", "HeroResources", "Settings",
}
EXPECTED_DEFS_KEYS = {
    "conditionDefs", "effectDefs", "statusDefs",
}
EXPECTED_DYNAMIC_KEYS = {
    "HeroHands", "EnemyHands", "HeroRules", "EnemySectors",
    "HeroStatuses", "EnemyStatuses", "HeroStates", "EnemyStates",
    "HeroTimePerks", "EnemyTimePerks", "HeroStockPerks", "EnemyStockPerks",
    "PlaceHands",
}

# Ключи, допустимые в _static (без defs) — строгой проверки нет, просто предупреждение
DEFS_KEYS = {"conditionDefs", "effectDefs", "statusDefs"}


def find_files(directory: Path, prefix: str = None):
    if prefix:
        candidates = {
            "static": directory / f"{prefix}{STATIC_SUFFIX}",
            "defs": directory / f"{prefix}{DEFS_SUFFIX}",
            "dynamic": directory / f"{prefix}{DYNAMIC_SUFFIX}",
        }
        return candidates, prefix

    # Автообнаружение по суффиксу
    statics = list(directory.glob(f"*{STATIC_SUFFIX}"))
    defss = list(directory.glob(f"*{DEFS_SUFFIX}"))
    dynamics = list(directory.glob(f"*{DYNAMIC_SUFFIX}"))

    if len(statics) != 1 or len(defss) != 1 or len(dynamics) != 1:
        return None, None

    # Определяем префикс из имени static файла
    prefix = statics[0].name[: -len(STATIC_SUFFIX)]
    return {
        "static": statics[0],
        "defs": defss[0],
        "dynamic": dynamics[0],
    }, prefix


def load(path: Path, label: str) -> dict:
    if not path.exists():
        print(f"⚠️  Файл не найден: {path}  ({label} — пропускаю, будет пустой блок)")
        return {}
    with open(path, encoding="utf-8") as f:
        return json.load(f)


def warn_unexpected_keys(data: dict, expected: set, label: str):
    unknown = set(data.keys()) - expected - DEFS_KEYS
    if unknown:
        print(f"  ⚠️  {label} содержит неожиданные ключи: {', '.join(sorted(unknown))}")
    if label == "_static" and any(k in data for k in DEFS_KEYS):
        found = [k for k in DEFS_KEYS if k in data]
        print(f"  ⚠️  _static содержит defs ({', '.join(found)}) — возможно, лишнее?")


def check_conflicts(static: dict, defs: dict, dynamic: dict):
    all_keys = list(static.keys()) + list(defs.keys()) + list(dynamic.keys())
    seen = {}
    for key in all_keys:
        if key in seen:
            print(f"  ❌  Конфликт: ключ '{key}' встречается в нескольких файлах")
        seen[key] = True


def main():
    if len(sys.argv) < 2:
        print("Usage: python scripts/compose_scene.py <directory> [prefix]", file=sys.stderr)
        sys.exit(1)

    directory = Path(sys.argv[1])
    if not directory.is_dir():
        # Может быть передан путь к одному из файлов — берём директорию
        directory = directory.parent

    prefix = sys.argv[2] if len(sys.argv) >= 3 else None

    files, detected_prefix = find_files(directory, prefix)

    if not files:
        print(
            f"❌  Не удалось найти файлы *{STATIC_SUFFIX}, *{DEFS_SUFFIX}, *{DYNAMIC_SUFFIX} "
            f"в {directory}. Укажи префикс явно:\n"
            f"  python scripts/compose_scene.py {directory} <prefix>",
            file=sys.stderr,
        )
        sys.exit(1)

    print(f"Сборка сцены: {detected_prefix}")
    print(f"  static:  {files['static'].name}")
    print(f"  defs:    {files['defs'].name}")
    print(f"  dynamic: {files['dynamic'].name}")

    static_data = load(files["static"], "_static")
    defs_data = load(files["defs"], "_defs")
    dynamic_data = load(files["dynamic"], "_dynamic")

    warn_unexpected_keys(static_data, EXPECTED_STATIC_KEYS, "_static")
    warn_unexpected_keys(defs_data, EXPECTED_DEFS_KEYS, "_defs")
    warn_unexpected_keys(dynamic_data, EXPECTED_DYNAMIC_KEYS, "_dynamic")
    check_conflicts(static_data, defs_data, dynamic_data)

    # Порядок блоков в результирующем _base.json:
    # 1) defs, 2) Enemy*, 3) Hero*, 4) общие (Rules, Sectors и т.д.), 5) Settings
    ORDER = [
        "conditionDefs", "effectDefs", "statusDefs",
        "Enemy", "EnemyStocks", "EnemyStatuses", "EnemyStates", "EnemyHands",
        "EnemyTimePerks", "EnemyStockPerks", "EnemySectors",
        "Hero", "HeroStocks", "HeroResources", "HeroStatuses", "HeroStates",
        "HeroHands", "HeroRules", "HeroTimePerks", "HeroStockPerks",
        "PlaceHands",
        "Settings",
    ]

    merged = {}
    all_sources = {**static_data, **defs_data, **dynamic_data}

    # Сначала в заданном порядке
    for key in ORDER:
        if key in all_sources:
            merged[key] = all_sources[key]

    # Остальные ключи (если есть неожиданные) — в конец
    for key, val in all_sources.items():
        if key not in merged:
            merged[key] = val

    out_path = directory / f"{detected_prefix}_base.json"
    with open(out_path, "w", encoding="utf-8") as f:
        json.dump(merged, f, ensure_ascii=False, indent=2)

    print(f"\n✅  {out_path.name}  ({out_path.stat().st_size // 1024} KB)")
    print(f"   Ключей: {len(merged)}")


if __name__ == "__main__":
    main()
