#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd -- "$SCRIPT_DIR/../.." && pwd)"
cd "$REPO_ROOT"

status="$(git status --porcelain)"
if [[ -n "$status" ]]; then
	printf 'Error: Commit or stash all other changes before release:\n%s\n' "$status" >&2
	exit 1
fi

version="$(./gradlew properties | sed -n 's/^pluginVersion:[[:space:]]*//p' | sed 's/[[:space:]]*$//')"
if [[ -z "$version" ]]; then
	printf 'Error: pluginVersion is missing from gradle.properties.\n' >&2
	exit 1
fi

tag="v$version"
if [[ -n "$(git tag --list "$tag")" ]]; then
	printf "Error: Git tag '%s' already exists.\n" "$tag" >&2
	exit 1
fi

if ! remote_tag="$(git ls-remote --tags origin "refs/tags/$tag")"; then
	printf "Error: Could not check remote Git tag '%s'.\n" "$tag" >&2
	exit 1
fi
if [[ -n "$remote_tag" ]]; then
	printf "Error: Git tag '%s' already exists on origin.\n" "$tag" >&2
	exit 1
fi

./gradlew patchChangelog

changelog_status="$(git status --porcelain -- CHANGELOG.md)"
if [[ -z "$changelog_status" ]]; then
	printf 'Error: patchChangelog did not update CHANGELOG.md.\n' >&2
	exit 1
fi

git add -- CHANGELOG.md
git commit -m "release: $version"

./gradlew publishPlugin
git tag --annotate --message "Release $tag" "$tag"
git push origin "refs/tags/$tag"

printf "Published and tagged release '%s'.\n" "$tag"
