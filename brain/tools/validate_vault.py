#!/usr/bin/env python3
"""Validate the Shadow Slave project brain against brain/protocol/note-schema.md.

This is agent-invoked local enforcement. PROJECT-STATUS.md records that hosted
CI runners are currently unavailable, so a CI-only gate could sit unexecuted;
every agent runs this directly instead, before writing and before handoff.

    python brain/tools/validate_vault.py
    python brain/tools/validate_vault.py --format json
    python brain/tools/validate_vault.py --changed-only

Exit codes: 0 clean (warnings allowed), 1 at least one error, 2 bad invocation.

Zero third-party dependencies on purpose. Any agent with a bare Python 3.8+
must be able to run it without a package install step.
"""

import argparse
import json
import re
import subprocess
import sys
from datetime import datetime
from pathlib import Path

# --- schema, mirrored from brain/protocol/note-schema.md -------------------

RECORD_KINDS = {
    "index", "protocol", "lore", "design", "implementation", "decision",
    "evidence", "idea", "context", "claim", "handoff", "log", "agent-profile",
}
AUTHORITIES = {"source-canon", "project-authority", "proposal", "evidence", "context"}
LORE_CLASSES = {"CANON", "INFERRED", "DESIGN", "UNKNOWN", "COMPATIBILITY", "mixed", "N/A"}
STATES = {
    "draft", "proposed", "accepted", "active", "blocked", "closed",
    "rejected", "superseded", "archived",
}
REQUIRED_KEYS = ("uid", "record_kind", "authority", "lore_class", "state",
                 "owner", "created", "updated")

# Agent records are timestamped and one-writer; concept notes are stable names.
# Claims, logs, and handoffs are inherently per-task and always timestamped.
# Evidence is split: a task-scoped observation carries task_id and is immutable,
# while a standing register like brain/implementation/authority-drift-register.md
# is a living topic note with a stable name. task_id is what tells them apart.
AGENT_RECORD_KINDS = {"claim", "handoff", "log"}
AGENT_FILENAME = re.compile(r"^\d{8}T\d{6}Z--[a-z0-9][a-z0-9-]*--[a-z0-9][a-z0-9-]*\.md$")
ADR_FILENAME = re.compile(r"^ADR-\d{8}-[a-z0-9][a-z0-9-]*\.md$")
CONCEPT_FILENAME = re.compile(r"^[a-z0-9][a-z0-9.-]*\.md$")

# Where each record kind is expected to live, relative to the vault root.
REQUIRED_HOME = {"claim": "brain/ai/claims", "handoff": "brain/ai/handoffs", "log": "brain/ai/logs"}
# Advisory, not required: a standing register may legitimately sit beside the
# topic notes it concerns rather than in the shared evidence store.
ADVISORY_HOME = {"decision": "brain/decisions"}

WIKILINK = re.compile(r"\[\[([^\]|#]+)(?:#[^\]|]*)?(?:\|[^\]]*)?\]\]")
ISO_DATE = "%Y-%m-%d"
ISO_STAMP = "%Y-%m-%dT%H:%M:%SZ"


class Finding:
    __slots__ = ("severity", "code", "path", "message")

    def __init__(self, severity, code, path, message):
        self.severity = severity
        self.code = code
        self.path = path
        self.message = message

    def as_dict(self):
        return {"severity": self.severity, "code": self.code,
                "path": self.path, "message": self.message}


# --- minimal frontmatter parsing -------------------------------------------

def _scalar(raw):
    raw = raw.strip()
    if len(raw) >= 2 and raw[0] == raw[-1] and raw[0] in "\"'":
        return raw[1:-1]
    return raw


def parse_frontmatter(text):
    """Return (mapping, error). Supports the scalar/list subset the vault uses.

    Deliberately not a general YAML parser. Nested mappings are not used by the
    note schema, and adding a dependency to read eight flat keys would cost more
    than it buys.
    """
    if not text.startswith("---"):
        return None, "no YAML frontmatter block"
    lines = text.splitlines()
    end = None
    for i in range(1, len(lines)):
        if lines[i].strip() == "---":
            end = i
            break
    if end is None:
        return None, "frontmatter block is never closed"

    data = {}
    key = None
    for line in lines[1:end]:
        if not line.strip() or line.lstrip().startswith("#"):
            continue
        stripped = line.strip()
        if stripped.startswith("- "):
            if key is None:
                continue
            # "targets:" on its own line parses as an empty scalar, and the
            # block sequence under it is what carries the values. Promote None
            # to a list here or every block list in the vault reads as empty --
            # which silently made the claim collision check answer "clear".
            if data.get(key) is None:
                data[key] = []
            if isinstance(data[key], list):
                data[key].append(_scalar(stripped[2:]))
            continue
        if ":" not in line:
            continue
        raw_key, _, raw_val = line.partition(":")
        key = raw_key.strip()
        raw_val = raw_val.strip()
        if raw_val in ("", "[]", "~", "null"):
            data[key] = [] if raw_val == "[]" else None
        else:
            data[key] = _scalar(raw_val)
    return data, None


