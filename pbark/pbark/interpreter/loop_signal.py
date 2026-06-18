from __future__ import annotations
from enum import Enum, auto

class LoopSignal(RuntimeError):
    class Kind(Enum):
        BREAK = auto()
        CONTINUE = auto()

    def __init__(self, kind: Kind) -> None:
        super().__init__()
        self.kind = kind
