#!/usr/bin/env python3
"""Structure + JSON validator for the shadowslave datapack.

Minecraft cannot run on the build box, so this checks the things that are
statically checkable: pack_format, singular directory names, JSON validity,
and that every function referenced by a function tag actually exists.
"""
import json
import sys
from pathlib import Path

PACK = Path(__file__).resolve().parent.parent
DATA = PACK / "data"
REQUIRED_PACK_FORMAT = 48

# Plural names silently do nothing in 1.21 — catching them is the whole point.
PLURAL_TRAPS = {
    "functions", "advancements", "loot_tables", "predicates",
    "recipes", "structures", "item_modifiers", "dimensions", "dimension_types",
}

errors = []


def check_pack_mcmeta():
    path = PACK / "pack.mcmeta"
    if not path.is_file():
        errors.append("pack.mcmeta is missing")
        return
    meta = json.loads(path.read_text())
    fmt = meta.get("pack", {}).get("pack_format")
    if fmt != REQUIRED_PACK_FORMAT:
        errors.append(f"pack_format is {fmt!r}, expected {REQUIRED_PACK_FORMAT} (1.21.1)")


def check_no_plural_dirs():
    for d in DATA.rglob("*"):
        if d.is_dir() and d.name in PLURAL_TRAPS:
            errors.append(f"plural directory {d.relative_to(PACK)} — 1.21 requires singular")


def check_json_parses():
    for f in DATA.rglob("*.json"):
        try:
            json.loads(f.read_text())
        except json.JSONDecodeError as exc:
            errors.append(f"{f.relative_to(PACK)}: invalid JSON — {exc}")


def function_path(ref):
    """shadowslave:nightmare/enter -> data/shadowslave/function/nightmare/enter.mcfunction"""
    namespace, _, name = ref.partition(":")
    return DATA / namespace / "function" / f"{name}.mcfunction"


def check_tagged_functions_exist():
    for tag in (DATA / "minecraft" / "tags" / "function").glob("*.json"):
        try:
            values = json.loads(tag.read_text()).get("values", [])
        except json.JSONDecodeError:
            continue  # already reported by check_json_parses
        for ref in values:
            if isinstance(ref, str) and not function_path(ref).is_file():
                errors.append(f"{tag.name} references missing function {ref}")


def main():
    check_pack_mcmeta()
    check_no_plural_dirs()
    check_json_parses()
    check_tagged_functions_exist()
    if errors:
        for e in errors:
            print(f"FAIL: {e}")
        return 1
    print("OK: datapack structure valid")
    return 0


if __name__ == "__main__":
    sys.exit(main())
