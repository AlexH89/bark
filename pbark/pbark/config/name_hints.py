from __future__ import annotations

import json
from pbark.config.config_loader import ConfigLoader


class NameHints:
    MAX_DISTANCE = 2

    @staticmethod
    def suggest(name: str | None, candidates) -> str | None:
        if name is None or candidates is None or not candidates:
            return None
        normalized = ConfigLoader.normalise(name)
        phrase = ConfigLoader.resolve_name_phrase(normalized)
        if phrase is not None:
            normalized = phrase
        best = None
        best_distance = NameHints.MAX_DISTANCE + 1
        for candidate in candidates:
            distance = NameHints._distance(normalized, candidate)
            if distance < best_distance:
                best_distance = distance
                best = candidate
        return best if best_distance <= NameHints.MAX_DISTANCE else None

    @staticmethod
    def hint_phrase(name: str, candidates) -> str:
        suggestion = NameHints.suggest(name, candidates)
        if suggestion is None or suggestion == ConfigLoader.normalise(name):
            return ""
        display = suggestion.replace("_", " ") if "_" in suggestion else suggestion
        return f" Did you mean {display}?"

    @staticmethod
    def _distance(left: str, right: str) -> int:
        table = [[0] * (len(right) + 1) for _ in range(len(left) + 1)]
        for i in range(len(left) + 1):
            table[i][0] = i
        for j in range(len(right) + 1):
            table[0][j] = j
        for i in range(1, len(left) + 1):
            for j in range(1, len(right) + 1):
                cost = 0 if left[i - 1] == right[j - 1] else 1
                table[i][j] = min(
                    table[i - 1][j] + 1,
                    table[i][j - 1] + 1,
                    table[i - 1][j - 1] + cost,
                )
        return table[len(left)][len(right)]
