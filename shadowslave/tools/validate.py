#!/usr/bin/env python3
"""Static structure/reference validator for the Shadow Slave datapack.

Minecraft datapack failures are often silent, so release validation deliberately checks both
Minecraft-facing structure and project-specific invariants that have caused real regressions.

Checks:
  - pack.mcmeta exists, parses, and targets the 1.21.1 pack format
  - 1.21 singular data-directory names are used
  - every JSON file parses
  - tagged functions, advancement parents and command references resolve
  - every scoreboard objective, bossbar and selector tag used is declared/applied
  - persistent attribute modifiers remove before re-adding
  - dimension/worldgen field shapes match the 1.21.1 expectations used by this pack
  - upper-bound-only selector score filters are rejected by project policy
  - pack.mcmeta, init.mcfunction and test/selfcheck.mcfunction advertise one version
"""

from __future__ import annotations

import json
import re
import sys
from pathlib import Path

PACK = Path(__file__).resolve().parent.parent
DATA = PACK / "data"
REQUIRED_PACK_FORMAT = 48
NAMESPACE = "shadowslave"

PLURAL_TRAPS = {
    "functions",
    "advancements",
    "loot_tables",
    "predicates",
    "recipes",
    "structures",
    "item_modifiers",
    "dimensions",
    "dimension_types",
}

errors: list[str] = []


def mcfunctions():
    """Yield every .mcfunction as (path, non-comment/non-blank lines)."""
    for path in sorted(DATA.rglob("*.mcfunction")):
        lines = [
            line
            for line in path.read_text(encoding="utf-8").splitlines()
            if line.strip() and not line.lstrip().startswith("#")
        ]
        yield path, lines


def resolves(ref: str, kind: str, suffix: str) -> bool:
    """Resolve shadowslave:path -> data/shadowslave/<kind>/path<suffix>."""
    namespace, separator, name = ref.partition(":")
    if not separator:
        namespace, name = "minecraft", ref
    return (DATA / namespace / kind / f"{name}{suffix}").is_file()


def is_ours(ref: str) -> bool:
    return ref.startswith(f"{NAMESPACE}:")


def check_pack_mcmeta() -> None:
    path = PACK / "pack.mcmeta"
    if not path.is_file():
        errors.append("pack.mcmeta is missing")
        return
    try:
        meta = json.loads(path.read_text(encoding="utf-8"))
    except json.JSONDecodeError as exc:
        errors.append(f"pack.mcmeta: invalid JSON — {exc}")
        return

    pack = meta.get("pack")
    if not isinstance(pack, dict):
        errors.append(f"pack.mcmeta: pack field must be an object, got {type(pack).__name__}")
        return
    if pack.get("pack_format") != REQUIRED_PACK_FORMAT:
        errors.append(
            f"pack_format is {pack.get('pack_format')!r}, expected {REQUIRED_PACK_FORMAT} (1.21.1)"
        )


def check_no_plural_dirs() -> None:
    for directory in DATA.rglob("*"):
        if directory.is_dir() and directory.name in PLURAL_TRAPS:
            errors.append(
                f"plural directory {directory.relative_to(PACK)} — 1.21 requires singular"
            )


def check_json_parses() -> None:
    for path in DATA.rglob("*.json"):
        try:
            json.loads(path.read_text(encoding="utf-8"))
        except json.JSONDecodeError as exc:
            errors.append(f"{path.relative_to(PACK)}: invalid JSON — {exc}")


def check_tagged_functions_exist() -> None:
    tag_root = DATA / "minecraft" / "tags" / "function"
    for path in tag_root.glob("*.json"):
        try:
            values = json.loads(path.read_text(encoding="utf-8")).get("values", [])
        except json.JSONDecodeError:
            continue
        for ref in values:
            if isinstance(ref, str) and not resolves(ref, "function", ".mcfunction"):
                errors.append(f"{path.name} references missing function {ref}")


