#!/usr/bin/env bash
# Deterministic dedicated-server runner for Issue #34's successful-completion
# physical restart matrix. This script owns process/log evidence only; a real
# player still performs the documented entry, terminal interaction, reconnect,
# state readout and second relog.

set -euo pipefail

REPO="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RUN_DIR="$REPO/mod/run-completion-fault"
EVIDENCE_ROOT="${COMPLETION_FAULT_EVIDENCE_ROOT:-$REPO/mod/completion-fault-evidence}"
GRADLE=("$REPO/mod/gradlew" -p "$REPO/mod" runCompletionFaultServer --no-daemon --console=plain)

POINTS=(
  after_terminal_registry_save
  after_appraisal_player_save
  after_appraisal_registry_save
  after_return_player_save
  after_return_registry_save
  after_teardown_registry_save
)

usage() {
  cat <<'EOF'
usage: bash mod/run-completion-fault-row.sh <command> [args]

commands:
  init
      Create the dedicated run directory, eula.txt and default offline-mode
      server.properties. Does not create, delete or replace a world.

  fault <point>
      Launch the dedicated server with the exact completion fault point armed.
      A real player must perform the normal First Nightmare completion. The
      command passes only if the Minecraft child reports exit value 86 and the
      exact intentional-fault marker is retained in the row evidence directory.
      Starting a new fault attempt invalidates stale evidence from an older
      attempt for the same point.

  recover <point>
      Restart the same run directory WITHOUT the fault property. Reconnect the
      same player, collect the documented state readouts, relog once more, then
      stop the server normally. The complete console log is retained. Starting
      a new recovery attempt invalidates stale recovery logs/status for that row.

  verify <point> <instance-uuid>
      Verify the retained fault marker plus exactly one instance-keyed preview
      appraisal and exactly one successful-completion teardown across the
      fault/recovery console logs. This does not prove the manual Soul/status/
      relog observations.

  self-test
      Exercise point validation, stale-evidence invalidation and evidence-count
      checking using synthetic files/logs; does not launch Minecraft.

Accepted points:
  after_terminal_registry_save
  after_appraisal_player_save
  after_appraisal_registry_save
  after_return_player_save
  after_return_registry_save
  after_teardown_registry_save
EOF
}

is_valid_point() {
  local candidate="$1" point
  for point in "${POINTS[@]}"; do
    [[ "$candidate" == "$point" ]] && return 0
  done
  return 1
}

require_point() {
  local point="${1:-}"
  if ! is_valid_point "$point"; then
    echo "FAIL: invalid completion fault point '$point'." >&2
    echo "Accepted: ${POINTS[*]}" >&2
    return 2
  fi
}

row_dir() {
  printf '%s/%s\n' "$EVIDENCE_ROOT" "$1"
}

init_run_dir() {
  mkdir -p "$RUN_DIR"
  [[ -f "$RUN_DIR/eula.txt" ]] || printf 'eula=true\n' >"$RUN_DIR/eula.txt"
  if [[ ! -f "$RUN_DIR/server.properties" ]]; then
    printf 'online-mode=false\nserver-port=%s\n' "${COMPLETION_FAULT_PORT:-25600}" >"$RUN_DIR/server.properties"
  fi
  echo "Prepared $RUN_DIR"
  if [[ -d "$RUN_DIR/world" ]]; then
    echo "World already present; it was preserved."
  else
    echo "No world is present yet. Start from the intended disposable baseline before collecting evidence."
  fi
}

copy_latest_log() {
  local destination="$1"
  if [[ -f "$RUN_DIR/logs/latest.log" ]]; then
    cp "$RUN_DIR/logs/latest.log" "$destination"
  fi
}

clear_fault_attempt_evidence() {
  local dir="$1"
  rm -f \
    "$dir/fault-console.log" \
    "$dir/fault-latest.log" \
    "$dir/point.txt" \
    "$dir/gradle-exit-status.txt" \
    "$dir/recovery-console.log" \
    "$dir/recovery-latest.log" \
    "$dir/recovery-gradle-exit-status.txt"
}

clear_recovery_attempt_evidence() {
  local dir="$1"
  rm -f \
    "$dir/recovery-console.log" \
    "$dir/recovery-latest.log" \
    "$dir/recovery-gradle-exit-status.txt"
}

