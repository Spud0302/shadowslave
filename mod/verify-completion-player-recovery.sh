#!/usr/bin/env bash
set -euo pipefail

usage() {
  echo "usage: bash mod/verify-completion-player-recovery.sh <recovery-console.log> <instance-uuid> | self-test" >&2
}

verify_log() {
  local log="$1" instance="$2"
  [[ -f "$log" ]] || { echo "FAIL: recovery log not found: $log" >&2; return 1; }

  local prefix="COMPLETION RECOVERY EVIDENCE nightmare=$instance "
  mapfile -t lines < <(grep -F "$prefix" "$log" || true)
  if [[ ${#lines[@]} -ne 2 ]]; then
    echo "FAIL: expected exactly two recovery-login evidence markers for $instance; got ${#lines[@]}." >&2
    return 1
  fi

  local expected_suffix="appraisal_applied=true active_present=false in_nightmare=false"
  local first_player second_player
  first_player="$(printf '%s\n' "${lines[0]}" | sed -n 's/.* player_uuid=\([^ ]*\) .*/\1/p')"
  second_player="$(printf '%s\n' "${lines[1]}" | sed -n 's/.* player_uuid=\([^ ]*\) .*/\1/p')"

  [[ -n "$first_player" && "$first_player" == "$second_player" ]] || {
    echo "FAIL: recovery markers do not authenticate one stable player UUID." >&2
    return 1
  }
  for line in "${lines[@]}"; do
    [[ "$line" == *"$expected_suffix"* ]] || {
      echo "FAIL: recovery marker is not fully converged: $line" >&2
      return 1
    }
  done

  echo "PASS: same player $first_player recovered $instance on two logins with appraisal applied, no active Nightmare, and outside the Nightmare dimension."
}

self_test() {
  local tmp instance player
  tmp="$(mktemp)"
  trap 'rm -f "$tmp"' RETURN
  instance=11111111-2222-3333-4444-555555555555
  player=aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee
  cat >"$tmp" <<EOF
COMPLETION RECOVERY EVIDENCE nightmare=$instance player_uuid=$player appraisal_applied=true active_present=false in_nightmare=false
COMPLETION RECOVERY EVIDENCE nightmare=$instance player_uuid=$player appraisal_applied=true active_present=false in_nightmare=false
EOF
  verify_log "$tmp" "$instance" >/dev/null

  sed -i '2s/aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee/ffffffff-bbbb-cccc-dddd-eeeeeeeeeeee/' "$tmp"
  if verify_log "$tmp" "$instance" >/dev/null 2>&1; then
    echo "FAIL: mismatched player UUID unexpectedly passed." >&2; return 1
  fi

  cat >"$tmp" <<EOF
COMPLETION RECOVERY EVIDENCE nightmare=$instance player_uuid=$player appraisal_applied=true active_present=false in_nightmare=false
COMPLETION RECOVERY EVIDENCE nightmare=$instance player_uuid=$player appraisal_applied=false active_present=false in_nightmare=false
EOF
  if verify_log "$tmp" "$instance" >/dev/null 2>&1; then
    echo "FAIL: incomplete recovery state unexpectedly passed." >&2; return 1
  fi
  echo "PASS: completion player recovery verifier self-test"
}

case "${1:-}" in
  self-test)
    [[ $# -eq 1 ]] || { usage; exit 2; }
    self_test
    ;;
  "") usage; exit 2 ;;
  *)
    [[ $# -eq 2 ]] || { usage; exit 2; }
    verify_log "$1" "$2"
    ;;
esac
