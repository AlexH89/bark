from __future__ import annotations

from dataclasses import dataclass


@dataclass(frozen=True)
class BarkOptions:
    strict: bool = False
    quiet: bool = False

    @staticmethod
    def defaults() -> BarkOptions:
        return BarkOptions(False, False)
