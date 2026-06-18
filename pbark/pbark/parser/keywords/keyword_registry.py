
from __future__ import annotations

class KeywordRegistry:
    def __init__(self) -> None:
        self._heard_word_groups: list[list[str]] = []

    def phrase_words(self, *items: str) -> list[str]:
        return list(dict.fromkeys(items))

    def heard_words(self, *items: str) -> list[str]:
        copy = list(dict.fromkeys(items))
        self._heard_word_groups.append(copy)
        return copy

    def fill_keyword_set(self, target: set[str]) -> None:
        for group in self._heard_word_groups:
            target.update(group)
