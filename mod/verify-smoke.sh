#!/usr/bin/env bash
# Local NeoForge smoke gate for the Java core: boot a real dedicated server and a real physical
# client, and pass only when each prints the markers proving it actually got there.
#
# WHY THIS EXISTS
#
# `./mod/gradlew -p mod runServerSmoke` is NOT a pass/fail gate. Observed 2026-07-30: the dedicated
# server failed to bind its port, threw an NPE while shutting down, never reached ready state — and
# Gradle still reported BUILD SUCCESSFUL with exit code 0. Reading the exit code proves nothing.
# The CI workflow is right to ignore it and grep the log instead; this script is that same criterion
# for local runs, so a reviewer cannot record a false pass by following the exit code.
#
# The markers are copied from .github/workflows/java-core.yml. If CI's criteria change, change both.
#
# The smoke runs never terminate on their own — they boot and keep running — so each is launched in
# its own process group and killed once its markers appear.
#
# Usage: mod/verify-smoke.sh [server|client]      (no argument runs both)

set -uo pipefail

REPO="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO"

# 25565 is deliberately avoided: the Mineflayer datapack harness holds it on this box, and that
# collision is exactly what produced the silent false pass described above.
SMOKE_PORT="${SMOKE_PORT:-25599}"
TIMEOUT_SECONDS="${TIMEOUT_SECONDS:-540}"

# Killing the setsid process group is NOT enough on its own: observed 2026-07-30, the Minecraft JVM
# Gradle spawns survived a group TERM/KILL and kept holding both its port and world/session.lock, so
# the NEXT run died with "already locked" while Gradle still reported BUILD SUCCESSFUL. Sweep any
# surviving DevLaunch JVM belonging to this repo's smoke runs afterwards. Matched narrowly on the
# repo's own moddev run args so it can never touch the Mineflayer datapack server.
stop_smoke() {
  local group="$1"
  kill -TERM -- "-$group" 2>/dev/null
  sleep 2
  kill -KILL -- "-$group" 2>/dev/null

  local stragglers
  stragglers=$(pgrep -f "moddev/(server|client)SmokeRun.*Args\.txt" 2>/dev/null || true)
  if [[ -n "$stragglers" ]]; then
    echo "note: sweeping leaked smoke JVM(s): $(tr '\n' ' ' <<<"$stragglers")"
    xargs -r kill -TERM <<<"$stragglers"; sleep 2
    xargs -r kill -KILL <<<"$stragglers" 2>/dev/null
  fi
}

run_smoke() {
  local name="$1" task="$2" wrapper="$3" log="$4"
  shift 4
  local markers=("$@")

  echo "=== ${name} smoke: ${task} ==="
  : >"$log"

  # shellcheck disable=SC2086 # wrapper is intentionally word-split (empty, or "xvfb-run -a")
  setsid $wrapper ./mod/gradlew -p mod "$task" --no-daemon >"$log" 2>&1 &
  local group=$!

  local deadline=$((SECONDS + TIMEOUT_SECONDS))
  while ((SECONDS < deadline)); do
    local found=1
    for marker in "${markers[@]}"; do
      grep -Fq "$marker" "$log" || { found=0; break; }
    done

    if ((found)); then
      for marker in "${markers[@]}"; do
        grep -F "$marker" "$log" | tail -1
      done
      stop_smoke "$group"
      echo "PASS: ${name} reached every required marker."
      return 0
    fi

    # Dying early is a failure even when Gradle is about to report success.
    if ! kill -0 "$group" 2>/dev/null; then
      echo "FAIL: ${name} exited before printing its markers. Last 25 lines:" >&2
      tail -25 "$log" >&2
      stop_smoke "$group"
      return 1
    fi

    sleep 3
  done

  echo "FAIL: ${name} timed out after ${TIMEOUT_SECONDS}s without printing its markers." >&2
  stop_smoke "$group"
  return 1
}

verify_server() {
  mkdir -p mod/run-server-smoke
  # Start from a fresh world, the way a CI runner does. A world left behind by a killed run keeps its
  # session.lock and the next boot dies with "already locked". Scratch only: mod/.gitignore covers
  # run-*/ and nothing under it is tracked.
  rm -rf mod/run-server-smoke/world
  printf 'eula=true\n' >mod/run-server-smoke/eula.txt
  printf 'online-mode=false\nserver-port=%s\n' "$SMOKE_PORT" >mod/run-server-smoke/server.properties
  run_smoke "dedicated server" runServerSmoke "" /tmp/shadowslave-server-smoke.log \
    'Shadow Slave Java core is loading' \
    'For help, type "help"'
}

verify_client() {
  command -v xvfb-run >/dev/null || {
    echo "FAIL: xvfb-run is required for the headless client smoke (apt-get install xvfb)." >&2
    return 1
  }
  mkdir -p mod/run-client-smoke
  run_smoke "physical client" runClientSmoke "xvfb-run -a" /tmp/shadowslave-client-smoke.log \
    'Shadow Slave client is loading' \
    'minecraft:textures/atlas/gui.png-atlas'
}

status=0
case "${1:-both}" in
  server) verify_server || status=1 ;;
  client) verify_client || status=1 ;;
  both) verify_server || status=1; verify_client || status=1 ;;
  *) echo "usage: $0 [server|client]" >&2; exit 2 ;;
esac

((status)) && echo "SMOKE GATE FAILED" >&2 || echo "SMOKE GATE PASSED"
exit "$status"
