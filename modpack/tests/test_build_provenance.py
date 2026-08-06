from __future__ import annotations

import json
from pathlib import Path
import tempfile
import unittest

from modpack.tools.build_provenance import (
    ProvenanceError,
    create_statement,
    validate,
    verify_statement,
    write_statement,
)


class BuildProvenanceTest(unittest.TestCase):
    def setUp(self) -> None:
        self.temp = tempfile.TemporaryDirectory()
        self.root = Path(self.temp.name)
        self.archive = self.root / "pack.zip"
        self.core = self.root / "core.jar"
        self.statement = self.root / "build-provenance.json"
        self.archive.write_bytes(b"archive-bytes")
        self.core.write_bytes(b"core-bytes")

    def tearDown(self) -> None:
        self.temp.cleanup()

    def make_statement(self) -> dict[str, object]:
        return create_statement(
            self.archive,
            self.core,
            "Spud0302/shadowslave",
            "a" * 40,
            123,
            2,
            456,
            "nightmare-spell-modpack",
        )

    def verify(self, **overrides: object) -> None:
        values = {
            "expected_repository": "Spud0302/shadowslave",
            "expected_commit": "a" * 40,
            "expected_run_id": 123,
            "expected_run_attempt": 2,
            "expected_artifact_id": 456,
            "expected_artifact_name": "nightmare-spell-modpack",
        }
        values.update(overrides)
        verify_statement(
            self.statement,
            self.archive,
            self.core,
            values["expected_repository"],
            values["expected_commit"],
            values["expected_run_id"],
            values["expected_run_attempt"],
            values["expected_artifact_id"],
            values["expected_artifact_name"],
        )

    def test_create_and_verify_exact_external_identity_and_bytes(self) -> None:
        write_statement(self.statement, self.make_statement())
        self.verify()

    def test_archive_tampering_is_rejected(self) -> None:
        write_statement(self.statement, self.make_statement())
        self.archive.write_bytes(b"tampered")
        with self.assertRaisesRegex(ProvenanceError, "archive_sha256 mismatch"):
            self.verify()

    def test_commit_mismatch_is_rejected(self) -> None:
        write_statement(self.statement, self.make_statement())
        with self.assertRaisesRegex(ProvenanceError, "commit_sha mismatch"):
            self.verify(expected_commit="b" * 40)

    def test_run_attempt_mismatch_is_rejected(self) -> None:
        write_statement(self.statement, self.make_statement())
        with self.assertRaisesRegex(ProvenanceError, "workflow_run_attempt mismatch"):
            self.verify(expected_run_attempt=3)

    def test_artifact_name_mismatch_is_rejected(self) -> None:
        write_statement(self.statement, self.make_statement())
        with self.assertRaisesRegex(ProvenanceError, "artifact_name mismatch"):
            self.verify(expected_artifact_name="replacement-artifact")

    def test_unknown_fields_are_rejected(self) -> None:
        statement = self.make_statement()
        statement["builder_claim"] = "trusted"
        with self.assertRaisesRegex(ProvenanceError, "extra=.*builder_claim"):
            validate(statement)

    def test_boolean_ids_are_rejected(self) -> None:
        statement = self.make_statement()
        statement["artifact_id"] = True
        with self.assertRaisesRegex(ProvenanceError, "artifact_id must be a positive integer"):
            validate(statement)

    def test_malformed_repository_and_digest_are_rejected(self) -> None:
        statement = self.make_statement()
        statement["repository"] = "shadowslave"
        with self.assertRaisesRegex(ProvenanceError, "owner/name"):
            validate(statement)
        statement = self.make_statement()
        statement["core_jar_sha256"] = "A" * 64
        with self.assertRaisesRegex(ProvenanceError, "lowercase SHA-256"):
            validate(statement)

    def test_json_statement_is_deterministic(self) -> None:
        write_statement(self.statement, self.make_statement())
        first = self.statement.read_bytes()
        write_statement(self.statement, self.make_statement())
        self.assertEqual(first, self.statement.read_bytes())
        parsed = json.loads(first)
        self.assertEqual(1, parsed["schema_version"])


if __name__ == "__main__":
    unittest.main()
