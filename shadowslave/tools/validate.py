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
  - every dimension_type covers the vertical range its noise settings generate
  - every dimension_type has its required fields, with the shapes 1.21.1 parses
  - every biome has the required fields, with the types 1.21.1 expects
  - pack.mcmeta and the load message declare the same version
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


# Vertical range each vanilla noise settings preset generates across.
# A dimension_type must cover its generator's range or the pack is rejected at WORLD LOAD —
# not at /reload — with "Data pack validation failed!", which is a much worse failure than
# anything the reference checks above catch.
NOISE_RANGES = {
    "minecraft:overworld": (-64, 384),
    "minecraft:large_biomes": (-64, 384),
    "minecraft:amplified": (-64, 384),
    "minecraft:nether": (0, 128),
    "minecraft:end": (0, 128),
    "minecraft:caves": (-64, 192),
    "minecraft:floating_islands": (0, 256),
}


def check_dimension_height():
    """A dimension_type must cover the vertical range its noise settings generate."""
    for f in sorted((DATA / NAMESPACE / "dimension").glob("*.json")):
        try:
            dim = json.loads(f.read_text())
        except json.JSONDecodeError:
            continue
        settings = dim.get("generator", {}).get("settings")
        if settings not in NOISE_RANGES:
            continue  # custom or unknown noise settings — nothing to compare against
        want_min, want_height = NOISE_RANGES[settings]

        type_ref = dim.get("type", "")
        namespace, _, name = type_ref.partition(":")
        type_file = DATA / namespace / "dimension_type" / f"{name}.json"
        if not type_file.is_file():
            errors.append(f"{f.relative_to(PACK)}: dimension_type {type_ref} does not exist")
            continue
        try:
            dtype = json.loads(type_file.read_text())
        except json.JSONDecodeError:
            continue

        got_min, got_height = dtype.get("min_y"), dtype.get("height")
        if (got_min, got_height) != (want_min, want_height):
            errors.append(
                f"{type_file.relative_to(PACK)}: min_y/height is {got_min}/{got_height}, but "
                f"generator settings {settings} need {want_min}/{want_height} — Minecraft will "
                f"reject the pack at world load, not at /reload"
            )
        logical = dtype.get("logical_height")
        if isinstance(logical, int) and isinstance(got_height, int) and logical > got_height:
            errors.append(
                f"{type_file.relative_to(PACK)}: logical_height {logical} exceeds height {got_height}"
            )


# Biome fields that must be present and must be of a specific JSON type. Getting one of
# these wrong fails the world at CREATION time with "Data pack validation failed!", which
# gives you a dialog box and no error text — the reason is only in logs/latest.log.
BIOME_FIELD_TYPES = {
    "has_precipitation": bool,
    "temperature": (int, float),
    "downfall": (int, float),
    "effects": dict,
    # `carvers` is an {air: […]} object in 1.21.1 and a flat list in later 1.21.x, so accept
    # either — pinning it to one wrongly broke a working biome once already.
    "carvers": (dict, list),
    # A list of generation steps, each itself a list. May be empty, must not be an object.
    "features": list,
}


def check_dimension_type_fields():
    """Field shapes in dimension_type that Minecraft parses strictly.

    A parse failure here rejects the world at creation with a dialog and no error
    text — the reason only appears in logs/latest.log.
    """
    required = {
        "ultrawarm", "natural", "piglin_safe", "respawn_anchor_works", "bed_works",
        "has_raids", "has_skylight", "has_ceiling", "coordinate_scale", "ambient_light",
        "min_y", "height", "logical_height", "infiniburn", "effects",
        "monster_spawn_block_light_limit", "monster_spawn_light_level",
    }
    for f in sorted((DATA / NAMESPACE / "dimension_type").glob("*.json")):
        try:
            dt = json.loads(f.read_text())
        except json.JSONDecodeError:
            continue
        for field in sorted(required - dt.keys()):
            errors.append(f"{f.relative_to(PACK)}: missing required field {field!r}")

        # Int providers put min_inclusive/max_inclusive at the TOP level of the object.
        # Wrapping them in a "value" key is a parse failure, not a silent no-op.
        level = dt.get("monster_spawn_light_level")
        if isinstance(level, dict):
            if "value" in level:
                errors.append(
                    f"{f.relative_to(PACK)}: monster_spawn_light_level wraps its bounds in "
                    f"'value' — min_inclusive/max_inclusive belong at the top level of the object"
                )
            elif level.get("type", "").endswith("uniform"):
                for key in ("min_inclusive", "max_inclusive"):
                    if key not in level:
                        errors.append(f"{f.relative_to(PACK)}: monster_spawn_light_level missing {key!r}")

        for field in ("min_y", "height"):
            val = dt.get(field)
            if isinstance(val, int) and val % 16 != 0:
                errors.append(f"{f.relative_to(PACK)}: {field} {val} must be a multiple of 16")
        if dt.get("effects") not in (None, "minecraft:overworld", "minecraft:the_nether", "minecraft:the_end"):
            errors.append(f"{f.relative_to(PACK)}: effects {dt['effects']!r} is not a valid preset")
        burn = dt.get("infiniburn")
        if isinstance(burn, str) and not burn.startswith("#"):
            errors.append(f"{f.relative_to(PACK)}: infiniburn {burn!r} must be a block tag, starting with '#'")


