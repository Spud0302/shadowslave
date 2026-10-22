#!/usr/bin/env python3
"""Fail closed when a combat action's phase timing disagrees with its animation.

Combat Core owns action timing in ticks: wind-up, active window, recovery.
GeckoLib owns animation length in seconds. Nothing connects the two, so an
animation can finish before the hit it is supposed to telegraph without any
build, test or lint noticing. That desync is invisible in source review and
shows up only as combat that does not read.

This tool reads the tick counts out of `new CombatActionDefinition(...)` in the
Java sources, reads clip lengths out of the GeckoLib `.animation.json` files,
and compares them through an explicit binding file. It cannot guess which clip
telegraphs which action, so a binding must be declared; an action that is
deliberately not animated is declared unbound with a reason instead.

Exit codes: 0 agreement, 1 mismatch, 2 the tool could not run.
"""

import argparse
import json
import re
import sys
from pathlib import Path

TICKS_PER_SECOND = 20
PHASES = ("windup", "active", "recovery")

# Source roots scanned for action definitions. Test sources are excluded: their
# definitions are fixtures for Combat Core's own unit tests, not shipped actions.
SOURCE_ROOTS = (
    "mod/src/main/java",
    "combat-core/src/main/java",
)

CONSTRUCTOR = "new CombatActionDefinition("
STRING_LITERAL = re.compile(r'^"((?:[^"\\]|\\.)*)"$')
INT_LITERAL = re.compile(r"^-?\d+$")
QUALIFIED_CONSTANT = re.compile(r"^(\w+)\.(\w+)$")
BARE_CONSTANT = re.compile(r"^(\w+)$")


class ToolError(Exception):
    """The tool cannot produce a verdict. Distinct from a timing mismatch."""


def find_repo_root(start):
    current = start.resolve()
    for candidate in (current, *current.parents):
        if (candidate / ".git").exists():
            return candidate
    return None


def split_arguments(text):
    """Split a constructor argument list on top-level commas.

    Nested calls and commas inside string literals must not split the list.
    """
    args = []
    depth = 0
    in_string = False
    escaped = False
    current = []

    for char in text:
        if in_string:
            current.append(char)
            if escaped:
                escaped = False
            elif char == "\\":
                escaped = True
            elif char == '"':
                in_string = False
            continue

        if char == '"':
            in_string = True
            current.append(char)
        elif char in "([":
            depth += 1
            current.append(char)
        elif char in ")]":
            depth -= 1
            current.append(char)
        elif char == "," and depth == 0:
            args.append("".join(current).strip())
            current = []
        else:
            current.append(char)

    tail = "".join(current).strip()
    if tail:
        args.append(tail)
    return args


def read_balanced(text, open_index):
    """Return the text between the parenthesis at open_index and its partner."""
    depth = 0
    in_string = False
    escaped = False

    for index in range(open_index, len(text)):
        char = text[index]
        if in_string:
            if escaped:
                escaped = False
            elif char == "\\":
                escaped = True
            elif char == '"':
                in_string = False
            continue
        if char == '"':
            in_string = True
        elif char == "(":
            depth += 1
        elif char == ")":
            depth -= 1
            if depth == 0:
                return text[open_index + 1:index]
    raise ToolError("unbalanced parentheses in a CombatActionDefinition call")


def strip_comments(text):
    """Remove block and line comments so commented-out calls are not parsed."""
    out = []
    index = 0
    length = len(text)
    while index < length:
        char = text[index]
        if char == '"':
            out.append(char)
            index += 1
            while index < length:
                out.append(text[index])
                if text[index] == "\\":
                    index += 1
                    if index < length:
                        out.append(text[index])
                elif text[index] == '"':
                    index += 1
                    break
                index += 1
            continue
        if text.startswith("//", index):
            while index < length and text[index] != "\n":
                index += 1
            continue
        if text.startswith("/*", index):
            end = text.find("*/", index + 2)
            index = length if end == -1 else end + 2
            continue
        out.append(char)
        index += 1
    return "".join(out)


def collect_int_constants(java_files):
    """Map ClassName.CONSTANT -> int for `static final int CONSTANT = N;`."""
    pattern = re.compile(
        r"static\s+final\s+int\s+(\w+)\s*=\s*(-?\d+)\s*;"
    )
    constants = {}
    for path in java_files:
        class_name = path.stem
        for name, value in pattern.findall(strip_comments(path.read_text(encoding="utf-8"))):
            constants[f"{class_name}.{name}"] = int(value)
    return constants


def resolve_int(expression, class_name, constants, context):
    if INT_LITERAL.match(expression):
        return int(expression)

    qualified = QUALIFIED_CONSTANT.match(expression)
    if qualified:
        key = f"{qualified.group(1)}.{qualified.group(2)}"
        if key in constants:
            return constants[key]
        raise ToolError(
            f"{context}: cannot resolve '{expression}'. "
            f"Expected `static final int {qualified.group(2)}` in "
            f"{qualified.group(1)}.java."
        )

    bare = BARE_CONSTANT.match(expression)
    if bare:
        key = f"{class_name}.{bare.group(1)}"
        if key in constants:
            return constants[key]
        raise ToolError(
            f"{context}: cannot resolve '{expression}' in {class_name}.java."
        )

    raise ToolError(
        f"{context}: '{expression}' is not an int literal or a named int constant. "
        "Computed tick counts cannot be checked statically."
    )


