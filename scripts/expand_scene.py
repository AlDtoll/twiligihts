#!/usr/bin/env python3
"""
Раскрывает *_base.json сцены в готовый .json файл.

Читает JSON с полями conditionDefs, effectDefs и statusDefs, подставляет по именам
все ссылки в массивах conditions/conditionsForDisplay/conditionsForEnable,
effects/additionalEffects, в поле status (эффекты EDIT_STATUS, HeroStates/EnemyStates),
а также строки в массивах HeroStatuses/EnemyStatuses, затем записывает результат без блоков defs.

Значение в effectDefs может быть либо одним объектом эффекта, либо массивом
таких объектов: ссылка в effects раскрывается в несколько эффектов подряд
(например strike_zevft / strike_ladron). Для reactionEffect по-прежнему
нужен один эффект, не массив.

Для статусов REACTION предпочтительно поле reactionPerk: объект Perk
(name, effects, conditionsForDisplay, …); вложенный ключ effects обрабатывается
как обычный массив эффектов. Старое reactionEffect по-прежнему поддерживается приложением.

Использование:
  python scripts/expand_scene.py <path_to_base.json>
  python scripts/expand_scene.py example/rebeld/10/9_sneak/rebeld_9_sneak_base.json

Выходной файл: тот же каталог, имя без _base (rebeld_9_sneak.json).
Требуется Python 3.6+.
"""

import copy
import json
import re
import sys
from pathlib import Path

CONDITION_KEYS = ("conditions", "conditionsForDisplay", "conditionsForEnable")
EFFECT_KEYS = ("effects", "additionalEffects")
REACTION_EFFECT_KEY = "reactionEffect"
STATUS_KEY = "status"
STATUS_LIST_KEYS = ("HeroStatuses", "EnemyStatuses")
_INT_RE = re.compile(r"^-?\d+$")
_FLOAT_RE = re.compile(r"^-?(?:\d+\.\d*|\d*\.\d+)$")


def expand(value, condition_defs, effect_defs, status_defs):
    """Рекурсивно раскрывает ссылки на условия, эффекты и статусы. Мутирует копию."""
    if isinstance(value, dict):
        out = {}
        for k, v in value.items():
            if k in CONDITION_KEYS and isinstance(v, list):
                out[k] = _expand_conditions(v, condition_defs, effect_defs, status_defs)
            elif k in EFFECT_KEYS and isinstance(v, list):
                out[k] = _expand_effects(v, condition_defs, effect_defs, status_defs)
            elif k in STATUS_LIST_KEYS and isinstance(v, list):
                out[k] = _expand_status_list(v, condition_defs, effect_defs, status_defs)
            elif k == REACTION_EFFECT_KEY:
                if isinstance(v, str):
                    if v not in effect_defs:
                        raise KeyError(f"Unknown effect ref in reactionEffect: '{v}'")
                    resolved = copy.deepcopy(effect_defs[v])
                    out[k] = expand(resolved, condition_defs, effect_defs, status_defs)
                else:
                    out[k] = expand(v, condition_defs, effect_defs, status_defs)
            elif k == STATUS_KEY:
                if isinstance(v, str):
                    if v not in status_defs:
                        raise KeyError(f"Unknown status ref: '{v}'")
                    resolved = copy.deepcopy(status_defs[v])
                    out[k] = expand(resolved, condition_defs, effect_defs, status_defs)
                else:
                    out[k] = expand(v, condition_defs, effect_defs, status_defs)
            else:
                out[k] = expand(v, condition_defs, effect_defs, status_defs)
        return out
    if isinstance(value, list):
        return [expand(item, condition_defs, effect_defs, status_defs) for item in value]
    return value


def _expand_conditions(arr, condition_defs, effect_defs, status_defs):
    result = []
    for item in arr:
        if isinstance(item, str):
            if item not in condition_defs:
                raise KeyError(f"Unknown condition ref: '{item}'")
            result.append(copy.deepcopy(condition_defs[item]))
        else:
            result.append(expand(item, condition_defs, effect_defs, status_defs))
    return result


