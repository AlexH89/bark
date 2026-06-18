from __future__ import annotations

from typing import Callable

from pbark.errors import BarktimeError
from pbark.interpreter import bark_value as bv
from pbark.parser.expression.comparison_op import ComparisonOp

Comparator = Callable[[bv.BarkValue, bv.BarkValue, int], bool]


class Comparators:
    @staticmethod
    def create() -> dict[ComparisonOp, Comparator]:
        return {
            ComparisonOp.EQUAL: lambda l, r, _: l.equal_to(r),
            ComparisonOp.NOT_EQUAL: lambda l, r, _: not l.equal_to(r),
            ComparisonOp.GREATER_THAN: Comparators._greater_than,
            ComparisonOp.LESS_THAN: Comparators._less_than,
            ComparisonOp.GREATER_THAN_OR_EQUAL: Comparators._greater_than_or_equal,
            ComparisonOp.LESS_THAN_OR_EQUAL: Comparators._less_than_or_equal,
        }

    @staticmethod
    def _greater_than_or_equal(left: bv.BarkValue, right: bv.BarkValue, line: int) -> bool:
        return Comparators._greater_than(left, right, line) or left.equal_to(right)

    @staticmethod
    def _less_than_or_equal(left: bv.BarkValue, right: bv.BarkValue, line: int) -> bool:
        return Comparators._less_than(left, right, line) or left.equal_to(right)

    @staticmethod
    def _greater_than(left: bv.BarkValue, right: bv.BarkValue, line: int) -> bool:
        if isinstance(left, bv.BarkNumber) and isinstance(right, bv.BarkNumber):
            return left.value > right.value
        raise BarktimeError(
            line,
            "Only bone counts can be ranked bigger, words and woofs don't make sense to dogs.",
        )

    @staticmethod
    def _less_than(left: bv.BarkValue, right: bv.BarkValue, line: int) -> bool:
        if isinstance(left, bv.BarkNumber) and isinstance(right, bv.BarkNumber):
            return left.value < right.value
        raise BarktimeError(line, "Only bone counts can be ranked smaller, dogs can't eat words.")
