#!/usr/bin/env bash
# Copy Java registry files into the pbark package for PyPI wheels.
# Run from repo root: ./scripts/sync-pbark-resources.sh
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
SRC="$ROOT/src/main/resources"
DEST="$ROOT/pbark/pbark/resources"

FILES=(breeds.txt objects.txt stashes.txt piles.txt traits.json dogfacts.txt)

mkdir -p "$DEST"
for f in "${FILES[@]}"; do
  cp "$SRC/$f" "$DEST/$f"
  echo "copied $f"
done
echo "Done. Registry files are in pbark/pbark/resources/"