def check_biomes():
    """Required biome fields, with the types 1.21.1 actually expects."""
    for f in sorted((DATA / NAMESPACE / "worldgen" / "biome").glob("*.json")):
        try:
            biome = json.loads(f.read_text())
        except json.JSONDecodeError:
            continue
        for field, want in BIOME_FIELD_TYPES.items():
            if field not in biome:
                errors.append(f"{f.relative_to(PACK)}: missing required biome field {field!r}")
                continue
            if not isinstance(biome[field], want):
                names = want.__name__ if isinstance(want, type) else "/".join(t.__name__ for t in want)
                errors.append(
                    f"{f.relative_to(PACK)}: biome field {field!r} is "
                    f"{type(biome[field]).__name__}, expected {names}"
                )
        effects = biome.get("effects")
        if isinstance(effects, dict):
            for key in ("sky_color", "fog_color", "water_color", "water_fog_color"):
                if key not in effects:
                    errors.append(f"{f.relative_to(PACK)}: biome effects missing required {key!r}")



def check_version_agreement():
    """pack.mcmeta and the load message must declare the same version.

    Datapacks have no variables, so the version is a hand-maintained literal in two
    places. A version string that lies is worse than not having one — you end up
    testing a build you think is something else.
    """
    meta = PACK / "pack.mcmeta"
    init = DATA / NAMESPACE / "function" / "init.mcfunction"
    if not (meta.is_file() and init.is_file()):
        return
    try:
        desc = json.loads(meta.read_text()).get("pack", {}).get("description", "")
    except json.JSONDecodeError:
        return
    in_meta = re.search(r"v(\d+\.\d+\.\d+)", desc if isinstance(desc, str) else "")
    in_init = re.search(r"v(\d+\.\d+\.\d+)", init.read_text())
    if not in_meta:
        errors.append("pack.mcmeta description carries no vX.Y.Z version")
        return
    if not in_init:
        errors.append("init.mcfunction announces no vX.Y.Z version")
        return
    if in_meta.group(1) != in_init.group(1):
        errors.append(
            f"version mismatch: pack.mcmeta says v{in_meta.group(1)}, "
            f"init.mcfunction says v{in_init.group(1)}"
        )


def check_absent_score_filters():
    """Reject selector score filters whose lower bound is open, e.g. scores={x=..0}.

    An absent score fails `matches` outright — nothing in this pack ever writes 0, so a score
    that has not been set has no entry at all and `..0` excludes the player entirely rather
    than matching "zero or unset". A filter like `scores={ss_cooldown=..0}` therefore matches
    NOBODY on a fresh player, which is exactly how sneak-to-enter died in v1.4.0 and stayed
    dead through five releases: every player who had never been ejected was filtered out of
    the entry path.

    This is the third bug of this shape in the project (see also the /trigger soul lockout and
    the v1.2.1 ejection guard), so it is worth catching statically rather than in play. The
    correct form is to invert: guard with `unless ... matches 1..` at the choke point.

    Ranges with an explicit lower bound (`0..`, `1..8`, `..8` on a score the pack always sets)
    are fine — only an open-ended upper-bound-only range that would have to match an absent
    score is rejected.

    POLICY, NOT SYNTAX. `scores={x=..0}` is valid Minecraft and is meaningful when the objective
    is guaranteed to have a value for every candidate. Nothing in this pack guarantees that, and
    the shape has now cost three bugs, so it is banned here as a project rule. If a legitimate
    use ever appears, add a narrow documented allowlist rather than deleting this check — the
    whole point is that the mistake is invisible in play.
    """
    pat = re.compile(r"scores=\{[^}]*?(\w+)\s*=\s*\.\.(-?\d+)")
    for path, lines in mcfunctions():
        # mcfunctions() has already dropped comments and blank lines.
        for n, line in enumerate(lines, 1):
            for m in pat.finditer(line):
                obj, bound = m.group(1), m.group(2)
                errors.append(
                    f"{path.name}:{n}: selector filter scores={{{obj}=..{bound}}} can never match a "
                    f"player whose {obj} is unset — an absent score fails `matches`. Invert it: "
                    f"filter on the tag alone and guard with `unless score @s {obj} matches 1..`"
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
    check_dimension_height()
    check_dimension_type_fields()
    check_biomes()
    check_absent_score_filters()
    check_version_agreement()
    if errors:
        for e in errors:
            print(f"FAIL: {e}")
        return 1
    print("OK: datapack structure valid")
    return 0


if __name__ == "__main__":
    sys.exit(main())
