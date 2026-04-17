#!/usr/bin/env python3
"""
Выводит структуру сцены (_base.json или .json) без тел эффектов.
Даёт «оглавление»: персонажи, руки, перки с ценами, словарь дефов и статусов.

Использование:
  python scripts/scene_outline.py <path_to_base.json>
"""

import json
import sys
from pathlib import Path


def fmt_price(prices):
    if not prices:
        return "бесплатно"
    parts = []
    for p in prices:
        v = p.get("value", 0)
        t = p.get("gemType", "?")
        if v == 0:
            parts.append("бесплатно")
        else:
            parts.append(f"{v} очков(тип {t})")
    return ", ".join(parts)


def fmt_conditions_display(perk):
    conds = perk.get("conditionsForDisplay", [])
    if not conds:
        return ""
    parts = []
    for c in conds:
        if isinstance(c, str):
            parts.append(c)
        else:
            param = c.get("parameter", "?")
            sym = c.get("symbol", "?")
            val = c.get("value", c.get("name", "?"))
            parts.append(f"{param} {sym} {val}")
    return f"  [виден если: {'; '.join(parts)}]"


def print_hands(hands, label):
    print(f"\n{label}:")
    for hand in hands:
        gem = hand.get("gemType", "?")
        name = hand.get("name", "—")
        perks = hand.get("perks", [])
        print(f"  gemType {gem}  \"{name}\"  — {len(perks)} перк(ов)")
        for p in perks:
            pname = p.get("name", "—")
            price = fmt_price(p.get("prices", []))
            cooldown = p.get("coolDown", None)
            charges = p.get("charges", None)
            place = p.get("place", False)
            flags = []
            if place:
                flags.append("авто")
            if cooldown:
                flags.append(f"кд {cooldown}")
            if charges:
                flags.append(f"зарядов {charges}")
            flag_str = f"  [{', '.join(flags)}]" if flags else ""
            cond_str = fmt_conditions_display(p)
            desc = p.get("description", "")
            desc_str = f"\n        {desc}" if desc else ""
            print(f"    ✦ {pname}  [{price}]{flag_str}{cond_str}{desc_str}")


def collect_status_names(data):
    """Собирает все имена статусов, встречающиеся в сцене (из EDIT_STATUS эффектов и HeroStatuses/EnemyStatuses)."""
    names = set()

    def scan_effects(effects):
        for e in effects:
            if isinstance(e, str):
                continue
            if e.get("command") == "EDIT_STATUS":
                s = e.get("status")
                if isinstance(s, dict) and "name" in s:
                    names.add(s["name"])
                elif isinstance(s, str):
                    names.add(s)
            for sub_key in ("additionalEffects",):
                if sub_key in e and isinstance(e[sub_key], list):
                    scan_effects(e[sub_key])

    def scan_perks(perks):
        for p in perks:
            if isinstance(p, str):
                continue
            scan_effects(p.get("effects", []))

    def scan_hands(hands):
        for hand in hands:
            if isinstance(hand, dict):
                scan_perks(hand.get("perks", []))

    scan_hands(data.get("HeroHands", []))
    scan_hands(data.get("EnemyHands", []))

    for key in ("HeroStatuses", "EnemyStatuses"):
        for s in data.get(key, []):
            if isinstance(s, str):
                names.add(s)
            elif isinstance(s, dict) and "name" in s:
                names.add(s["name"])

    for key in ("HeroStates", "EnemyStates"):
        for st in data.get(key, []):
            if isinstance(st, dict):
                s = st.get("status")
                if isinstance(s, dict) and "name" in s:
                    names.add(s["name"])

    for ef in data.get("effectDefs", {}).values():
        items = ef if isinstance(ef, list) else [ef]
        for item in items:
            if isinstance(item, dict):
                scan_effects([item])

    return sorted(names)