def _expand_effects(arr, condition_defs, effect_defs, status_defs):
    result = []
    for item in arr:
        if isinstance(item, str):
            effect_ref, overrides = _parse_effect_ref_with_overrides(item)
            if effect_ref not in effect_defs:
                raise KeyError(f"Unknown effect ref: '{effect_ref}'")
            resolved = copy.deepcopy(effect_defs[effect_ref])
            if overrides:
                # If effectDefs contains a LIST and the reference uses DSL overrides,
                # interpret it as: "apply overrides to a wrapper effect, and run the list
                # as additionalEffects".
                #
                # This keeps list-expansion semantics (as before) while allowing DSL
                # attributes like probability/successType to gate the whole list.
                if isinstance(resolved, list):
                    wrapper = {
                        "command": "INFO",
                        "showFail": False,
                        "message": "",
                        "additionalEffects": resolved,
                    }
                    resolved = _apply_effect_overrides(wrapper, overrides)
                else:
                    # For object effects, overrides always set/append keys even if the key
                    # was not present in the base effect.
                    resolved = _apply_effect_overrides(resolved, overrides)
            if isinstance(resolved, list):
                for sub in resolved:
                    result.append(expand(sub, condition_defs, effect_defs, status_defs))
            else:
                result.append(expand(resolved, condition_defs, effect_defs, status_defs))
        else:
            result.append(expand(item, condition_defs, effect_defs, status_defs))
    return result


def _expand_status_list(arr, condition_defs, effect_defs, status_defs):
    result = []
    for item in arr:
        if isinstance(item, str):
            if item not in status_defs:
                raise KeyError(f"Unknown status ref in {STATUS_LIST_KEYS}: '{item}'")
            result.append(expand(copy.deepcopy(status_defs[item]), condition_defs, effect_defs, status_defs))
        else:
            result.append(expand(item, condition_defs, effect_defs, status_defs))
    return result


def _parse_effect_ref_with_overrides(raw):
    """
    Parse effect reference with optional DSL:
    refName|field=value|arrayField+=item
    """
    if "|" not in raw:
        return raw, []

    parts = [part.strip() for part in raw.split("|")]
    if not parts or not parts[0]:
        raise ValueError(f"Invalid effect DSL ref: '{raw}'")

    ref = parts[0]
    overrides = []
    for token in parts[1:]:
        if not token:
            continue
        if "+=" in token:
            key, value = token.split("+=", 1)
            op = "append"
        elif "=" in token:
            key, value = token.split("=", 1)
            op = "set"
        else:
            raise ValueError(
                f"Invalid effect DSL token '{token}' in '{raw}'. Use key=value or key+=value."
            )
        key = key.strip()
        if not key:
            raise ValueError(f"Invalid effect DSL token '{token}' in '{raw}': empty key")
        overrides.append((key, op, _parse_scalar_or_json(value.strip())))
    return ref, overrides


def _parse_scalar_or_json(raw):
    lower = raw.lower()
    if lower == "true":
        return True
    if lower == "false":
        return False
    if lower == "null":
        return None
    if _INT_RE.match(raw):
        return int(raw)
    if _FLOAT_RE.match(raw):
        return float(raw)
    if raw.startswith("{") or raw.startswith("[") or raw.startswith('"'):
        try:
            return json.loads(raw)
        except json.JSONDecodeError:
            # Keep as plain string when it is not valid JSON.
            return raw
    return raw


def _apply_effect_overrides(base_effect, overrides):
    merged = copy.deepcopy(base_effect)
    for key, op, value in overrides:
        existing = merged.get(key)
        if isinstance(existing, list) or op == "append":
            if key not in merged or merged[key] is None:
                merged[key] = []
            elif not isinstance(merged[key], list):
                raise ValueError(
                    f"Effect DSL tries to append to non-list field '{key}' in effect '{base_effect.get('command', 'unknown')}'"
                )
            values_to_add = value if isinstance(value, list) else [value]
            merged[key].extend(values_to_add)
        else:
            merged[key] = value
    return merged


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
    status_defs = data.pop("statusDefs", None) or {}

    expanded = expand(data, condition_defs, effect_defs, status_defs)

    with open(out_path, "w", encoding="utf-8") as f:
        json.dump(expanded, f, ensure_ascii=False, indent=2)

    print(f"Written: {out_path}")


if __name__ == "__main__":
    main()
