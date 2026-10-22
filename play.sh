#!/usr/bin/env bash
# Build the mod from this checkout and launch a Minecraft 1.21.1 client with it
# loaded. NeoForge, GeckoLib, SmartBrainLib and PlayerAnimationLib come along
# automatically through Gradle's runClient. See PLAYTEST.md.
set -euo pipefail

cd "$(dirname "$0")"

usage() {
    cat <<'EOF'
Usage: ./play.sh [--branch NAME] [--pull] [--world NAME]

  --branch NAME  switch to NAME (fetched from GitHub) before building
  --pull         pull the latest commits for the current branch first
  --world NAME   skip the menus and load the singleplayer world NAME
                 (the world must already exist in mod/run/saves)
EOF
}

branch=""
pull=0
world=""
while [ $# -gt 0 ]; do
    case "$1" in
        --branch) branch="${2:?--branch needs a name}"; shift 2 ;;
        --pull) pull=1; shift ;;
        --world) world="${2:?--world needs a name}"; shift 2 ;;
        -h|--help) usage; exit 0 ;;
        *) echo "Unknown option: $1" >&2; usage >&2; exit 2 ;;
    esac
done

# Gradle needs a JDK (17 or newer) to start; it downloads JDK 21 for the build
# itself if it is missing.
java_cmd="java"
if [ -n "${JAVA_HOME:-}" ]; then
    java_cmd="$JAVA_HOME/bin/java"
fi
if ! command -v "$java_cmd" >/dev/null 2>&1; then
    echo "No Java found. Install JDK 21 (https://adoptium.net/temurin/releases/?version=21) and run this again." >&2
    exit 1
fi
java_major="$("$java_cmd" -version 2>&1 | sed -n 's/.*version "\([0-9]*\).*/\1/p' | head -n 1)"
if [ -n "$java_major" ] && [ "$java_major" -lt 17 ]; then
    echo "Java $java_major is too old. Install JDK 21 (https://adoptium.net/temurin/releases/?version=21) and run this again." >&2
    exit 1
fi

if [ -n "$branch" ] || [ "$pull" -eq 1 ]; then
    if [ -n "$(git status --porcelain --untracked-files=no)" ]; then
        echo "You have uncommitted changes, so I won't switch or pull. Commit or stash them first." >&2
        exit 1
    fi
    if [ -n "$branch" ]; then
        git fetch origin "$branch"
        git checkout "$branch" 2>/dev/null || git checkout -b "$branch" --track "origin/$branch"
    fi
    git pull --ff-only
fi

echo "Launching $(git rev-parse --abbrev-ref HEAD) @ $(git rev-parse --short HEAD)"

gradle_args=(-p mod runClient)
if [ -n "$world" ]; then
    gradle_args+=("-PquickPlayWorld=$world")
fi
exec ./mod/gradlew "${gradle_args[@]}"