def parse_actions(root):
    """Return {action_id: {windup, active, recovery, source}} from the sources."""
    java_files = []
    for relative in SOURCE_ROOTS:
        source_root = root / relative
        if source_root.is_dir():
            java_files.extend(sorted(source_root.rglob("*.java")))

    constants = collect_int_constants(java_files)
    actions = {}

    for path in java_files:
        text = strip_comments(path.read_text(encoding="utf-8"))
        search_from = 0
        while True:
            found = text.find(CONSTRUCTOR, search_from)
            if found == -1:
                break
            open_index = found + len(CONSTRUCTOR) - 1
            inner = read_balanced(text, open_index)
            search_from = found + len(CONSTRUCTOR)

            relative_path = path.relative_to(root).as_posix()
            args = split_arguments(inner)
            if len(args) != 5:
                raise ToolError(
                    f"{relative_path}: CombatActionDefinition takes 5 arguments, "
                    f"found {len(args)}."
                )

            literal = STRING_LITERAL.match(args[0])
            if not literal:
                raise ToolError(
                    f"{relative_path}: action id '{args[0]}' is not a string literal, "
                    "so it cannot be matched to a binding."
                )
            action_id = literal.group(1)

            context = f"{relative_path} ({action_id})"
            resolved = {
                phase: resolve_int(args[index], path.stem, constants, context)
                for index, phase in enumerate(PHASES, start=1)
            }

            if action_id in actions:
                raise ToolError(
                    f"action id '{action_id}' is defined twice: "
                    f"{actions[action_id]['source']} and {relative_path}."
                )

            resolved["source"] = relative_path
            actions[action_id] = resolved

    return actions


def clip_length_seconds(clip):
    """Length of one bedrock animation clip, in seconds.

    `animation_length` is authoritative when present. GeckoLib otherwise
    derives length from the last keyframe, so fall back to that.
    """
    declared = clip.get("animation_length")
    if isinstance(declared, (int, float)):
        return float(declared)

    latest = 0.0
    for channels in clip.get("bones", {}).values():
        if not isinstance(channels, dict):
            continue
        for channel in channels.values():
            if isinstance(channel, dict):
                for stamp in channel:
                    try:
                        latest = max(latest, float(stamp))
                    except (TypeError, ValueError):
                        continue
    return latest


def load_animation_clips(path):
    try:
        data = json.loads(path.read_text(encoding="utf-8"))
    except json.JSONDecodeError as error:
        raise ToolError(f"{path}: not valid JSON ({error}).")

    clips = data.get("animations")
    if not isinstance(clips, dict):
        raise ToolError(f"{path}: no 'animations' object.")
    return clips


def to_ticks(seconds):
    return seconds * TICKS_PER_SECOND


