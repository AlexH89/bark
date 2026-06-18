from __future__ import annotations

from pathlib import Path


def resources_dir() -> Path:
    # Bundled copies ship inside the wheel (pip install pbark).
    bundled = Path(__file__).resolve().parent.parent / "resources"
    if (bundled / "breeds.txt").is_file():
        return bundled
    # Git clone dev layout: shared monorepo registry files.
    return Path(__file__).resolve().parent.parent.parent.parent / "src" / "main" / "resources"
