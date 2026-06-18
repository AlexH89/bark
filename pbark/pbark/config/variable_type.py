from __future__ import annotations

from enum import Enum, auto


class VariableType(Enum):
    BREED = auto()
    OBJECT = auto()
    STASH = auto()
    PILE = auto()
    STORY_NUMBER = auto()
    STORY_TEXT = auto()
