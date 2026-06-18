from __future__ import annotations

import random

from pbark.cli.dog_art import FACT_PREFIX
from pbark.config._resources import resources_dir

NAME = "pbark"
NUMBER = "1.0.0"


def _load_facts() -> list[str]:
    path = resources_dir() / "dogfacts.txt"
    if not path.is_file():
        return []
    return [line.strip() for line in path.read_text(encoding="utf-8").splitlines() if line.strip()]


_DOG_FACTS = _load_facts()


def print_version() -> None:
    print(f"{NAME} {NUMBER}")
    fact = random_dog_fact()
    if fact is not None:
        print(FACT_PREFIX + fact)


def random_dog_fact() -> str | None:
    if not _DOG_FACTS:
        return None
    return random.choice(_DOG_FACTS)
