from pathlib import Path
import re
import unittest


ROOT = Path(__file__).resolve().parents[2]
WORKFLOW = ROOT / ".github" / "workflows" / "modpack-shell.yml"


class ProvenanceWorkflowTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls) -> None:
        cls.workflow = WORKFLOW.read_text(encoding="utf-8")

    def test_package_upload_exposes_real_artifact_id_before_statement_creation(self) -> None:
        upload = self.workflow.index("id: package-upload")
        create = self.workflow.index("name: Create and verify external build provenance")
        self.assertLess(upload, create)
        self.assertIn(
            "PACKAGE_ARTIFACT_ID: ${{ steps.package-upload.outputs.artifact-id }}",
            self.workflow,
        )
        self.assertIn('--artifact-id "$PACKAGE_ARTIFACT_ID"', self.workflow)

    def test_build_and_statement_use_same_source_repository_and_commit(self) -> None:
        repository_expression = (
            "${{ github.event.pull_request.head.repo.full_name || github.repository }}"
        )
        commit_expression = "${{ github.event.pull_request.head.sha || github.sha }}"
        checkout = self.workflow.index("name: Check out claimed source repository and commit")
        validate = self.workflow.index("name: Validate manifest")
        self.assertLess(checkout, validate)
        self.assertIn(f"repository: {repository_expression}", self.workflow)
        self.assertIn(f"ref: {commit_expression}", self.workflow)
        self.assertIn(f"SOURCE_REPOSITORY: {repository_expression}", self.workflow)
        self.assertIn(f"SOURCE_COMMIT: {commit_expression}", self.workflow)
        self.assertIn('--repository "$SOURCE_REPOSITORY"', self.workflow)
        self.assertIn('--expected-repository "$SOURCE_REPOSITORY"', self.workflow)
        self.assertIn('--commit-sha "$SOURCE_COMMIT"', self.workflow)
        self.assertIn('--expected-commit "$SOURCE_COMMIT"', self.workflow)
        self.assertNotIn('--repository "$GITHUB_REPOSITORY"', self.workflow)
        self.assertNotIn('--expected-repository "$GITHUB_REPOSITORY"', self.workflow)

    def test_third_party_actions_are_pinned_to_full_commit_shas(self) -> None:
        uses_entries = re.findall(r"^\s*uses:\s*([^\s#]+)", self.workflow, re.MULTILINE)
        self.assertGreaterEqual(len(uses_entries), 3)
        for entry in uses_entries:
            action, separator, revision = entry.partition("@")
            self.assertTrue(separator, f"action is missing a revision: {entry}")
            self.assertRegex(action, r"^[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+$")
            self.assertRegex(
                revision,
                r"^[0-9a-f]{40}$",
                f"action is not pinned to a full commit SHA: {entry}",
            )

    def test_statement_is_verified_before_separate_upload(self) -> None:
        create = self.workflow.index("name: Create and verify external build provenance")
        verify = self.workflow.index("modpack/tools/build_provenance.py verify")
        publish = self.workflow.index("name: Upload external build provenance")
        self.assertLess(create, verify)
        self.assertLess(verify, publish)
        self.assertIn("name: nightmare-spell-modpack-shell-provenance", self.workflow)
        self.assertIn(
            "path: ${{ runner.temp }}/nightmare-spell-modpack-dev.provenance.json",
            self.workflow,
        )


if __name__ == "__main__":
    unittest.main()