# --- vault discovery --------------------------------------------------------

def find_vault_root(start):
    for candidate in [start] + list(start.parents):
        if (candidate / "brain" / "protocol" / "note-schema.md").is_file():
            return candidate
    return None


def git_output(root, args):
    try:
        done = subprocess.run(["git"] + args, cwd=str(root), capture_output=True,
                              text=True, timeout=30)
    except (OSError, subprocess.SubprocessError):
        return None
    return done.stdout.strip() if done.returncode == 0 else None


def changed_markdown(root):
    out = git_output(root, ["status", "--porcelain", "--", "brain"])
    if out is None:
        return None
    paths = []
    for line in out.splitlines():
        name = line[3:].strip().strip('"')
        if " -> " in name:
            name = name.split(" -> ", 1)[1]
        if name.endswith(".md"):
            paths.append(root / name)
        elif name.endswith("/"):
            paths.extend(sorted((root / name).rglob("*.md")))
    return [p for p in paths if p.is_file()]


# --- link resolution --------------------------------------------------------

def build_link_index(root):
    """Map every resolvable link target to its file.

    Links in this vault take three shapes: vault-relative with no extension
    (`brain/design/combat-v1`), repo-root documents (`PROJECT-STATUS`), and
    bare basenames Obsidian resolves by search (`combat-core/ROADMAP`).
    """
    index = {}
    skip = {".git", "node_modules", "build", ".gradle", "artifacts", "__pycache__"}
    for path in root.rglob("*"):
        if path.is_dir() or path.suffix.lower() not in (".md", ".canvas"):
            continue
        if any(part in skip for part in path.relative_to(root).parts):
            continue
        rel = path.relative_to(root).as_posix()
        index.setdefault(rel, path)
        index.setdefault(rel.rsplit(".", 1)[0], path)
        index.setdefault(path.name, path)
        index.setdefault(path.stem, path)
    return index


# --- checks -----------------------------------------------------------------

