#!/usr/bin/env python3
"""
Валидирует *_base.json сцены до expand:
- все строковые ссылки в effects/additionalEffects → effectDefs
- все строковые ссылки в conditions* → conditionDefs
- все строковые ссылки в HeroStatuses/EnemyStatuses → statusDefs
- все имена статусов в EDIT_STATUS эффектах — строит словарь и предупреждает о похожих

Использование:
  python scripts/validate_scene.py <path_to_base.json>
"""

import json
import sys
from collections import defaultdict
from pathlib import Path


def similar(a, b):
    """Простая проверка похожести: нижний регистр совпадает."""
    return a.lower() == b.lower() and a != b


class Validator:
    def __init__(self, data, path):
        self.data = data
        self.path = path
        self.condition_defs = data.get("conditionDefs", {})
        self.effect_defs = data.get("effectDefs", {})
        self.status_defs = data.get("statusDefs", {})
        self.errors = []
        self.warnings = []
        self.status_usages = defaultdict(list)  # name → [locations]

    def error(self, msg):
        self.errors.append(msg)

    def warn(self, msg):
        self.warnings.append(msg)

    def check_effect_refs(self, effects, location):
        for i, e in enumerate(effects):
            if isinstance(e, str):
                ref = e.split("|")[0].strip()
                if ref not in self.effect_defs:
                    suggestions = [k for k in self.effect_defs if similar(k, ref)]
                    hint = f"  Похожее: {suggestions[0]}" if suggestions else ""
                    self.error(f"Неизвестный effectDef '{ref}' в {location}[{i}]{hint}")
                continue
            if not isinstance(e, dict):
                continue
            cmd = e.get("command")
            if cmd == "EDIT_STATUS":
                s = e.get("status")
                if isinstance(s, str):
                    if s not in self.status_defs:
                        suggestions = [k for k in self.status_defs if similar(k, s)]
                        hint = f"  Похожее: {suggestions[0]}" if suggestions else ""
                        self.error(f"Неизвестный statusDef '{s}' в {location}[{i}].status{hint}")
                    else:
                        name = self.status_defs[s].get("name", s)
                        self.status_usages[name].append(location)
                elif isinstance(s, dict) and "name" in s:
                    self.status_usages[s["name"]].append(location)
            for sub in ("additionalEffects",):
                if sub in e and isinstance(e[sub], list):
                    self.check_effect_refs(e[sub], f"{location}[{i}].{sub}")
            for ckey in ("conditions", "conditionsForDisplay", "conditionsForEnable"):
                if ckey in e:
                    self.check_condition_refs(e[ckey], f"{location}[{i}].{ckey}")

    def check_condition_refs(self, conditions, location):
        for i, c in enumerate(conditions):
            if isinstance(c, str):
                if c not in self.condition_defs:
                    suggestions = [k for k in self.condition_defs if similar(k, c)]
                    hint = f"  Похожее: {suggestions[0]}" if suggestions else ""
                    self.error(f"Неизвестный conditionDef '{c}' в {location}[{i}]{hint}")

    def check_perk(self, perk, location):
        if not isinstance(perk, dict):
            return
        for ckey in ("conditions", "conditionsForDisplay", "conditionsForEnable"):
            if ckey in perk:
                self.check_condition_refs(perk[ckey], f"{location}.{ckey}")
        if "effects" in perk:
            self.check_effect_refs(perk["effects"], f"{location}.effects")

    def check_hands(self, hands, label):
        for hi, hand in enumerate(hands):
            if not isinstance(hand, dict):
                continue
            for pi, perk in enumerate(hand.get("perks", [])):
                loc = f"{label}[{hi}]({hand.get('name','?')}).perks[{pi}]({perk.get('name','?')})"
                self.check_perk(perk, loc)

    def check_status_list(self, key):
        for i, s in enumerate(self.data.get(key, [])):
            if isinstance(s, str):
                if s not in self.status_defs:
                    suggestions = [k for k in self.status_defs if similar(k, s)]
                    hint = f"  Похожее: {suggestions[0]}" if suggestions else ""
                    self.error(f"Неизвестный statusDef '{s}' в {key}[{i}]{hint}")
                else:
                    name = self.status_defs[s].get("name", s)
                    self.status_usages[name].append(key)
            elif isinstance(s, dict) and "name" in s:
                self.status_usages[s["name"]].append(key)

    def check_states(self, key):
        for i, st in enumerate(self.data.get(key, [])):
            if not isinstance(st, dict):
                continue
            loc = f"{key}[{i}]({st.get('name','?')})"
            self.check_condition_refs(st.get("conditions", []), f"{loc}.conditions")
            s = st.get("status")
            if isinstance(s, str):
                if s not in self.status_defs:
                    self.error(f"Неизвестный statusDef '{s}' в {loc}.status")
                else:
                    name = self.status_defs[s].get("name", s)
                    self.status_usages[name].append(loc)
            elif isinstance(s, dict) and "name" in s:
                self.status_usages[s["name"]].append(loc)

    def check_hero_rules(self):
        for i, rule in enumerate(self.data.get("HeroRules", [])):
            if not isinstance(rule, dict):
                continue
            perk = rule.get("perk", {})
            self.check_perk(perk, f"HeroRules[{i}]({rule.get('name','?')}).perk")

    def check_sectors(self):
        for i, sector in enumerate(self.data.get("EnemySectors", [])):
            if not isinstance(sector, dict):
                continue
            perk = sector.get("perk", {})
            self.check_perk(perk, f"EnemySectors[{i}]({sector.get('name','?')}).perk")

    def check_effectdefs(self):
        """Проверяем рекурсивные ссылки внутри самих effectDefs."""
        for name, val in self.effect_defs.items():
            items = val if isinstance(val, list) else [val]
            for item in items:
                if isinstance(item, str):
                    ref = item.split("|")[0].strip()
                    if ref not in self.effect_defs:
                        self.error(f"effectDefs['{name}'] ссылается на неизвестный effectDef '{ref}'")
                elif isinstance(item, dict):
                    for sub in ("additionalEffects", "effects"):
                        if sub in item:
                            self.check_effect_refs(item[sub], f"effectDefs['{name}'].{sub}")

    def check_similar_status_names(self):
        names = list(self.status_usages.keys())
        for i in range(len(names)):
            for j in range(i + 1, len(names)):
                if similar(names[i], names[j]):
                    self.warn(
                        f"Похожие имена статусов (возможная опечатка): '{names[i]}' и '{names[j]}'"
                    )

    def run(self):
        self.check_hands(self.data.get("HeroHands", []), "HeroHands")
        self.check_hands(self.data.get("EnemyHands", []), "EnemyHands")
        self.check_status_list("HeroStatuses")
        self.check_status_list("EnemyStatuses")
        self.check_states("HeroStates")
        self.check_states("EnemyStates")
        self.check_hero_rules()
        self.check_sectors()
        self.check_effectdefs()
        self.check_similar_status_names()

    def report(self):
        print(f"Валидация: {self.path.name}")
        print("=" * 50)

        if not self.errors and not self.warnings:
            print("✅  Ошибок не найдено")
        else:
            if self.errors:
                print(f"\n❌  ОШИБКИ ({len(self.errors)}):")
                for e in self.errors:
                    print(f"  • {e}")
            if self.warnings:
                print(f"\n⚠️   ПРЕДУПРЕЖДЕНИЯ ({len(self.warnings)}):")
                for w in self.warnings:
                    print(f"  • {w}")

        print(f"\n📋  Статусы в сцене ({len(self.status_usages)}):")
        for name in sorted(self.status_usages.keys()):
            count = len(self.status_usages[name])
            print(f"  • \"{name}\"  ({count} использований)")

        return len(self.errors) == 0


def main():
    if len(sys.argv) != 2:
        print("Usage: python scripts/validate_scene.py <path_to_base.json>", file=sys.stderr)
        sys.exit(1)

    path = Path(sys.argv[1])
    if not path.exists():
        print(f"File not found: {path}", file=sys.stderr)
        sys.exit(1)

    with open(path, encoding="utf-8") as f:
        data = json.load(f)

    validator = Validator(data, path)
    validator.run()
    ok = validator.report()
    sys.exit(0 if ok else 1)


if __name__ == "__main__":
    main()
