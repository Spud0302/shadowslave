#!/usr/bin/env python3
"""Structure + reference validator for the shadowslave datapack.

Minecraft cannot run on the build box, so this checks everything that is
statically checkable. That matters more than usual here: almost every way a
datapack breaks is silent. A misspelled objective, a tag that is tested but
never applied, a function or predicate id that does not resolve — none of
these produce an error a player would notice. They just quietly do nothing.

Checks:
  - pack.mcmeta exists, parses, and declares the right pack_format
  - no plural directory names (1.21 requires singular; plurals are ignored)
  - every JSON file parses
  - every function named by a function tag exists
  - every advancement `parent` resolves
  - every `advancement grant|revoke` names a real advancement
  - every `function <id>` call resolves
  - every `predicate <id>` reference resolves
  - every `execute in <dimension>` resolves
  - every scoreboard objective used is declared in init.mcfunction
  - every bossbar id used is declared
  - every tag tested in a selector is applied somewhere
  - every `attribute ... modifier add <id>` has a paired `remove` in the same file
"""
import json
import re
import sys
from pathlib import Path

PACK = Path(__file__).resolve().parent.parent
DATA = PACK / "data"
REQUIRED_PACK_FORMAT = 48
NAMESPACE = "shadowslave"

# Plural names silently do nothing in 1.21 — catching them is the whole point.
PLURAL_TRAPS = {
    "functions", "advancements", "loot_tables", "predicates",
    "recipes", "structures", "item_modifiers", "dimensions", "dimension_types",
}

errors = []


def mcfunctions():
    """Every .mcfunction file, as (path, [non-comment lines])."""
    for f in sorted(DATA.rglob("*.mcfunction")):
        lines = [
            line for line in f.read_text().splitlines()
            if line.strip() and not line.lstrip().startswith("#")
        ]
        yield f, lines


def resolves(ref, kind, suffix):
    """shadowslave:nightmare/enter -> data/shadowslave/<kind>/nightmare/enter<suffix>"""
    namespace, _, name = ref.partition(":")
    if not name:  # unqualified ids default to the minecraft namespace
        namespace, name = "minecraft", ref
    return (DATA / namespace / kind / f"{name}{suffix}").is_file()


def is_ours(ref):
    """Only our own namespace can be checked — vanilla ids live in the jar."""
    return ref.startswith(f"{NAMESPACE}:")


def check_pack_mcmeta():
    path = PACK / "pack.mcmeta"
    if not path.is_file():
        errors.append("pack.mcmeta is missing")
        return
    try:
        meta = json.loads(path.read_text())
    except json.JSONDecodeError as exc:
        errors.append(f"{path.relative_to(PACK)}: invalid JSON — {exc}")
        return
    pack = meta.get("pack")
    if not isinstance(pack, dict):
        errors.append(f"pack.mcmeta: pack field must be a JSON object, got {type(pack).__name__}")
        return
    fmt = pack.get("pack_format")
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


def check_tagged_functions_exist():
    for tag in (DATA / "minecraft" / "tags" / "function").glob("*.json"):
        try:
            values = json.loads(tag.read_text()).get("values", [])
        except json.JSONDecodeError:
            continue  # already reported by check_json_parses
        for ref in values:
            if isinstance(ref, str) and not resolves(ref, "function", ".mcfunction"):
                errors.append(f"{tag.name} references missing function {ref}")


def check_advancement_parents():
    for f in sorted((DATA / NAMESPACE / "advancement").rglob("*.json")):
        try:
            parent = json.loads(f.read_text()).get("parent")
        except json.JSONDecodeError:
            continue
        if parent and not resolves(parent, "advancement", ".json"):
            errors.append(f"{f.relative_to(PACK)}: parent {parent} does not exist")


def check_references():
    """Function, advancement, predicate and dimension ids used in commands."""
    patterns = [
        (re.compile(r"\bfunction ([\w:/.-]+)"), "function", ".mcfunction", "function"),
        (re.compile(r"\badvancement (?:grant|revoke) \S+ (?:only|from) ([\w:/.-]+)"),
         "advancement", ".json", "advancement"),
        (re.compile(r"\bpredicate ([\w:/.-]+)"), "predicate", ".json", "predicate"),
        (re.compile(r"\bexecute .*?\bin ([\w:/.-]+)"), "dimension", ".json", "dimension"),
    ]
    for f, lines in mcfunctions():
        for line in lines:
            for pattern, kind, suffix, label in patterns:
                for ref in pattern.findall(line):
                    if is_ours(ref) and not resolves(ref, kind, suffix):
                        errors.append(f"{f.relative_to(PACK)}: missing {label} {ref}")


