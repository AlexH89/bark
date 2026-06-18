from __future__ import annotations
from dataclasses import dataclass
from pbark.interpreter.bark_value import BarkValue

@dataclass(frozen=True)
class Prop:
    value: BarkValue

    @staticmethod
    def zero() -> Prop:
        return Prop(BarkValue.of(0))
