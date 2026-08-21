#!/usr/bin/env python3
"""Scaffold a correctly named and stamped vault record from a template.

Templates carry Obsidian placeholders ({{date}}, {{title}}) that only expand
when a note is created through the Templates plugin. An agent writing the file
directly gets the literal text, which lands an invalid date in `created:`.
The templates also predate the uid conventions the vault actually settled on:
concept notes use `ss-<kind>-<slug>` while agent records use
`<stamp>-<agent>-<slug>`.

This tool derives uid, filename, dates, and git context itself rather than
trusting the template, so it produces conforming records against the templates
exactly as they stand today.

    python brain/tools/new_record.py claim --agent claude --slug combat-tuning
    python brain/tools/new_record.py handoff --agent claude --slug combat-tuning \\
        --task-id 20260821T063734Z-claude-combat-tuning
    python brain/tools/new_record.py decision --agent claude --slug lane-geometry
    python brain/tools/new_record.py lore --agent claude --slug drowned-listener

Always run brain/tools/validate_vault.py afterwards. This tool gets the
mechanical fields right; only a writer can get the content right.
"""

import argparse
import re
import sys
from datetime import datetime, timedelta
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from validate_vault import find_vault_root, git_output  # noqa: E402

# kind -> (template file, destination folder, filename style)
KINDS = {
    "claim":    ("task-claim.md",        "brain/ai/claims",   "agent"),
    "log":      ("run-log.md",           "brain/ai/logs",     "agent"),
    "handoff":  ("handoff.md",           "brain/ai/handoffs", "agent"),
    "evidence": ("evidence.md",          "brain/evidence",    "agent"),
    "decision": ("decision.md",          "brain/decisions",   "adr"),
    "idea":     ("idea.md",              "brain/inbox",       "concept"),
    "lore":     ("lore-entity.md",       "brain/lore",        "concept"),
    "feature":  ("feature.md",           "brain/design",      "concept"),
    "context":  ("ai-context-packet.md", "brain/ai/context",  "concept"),
}

SLUG = re.compile(r"^[a-z0-9][a-z0-9-]*$")
DEFAULT_LEASE_HOURS = 8


def set_key(lines, key, value):
    """Set a frontmatter key in place, or insert it before the closing fence."""
    pattern = re.compile(r"^%s\s*:" % re.escape(key))
    for i, line in enumerate(lines):
        if line.strip() == "---" and i > 0:
            break
        if pattern.match(line):
            lines[i] = "%s: %s" % (key, value)
            return
    for i in range(1, len(lines)):
        if lines[i].strip() == "---":
            lines.insert(i, "%s: %s" % (key, value))
            return


def read_frontmatter_value(lines, key):
    pattern = re.compile(r"^%s\s*:\s*(.*)$" % re.escape(key))
    for line in lines[1:]:
        if line.strip() == "---":
            break
        match = pattern.match(line)
        if match:
            return match.group(1).strip().strip('"').strip("'")
    return None