def declared_objectives():
    init = DATA / NAMESPACE / "function" / "init.mcfunction"
    if not init.is_file():
        errors.append("init.mcfunction is missing — cannot check objectives")
        return set()
    return set(re.findall(r"scoreboard objectives add (\S+)", init.read_text()))


def check_objectives():
    """Every objective used must be declared in init.mcfunction."""
    declared = declared_objectives()
    if not declared:
        return
    used = {}  # objective -> first file that used it
    forms = [
        re.compile(r"scoreboard players (?:set|add|remove|get|enable|reset) \S+ (\S+)"),
        re.compile(r"\b(?:if|unless) score \S+ (\S+)"),
        re.compile(r"store result score \S+ (\S+)"),
    ]
    for f, lines in mcfunctions():
        for line in lines:
            for pattern in forms:
                for obj in pattern.findall(line):
                    used.setdefault(obj, f)
            # scores={obj=range,obj2=range}
            for block in re.findall(r"scores=\{([^}]*)\}", line):
                for pair in block.split(","):
                    name = pair.split("=")[0].strip()
                    if name:
                        used.setdefault(name, f)
    for obj, f in sorted(used.items()):
        if obj not in declared:
            errors.append(f"{f.relative_to(PACK)}: objective {obj!r} is never declared in init.mcfunction")


def check_bossbars():
    """Every bossbar id used must be created by `bossbar add`."""
    declared, used = set(), {}
    for f, lines in mcfunctions():
        for line in lines:
            for bar in re.findall(r"bossbar add ([\w:/.-]+)", line):
                declared.add(bar)
            for bar in re.findall(r"bossbar (?:set|get|remove) ([\w:/.-]+)", line):
                used.setdefault(bar, f)
            for bar in re.findall(r"store result bossbar ([\w:/.-]+)", line):
                used.setdefault(bar, f)
    for bar, f in sorted(used.items()):
        if bar not in declared:
            errors.append(f"{f.relative_to(PACK)}: bossbar {bar!r} is never created by `bossbar add`")


def check_tags():
    """Every tag tested in a selector must be applied somewhere.

    Catches the classic silent failure: `tag=ss_creature_spawnd` in a selector
    simply never matches, and nothing complains.
    """
    applied, tested = set(), {}
    for f, lines in mcfunctions():
        for line in lines:
            applied.update(re.findall(r"\btag \S+ add (\S+)", line))
            # Entities can also be born with tags, via summon NBT: Tags:["ss_creature"]
            for block in re.findall(r"Tags:\[([^\]]*)\]", line):
                applied.update(re.findall(r'"([^"]+)"', block))
            for name in re.findall(r"tag=!?(\w+)", line):
                tested.setdefault(name, f)
    for name, f in sorted(tested.items()):
        if name not in applied:
            errors.append(f"{f.relative_to(PACK)}: tag {name!r} is tested but never applied")


def check_attribute_modifiers():
    """Each `modifier add <id>` needs a paired `remove` in the same file.

    These upkeep functions run once a second forever. Without the remove, the
    modifier stacks until the player is unplayably fast or unkillable.
    """
    for f, lines in mcfunctions():
        added, removed = set(), set()
        for line in lines:
            added.update(re.findall(r"modifier add ([\w:/.-]+)", line))
            removed.update(re.findall(r"modifier remove ([\w:/.-]+)", line))
        for mod in sorted(added - removed):
            errors.append(
                f"{f.relative_to(PACK)}: modifier {mod!r} is added without a paired "
                f"`modifier remove` — it will stack on every upkeep tick"
            )


def main():
    check_pack_mcmeta()
    check_no_plural_dirs()
    check_json_parses()
    check_tagged_functions_exist()
    check_advancement_parents()
    check_references()
    check_objectives()
    check_bossbars()
    check_tags()
    check_attribute_modifiers()
    if errors:
        for e in errors:
            print(f"FAIL: {e}")
        return 1
    print("OK: datapack structure valid")
    return 0


if __name__ == "__main__":
    sys.exit(main())