def check_advancement_parents() -> None:
    root = DATA / NAMESPACE / "advancement"
    for path in sorted(root.rglob("*.json")):
        try:
            parent = json.loads(path.read_text(encoding="utf-8")).get("parent")
        except json.JSONDecodeError:
            continue
        if parent and is_ours(parent) and not resolves(parent, "advancement", ".json"):
            errors.append(f"{path.relative_to(PACK)}: parent {parent} does not exist")


def check_references() -> None:
    patterns = [
        (re.compile(r"\bfunction ([\w:/.-]+)"), "function", ".mcfunction", "function"),
        (
            re.compile(r"\badvancement (?:grant|revoke) \S+ (?:only|from) ([\w:/.-]+)"),
            "advancement",
            ".json",
            "advancement",
        ),
        (re.compile(r"\bpredicate ([\w:/.-]+)"), "predicate", ".json", "predicate"),
        (re.compile(r"\bexecute .*?\bin ([\w:/.-]+)"), "dimension", ".json", "dimension"),
    ]
    for path, lines in mcfunctions():
        for line in lines:
            for pattern, kind, suffix, label in patterns:
                for ref in pattern.findall(line):
                    if is_ours(ref) and not resolves(ref, kind, suffix):
                        errors.append(f"{path.relative_to(PACK)}: missing {label} {ref}")


def declared_objectives() -> set[str]:
    init = DATA / NAMESPACE / "function" / "init.mcfunction"
    if not init.is_file():
        errors.append("init.mcfunction is missing — cannot check objectives")
        return set()
    return set(
        re.findall(
            r"scoreboard objectives add (\S+)", init.read_text(encoding="utf-8")
        )
    )


def check_objectives() -> None:
    declared = declared_objectives()
    if not declared:
        return

    used: dict[str, Path] = {}
    forms = [
        re.compile(r"scoreboard players (?:set|add|remove|get|enable|reset) \S+ (\S+)"),
        re.compile(r"\b(?:if|unless) score \S+ (\S+)"),
        re.compile(r"store result score \S+ (\S+)"),
    ]
    for path, lines in mcfunctions():
        for line in lines:
            for pattern in forms:
                for objective in pattern.findall(line):
                    used.setdefault(objective, path)
            for block in re.findall(r"scores=\{([^}]*)\}", line):
                for pair in block.split(","):
                    name = pair.split("=")[0].strip()
                    if name:
                        used.setdefault(name, path)

    for objective, path in sorted(used.items()):
        if objective not in declared:
            errors.append(
                f"{path.relative_to(PACK)}: objective {objective!r} is never declared in init.mcfunction"
            )


def check_bossbars() -> None:
    declared: set[str] = set()
    used: dict[str, Path] = {}
    for path, lines in mcfunctions():
        for line in lines:
            declared.update(re.findall(r"bossbar add ([\w:/.-]+)", line))
            for bar in re.findall(r"bossbar (?:set|get|remove) ([\w:/.-]+)", line):
                used.setdefault(bar, path)
            for bar in re.findall(r"store result bossbar ([\w:/.-]+)", line):
                used.setdefault(bar, path)

    for bar, path in sorted(used.items()):
        if bar not in declared:
            errors.append(
                f"{path.relative_to(PACK)}: bossbar {bar!r} is never created by `bossbar add`"
            )


def check_tags() -> None:
    applied: set[str] = set()
    tested: dict[str, Path] = {}
    for path, lines in mcfunctions():
        for line in lines:
            applied.update(re.findall(r"\btag \S+ add (\S+)", line))
            for block in re.findall(r"Tags:\[([^\]]*)\]", line):
                applied.update(re.findall(r'"([^"]+)"', block))
            for name in re.findall(r"tag=!?([\w.-]+)", line):
                tested.setdefault(name, path)

    for name, path in sorted(tested.items()):
        if name not in applied:
            errors.append(
                f"{path.relative_to(PACK)}: tag {name!r} is tested but never applied"
            )


def check_attribute_modifiers() -> None:
    for path, lines in mcfunctions():
        added: set[str] = set()
        removed: set[str] = set()
        for line in lines:
            added.update(re.findall(r"modifier add ([\w:/.-]+)", line))
            removed.update(re.findall(r"modifier remove ([\w:/.-]+)", line))
        for modifier in sorted(added - removed):
            errors.append(
                f"{path.relative_to(PACK)}: modifier {modifier!r} is added without a paired "
                "`modifier remove` — it will stack on every upkeep tick"
            )