def build(kind, slug, agent, args, root, now):
    template_name, folder, style = KINDS[kind]
    template = root / "brain" / "templates" / template_name
    if not template.is_file():
        raise SystemExit("error: template not found: %s" % template)

    stamp = now.strftime("%Y%m%dT%H%M%SZ")
    day = now.strftime("%Y-%m-%d")
    compact_day = now.strftime("%Y%m%d")
    title = args.title or slug.replace("-", " ").capitalize()

    text = template.read_text(encoding="utf-8")
    record_kind = read_frontmatter_value(text.splitlines(), "record_kind") or kind

    if style == "agent":
        filename = "%s--%s--%s.md" % (stamp, agent, slug)
        uid = "%s-%s-%s" % (stamp, agent, slug)
    elif style == "adr":
        filename = "ADR-%s-%s.md" % (compact_day, slug)
        uid = "ss-adr-%s-%s" % (compact_day, slug)
    else:
        filename = "%s.md" % slug
        uid = "ss-%s-%s" % (record_kind, slug)

    task_id = args.task_id or (uid if kind == "claim" else None)

    # Longest tokens first so replace-unique-task-id is not clipped by
    # replace-task-id, and so on.
    tokens = {
        "replace-compact-date": compact_day,
        "replace-iso-date": day,
        "replace-iso-time": now.strftime("%H:%M"),
        "replace-title": title,
        "replace-stamp": stamp,
        "replace-slug": slug,
        # Retained so the tool still works against a template that has not been
        # migrated off the Obsidian placeholders.
        "{{date}}": day,
        "{{time}}": now.strftime("%H:%M"),
        "{{title}}": title,
        "replace-unique-task-id": task_id or "replace-task-id",
        "replace-iso-timestamp": (now + timedelta(hours=args.lease_hours)).strftime(
            "%Y-%m-%dT%H:%M:%SZ"),
        "replace-maintainer": agent,
        "replace-proposer": agent,
        "replace-worktree": root.as_posix(),
        "replace-full-sha": git_output(root, ["rev-parse", "HEAD"]) or "replace-full-sha",
        "replace-task-id": task_id or "replace-task-id",
        "replace-branch": git_output(root, ["rev-parse", "--abbrev-ref", "HEAD"])
                          or "replace-branch",
        "replace-agent": agent,
        "replace-owner": agent,
        "replace-tool": args.tool,
    }
    for token in sorted(tokens, key=len, reverse=True):
        text = text.replace(token, tokens[token])

    # Without git context a claim would carry base_commit='replace-full-sha',
    # which names no commit. validate_vault.py rejects that, so say so here
    # rather than letting the writer discover it later.
    if tokens["replace-full-sha"] == "replace-full-sha":
        print("warning: no git context available; fill branch and base_commit by hand",
              file=sys.stderr)

    lines = text.splitlines()
    set_key(lines, "uid", uid)
    set_key(lines, "created", day)
    set_key(lines, "updated", day)
    if task_id:
        set_key(lines, "task_id", task_id)
    # The immutability model depends on supersede chains, so make the field
    # present and obvious rather than something a writer must remember to add.
    if kind in ("handoff", "evidence") and read_frontmatter_value(lines, "supersedes") is None:
        set_key(lines, "supersedes", "[]")

    return root / folder / filename, "\n".join(lines) + "\n"


def main(argv=None):
    parser = argparse.ArgumentParser(description="Scaffold a vault record from a template.")
    parser.add_argument("kind", choices=sorted(KINDS))
    parser.add_argument("--slug", required=True, help="lowercase-hyphen short name")
    parser.add_argument("--agent", required=True, help="agent slug from brain/ai/agents/")
    parser.add_argument("--tool", default="unspecified")
    parser.add_argument("--title", default=None)
    parser.add_argument("--task-id", default=None,
                        help="required for records belonging to an existing claim")
    parser.add_argument("--lease-hours", type=int, default=DEFAULT_LEASE_HOURS)
    parser.add_argument("--dry-run", action="store_true", help="print instead of writing")
    parser.add_argument("--root", type=Path, default=None)
    args = parser.parse_args(argv)

    for value, label in ((args.slug, "slug"), (args.agent, "agent")):
        if not SLUG.match(value):
            print("error: %s '%s' must be lowercase letters, digits, and hyphens"
                  % (label, value), file=sys.stderr)
            return 2

    root = args.root or find_vault_root(Path(__file__).resolve().parent)
    if root is None:
        print("error: could not locate the vault root", file=sys.stderr)
        return 2

    if args.kind in ("handoff", "evidence", "log") and not args.task_id:
        print("error: %s records must reference the claim they belong to (--task-id)"
              % args.kind, file=sys.stderr)
        return 2

    path, content = build(args.kind, args.slug, args.agent, args, root, datetime.utcnow())

    if args.dry_run:
        print("# would write %s\n" % path.relative_to(root).as_posix())
        print(content)
        return 0

    if path.exists():
        print("error: %s already exists; records are not overwritten"
              % path.relative_to(root).as_posix(), file=sys.stderr)
        return 1

    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content, encoding="utf-8")
    print(path.relative_to(root).as_posix())
    print("\nNext: fill in the content, then run "
          "python brain/tools/validate_vault.py", file=sys.stderr)
    return 0


if __name__ == "__main__":
    sys.exit(main())
