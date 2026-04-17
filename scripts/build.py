#!/usr/bin/env python3
"""
Полный пайплайн сборки сцены: compose → validate → expand.

Использование:
  python scripts/build.py <директория сцены>
  python scripts/build.py example/palabrot/10/1_investigate/

  # Если несколько наборов в директории:
  python scripts/build.py example/palabrot/10/1_investigate/ palabrot_1_investigate

Флаги:
  --no-validate   пропустить validate_scene (быстрая сборка)
  --only-compose  только compose, без validate и expand
"""

import subprocess
import sys
from pathlib import Path

SCRIPTS = Path(__file__).parent


def run(cmd, label):
    print(f"\n{'─' * 50}")
    print(f"▶  {label}")
    print(f"   {' '.join(str(c) for c in cmd)}")
    print(f"{'─' * 50}")
    result = subprocess.run(cmd, text=True)
    if result.returncode != 0:
        print(f"\n❌  {label} завершился с ошибкой. Остановка.")
        sys.exit(result.returncode)


def find_base(directory: Path, prefix: str = None) -> Path:
    if prefix:
        return directory / f"{prefix}_base.json"
    candidates = list(directory.glob("*_base.json"))
    if len(candidates) == 1:
        return candidates[0]
    if len(candidates) > 1:
        print(f"⚠️  Найдено несколько _base.json: {[c.name for c in candidates]}")
        print("    Укажи префикс явно как второй аргумент.")
        sys.exit(1)
    return None


def main():
    args = [a for a in sys.argv[1:] if not a.startswith("--")]
    flags = [a for a in sys.argv[1:] if a.startswith("--")]

    no_validate = "--no-validate" in flags
    only_compose = "--only-compose" in flags

    if not args:
        print("Usage: python scripts/build.py <directory> [prefix] [--no-validate] [--only-compose]")
        sys.exit(1)

    directory = Path(args[0])
    if not directory.is_dir():
        directory = directory.parent
    prefix = args[1] if len(args) >= 2 else None

    py = sys.executable

    # Шаг 1: compose (только если есть _dynamic.json)
    dynamic_files = list(directory.glob("*_dynamic.json"))
    if dynamic_files:
        cmd = [py, str(SCRIPTS / "compose_scene.py"), str(directory)]
        if prefix:
            cmd.append(prefix)
        run(cmd, "compose_scene")
    else:
        print("⏭  _dynamic.json не найден — пропускаю compose, использую существующий _base.json")

    if only_compose:
        print("\n✅  Готово (только compose).")
        return

    # Определяем путь к _base.json
    base_path = find_base(directory, prefix)
    if not base_path or not base_path.exists():
        print(f"❌  _base.json не найден в {directory}")
        sys.exit(1)

    # Шаг 2: validate
    if not no_validate:
        run([py, str(SCRIPTS / "validate_scene.py"), str(base_path)], "validate_scene")

    # Шаг 3: expand
    run([py, str(SCRIPTS / "expand_scene.py"), str(base_path)], "expand_scene")

    print(f"\n{'=' * 50}")
    print(f"✅  Сборка завершена: {base_path.stem.replace('_base', '')}.json")
    print(f"{'=' * 50}")


if __name__ == "__main__":
    main()
