#!/usr/bin/env python3
"""Create and verify external build provenance for a packaged modpack archive."""

from __future__ import annotations

import argparse
import hashlib
import json
from pathlib import Path
import re

SHA256_RE = re.compile(r"^[0-9a-f]{64}$")
COMMIT_RE = re.compile(r"^[0-9a-f]{40}$")
REPOSITORY_RE = re.compile(r"^[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+$")


class ProvenanceError(ValueError):
    pass


def sha256_file(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def require_non_empty(value: object, field: str) -> str:
    if not isinstance(value, str) or not value.strip():
        raise ProvenanceError(f"{field} must be a non-empty string")
    return value


def validate(statement: object) -> dict[str, object]:
    if not isinstance(statement, dict):
        raise ProvenanceError("provenance must be a JSON object")
    expected = {
        "schema_version", "repository", "commit_sha", "workflow_run_id",
        "workflow_run_attempt", "artifact_id", "artifact_name",
        "archive_name", "archive_sha256", "core_jar_sha256",
    }
    if set(statement) != expected:
        missing = sorted(expected - set(statement))
        extra = sorted(set(statement) - expected)
        raise ProvenanceError(f"invalid fields; missing={missing}, extra={extra}")
    if statement["schema_version"] != 1:
        raise ProvenanceError("schema_version must be 1")
    repository = require_non_empty(statement["repository"], "repository")
    if not REPOSITORY_RE.fullmatch(repository):
        raise ProvenanceError("repository must use owner/name form")
    commit = require_non_empty(statement["commit_sha"], "commit_sha")
    if not COMMIT_RE.fullmatch(commit):
        raise ProvenanceError("commit_sha must be a lowercase 40-character Git SHA")
    for field in ("workflow_run_id", "workflow_run_attempt", "artifact_id"):
        value = statement[field]
        if not isinstance(value, int) or isinstance(value, bool) or value < 1:
            raise ProvenanceError(f"{field} must be a positive integer")
    for field in ("artifact_name", "archive_name"):
        require_non_empty(statement[field], field)
    for field in ("archive_sha256", "core_jar_sha256"):
        value = require_non_empty(statement[field], field)
        if not SHA256_RE.fullmatch(value):
            raise ProvenanceError(f"{field} must be a lowercase SHA-256")
    return statement


def create_statement(
    archive: Path,
    core_jar: Path,
    repository: str,
    commit_sha: str,
    workflow_run_id: int,
    workflow_run_attempt: int,
    artifact_id: int,
    artifact_name: str,
) -> dict[str, object]:
    if not archive.is_file():
        raise ProvenanceError(f"archive does not exist: {archive}")
    if not core_jar.is_file():
        raise ProvenanceError(f"core JAR does not exist: {core_jar}")
    return validate({
        "schema_version": 1,
        "repository": repository,
        "commit_sha": commit_sha,
        "workflow_run_id": workflow_run_id,
        "workflow_run_attempt": workflow_run_attempt,
        "artifact_id": artifact_id,
        "artifact_name": artifact_name,
        "archive_name": archive.name,
        "archive_sha256": sha256_file(archive),
        "core_jar_sha256": sha256_file(core_jar),
    })


def verify_statement(
    statement_path: Path,
    archive: Path,
    core_jar: Path,
    expected_repository: str,
    expected_commit: str,
    expected_run_id: int,
    expected_artifact_id: int,
) -> None:
    with statement_path.open("r", encoding="utf-8") as handle:
        statement = validate(json.load(handle))
    checks = {
        "repository": expected_repository,
        "commit_sha": expected_commit,
        "workflow_run_id": expected_run_id,
        "artifact_id": expected_artifact_id,
        "archive_name": archive.name,
        "archive_sha256": sha256_file(archive),
        "core_jar_sha256": sha256_file(core_jar),
    }
    for field, expected in checks.items():
        if statement[field] != expected:
            raise ProvenanceError(
                f"{field} mismatch: statement={statement[field]!r}, expected={expected!r}"
            )


def write_statement(path: Path, statement: dict[str, object]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(statement, indent=2, sort_keys=True) + "\n", encoding="utf-8")


def main() -> int:
    parser = argparse.ArgumentParser()
    subparsers = parser.add_subparsers(dest="command", required=True)
    create = subparsers.add_parser("create")
    create.add_argument("--archive", required=True)
    create.add_argument("--core-jar", required=True)
    create.add_argument("--repository", required=True)
    create.add_argument("--commit-sha", required=True)
    create.add_argument("--workflow-run-id", required=True, type=int)
    create.add_argument("--workflow-run-attempt", required=True, type=int)
    create.add_argument("--artifact-id", required=True, type=int)
    create.add_argument("--artifact-name", required=True)
    create.add_argument("--output", required=True)

    verify = subparsers.add_parser("verify")
    verify.add_argument("--statement", required=True)
    verify.add_argument("--archive", required=True)
    verify.add_argument("--core-jar", required=True)
    verify.add_argument("--expected-repository", required=True)
    verify.add_argument("--expected-commit", required=True)
    verify.add_argument("--expected-run-id", required=True, type=int)
    verify.add_argument("--expected-artifact-id", required=True, type=int)
    args = parser.parse_args()
    try:
        if args.command == "create":
            statement = create_statement(
                Path(args.archive), Path(args.core_jar), args.repository, args.commit_sha,
                args.workflow_run_id, args.workflow_run_attempt, args.artifact_id,
                args.artifact_name,
            )
            write_statement(Path(args.output), statement)
            print(f"OK: {args.output}")
        else:
            verify_statement(
                Path(args.statement), Path(args.archive), Path(args.core_jar),
                args.expected_repository, args.expected_commit,
                args.expected_run_id, args.expected_artifact_id,
            )
            print("OK: external build provenance matches expected identity and bytes")
    except (OSError, json.JSONDecodeError, ProvenanceError) as error:
        print(f"ERROR: {error}")
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
