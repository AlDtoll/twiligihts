#!/usr/bin/env python3
"""
Создаёт шаблон новой сцены: три файла _static.json, _defs.json, _dynamic.json.

Использование:
  python scripts/new_scene.py <директория> <префикс>
  python scripts/new_scene.py example/palabrot/10/2_chase/ palabrot_2_chase

  # Количество типов гемов (по умолчанию 5):
  python scripts/new_scene.py example/rook/10/3_battle/ rook_3_battle --types=5

Директория создаётся автоматически если не существует.
Существующие файлы НЕ перезаписываются (добавляй --force чтобы перезаписать).
"""

import json
import sys
from pathlib import Path


def gem_setting(t: int, name: str, display: str) -> dict:
    return {
        "type": str(t),
        "name": name,
        "displayName": display,
        "fullValue": 10,
        "bonusValue": 2,
        "bonusProbability": 20,
        "halfProbability": 25,
        "turnKeepStrategy": 50,
        "damageKeepStrategy": 100,
    }


DEFAULT_GEM_SETTINGS = [
    gem_setting(1, "type1", "очков1"),
    gem_setting(2, "type2", "очков2"),
    gem_setting(3, "type3", "очков3"),
    gem_setting(4, "type4", "очков4"),
    gem_setting(5, "type5", "очков5"),
]

STATIC_TEMPLATE = {
    "Hero": {
        "hp": 20,
        "maxHp": 20,
        "name": "Герой",
        "shield": 0
    },
    "Enemy": {
        "hp": 100,
        "maxHp": 100,
        "name": "Противник",
        "info": "Описание шкалы противника",
        "shield": 0
    },
    "HeroStocks": [
        {"gemType": i, "maxValue": 100, "value": 0} for i in range(1, 6)
    ],
    "EnemyStocks": [
        {"gemType": i, "maxValue": 100, "value": 0} for i in range(1, 6)
    ],
    "Settings": {
        "types": 5,
        "stopGenerate": False,
        "animateEnemy": False,
        "makeEnemyMove": False,
        "showHeroAnimation": False,
        "showHeroPortrait": False,
        "showEnemyAnimation": False,
        "gemSettings": DEFAULT_GEM_SETTINGS,
    }
}

DEFS_TEMPLATE = {
    "conditionDefs": {
        "_example_condition": {
            "parameter": "STATUS",
            "symbol": "HAVE",
            "target": "HERO",
            "name": "Пример статуса"
        }
    },
    "effectDefs": {
        "_example_effect": {
            "command": "INFO",
            "message": "Пример эффекта"
        }
    },
    "statusDefs": {
        "_example_status": {
            "name": "Пример статуса",
            "type": "INFO",
            "value": 1,
            "duration": 1
        }
    }
}

DYNAMIC_TEMPLATE = {
    "EnemyStatuses": [],
    "EnemyStates": [],
    "EnemyHands": [
        {
            "gemType": 1,
            "name": "Противник",
            "perks": [
                {
                    "place": True,
                    "name": "Пример: авто-навык противника",
                    "description": "Описание",
                    "effects": [
                        {
                            "command": "INFO",
                            "message": "Пример авто-навыка"
                        }
                    ],
                    "prices": [{"gemType": 1, "value": 0}]
                }
            ]
        }
    ],
    "HeroStatuses": [],
    "HeroStates": [],
    "HeroHands": [
        {
            "gemType": 1,
            "name": "Рука 1",
            "perks": [
                {
                    "name": "Рука 1: Действие",
                    "description": "Описание навыка",
                    "effects": [
                        {
                            "command": "ATTACK",
                            "target": "ENEMY",
                            "value": 5
                        }
                    ],
                    "prices": [{"gemType": 1, "value": 20}]
                }
            ]
        },
        {
            "gemType": 6,
            "name": "Финал",
            "perks": [
                {
                    "name": "Завершить сцену",
                    "description": "Цель достигнута",
                    "conditionsForDisplay": [
                        {
                            "parameter": "HP",
                            "symbol": "LESS",
                            "value": 20,
                            "target": "ENEMY"
                        }
                    ],
                    "effects": [
                        {
                            "command": "INFO",
                            "message": "Сцена завершена."
                        },
                        {
                            "command": "FINISH",
                            "target": "HERO"
                        }
                    ],
                    "prices": [{"gemType": 6, "value": 0}]
                }
            ]
        }
    ],
    "HeroRules": [
        {
            "name": "4_gems_any",
            "orientation": None,
            "gemType": None,
            "minSize": 4,
            "perk": {
                "name": "Комбо 4",
                "effects": [
                    {
                        "command": "EDIT_STOCK",
                        "type": "CHANGE",
                        "gemTypes": [1, 2, 3, 4, 5],
                        "target": "HERO",
                        "value": 3
                    }
                ]
            }
        }
    ],
    "EnemySectors": []
}


def write_json(path: Path, data: dict, force: bool):
    if path.exists() and not force:
        print(f"⏭  {path.name} уже существует (пропускаю, используй --force)")
        return
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
    print(f"✅  {path.name}  ({path.stat().st_size} bytes)")


def main():
    args = [a for a in sys.argv[1:] if not a.startswith("--")]
    flags = [a for a in sys.argv[1:] if a.startswith("--")]
    force = "--force" in flags

    types_flag = next((f for f in flags if f.startswith("--types=")), None)
    gem_types = int(types_flag.split("=")[1]) if types_flag else 5

    if len(args) < 2:
        print("Usage: python scripts/new_scene.py <directory> <prefix> [--types=5] [--force]")
        sys.exit(1)

    directory = Path(args[0])
    prefix = args[1]

    directory.mkdir(parents=True, exist_ok=True)

    # Настроить количество типов гемов
    import copy
    static = copy.deepcopy(STATIC_TEMPLATE)
    dynamic = copy.deepcopy(DYNAMIC_TEMPLATE)

    static["Settings"]["types"] = gem_types
    static["HeroStocks"] = [{"gemType": i, "maxValue": 100, "value": 0} for i in range(1, gem_types + 1)]
    static["EnemyStocks"] = [{"gemType": i, "maxValue": 100, "value": 0} for i in range(1, gem_types + 1)]
    static["Settings"]["gemSettings"] = DEFAULT_GEM_SETTINGS[:gem_types]

    # Обновить цену финального перка на gemType = gem_types + 1
    final_hand = dynamic["HeroHands"][-1]
    final_hand["gemType"] = gem_types + 1
    final_hand["perks"][0]["prices"][0]["gemType"] = gem_types + 1

    write_json(directory / f"{prefix}_static.json", static, force)
    write_json(directory / f"{prefix}_defs.json", DEFS_TEMPLATE, force)
    write_json(directory / f"{prefix}_dynamic.json", dynamic, force)

    print(f"\nСледующий шаг:")
    print(f"  python scripts/build.py {directory} {prefix}")


if __name__ == "__main__":
    main()
