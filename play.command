#!/usr/bin/env bash
# macOS double-click launcher: Finder runs .command files in Terminal.
cd "$(dirname "$0")" && ./play.sh "$@"
status=$?
if [ "$status" -ne 0 ]; then
    read -r -p "Press Return to close."
fi
exit "$status"