run_fault() {
  local point="$1"
  require_point "$point"
  init_run_dir >/dev/null

  local dir console latest status
  dir="$(row_dir "$point")"
  mkdir -p "$dir"
  clear_fault_attempt_evidence "$dir"
  console="$dir/fault-console.log"
  latest="$dir/fault-latest.log"
  : >"$console"

  echo "Arming completion fault: $point"
  echo "Use the SAME disposable world/JAR for this row. Complete The Last Signal normally with a real player."

  set +e
  "${GRADLE[@]}" -PcompletionFault="$point" >"$console" 2>&1
  status=$?
  set -e
  copy_latest_log "$latest"

  local marker="INTENTIONAL COMPLETION FAULT after durable boundary $point. Halting with exit code 86."
  if ! grep -Fq "$marker" "$console" && ! grep -Fq "$marker" "$latest" 2>/dev/null; then
    echo "FAIL: exact intentional-fault marker was not observed for $point." >&2
    echo "Gradle exit status: $status" >&2
    tail -40 "$console" >&2 || true
    return 1
  fi

  # ModDev runs the Minecraft JVM as a Gradle child. Gradle itself commonly exits
  # with status 1 when that child halts with 86, so validate the child diagnostic
  # instead of falsely requiring the wrapper process to return 86.
  if ! grep -Eq '(exit value|exit code)[^0-9]*86|non-zero exit value 86' "$console"; then
    echo "FAIL: fault marker was present, but the launcher did not retain evidence that the Minecraft child exited with 86." >&2
    echo "Gradle exit status: $status" >&2
    tail -40 "$console" >&2 || true
    return 1
  fi

  if ((status == 0)); then
    echo "FAIL: Gradle reported success even though the Minecraft child intentionally halted." >&2
    return 1
  fi

  printf '%s\n' "$point" >"$dir/point.txt"
  printf '%s\n' "$status" >"$dir/gradle-exit-status.txt"
  echo "PASS: retained exact $point fault marker and Minecraft child exit-86 evidence."
  echo "Next: bash mod/run-completion-fault-row.sh recover $point"
}

run_recovery() {
  local point="$1"
  require_point "$point"
  local dir console latest status
  dir="$(row_dir "$point")"
  [[ -f "$dir/point.txt" ]] || {
    echo "FAIL: no retained successful fault-stage evidence for $point. Run the fault stage first." >&2
    return 1
  }

  clear_recovery_attempt_evidence "$dir"
  console="$dir/recovery-console.log"
  latest="$dir/recovery-latest.log"
  : >"$console"

  echo "Restarting the SAME world without shadowslave.completionFault."
  echo "Reconnect the same player, record /shadowslave soul and nightmare_status, relog once, repeat the checks, then stop the server normally."

  set +e
  "${GRADLE[@]}" >"$console" 2>&1
  status=$?
  set -e
  copy_latest_log "$latest"
  printf '%s\n' "$status" >"$dir/recovery-gradle-exit-status.txt"

  if ((status != 0)); then
    echo "FAIL: recovery server did not stop normally (Gradle status $status)." >&2
    tail -40 "$console" >&2 || true
    return 1
  fi
  if grep -Fq 'INTENTIONAL COMPLETION FAULT' "$console" \
      || grep -Fq 'INTENTIONAL COMPLETION FAULT' "$latest" 2>/dev/null; then
    echo "FAIL: recovery stage still contains an armed completion fault." >&2
    return 1
  fi

  echo "Recovery process stopped normally. Manual state/relog observations are still required."
}

count_marker() {
  local dir="$1" marker="$2"
  cat "$dir"/fault-console.log "$dir"/recovery-console.log 2>/dev/null | grep -Fc "$marker" || true
}

verify_evidence() {
  local point="$1" instance="$2"
  require_point "$point"
  local dir="$(row_dir "$point")"
  [[ -f "$dir/point.txt" && -f "$dir/fault-console.log" && -f "$dir/recovery-console.log" ]] || {
    echo "FAIL: successful fault-stage evidence plus fault/recovery console logs are required for $point." >&2
    return 1
  }

  if [[ "$(cat "$dir/point.txt")" != "$point" ]]; then
    echo "FAIL: retained point.txt does not authenticate the requested fault point $point." >&2
    return 1
  fi

  local fault_marker appraisal_marker teardown_marker appraisals teardowns
  fault_marker="INTENTIONAL COMPLETION FAULT after durable boundary $point. Halting with exit code 86."
  appraisal_marker="Preview appraisal completed for Nightmare $instance"
  teardown_marker="Nightmare $instance successful-completion teardown completed"

  # Collection accepts the server-side marker from either Gradle's console or
  # Minecraft's retained latest.log because ModDev can route child logging
  # differently. Verification must honor the same evidence contract.
  if ! grep -Fq "$fault_marker" "$dir/fault-console.log" \
      && ! grep -Fq "$fault_marker" "$dir/fault-latest.log" 2>/dev/null; then
    echo "FAIL: exact fault marker is missing from retained fault evidence." >&2
    return 1
  fi

  appraisals="$(count_marker "$dir" "$appraisal_marker")"
  teardowns="$(count_marker "$dir" "$teardown_marker")"

  if [[ "$appraisals" != "1" || "$teardowns" != "1" ]]; then
    echo "FAIL: expected exactly one appraisal and one successful-completion teardown for $instance; got appraisal=$appraisals teardown=$teardowns." >&2
    return 1
  fi

  if cat "$dir"/fault-console.log "$dir"/recovery-console.log | grep -Fq 'teardown skipped because its ownership was already absent'; then
    echo "FAIL: duplicate/stale teardown warning was observed." >&2
    return 1
  fi

  echo "PASS: retained logs contain one appraisal and one successful-completion teardown for $instance at $point."
  echo "MANUAL EVIDENCE STILL REQUIRED: Dreamer/Dormant + expected identity, no active Nightmare, same world/JAR/player, and a completed second relog with unchanged counts."
}

