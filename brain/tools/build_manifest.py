#!/usr/bin/env python3
"""Build a deterministic, provenance-stamped machine manifest of the brain.

Emits brain/manifest.json containing all notes, their frontmatter metadata,
titles, summaries, and relation graphs.

    python brain/tools/build_manifest.py
    python brain/tools/build_manifest.py --check
    python brain/tools/build_manifest.py --output custom_manifest.json

Zero third-party dependencies, Python 3.8+.
"""

import argparse
import json
import re
import sys
from datetime import datetime
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from validate_vault import find_vault_root, git_output, parse_frontmatter  # noqa: E402

SCHEMA_VERSION = 1
GENERATOR_VERSION = "1.0.0"

TITLE_RE = re.compile(r"^#\s+(.+)$", re.MULTILINE)
ABSTRACT_RE = re.compile(r"^>\s*\[!abstract\][^\n]*\n((?:>[^\n]*\n?)+)", re.MULTILINE)


def extract_title_and_summary(text):
    """Extract document title and short summary from markdown text."""
    # Find body after frontmatter
    lines = text.splitlines()
    body_lines = []
    in_fm = False
    fm_closed = False
    for line in lines:
        if line.strip() == "---" and not fm_closed:
            if not in_fm:
                in_fm = True
                continue
            else:
                in_fm = False
                fm_closed = True
                continue
        if fm_closed or not in_fm:
            body_lines.append(line)

    body = "\n".join(body_lines).strip()

    title_match = TITLE_RE.search(body)
    title = title_match.group(1).strip() if title_match else None

    # Check for abstract/snapshot block
    abstract_match = ABSTRACT_RE.search(body)
    if abstract_match:
        abstract_lines = [
            l.strip().lstrip(">").strip()
            for l in abstract_match.group(1).splitlines()
            if l.strip().lstrip(">").strip()
        ]
        summary = " ".join(abstract_lines)
    else:
        # Extract first non-heading paragraph
        paragraphs = [p.strip() for p in body.split("\n\n") if p.strip()]
        summary = None
        for p in paragraphs:
            if p.startswith("#") or p.startswith(">") or p.startswith("```"):
                continue
            cleaned = " ".join(p.splitlines()).strip()
            if cleaned:
                summary = cleaned[:250] + ("..." if len(cleaned) > 250 else "")
                break

    return title, summary


def collect_manifest(root):
    """Collect all notes under brain/ and compile sorted manifest data."""
    brain_dir = root / "brain"
    if not brain_dir.is_dir():
        raise SystemExit(f"error: brain directory not found at {brain_dir}")

    notes = {}
    for md_path in sorted(brain_dir.rglob("*.md")):
        rel_path = md_path.relative_to(root).as_posix()
        try:
            content = md_path.read_text(encoding="utf-8")
        except UnicodeDecodeError:
            continue

        fm, err = parse_frontmatter(content)
        if fm is None:
            fm = {}

        uid = fm.get("uid")
        if not uid:
            uid = "ss-untracked-" + md_path.stem

        title, summary = extract_title_and_summary(content)

        note_entry = {
            "uid": uid,
            "path": rel_path,
            "record_kind": fm.get("record_kind", "unknown"),
            "authority": fm.get("authority", "context"),
            "lore_class": fm.get("lore_class", "N/A"),
            "state": fm.get("state", "active"),
            "owner": fm.get("owner", "unassigned"),
            "created": fm.get("created"),
            "updated": fm.get("updated"),
            "title": title,
            "summary": summary,
            "tags": sorted(fm.get("tags", [])) if isinstance(fm.get("tags"), list) else [],
            "sources": fm.get("sources", []) if isinstance(fm.get("sources"), list) else [],
            "related": fm.get("related", []) if isinstance(fm.get("related"), list) else [],
        }

        # Include specific task / implementation metadata if present
        for optional_key in ("task_id", "lease_until", "base_commit", "captured_commit", "supersedes", "implementation_links", "test_links"):
            if optional_key in fm and fm[optional_key]:
                note_entry[optional_key] = fm[optional_key]

        notes[uid] = note_entry

    head_commit = git_output(root, ["rev-parse", "HEAD"])
    is_dirty = bool(git_output(root, ["status", "--porcelain"]))

    manifest = {
        "schema_version": SCHEMA_VERSION,
        "generator_version": GENERATOR_VERSION,
        "generated_at": datetime.utcnow().strftime("%Y-%m-%dT%H:%M:%SZ"),
        "source_commit": head_commit,
        "source_tree_dirty": is_dirty,
        "note_count": len(notes),
        "notes": dict(sorted(notes.items())),
    }
    return manifest


def main(argv=None):
    parser = argparse.ArgumentParser(description="Build or check deterministic brain manifest.")
    parser.add_argument("--root", type=Path, default=None, help="vault root path")
    parser.add_argument("--output", type=Path, default=None, help="output manifest json path")
    parser.add_argument("--check", action="store_true", help="verify existing manifest is up-to-date")
    args = parser.parse_args(argv)

    root = args.root or find_vault_root(Path(__file__).resolve().parent)
    if root is None:
        print("error: could not locate the vault root", file=sys.stderr)
        return 2

    manifest_path = args.output or (root / "brain" / "manifest.json")
    manifest = collect_manifest(root)
    rendered = json.dumps(manifest, indent=2, sort_keys=False) + "\n"

    if args.check:
        if not manifest_path.is_file():
            print(f"error: manifest file {manifest_path} does not exist", file=sys.stderr)
            return 1
        existing = manifest_path.read_text(encoding="utf-8")
        try:
            existing_data = json.loads(existing)
            # Compare note mappings ignoring generated_at timestamp
            m_copy = dict(manifest)
            m_copy.pop("generated_at", None)
            e_copy = dict(existing_data)
            e_copy.pop("generated_at", None)
            if m_copy == e_copy:
                print("Manifest is up-to-date.")
                return 0
            else:
                print("error: manifest content is stale or out of sync", file=sys.stderr)
                return 1
        except json.JSONDecodeError:
            print("error: existing manifest is not valid JSON", file=sys.stderr)
            return 1

    manifest_path.parent.mkdir(parents=True, exist_ok=True)
    manifest_path.write_text(rendered, encoding="utf-8")
    print(f"Generated {manifest_path.relative_to(root).as_posix()} with {manifest['note_count']} notes.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
