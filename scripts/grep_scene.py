#!/usr/bin/env python3
"""
Поиск по сцене (_base.json, _dynamic.json или любому JSON сцены).
Находит все места где встречается строка: в именах перков, статусах, эффектах, условиях.

Использование:
  python scripts/grep_scene.py <файл.json> <запрос>
  python scripts/grep_scene.py palabrot_1_investigate_base.json "Патруль"
  python scripts/grep_scene.py palabrot_1_investigate_dynamic.json "ATTACK"

Флаги:
  --names-only   показывать только имена перков (без контекста)
  --status       искать только по именам статусов
  --perk         искать только по именам перков
  --effect       искать только в командах/значениях эффектов
"""

import json
import sys
from pathlib import Path


def matches(text: str, query: str) -> bool:
    return query.lower() in str(text).lower()


class Finder:
    def __init__(self, query: str, names_only=False, filter_status=False,
                 filter_perk=False, filter_effect=False):
        self.query = query
        self.names_only = names_only
        self.filter_status = filter_status
        self.filter_perk = filter_perk
        self.filter_effect = filter_effect
        self.results = []

    def hit(self, location: str, context: str):
        self.results.append((location, context))

    def scan_condition(self, cond, location):
        if isinstance(cond, str):
            if matches(cond, self.query):
                self.hit(location, f"conditionDef ref: \"{cond}\"")
            return
        if not isinstance(cond, dict):
            return
        name = cond.get("name", "")
        if name and matches(name, self.query):
            self.hit(location, f"condition name: \"{name}\" [{cond.get('parameter','')} {cond.get('symbol','')}]")

    def scan_effect(self, eff, location):
        if isinstance(eff, str):
            if matches(eff, self.query):
                self.hit(location, f"effectDef ref: \"{eff}\"")
            return
        if not isinstance(eff, dict):
            return
        cmd = eff.get("command", "")
        if not self.filter_status and not self.filter_perk:
            if matches(cmd, self.query):
                self.hit(location, f"command: {cmd}")

        if cmd == "EDIT_STATUS":
            s = eff.get("status")
            if isinstance(s, dict) and matches(s.get("name", ""), self.query):
                self.hit(location, f"EDIT_STATUS name: \"{s.get('name')}\"")
            elif isinstance(s, str) and matches(s, self.query):
                self.hit(location, f"EDIT_STATUS statusDef: \"{s}\"")

        msg = eff.get("message", "")
        if msg and matches(msg, self.query):
            self.hit(location, f"INFO message: \"{msg[:80]}\"")

        for key in ("conditions", "conditionsForDisplay", "conditionsForEnable"):
            for i, c in enumerate(eff.get(key, [])):
                self.scan_condition(c, f"{location}.{key}[{i}]")

        for sub in eff.get("additionalEffects", []):
            self.scan_effect(sub, f"{location}.additionalEffects")

    def scan_perk(self, perk, location):
        if not isinstance(perk, dict):
            return
        name = perk.get("name", "")
        desc = perk.get("description", "")

        if matches(name, self.query):
            self.hit(location, f"perk name: \"{name}\"")
        if not self.filter_status and not self.filter_effect and desc and matches(desc, self.query):
            self.hit(location, f"perk description: \"{desc[:80]}\"")

        for key in ("conditions", "conditionsForDisplay", "conditionsForEnable"):
            for i, c in enumerate(perk.get(key, [])):
                self.scan_condition(c, f"{location}.{key}[{i}]")

        for i, e in enumerate(perk.get("effects", [])):
            self.scan_effect(e, f"{location}.effects[{i}]")

    def scan_hands(self, hands, label):
        for hi, hand in enumerate(hands):
            if not isinstance(hand, dict):
                continue
            hname = hand.get("name", f"[{hi}]")
            for pi, perk in enumerate(hand.get("perks", [])):
                self.scan_perk(perk, f"{label}[{hi}]({hname}).perks[{pi}]")

    def scan_status_list(self, key, statuses):
        for i, s in enumerate(statuses):
            if isinstance(s, str):
                if matches(s, self.query):
                    self.hit(f"{key}[{i}]", f"statusDef ref: \"{s}\"")
            elif isinstance(s, dict):
                name = s.get("name", "")
                if matches(name, self.query):
                    self.hit(f"{key}[{i}]", f"status name: \"{name}\"  dur={s.get('duration','?')} val={s.get('value','?')}")

    def scan_states(self, key, states):
        for i, st in enumerate(states):
            if not isinstance(st, dict):
                continue
            loc = f"{key}[{i}]({st.get('name','?')})"
            status = st.get("status")
            if isinstance(status, dict) and matches(status.get("name", ""), self.query):
                self.hit(loc, f"auto-status name: \"{status.get('name')}\"")
            elif isinstance(status, str) and matches(status, self.query):
                self.hit(loc, f"auto-status ref: \"{status}\"")
            for j, c in enumerate(st.get("conditions", [])):
                self.scan_condition(c, f"{loc}.conditions[{j}]")

    def scan_rules(self, rules):
        for i, rule in enumerate(rules):
            if not isinstance(rule, dict):
                continue
            loc = f"HeroRules[{i}]({rule.get('name','?')})"
            self.scan_perk(rule.get("perk", {}), f"{loc}.perk")

    def scan_sectors(self, sectors):
        for i, s in enumerate(sectors):
            if not isinstance(s, dict):
                continue
            loc = f"EnemySectors[{i}]({s.get('name','?')})"
            self.scan_perk(s.get("perk", {}), f"{loc}.perk")

    def scan_defs(self, data):
        for name, val in data.get("conditionDefs", {}).items():
            if matches(name, self.query):
                self.hit(f"conditionDefs['{name}']", "ключ")
            if isinstance(val, dict):
                n = val.get("name", "")
                if n and matches(n, self.query):
                    self.hit(f"conditionDefs['{name}']", f"name: \"{n}\"")

        for name, val in data.get("effectDefs", {}).items():
            if matches(name, self.query):
                self.hit(f"effectDefs['{name}']", "ключ")
            items = val if isinstance(val, list) else [val]
            for item in items:
                if isinstance(item, dict):
                    self.scan_effect(item, f"effectDefs['{name}']")

        for name, val in data.get("statusDefs", {}).items():
            if matches(name, self.query):
                self.hit(f"statusDefs['{name}']", "ключ")
            if isinstance(val, dict):
                n = val.get("name", "")
                if n and matches(n, self.query):
                    self.hit(f"statusDefs['{name}']", f"name: \"{n}\"")

    def scan(self, data):
        self.scan_defs(data)
        self.scan_hands(data.get("HeroHands", []), "HeroHands")
        self.scan_hands(data.get("EnemyHands", []), "EnemyHands")
        self.scan_hands(data.get("HeroTimePerks", []), "HeroTimePerks")
        self.scan_hands(data.get("EnemyTimePerks", []), "EnemyTimePerks")
        self.scan_hands(data.get("HeroStockPerks", []), "HeroStockPerks")
        self.scan_hands(data.get("EnemyStockPerks", []), "EnemyStockPerks")
        self.scan_status_list("HeroStatuses", data.get("HeroStatuses", []))
        self.scan_status_list("EnemyStatuses", data.get("EnemyStatuses", []))
        self.scan_states("HeroStates", data.get("HeroStates", []))
        self.scan_states("EnemyStates", data.get("EnemyStates", []))
        self.scan_rules(data.get("HeroRules", []))
        self.scan_sectors(data.get("EnemySectors", []))


def main():
    args = [a for a in sys.argv[1:] if not a.startswith("--")]
    flags = [a for a in sys.argv[1:] if a.startswith("--")]

    if len(args) < 2:
        print("Usage: python scripts/grep_scene.py <file.json> <query> [--names-only] [--status] [--perk] [--effect]")
        sys.exit(1)

    path = Path(args[0])
    query = args[1]

    if not path.exists():
        print(f"File not found: {path}", file=sys.stderr)
        sys.exit(1)

    with open(path, encoding="utf-8") as f:
        data = json.load(f)

    finder = Finder(
        query,
        names_only="--names-only" in flags,
        filter_status="--status" in flags,
        filter_perk="--perk" in flags,
        filter_effect="--effect" in flags,
    )
    finder.scan(data)

    if not finder.results:
        print(f'Ничего не найдено по запросу "{query}" в {path.name}')
        sys.exit(0)

    print(f'Результаты для "{query}" в {path.name}  ({len(finder.results)} совпадений):')
    print("=" * 60)
    for location, context in finder.results:
        print(f"  {location}")
        if not finder.names_only:
            print(f"    → {context}")


if __name__ == "__main__":
    main()