def main():
    if len(sys.argv) != 2:
        print("Usage: python scripts/scene_outline.py <path_to_json>", file=sys.stderr)
        sys.exit(1)

    path = Path(sys.argv[1])
    if not path.exists():
        print(f"File not found: {path}", file=sys.stderr)
        sys.exit(1)

    with open(path, encoding="utf-8") as f:
        data = json.load(f)

    condition_defs = data.get("conditionDefs", {})
    effect_defs = data.get("effectDefs", {})
    status_defs = data.get("statusDefs", {})

    hero = data.get("Hero", {})
    enemy = data.get("Enemy", {})

    print("=" * 60)
    print(f"СЦЕНА: {path.name}")
    print("=" * 60)

    print(f"\nГЕРОЙ: {hero.get('name', '—')}  HP {hero.get('hp', '?')}/{hero.get('maxHp', '?')}  щит {hero.get('shield', 0)}")
    print(f"ПРОТИВНИК: {enemy.get('name', '—')}  HP {enemy.get('hp', '?')}/{enemy.get('maxHp', '?')}  щит {enemy.get('shield', 0)}")
    enemy_info = enemy.get("info", "")
    if enemy_info:
        print(f"  → {enemy_info}")

    settings = data.get("Settings", {})
    gem_types = settings.get("types", "?")
    print(f"\nНАСТРОЙКИ: типов гемов {gem_types}  makeEnemyMove={settings.get('makeEnemyMove', False)}")

    gem_settings = settings.get("gemSettings", [])
    if gem_settings:
        print("  Гемы:", "  |  ".join(
            f"тип {g['type']}={g.get('displayName', g.get('name', '?'))}" for g in gem_settings
        ))

    print_hands(data.get("HeroHands", []), "РУКИ ГЕРОЯ")
    print_hands(data.get("EnemyHands", []), "РУКИ ПРОТИВНИКА")

    hero_rules = data.get("HeroRules", [])
    if hero_rules:
        print(f"\nHEROROLES ({len(hero_rules)}):")
        for r in hero_rules:
            perk = r.get("perk", {})
            print(f"  {r.get('name', '—')}  → {perk.get('name', '—')}")

    sectors = data.get("EnemySectors", [])
    if sectors:
        print(f"\nСЕКТОРА ({len(sectors)}):")
        for s in sectors:
            print(f"  [{s.get('id')}] {s.get('name', '—')}")

    hero_statuses = data.get("HeroStatuses", [])
    enemy_statuses = data.get("EnemyStatuses", [])
    hero_states = data.get("HeroStates", [])
    enemy_states = data.get("EnemyStates", [])

    if hero_statuses or hero_states:
        print(f"\nСТАРТОВЫЕ СТАТУСЫ ГЕРОЯ:")
        for s in hero_statuses:
            if isinstance(s, str):
                print(f"  • {s}  [ref→statusDefs]")
            else:
                print(f"  • {s.get('name','?')}  dur={s.get('duration','?')}  val={s.get('value','?')}")
        for st in hero_states:
            print(f"  ⚡ авто: {st.get('name','?')}")

    if enemy_statuses or enemy_states:
        print(f"\nСТАРТОВЫЕ СТАТУСЫ ПРОТИВНИКА:")
        for s in enemy_statuses:
            if isinstance(s, str):
                print(f"  • {s}  [ref→statusDefs]")
            else:
                print(f"  • {s.get('name','?')}  dur={s.get('duration','?')}  val={s.get('value','?')}")
        for st in enemy_states:
            print(f"  ⚡ авто: {st.get('name','?')}")

    all_status_names = collect_status_names(data)
    if all_status_names:
        print(f"\nВСЕ ИМЕНА СТАТУСОВ В СЦЕНЕ ({len(all_status_names)}):")
        for i, n in enumerate(all_status_names):
            end = "\n" if (i + 1) % 4 == 0 else ""
            print(f"  • {n}", end=end)
        print()

    if condition_defs:
        print(f"\nconditionDefs ({len(condition_defs)}): {', '.join(sorted(condition_defs.keys()))}")

    if effect_defs:
        print(f"\neffectDefs ({len(effect_defs)}): {', '.join(sorted(effect_defs.keys()))}")

    if status_defs:
        print(f"\nstatusDefs ({len(status_defs)}): {', '.join(sorted(status_defs.keys()))}")

    print("\n" + "=" * 60)


if __name__ == "__main__":
    main()
