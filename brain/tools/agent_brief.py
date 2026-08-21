#!/usr/bin/env python3
"""Print everything an agent needs before its first write, in one call.

Without this, starting a task correctly costs roughly eight file reads plus a
manual directory inspection: the entry point, home, two protocol notes, a
context packet, every file in the claims folder, and git state. Agents pay for
each of those in context. This collapses them into one command whose output is
generated, so it cannot drift from the vault the way a hand-maintained index
would.

    python brain/tools/agent_brief.py
    python brain/tools/agent_brief.py --paths mod/src/main/java combat-core/src
    python brain/tools/agent_brief.py --json

Exit codes: 0 normal, 1 the vault itself fails validation. Overlapping claims
and expired leases are reported as coordination information rather than
failures -- concurrent editing of one file is supported, so failing here on an
overlap would only train agents to ignore the exit code.
"""

import argparse
import json
import sys
from datetime import datetime
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from validate_vault import (ISO_STAMP, find_vault_root, git_output,  # noqa: E402
                            parse_frontmatter)
import validate_vault  # noqa: E402


def load_notes(folder):
    out = []
    if not folder.is_dir():
        return out
    for path in sorted(folder.glob("*.md")):
        if path.name == "README.md":
            continue
        meta, err = parse_frontmatter(path.read_text(encoding="utf-8", errors="replace"))
        if not err:
            out.append((path, meta or {}))
    return out


def section_text(path, heading):
    """Return the first non-empty line under a heading."""
    lines = path.read_text(encoding="utf-8", errors="replace").splitlines()
    for i, line in enumerate(lines):
        if line.strip().lower() == heading.lower():
            for follow in lines[i + 1:]:
                if follow.strip() and not follow.startswith("#"):
                    return follow.strip()
            return ""
    return ""


def as_list(value):
    if value is None:
        return []
    return value if isinstance(value, list) else [value]


def overlaps(candidate, target):
    """True when two repo paths refer to the same file or nest one inside the other."""
    a = candidate.strip().strip("/").rstrip("/")
    b = target.strip().strip("/").rstrip("/")
    if not a or not b:
        return False
    return a == b or a.startswith(b + "/") or b.startswith(a + "/")


def gather(root, paths, now):
    branch = git_output(root, ["rev-parse", "--abbrev-ref", "HEAD"]) or "unknown"
    head = git_output(root, ["rev-parse", "HEAD"]) or ""
    porcelain = git_output(root, ["status", "--porcelain"]) or ""
    dirty = [line for line in porcelain.splitlines() if line.strip()]

    claims = []
    for path, meta in load_notes(root / "brain" / "ai" / "claims"):
        lease = meta.get("lease_until")
        expired = None
        if isinstance(lease, str):
            try:
                expired = datetime.strptime(lease, ISO_STAMP) < now
            except ValueError:
                expired = None
        claims.append({
            "file": path.relative_to(root).as_posix(),
            "owner": meta.get("owner"),
            "tool": meta.get("tool"),
            "task_id": meta.get("task_id"),
            "state": meta.get("state"),
            "branch": meta.get("branch"),
            "lease_until": lease,
            "lease_expired": expired,
            "targets": as_list(meta.get("targets")),
            "excludes": as_list(meta.get("excludes")),
        })

    active = [c for c in claims if c["state"] == "active"]
    stale_locks = [c for c in active if c["lease_expired"]]

    collisions = []
    for candidate in paths:
        for claim in active:
            hits = [t for t in claim["targets"] if overlaps(candidate, t)]
            excluded = any(overlaps(candidate, e) for e in claim["excludes"])
            if hits and not excluded:
                collisions.append({"path": candidate, "claim": claim["file"],
                                   "owner": claim["owner"], "targets": hits})

    packets = []
    for path, meta in load_notes(root / "brain" / "ai" / "context"):
        packets.append({"file": path.relative_to(root).as_posix(),
                        "name": path.stem,
                        "goal": section_text(path, "## Goal"),
                        "state": meta.get("state")})

    agents = []
    for path, meta in load_notes(root / "brain" / "ai" / "agents"):
        agents.append({"file": path.relative_to(root).as_posix(),
                       "owner": meta.get("owner"), "state": meta.get("state")})

    stale_notes = []
    if head:
        for path in sorted((root / "brain").rglob("*.md")):
            meta, err = parse_frontmatter(path.read_text(encoding="utf-8", errors="replace"))
            if err or not meta:
                continue
            captured = meta.get("captured_commit")
            if isinstance(captured, str) and captured and "replace" not in captured \
                    and not head.startswith(captured):
                stale_notes.append({"file": path.relative_to(root).as_posix(),
                                    "captured_commit": captured})

    findings = run_validator(root)

    return {
        "branch": branch,
        "head": head,
        "dirty_entries": len(dirty),
        "brain_dirty": any("brain/" in line for line in dirty),
        "active_claims": active,
        "expired_leases": stale_locks,
        "collisions": collisions,
        "context_packets": packets,
        "registered_agents": agents,
        "stale_snapshots": stale_notes,
        "validation": findings,
    }