def check_note(path, root, link_index, uid_owners, findings, now):
    rel = path.relative_to(root).as_posix()
    is_template = rel.startswith("brain/templates/")
    text = path.read_text(encoding="utf-8", errors="replace")

    meta, err = parse_frontmatter(text)
    if err:
        findings.append(Finding("error", "FRONTMATTER", rel, err))
        return

    for key in REQUIRED_KEYS:
        if key not in meta or meta[key] in (None, ""):
            findings.append(Finding("error", "REQUIRED_MISSING", rel,
                                    "missing required property '%s'" % key))

    # Templates legitimately carry {{date}} placeholders; they are checked for
    # structure only, so a template can never be a silent source of bad values.
    if is_template:
        for key in ("uid", "created", "updated"):
            val = meta.get(key)
            if isinstance(val, str) and "{{" not in val and "replace" not in val:
                findings.append(Finding("warn", "TEMPLATE_LITERAL", rel,
                                        "'%s' looks like a real value, not a placeholder" % key))
        return

    # A filed record still carrying template scaffolding is broken even when
    # every other field is well formed: base_commit='replace-full-sha' names no
    # commit, and created='{{date}}' is not a date.
    for key, value in meta.items():
        for item in (value if isinstance(value, list) else [value]):
            if isinstance(item, str) and ("{{" in item or item.startswith("replace-")):
                findings.append(Finding("error", "PLACEHOLDER", rel,
                                        "%s='%s' is unsubstituted template scaffolding" % (key, item)))

    kind = meta.get("record_kind")
    for key, allowed in (("record_kind", RECORD_KINDS), ("authority", AUTHORITIES),
                         ("lore_class", LORE_CLASSES), ("state", STATES)):
        val = meta.get(key)
        if isinstance(val, str) and val not in allowed:
            findings.append(Finding("error", "ENUM_INVALID", rel,
                                    "%s='%s' is not one of: %s"
                                    % (key, val, ", ".join(sorted(allowed)))))

    created = parsed = None
    for key in ("created", "updated"):
        val = meta.get(key)
        if not isinstance(val, str):
            continue
        try:
            parsed = datetime.strptime(val, ISO_DATE)
        except ValueError:
            findings.append(Finding("error", "DATE_INVALID", rel,
                                    "%s='%s' is not an ISO YYYY-MM-DD date" % (key, val)))
            continue
        if key == "created":
            created = parsed
        elif created and parsed < created:
            findings.append(Finding("error", "DATE_ORDER", rel,
                                    "updated '%s' precedes created '%s'" % (val, meta["created"])))

    uid = meta.get("uid")
    if isinstance(uid, str) and uid:
        if uid in uid_owners:
            findings.append(Finding("error", "UID_DUPLICATE", rel,
                                    "uid '%s' is already used by %s" % (uid, uid_owners[uid])))
        else:
            uid_owners[uid] = rel

    name = path.name
    task_scoped = kind in AGENT_RECORD_KINDS or (kind == "evidence" and meta.get("task_id"))
    if task_scoped:
        if not AGENT_FILENAME.match(name):
            findings.append(Finding("error", "FILENAME", rel,
                                    "task-scoped %s records must be named "
                                    "YYYYMMDDTHHMMSSZ--agent--slug.md" % kind))
    elif kind == "decision":
        if not ADR_FILENAME.match(name):
            findings.append(Finding("error", "FILENAME", rel,
                                    "decision records must be named ADR-YYYYMMDD-slug.md"))
    elif name != "README.md" and not CONCEPT_FILENAME.match(name):
        findings.append(Finding("warn", "FILENAME", rel,
                                "concept notes use short stable lowercase names"))

    parent = path.parent.relative_to(root).as_posix()
    if kind in REQUIRED_HOME and parent != REQUIRED_HOME[kind]:
        findings.append(Finding("error", "PLACEMENT", rel,
                                "%s records belong in %s/" % (kind, REQUIRED_HOME[kind])))
    elif kind in ADVISORY_HOME and parent != ADVISORY_HOME[kind]:
        findings.append(Finding("warn", "PLACEMENT", rel,
                                "%s records usually live in %s/" % (kind, ADVISORY_HOME[kind])))
    elif kind == "evidence" and meta.get("task_id") and parent != "brain/evidence":
        findings.append(Finding("warn", "PLACEMENT", rel,
                                "task-scoped evidence usually lives in brain/evidence/"))

    # Deliberately no check that agent cards use one particular record_kind.
    # Codex settled on 'context' for its own card and template while this was
    # being written; a warning here would have pressured three other agents'
    # files toward one agent's taxonomic preference. Andrew can settle it.

    # A claim is a lock. An unbounded or silently expired lock is worse than none.
    if kind == "claim":
        lease = meta.get("lease_until")
        if not isinstance(lease, str) or not lease:
            findings.append(Finding("error", "LEASE_MISSING", rel,
                                    "claims must declare lease_until"))
        else:
            try:
                until = datetime.strptime(lease, ISO_STAMP)
            except ValueError:
                findings.append(Finding("error", "LEASE_INVALID", rel,
                                        "lease_until='%s' is not ISO YYYY-MM-DDTHH:MM:SSZ" % lease))
            else:
                if until < now and meta.get("state") == "active":
                    findings.append(Finding("error", "LEASE_EXPIRED", rel,
                                            "lease expired %s but state is still 'active'; "
                                            "expire the claim or extend the lease" % lease))
        for key in ("task_id", "branch", "base_commit"):
            if not meta.get(key):
                findings.append(Finding("error", "REQUIRED_MISSING", rel,
                                        "claims must declare '%s'" % key))

    for key in ("worktree_dirty",):
        val = meta.get(key)
        if isinstance(val, str) and val.lower() not in ("true", "false"):
            findings.append(Finding("warn", "BOOL_INVALID", rel,
                                    "%s='%s' should be true or false" % (key, val)))

    for target in set(WIKILINK.findall(text)):
        # Inside a Markdown table an alias pipe must be escaped as "\|", which
        # otherwise leaves a trailing backslash on the captured target.
        target = target.strip().rstrip("\\").strip()
        if target and target not in link_index:
            findings.append(Finding("warn", "LINK_BROKEN", rel,
                                    "wikilink [[%s]] does not resolve" % target))

    return meta


