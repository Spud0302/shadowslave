from pathlib import Path
import unittest


class PreviewGatesWorkflowContractTest(unittest.TestCase):
    def test_reviewable_pull_requests_and_corrected_heads_trigger_preview_gates(self):
        repository_root = Path(__file__).resolve().parents[3]
        workflow = repository_root / ".github" / "workflows" / "java-core.yml"
        text = workflow.read_text(encoding="utf-8")

        pull_request_block = text.split("  pull_request:\n", 1)[1].split("  push:\n", 1)[0]
        for event in ("opened", "synchronize", "reopened", "ready_for_review"):
            self.assertIn(
                f"      - {event}\n",
                pull_request_block,
                f"Preview Gates must run for pull_request event {event}",
            )


if __name__ == "__main__":
    unittest.main()