def run_validator(root):
    import io
    import contextlib
    buf = io.StringIO()
    with contextlib.redirect_stdout(buf):
        validate_vault.main(["--root", str(root), "--format", "json"])
    return json.loads(buf.getvalue())


def render(brief, paths):
    out = []
    add = out.append

    add("REPOSITORY")
    add("  branch %s at %s" % (brief["branch"], (brief["head"] or "unknown")[:12]))
    add("  %d uncommitted worktree entries; preserve anything outside your claim"
        % brief["dirty_entries"])

    add("")
    add("VAULT VALIDATION")
    val = brief["validation"]
    add("  %d notes, %d errors, %d warnings" % (val["notes_checked"], val["errors"],
                                                val["warnings"]))
    for finding in val["findings"][:10]:
        add("  %-5s %s: %s" % (finding["severity"].upper(), finding["path"],
                               finding["message"]))

    add("")
    add("ACTIVE CLAIMS (%d)" % len(brief["active_claims"]))
    if brief["active_claims"]:
        add("  Other agents working here is normal, not a conflict.")
        add("  See brain/protocol/concurrent-editing.md")
    if not brief["active_claims"]:
        add("  none - the vault is free for a new claim")
    for claim in brief["active_claims"]:
        flag = "  EXPIRED LEASE" if claim["lease_expired"] else ""
        add("  %s" % claim["file"])
        add("    owner %s, lease %s%s" % (claim["owner"], claim["lease_until"], flag))
        for target in claim["targets"][:8]:
            add("    holds %s" % target)

    if brief["expired_leases"]:
        add("")
        add("EXPIRED LEASES - these claims need tidying")
        for claim in brief["expired_leases"]:
            add("  %s (owner %s)" % (claim["file"], claim["owner"]))
        add("  Set state to 'expired' with a dated closure note naming who did it")
        add("  and why, or ask the owner to extend. Change state and closure only;")
        add("  the claim's scope and history stay immutable.")
        add("  Not an error -> brain/protocol/concurrent-editing.md")

    if paths:
        add("")
        add("CONCURRENT WORK ON YOUR PATHS")
        if not brief["collisions"]:
            add("  no other active claim names: %s" % ", ".join(paths))
            add("  a claim filed seconds ago may not be visible; re-check before writing")
        for hit in brief["collisions"]:
            add("  %s is also claimed by %s (owner %s) via %s"
                % (hit["path"], hit["claim"], hit["owner"], ", ".join(hit["targets"])))
        if brief["collisions"]:
            add("  This is coordination information, not a stop. Co-editing is")
            add("  supported: make targeted edits or append an attributed section,")
            add("  never rewrite the file wholesale, and record the overlap in your")
            add("  own claim.")
            add("  Nothing is wrong -> brain/protocol/concurrent-editing.md")

    add("")
    add("CONTEXT PACKETS - load one, not the repository")
    for packet in brief["context_packets"]:
        add("  %-16s %s" % (packet["name"], packet["goal"][:96]))

    add("")
    add("REGISTERED AGENTS")
    for agent in brief["registered_agents"]:
        add("  %s" % agent["file"])

    if brief["stale_snapshots"]:
        add("")
        add("STALE DERIVED NOTES — captured_commit is behind HEAD")
        for note in brief["stale_snapshots"]:
            add("  %s (%s)" % (note["file"], note["captured_commit"]))

    add("")
    add("BEFORE YOU WRITE")
    add("  1. Load one context packet above.")
    add("  2. python brain/tools/agent_brief.py --paths <the paths you will touch>")
    add("  3. python brain/tools/new_record.py claim --agent <slug> --slug <task>")
    add("  4. Work only inside the claim. Preserve unrelated dirty-worktree changes.")
    add("  5. On a shared note: targeted edits or an appended attributed section.")
    add("     Never rewrite one wholesale - that discards concurrent work silently.")
    add("  6. python brain/tools/validate_vault.py before the handoff.")
    return "\n".join(out)


def main(argv=None):
    parser = argparse.ArgumentParser(description="Session brief for an agent starting work.")
    parser.add_argument("--paths", nargs="*", default=[],
                        help="repo paths you intend to write; checked against active claims")
    parser.add_argument("--json", action="store_true")
    parser.add_argument("--root", type=Path, default=None)
    args = parser.parse_args(argv)

    root = args.root or find_vault_root(Path(__file__).resolve().parent)
    if root is None:
        print("error: could not locate the vault root", file=sys.stderr)
        return 2

    brief = gather(root, args.paths, datetime.utcnow())
    print(json.dumps(brief, indent=2) if args.json else render(brief, args.paths))

    # Only a genuinely broken vault is a stop. Overlapping claims and expired
    # leases are coordination information: concurrent editing of one file is
    # supported, so failing the brief on an overlap would train agents to
    # ignore the exit code.
    return 1 if brief["validation"]["errors"] > 0 else 0


if __name__ == "__main__":
    sys.exit(main())
