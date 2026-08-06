from pathlib import Path
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

    def test_pull_request_build_and_statement_use_same_head_commit(self) -> None:
        source_expression = "${{ github.event.pull_request.head.sha || github.sha }}"
        checkout = self.workflow.index("name: Check out claimed source commit")
        validate = self.workflow.index("name: Validate manifest")
        self.assertLess(checkout, validate)
        self.assertIn(f"ref: {source_expression}", self.workflow)
        self.assertIn(f"SOURCE_COMMIT: {source_expression}", self.workflow)
        self.assertIn('--commit-sha "$SOURCE_COMMIT"', self.workflow)
        self.assertIn('--expected-commit "$SOURCE_COMMIT"', self.workflow)

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
