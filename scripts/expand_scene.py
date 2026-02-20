#!/usr/bin/env python3
"""
Раскрывает *_base.json сцены в готовый .json файл.

Читает JSON с полями conditionDefs и effectDefs, подставляет по именам
все ссылки в массивах conditions/conditionsForDisplay/conditionsForEnable
и effects/additionalEffects, затем записывает результат без блоков defs.

Использование:
  python scripts/expand_scene.py <path_to_base.json>
  python scripts/expand_scene.py example/rebeld/10/9_sneak/rebeld_9_sneak_base.json

Выходной файл: тот же каталог, имя без _base (rebeld_9_sneak.json).
Требуется Python 3.6+.
"""

import copy
import json
import sys
from pathlib import Path

CONDITION_KEYS = ("conditions", "conditionsForDisplay", "conditionsForEnable")
EFFECT_KEYS = ("effects", "additionalEffects")
REACTION_EFFECT_KEY = "reactionEffect"


def expand(value, condition_defs, effect_defs):
    """Рекурсивно раскрывает ссылки на условия и эффекты. Мутирует копию."""
    if isinstance(value, dict):
        out = {}
        for k, v in value.items():
            if k in CONDITION_KEYS and isinstance(v, list):
                out[k] = _expand_conditions(v, condition_defs, effect_defs)
            elif k in EFFECT_KEYS and isinstance(v, list):
                out[k] = _expand_effects(v, condition_defs, effect_defs)
            elif k == REACTION_EFFECT_KEY:
                if isinstance(v, str):
                    if v not in effect_defs:
                        raise KeyError(f"Unknown effect ref in reactionEffect: '{v}'")
                    resolved = copy.deepcopy(effect_defs[v])
                    out[k] = expand(resolved, condition_defs, effect_defs)
                else:
                    out[k] = expand(v, condition_defs, effect_defs)
            else:
                out[k] = expand(v, condition_defs, effect_defs)
        return out
    if isinstance(value, list):
        return [expand(item, condition_defs, effect_defs) for item in value]
    return value


def _expand_conditions(arr, condition_defs, effect_defs):
    result = []
    for item in arr:
        if isinstance(item, str):
            if item not in condition_defs:
                raise KeyError(f"Unknown condition ref: '{item}'")
            result.append(copy.deepcopy(condition_defs[item]))
        else:
            result.append(expand(item, condition_defs, effect_defs))
    return result


def _expand_effects(arr, condition_defs, effect_defs):
    result = []
    for item in arr:
        if isinstance(item, str):
            if item not in effect_defs:
                raise KeyError(f"Unknown effect ref: '{item}'")
            resolved = copy.deepcopy(effect_defs[item])
            result.append(expand(resolved, condition_defs, effect_defs))
        else:
            result.append(expand(item, condition_defs, effect_defs))
    return result


def main():
    if len(sys.argv) != 2:
        print("Usage: python expand_scene.py <path_to_base.json>", file=sys.stderr)
        sys.exit(1)

    base_path = Path(sys.argv[1])
    if not base_path.exists():
        print(f"File not found: {base_path}", file=sys.stderr)
        sys.exit(1)
    if "_base" not in base_path.stem or not base_path.suffix.lower() == ".json":
        print("Input file name must end with _base.json", file=sys.stderr)
        sys.exit(1)

    out_name = base_path.stem.replace("_base", "") + base_path.suffix
    out_path = base_path.parent / out_name

    with open(base_path, "r", encoding="utf-8") as f:
        data = json.load(f)

    condition_defs = data.pop("conditionDefs", None) or {}
    effect_defs = data.pop("effectDefs", None) or {}

    expanded = expand(data, condition_defs, effect_defs)

    with open(out_path, "w", encoding="utf-8") as f:
        json.dump(expanded, f, ensure_ascii=False, indent=2)

    print(f"Written: {out_path}")


if __name__ == "__main__":
    main()
