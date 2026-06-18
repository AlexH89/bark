from __future__ import annotations

from dataclasses import dataclass
from enum import Enum, auto

from pbark.parser.expression.comparison_op import ComparisonOp


class BinaryOp(Enum):
    PLUS = auto()
    MINUS = auto()
    STAR = auto()
    SLASH = auto()


class LogicalOp(Enum):
    AND = auto()
    OR = auto()
    NOR = auto()


class StashPart(Enum):
    ALL = auto()
    COUNT = auto()
    ELEMENT = auto()


class PilePart(Enum):
    ALL = auto()
    COUNT = auto()
    TOP = auto()


class ValueType(Enum):
    NUMBER = auto()
    WORDS = auto()
    NOTHING = auto()


@dataclass(frozen=True)
class StringLiteral:
    value: str


@dataclass(frozen=True)
class NumberLiteral:
    value: float


@dataclass(frozen=True)
class BooleanLiteral:
    value: bool


@dataclass(frozen=True)
class PeriodLiteral:
    pass


@dataclass(frozen=True)
class Empty:
    pass


@dataclass(frozen=True)
class NullLiteral:
    pass


@dataclass(frozen=True)
class Variable:
    value: str


@dataclass(frozen=True)
class Field:
    subject: str
    topic: str


@dataclass(frozen=True)
class Binary:
    left: ParseExpression
    op: BinaryOp
    right: ParseExpression


@dataclass(frozen=True)
class Not:
    operand: ParseExpression


@dataclass(frozen=True)
class Comparison:
    left: ParseExpression
    op: ComparisonOp
    right: ParseExpression


@dataclass(frozen=True)
class HasExact:
    subject: str
    topic: str
    amount: float


@dataclass(frozen=True)
class HasTrait:
    subject: str
    trait: str


@dataclass(frozen=True)
class Logical:
    left: ParseExpression
    op: LogicalOp
    right: ParseExpression


@dataclass(frozen=True)
class StashAccess:
    stash: str
    part: StashPart
    which: str | None = None


@dataclass(frozen=True)
class PileAccess:
    pile: str
    part: PilePart


@dataclass(frozen=True)
class Join:
    stash: str
    delimiter: ParseExpression


@dataclass(frozen=True)
class FunctionCall:
    name: str
    args: list[ParseExpression]


@dataclass(frozen=True)
class Length:
    value: ParseExpression


@dataclass(frozen=True)
class Contains:
    haystack: ParseExpression
    needle: ParseExpression


@dataclass(frozen=True)
class TypeCheck:
    value: ParseExpression
    type: ValueType


ParseExpression = (
    StringLiteral
    | NumberLiteral
    | BooleanLiteral
    | PeriodLiteral
    | Empty
    | NullLiteral
    | Variable
    | Field
    | Binary
    | Not
    | Comparison
    | HasExact
    | HasTrait
    | Logical
    | StashAccess
    | PileAccess
    | Join
    | FunctionCall
    | Length
    | Contains
    | TypeCheck
)