def check_cross_references(notes, findings):
    uids = {m.get("uid") for m in notes.values() if m and m.get("uid")}
    for rel, meta in notes.items():
        if not meta:
            continue
        sup = meta.get("supersedes")
        for value in (sup if isinstance(sup, list) else [sup] if sup else []):
            if value not in uids:
                findings.append(Finding("error", "SUPERSEDES_UNRESOLVED", rel,
                                        "supersedes '%s' matches no uid in the vault" % value))


def check_commits(root, notes, findings):
    head = git_output(root, ["rev-parse", "HEAD"])
    if head is None:
        return
    for rel, meta in notes.items():
        if not meta:
            continue
        for key in ("captured_commit", "source_commit", "base_commit", "head_commit"):
            sha = meta.get(key)
            if not isinstance(sha, str) or not sha or "replace" in sha:
                continue
            if git_output(root, ["cat-file", "-e", sha + "^{commit}"]) is None:
                findings.append(Finding("warn", "COMMIT_UNKNOWN", rel,
                                        "%s='%s' is not a commit in this repository" % (key, sha)))
            elif key == "captured_commit" and not head.startswith(sha) \
                    and meta.get("state") not in ("closed", "superseded", "archived"):
                # Only living notes can be stale. On a closed evidence record
                # captured_commit is an immutable historical fact, and warning
                # about it would fire on every record forever once HEAD moves --
                # noise that teaches agents to ignore warnings.
                findings.append(Finding("warn", "SNAPSHOT_STALE", rel,
                                        "captured_commit '%s' is behind HEAD '%s'; refresh or "
                                        "mark the note stale before relying on it" % (sha, head[:12])))


def check_canvases(root, link_index, findings):
    for canvas in sorted((root / "brain").rglob("*.canvas")):
        rel = canvas.relative_to(root).as_posix()
        try:
            data = json.loads(canvas.read_text(encoding="utf-8"))
        except ValueError as exc:
            findings.append(Finding("error", "CANVAS_INVALID", rel, "not valid JSON: %s" % exc))
            continue
        for node in data.get("nodes", []):
            ref = node.get("file")
            if ref and ref not in link_index and not (root / ref).exists():
                findings.append(Finding("warn", "CANVAS_LINK_BROKEN", rel,
                                        "node references missing file '%s'" % ref))


# --- entry point ------------------------------------------------------------

def main(argv=None):
    parser = argparse.ArgumentParser(description="Validate the Shadow Slave project brain.")
    parser.add_argument("--format", choices=("text", "json"), default="text")
    parser.add_argument("--changed-only", action="store_true",
                        help="only validate notes git reports as changed or untracked")
    parser.add_argument("--strict", action="store_true", help="treat warnings as errors")
    parser.add_argument("--root", type=Path, default=None)
    args = parser.parse_args(argv)

    root = args.root or find_vault_root(Path(__file__).resolve().parent)
    if root is None or not (root / "brain").is_dir():
        print("error: could not locate the vault root (brain/protocol/note-schema.md)",
              file=sys.stderr)
        return 2

    if args.changed_only:
        targets = changed_markdown(root)
        if targets is None:
            print("error: --changed-only needs a working git repository", file=sys.stderr)
            return 2
    else:
        targets = sorted((root / "brain").rglob("*.md"))

    findings = []
    notes = {}
    link_index = build_link_index(root)
    uid_owners = {}
    now = datetime.utcnow()

    for path in targets:
        rel = path.relative_to(root).as_posix()
        try:
            notes[rel] = check_note(path, root, link_index, uid_owners, findings, now)
        except OSError as exc:
            findings.append(Finding("error", "UNREADABLE", rel, str(exc)))

    check_cross_references(notes, findings)
    check_commits(root, notes, findings)
    if not args.changed_only:
        check_canvases(root, link_index, findings)

    errors = [f for f in findings if f.severity == "error"]
    warnings = [f for f in findings if f.severity == "warn"]

    if args.format == "json":
        print(json.dumps({
            "notes_checked": len(notes),
            "errors": len(errors),
            "warnings": len(warnings),
            "findings": [f.as_dict() for f in findings],
        }, indent=2))
    else:
        for finding in sorted(findings, key=lambda f: (f.severity != "error", f.path, f.code)):
            print("%-5s %-20s %s: %s" % (finding.severity.upper(), finding.code,
                                         finding.path, finding.message))
        print("\n%d note(s) checked, %d error(s), %d warning(s)"
              % (len(notes), len(errors), len(warnings)))

    if errors or (args.strict and warnings):
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
