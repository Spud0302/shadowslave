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

    def test_expensive_jobs_skip_draft_pull_requests(self):
        repository_root = Path(__file__).resolve().parents[3]
        workflow = repository_root / ".github" / "workflows" / "java-core.yml"
        text = workflow.read_text(encoding="utf-8")
        draft_guard = "if: github.event_name != 'pull_request' || github.event.pull_request.draft == false"

        self.assertEqual(
            2,
            text.count(draft_guard),
            "Both Java and datapack jobs must skip draft pull requests",
        )

    def test_java_gate_restarts_the_same_dedicated_server_world(self):
        repository_root = Path(__file__).resolve().parents[3]
        workflow = repository_root / ".github" / "workflows" / "java-core.yml"
        smoke = repository_root / "mod" / "verify-smoke.sh"
        workflow_text = workflow.read_text(encoding="utf-8")
        smoke_text = smoke.read_text(encoding="utf-8")

        self.assertIn("./mod/verify-smoke.sh server-restart", workflow_text)
        self.assertIn("verify_server_restart()", smoke_text)
        self.assertIn('prepare_server_smoke true', smoke_text)
        self.assertIn('prepare_server_smoke false', smoke_text)
        self.assertIn('mod/run-server-smoke/world/level.dat', smoke_text)
        self.assertIn('dedicated server same-world restart', smoke_text)

    def test_java_gate_runs_moddevgradle_gametest_server(self):
        repository_root = Path(__file__).resolve().parents[3]
        workflow = repository_root / ".github" / "workflows" / "java-core.yml"
        build = repository_root / "mod" / "build.gradle"
        workflow_text = workflow.read_text(encoding="utf-8")
        build_text = build.read_text(encoding="utf-8")

        self.assertIn(
            "./mod/gradlew -p mod runGameTestServer --no-daemon --stacktrace",
            workflow_text,
            "Preview Gates must execute the configured NeoForge GameTest server",
        )
        game_test_block = build_text.split("        gameTestServer {\n", 1)[1].split("        }\n", 1)[0]
        self.assertIn("type = 'gameTestServer'", game_test_block)
        self.assertIn("systemProperty 'neoforge.enabledGameTestNamespaces', project.mod_id", game_test_block)
        self.assertNotIn(
            "setForceExit",
            game_test_block,
            "ModDevGradle RunModel does not expose NeoGradle's setForceExit DSL",
        )


if __name__ == "__main__":
    unittest.main()
