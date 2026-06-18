from __future__ import annotations

from dataclasses import dataclass
from typing import Union


@dataclass(frozen=True)
class BarkString:
    value: str

    def display(self) -> str:
        return self.value

    def equal_to(self, other: BarkValue) -> bool:
        return isinstance(other, BarkString) and self.value == other.value


@dataclass(frozen=True)
class BarkNumber:
    value: float

    def display(self) -> str:
        is_whole = self.value == int(self.value) and abs(self.value) != float("inf")
        return str(int(self.value)) if is_whole else str(self.value)

    def equal_to(self, other: BarkValue) -> bool:
        return isinstance(other, BarkNumber) and self.value == other.value


@dataclass(frozen=True)
class BarkBoolean:
    value: bool

    def display(self) -> str:
        return "true" if self.value else "false"

    def equal_to(self, other: BarkValue) -> bool:
        return isinstance(other, BarkBoolean) and self.value == other.value


@dataclass(frozen=True)
class BarkNull:
    def display(self) -> str:
        return "null"

    def equal_to(self, other: BarkValue) -> bool:
        return isinstance(other, BarkNull)


BarkValue = Union[BarkString, BarkNumber, BarkBoolean, BarkNull]


def describe(value: BarkValue | None) -> str:
    return "nothing" if value is None else value.display()


def of(value: str | float | bool) -> BarkValue:
    if isinstance(value, bool):
        return BarkBoolean(value)
    if isinstance(value, (int, float)):
        return BarkNumber(float(value))
    return BarkString(value)
