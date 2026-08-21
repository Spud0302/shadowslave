#!/usr/bin/env python3
"""Query the Shadow Slave project brain by metadata, state, tags, or search terms.

Fast CLI utility for AI agents and maintainers to discover notes, inspect claims,
and verify state without manual regex parsing.

    python brain/tools/query_vault.py --kind design
    python brain/tools/query_vault.py --tag combat-v1 --state active
    python brain/tools/query_vault.py --active-claims
    python brain/tools/query_vault.py --search "Chainback" --json
    python brain/tools/query_vault.py --lore-class CANON

Zero third-party dependencies, Python 3.8+.
"""

import argparse
import json
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from build_manifest import collect_manifest, find_vault_root  # noqa: E402


def filter_notes(notes, args):
    """Filter notes dictionary based on query arguments."""
    results = []
    for uid, entry in notes.items():
        if args.kind and entry.get("record_kind") != args.kind:
            continue
        if args.authority and entry.get("authority") != args.authority:
            continue
        if args.lore_class and entry.get("lore_class") != args.lore_class:
            continue
        if args.state and entry.get("state") != args.state:
            continue
        if args.owner and entry.get("owner") != args.owner:
            continue
        if args.tag and args.tag not in entry.get("tags", []):
            continue
        if args.active_claims:
            if entry.get("record_kind") != "claim" or entry.get("state") != "active":
                continue
        if args.search:
            q = args.search.lower()
            title = (entry.get("title") or "").lower()
            summary = (entry.get("summary") or "").lower()
            path = entry.get("path", "").lower()
            if q not in uid.lower() and q not in title and q not in summary and q not in path:
                continue

        results.append(entry)
    return results


def format_table(notes, fields):
    """Render a clean text table."""
    if not notes:
        return "No matching notes found."
    lines = []
    # Print headers
    header = "  ".join(f.upper().ljust(20) if f != "title" else f.upper().ljust(35) for f in fields)
    lines.append(header)
    lines.append("-" * len(header))
    for note in notes:
        row = []
        for f in fields:
            val = note.get(f, "")
            if isinstance(val, list):
                val = ",".join(val)
            val_str = str(val or "")
            width = 35 if f == "title" else 20
            if len(val_str) > width:
                val_str = val_str[:width - 3] + "..."
            row.append(val_str.ljust(width))
        lines.append("  ".join(row))
    return "\n".join(lines)


def main(argv=None):
    parser = argparse.ArgumentParser(description="Query the brain vault.")
    parser.add_argument("--kind", help="filter by record_kind (design, lore, claim, etc.)")
    parser.add_argument("--authority", help="filter by authority (project-authority, etc.)")
    parser.add_argument("--lore-class", help="filter by lore_class (CANON, DESIGN, etc.)")
    parser.add_argument("--state", help="filter by state (active, proposed, accepted, closed, etc.)")
    parser.add_argument("--owner", help="filter by owner (Andrew, claude, codex, antigravity)")
    parser.add_argument("--tag", help="filter by tag")
    parser.add_argument("--search", help="substring search across uid, title, summary, and path")
    parser.add_argument("--active-claims", action="store_true", help="show all active task claims")
    parser.add_argument("--json", action="store_true", help="output JSON array of matching notes")
    parser.add_argument("--fields", default="uid,record_kind,state,title",
                        help="comma-separated fields for table output (default: uid,record_kind,state,title)")
    parser.add_argument("--root", type=Path, default=None, help="vault root path")
    args = parser.parse_args(argv)

    root = args.root or find_vault_root(Path(__file__).resolve().parent)
    if root is None:
        print("error: could not locate vault root", file=sys.stderr)
        return 2

    # Load from manifest if present, otherwise collect live
    manifest_file = root / "brain" / "manifest.json"
    if manifest_file.is_file():
        try:
            data = json.loads(manifest_file.read_text(encoding="utf-8"))
            notes = data.get("notes", {})
        except json.JSONDecodeError:
            notes = collect_manifest(root).get("notes", {})
    else:
        notes = collect_manifest(root).get("notes", {})

    matched = filter_notes(notes, args)

    if args.json:
        print(json.dumps(matched, indent=2))
        return 0

    fields = [f.strip() for f in args.fields.split(",") if f.strip()]
    print(f"Matched {len(matched)} note(s):\n")
    print(format_table(matched, fields))
    return 0


if __name__ == "__main__":
    sys.exit(main())