NOISE_RANGES = {
    "minecraft:overworld": (-64, 384),
    "minecraft:large_biomes": (-64, 384),
    "minecraft:amplified": (-64, 384),
    "minecraft:nether": (0, 128),
    "minecraft:end": (0, 128),
    "minecraft:caves": (-64, 192),
    "minecraft:floating_islands": (0, 256),
}


def check_dimension_height() -> None:
    for path in sorted((DATA / NAMESPACE / "dimension").glob("*.json")):
        try:
            dimension = json.loads(path.read_text(encoding="utf-8"))
        except json.JSONDecodeError:
            continue

        settings = dimension.get("generator", {}).get("settings")
        if settings not in NOISE_RANGES:
            continue
        wanted_min, wanted_height = NOISE_RANGES[settings]

        type_ref = dimension.get("type", "")
        namespace, separator, name = type_ref.partition(":")
        if not separator:
            namespace, name = "minecraft", type_ref
        type_file = DATA / namespace / "dimension_type" / f"{name}.json"
        if not type_file.is_file():
            errors.append(f"{path.relative_to(PACK)}: dimension_type {type_ref} does not exist")
            continue

        try:
            dimension_type = json.loads(type_file.read_text(encoding="utf-8"))
        except json.JSONDecodeError:
            continue

        got_min = dimension_type.get("min_y")
        got_height = dimension_type.get("height")
        if (got_min, got_height) != (wanted_min, wanted_height):
            errors.append(
                f"{type_file.relative_to(PACK)}: min_y/height is {got_min}/{got_height}, but "
                f"generator settings {settings} need {wanted_min}/{wanted_height} — Minecraft "
                "will reject the pack at world load"
            )
        logical = dimension_type.get("logical_height")
        if isinstance(logical, int) and isinstance(got_height, int) and logical > got_height:
            errors.append(
                f"{type_file.relative_to(PACK)}: logical_height {logical} exceeds height {got_height}"
            )


BIOME_FIELD_TYPES = {
    "has_precipitation": bool,
    "temperature": (int, float),
    "downfall": (int, float),
    "effects": dict,
    # 1.21.1 uses an object; later 1.21.x accepts a flat list. The pack has encountered both.
    "carvers": (dict, list),
    "features": list,
}


def check_dimension_type_fields() -> None:
    required = {
        "ultrawarm",
        "natural",
        "piglin_safe",
        "respawn_anchor_works",
        "bed_works",
        "has_raids",
        "has_skylight",
        "has_ceiling",
        "coordinate_scale",
        "ambient_light",
        "min_y",
        "height",
        "logical_height",
        "infiniburn",
        "effects",
        "monster_spawn_block_light_limit",
        "monster_spawn_light_level",
    }

    for path in sorted((DATA / NAMESPACE / "dimension_type").glob("*.json")):
        try:
            dimension_type = json.loads(path.read_text(encoding="utf-8"))
        except json.JSONDecodeError:
            continue

        for field in sorted(required - dimension_type.keys()):
            errors.append(f"{path.relative_to(PACK)}: missing required field {field!r}")

        level = dimension_type.get("monster_spawn_light_level")
        if isinstance(level, dict):
            if "value" in level:
                errors.append(
                    f"{path.relative_to(PACK)}: monster_spawn_light_level wraps its bounds in "
                    "'value' — min_inclusive/max_inclusive belong at the top level"
                )
            elif level.get("type", "").endswith("uniform"):
                for key in ("min_inclusive", "max_inclusive"):
                    if key not in level:
                        errors.append(
                            f"{path.relative_to(PACK)}: monster_spawn_light_level missing {key!r}"
                        )

        for field in ("min_y", "height"):
            value = dimension_type.get(field)
            if isinstance(value, int) and value % 16 != 0:
                errors.append(
                    f"{path.relative_to(PACK)}: {field} {value} must be a multiple of 16"
                )

        if dimension_type.get("effects") not in (
            None,
            "minecraft:overworld",
            "minecraft:the_nether",
            "minecraft:the_end",
        ):
            errors.append(
                f"{path.relative_to(PACK)}: effects {dimension_type['effects']!r} is not a valid preset"
            )

        infiniburn = dimension_type.get("infiniburn")
        if isinstance(infiniburn, str) and not infiniburn.startswith("#"):
            errors.append(
                f"{path.relative_to(PACK)}: infiniburn {infiniburn!r} must be a block tag, starting with '#'"
            )