self_test() {
  require_point after_return_registry_save
  if require_point after_return_regsitry_save >/dev/null 2>&1; then
    echo "FAIL: misspelled point unexpectedly passed validation." >&2
    return 1
  fi

  local old_root="$EVIDENCE_ROOT" tmp point instance dir
  tmp="$(mktemp -d)"
  EVIDENCE_ROOT="$tmp"
  point=after_appraisal_registry_save
  instance=11111111-2222-3333-4444-555555555555
  dir="$(row_dir "$point")"
  mkdir -p "$dir"

  # A new fault attempt must invalidate every artifact that could authenticate
  # or contaminate a prior attempt for the same row.
  touch \
    "$dir/fault-console.log" \
    "$dir/fault-latest.log" \
    "$dir/point.txt" \
    "$dir/gradle-exit-status.txt" \
    "$dir/recovery-console.log" \
    "$dir/recovery-latest.log" \
    "$dir/recovery-gradle-exit-status.txt"
  clear_fault_attempt_evidence "$dir"
  if find "$dir" -mindepth 1 -maxdepth 1 -type f | grep -q .; then
    rm -rf "$tmp"
    EVIDENCE_ROOT="$old_root"
    echo "FAIL: stale fault-row evidence survived new-attempt invalidation." >&2
    return 1
  fi

  cat >"$dir/fault-console.log" <<EOF
Preview appraisal completed for Nightmare $instance
EOF
  cat >"$dir/fault-latest.log" <<EOF
INTENTIONAL COMPLETION FAULT after durable boundary $point. Halting with exit code 86.
EOF
  printf '%s\n' "$point" >"$dir/point.txt"
  cat >"$dir/recovery-console.log" <<EOF
Nightmare $instance successful-completion teardown completed
EOF
  verify_evidence "$point" "$instance" >/dev/null

  # A new recovery attempt must not be able to reuse a prior recovery latest.log
  # or status file if the current Minecraft launch fails before creating them.
  touch "$dir/recovery-latest.log" "$dir/recovery-gradle-exit-status.txt"
  clear_recovery_attempt_evidence "$dir"
  if [[ -e "$dir/recovery-console.log" || -e "$dir/recovery-latest.log" || -e "$dir/recovery-gradle-exit-status.txt" ]]; then
    rm -rf "$tmp"
    EVIDENCE_ROOT="$old_root"
    echo "FAIL: stale recovery evidence survived new-attempt invalidation." >&2
    return 1
  fi

  cat >"$dir/recovery-console.log" <<EOF
Nightmare $instance successful-completion teardown completed
EOF
  verify_evidence "$point" "$instance" >/dev/null

  printf 'Nightmare %s successful-completion teardown completed\n' "$instance" >>"$dir/recovery-console.log"
  if verify_evidence "$point" "$instance" >/dev/null 2>&1; then
    rm -rf "$tmp"
    EVIDENCE_ROOT="$old_root"
    echo "FAIL: duplicate teardown unexpectedly passed evidence verification." >&2
    return 1
  fi

  rm -rf "$tmp"
  EVIDENCE_ROOT="$old_root"
  echo "PASS: completion fault runner self-test"
}

command="${1:-}"
case "$command" in
  init)
    [[ $# -eq 1 ]] || { usage >&2; exit 2; }
    init_run_dir
    ;;
  fault)
    [[ $# -eq 2 ]] || { usage >&2; exit 2; }
    run_fault "$2"
    ;;
  recover)
    [[ $# -eq 2 ]] || { usage >&2; exit 2; }
    run_recovery "$2"
    ;;
  verify)
    [[ $# -eq 3 ]] || { usage >&2; exit 2; }
    verify_evidence "$2" "$3"
    ;;
  self-test)
    [[ $# -eq 1 ]] || { usage >&2; exit 2; }
    self_test
    ;;
  *)
    usage >&2
    exit 2
    ;;
esac