def check_bindings(root, config, actions):
    """Compare each declared binding. Returns (findings, checked, unbound)."""
    findings = []
    tolerance = config.get("tolerance_ticks", 1)
    bindings = config.get("bindings", [])
    unbound = config.get("unbound", [])

    declared = {b.get("action_id") for b in bindings} | {
        u.get("action_id") for u in unbound
    }
    for action_id in sorted(set(actions) - declared):
        findings.append({
            "code": "ACTION_UNDECLARED",
            "action_id": action_id,
            "source": actions[action_id]["source"],
            "message": (
                f"action '{action_id}' has no binding and is not declared unbound. "
                "Add it to 'bindings' or to 'unbound' with a reason."
            ),
        })

    for entry in unbound:
        action_id = entry.get("action_id")
        if action_id not in actions:
            findings.append({
                "code": "UNKNOWN_ACTION",
                "action_id": action_id,
                "source": "(binding file)",
                "message": (
                    f"'unbound' names '{action_id}', which no source defines. "
                    "Remove the stale entry."
                ),
            })
        elif not str(entry.get("reason", "")).strip():
            findings.append({
                "code": "UNBOUND_WITHOUT_REASON",
                "action_id": action_id,
                "source": "(binding file)",
                "message": f"'{action_id}' is declared unbound with no reason.",
            })

    checked = 0
    for entry in bindings:
        action_id = entry.get("action_id")
        action = actions.get(action_id)
        if action is None:
            findings.append({
                "code": "UNKNOWN_ACTION",
                "action_id": action_id,
                "source": "(binding file)",
                "message": (
                    f"binding names '{action_id}', which no source defines. "
                    "Remove the stale binding."
                ),
            })
            continue

        animation_path = root / entry.get("animation_file", "")
        if not animation_path.is_file():
            findings.append({
                "code": "ANIMATION_FILE_MISSING",
                "action_id": action_id,
                "source": entry.get("animation_file", "(unset)"),
                "message": f"animation file not found: {entry.get('animation_file')}",
            })
            continue

        clips = load_animation_clips(animation_path)

        covered = {}
        total_seconds = 0.0
        clip_error = False

        for clip_entry in entry.get("clips", []):
            name = clip_entry.get("animation")
            if name not in clips:
                findings.append({
                    "code": "CLIP_MISSING",
                    "action_id": action_id,
                    "source": entry.get("animation_file"),
                    "message": (
                        f"clip '{name}' is not in {entry.get('animation_file')}. "
                        f"Available: {', '.join(sorted(clips)) or '(none)'}."
                    ),
                })
                clip_error = True
                continue

            phases = clip_entry.get("covers", [])
            if not phases:
                findings.append({
                    "code": "CLIP_COVERS_NOTHING",
                    "action_id": action_id,
                    "source": entry.get("animation_file"),
                    "message": f"clip '{name}' declares no phases in 'covers'.",
                })
                clip_error = True
                continue

            for phase in phases:
                if phase not in PHASES:
                    findings.append({
                        "code": "UNKNOWN_PHASE",
                        "action_id": action_id,
                        "source": entry.get("animation_file"),
                        "message": (
                            f"clip '{name}' covers unknown phase '{phase}'. "
                            f"Valid phases: {', '.join(PHASES)}."
                        ),
                    })
                    clip_error = True
                elif phase in covered:
                    findings.append({
                        "code": "PHASE_COVERED_TWICE",
                        "action_id": action_id,
                        "source": entry.get("animation_file"),
                        "message": (
                            f"phase '{phase}' is covered by both '{covered[phase]}' "
                            f"and '{name}'."
                        ),
                    })
                    clip_error = True
                else:
                    covered[phase] = name

            total_seconds += clip_length_seconds(clips[name])

        if clip_error:
            continue

        missing = [phase for phase in PHASES if phase not in covered]
        if missing:
            findings.append({
                "code": "PHASE_UNCOVERED",
                "action_id": action_id,
                "source": entry.get("animation_file"),
                "message": (
                    f"no clip covers {', '.join(missing)}. Every phase must be "
                    "covered exactly once, or the action declared unbound."
                ),
            })
            continue

        expected_ticks = sum(action[phase] for phase in PHASES)
        actual_ticks = to_ticks(total_seconds)
        drift = actual_ticks - expected_ticks
        checked += 1

        if abs(drift) > tolerance:
            clip_names = ", ".join(
                f"'{c.get('animation')}'" for c in entry.get("clips", [])
            )
            findings.append({
                "code": "TIMING_MISMATCH",
                "action_id": action_id,
                "source": action["source"],
                "message": (
                    f"action is {expected_ticks} ticks "
                    f"({action['windup']} wind-up + {action['active']} active + "
                    f"{action['recovery']} recovery = "
                    f"{expected_ticks / TICKS_PER_SECOND:.2f}s) but {clip_names} "
                    f"runs {actual_ticks:g} ticks ({total_seconds:.2f}s). "
                    f"Off by {drift:+g} ticks."
                ),
            })

    return findings, checked, len(unbound)


def main(argv=None):
    parser = argparse.ArgumentParser(
        description="Fail closed when combat action timing and animation length disagree."
    )
    parser.add_argument("--root", help="repository root")
    parser.add_argument("--bindings", help="path to the binding file")
    parser.add_argument(
        "--strict",
        action="store_true",
        help="also fail on actions declared unbound",
    )
    parser.add_argument("--format", choices=["text", "json"], default="text")
    args = parser.parse_args(argv)

    root = Path(args.root) if args.root else find_repo_root(Path.cwd())
    if root is None or not root.is_dir():
        print("error: could not locate the repository root", file=sys.stderr)
        return 2

    bindings_path = (
        Path(args.bindings) if args.bindings else root / "mod" / "tools" / "animation-timing.json"
    )
    if not bindings_path.is_file():
        print(f"error: binding file not found: {bindings_path}", file=sys.stderr)
        return 2

    try:
        config = json.loads(bindings_path.read_text(encoding="utf-8"))
        actions = parse_actions(root)
        findings, checked, unbound = check_bindings(root, config, actions)
    except ToolError as error:
        print(f"error: {error}", file=sys.stderr)
        return 2
    except json.JSONDecodeError as error:
        print(f"error: {bindings_path} is not valid JSON ({error})", file=sys.stderr)
        return 2

    if args.format == "json":
        print(json.dumps(
            {
                "actions_found": len(actions),
                "bindings_checked": checked,
                "unbound": unbound,
                "findings": findings,
            },
            indent=2,
        ))
    else:
        for finding in findings:
            print(f"MISMATCH {finding['code']}  {finding['source']}")
            print(f"         {finding['message']}")
        print(
            f"{len(actions)} action(s) found, {checked} binding(s) checked, "
            f"{unbound} declared unbound, {len(findings)} problem(s)."
        )

    if findings:
        return 1
    if args.strict and unbound:
        print("error: --strict and actions are declared unbound", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