def check_biomes() -> None:
    for path in sorted((DATA / NAMESPACE / "worldgen" / "biome").glob("*.json")):
        try:
            biome = json.loads(path.read_text(encoding="utf-8"))
        except json.JSONDecodeError:
            continue

        for field, wanted_type in BIOME_FIELD_TYPES.items():
            if field not in biome:
                errors.append(f"{path.relative_to(PACK)}: missing required biome field {field!r}")
                continue
            if not isinstance(biome[field], wanted_type):
                names = (
                    wanted_type.__name__
                    if isinstance(wanted_type, type)
                    else "/".join(item.__name__ for item in wanted_type)
                )
                errors.append(
                    f"{path.relative_to(PACK)}: biome field {field!r} is "
                    f"{type(biome[field]).__name__}, expected {names}"
                )

        effects = biome.get("effects")
        if isinstance(effects, dict):
            for key in ("sky_color", "fog_color", "water_color", "water_fog_color"):
                if key not in effects:
                    errors.append(
                        f"{path.relative_to(PACK)}: biome effects missing required {key!r}"
                    )


def check_absent_score_filters() -> None:
    """Ban selector ranges like scores={x=..0} for optional player scores.

    An absent scoreboard entry does not behave like numeric zero. This shape silently killed
    sneak-to-enter and contributed to two earlier regressions, so it is a project policy rather
    than a Minecraft syntax rule.
    """
    pattern = re.compile(r"scores=\{[^}]*?(\w+)\s*=\s*\.\.(-?\d+)")
    for path, lines in mcfunctions():
        for line_number, line in enumerate(lines, 1):
            for match in pattern.finditer(line):
                objective, bound = match.group(1), match.group(2)
                errors.append(
                    f"{path.name}:{line_number}: selector filter scores={{{objective}=..{bound}}} "
                    f"can never match a player whose {objective} is unset — invert the guard "
                    f"and use `unless score @s {objective} matches 1..` at the choke point"
                )


def check_version_agreement() -> None:
    """All three player/operator-facing version literals must agree."""
    meta = PACK / "pack.mcmeta"
    init = DATA / NAMESPACE / "function" / "init.mcfunction"
    selfcheck = DATA / NAMESPACE / "function" / "test" / "selfcheck.mcfunction"
    if not (meta.is_file() and init.is_file() and selfcheck.is_file()):
        # Missing function files are reported by the relevant structural checks; do not duplicate.
        return

    try:
        description = json.loads(meta.read_text(encoding="utf-8")).get("pack", {}).get(
            "description", ""
        )
    except json.JSONDecodeError:
        return

    sources = {
        "pack.mcmeta": description if isinstance(description, str) else "",
        "init.mcfunction": init.read_text(encoding="utf-8"),
        "test/selfcheck.mcfunction": selfcheck.read_text(encoding="utf-8"),
    }
    versions: dict[str, str] = {}
    for label, text in sources.items():
        match = re.search(r"v(\d+\.\d+\.\d+)", text)
        if not match:
            errors.append(f"{label} carries no vX.Y.Z version")
        else:
            versions[label] = match.group(1)

    if len(versions) == len(sources) and len(set(versions.values())) != 1:
        detail = ", ".join(f"{label}=v{version}" for label, version in versions.items())
        errors.append(f"version mismatch: {detail}")


def main() -> int:
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
    check_dimension_height()
    check_dimension_type_fields()
    check_biomes()
    check_absent_score_filters()
    check_version_agreement()

    if errors:
        for error in errors:
            print(f"FAIL: {error}")
        return 1

    print("OK: datapack structure valid")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
