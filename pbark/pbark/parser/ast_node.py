from __future__ import annotations

from dataclasses import dataclass, field
from typing import Any

from pbark.parser.parse_expression import ParseExpression
from pbark.print_style import PrintStyle


@dataclass
class Program:
    statements: list[Any]
    line: int = 1


@dataclass
class Print:
    style: PrintStyle
    values: list[ParseExpression]
    line: int
    paw_print: bool = False
    dog_speaker: str | None = None


@dataclass
class Listen:
    dog: str
    line: int


@dataclass
class Exit:
    line: int


@dataclass
class Wait:
    seconds: float
    line: int


@dataclass
class Assign:
    variable: str
    value: ParseExpression
    line: int


@dataclass
class SetAttribute:
    subject: str
    topic: str
    value: ParseExpression
    line: int


@dataclass
class IfBranch:
    condition: ParseExpression
    steps: list[Any]


@dataclass
class IfChain:
    branches: list[IfBranch]
    else_steps: list[Any] | None
    line: int


@dataclass
class WhileLoop:
    condition: ParseExpression
    steps: list[Any]
    line: int


@dataclass
class UntilLoop:
    condition: ParseExpression
    steps: list[Any]
    line: int


@dataclass
class ForEach:
    variable: str
    stash: str
    steps: list[Any]
    line: int


@dataclass
class FunctionDef:
    name: str
    params: list[str]
    steps: list[Any]
    return_expression: ParseExpression
    line: int


@dataclass
class Break:
    line: int


@dataclass
class Continue:
    line: int


@dataclass
class StashInit:
    stash: str
    items: list[ParseExpression]
    line: int


@dataclass
class StashAppend:
    stash: str
    item: ParseExpression
    line: int


@dataclass
class StashSet:
    stash: str
    which: str
    value: ParseExpression
    line: int


@dataclass
class StashRemove:
    stash: str
    which: str
    line: int


@dataclass
class StashClear:
    stash: str
    line: int


@dataclass
class PileInit:
    pile: str
    items: list[ParseExpression]
    line: int


@dataclass
class PilePush:
    pile: str
    item: ParseExpression
    line: int


@dataclass
class PilePop:
    pile: str
    line: int


@dataclass
class PileClear:
    pile: str
    line: int


@dataclass
class Share:
    giver: str
    stash: str | None
    recipients: list[str]
    line: int


@dataclass
class Pass:
    giver: str
    recipient: str
    line: int


@dataclass
class SetTrait:
    subject: str
    trait: str
    enabled: bool
    line: int


@dataclass
class TakeFromStash:
    subject: str
    stash: str
    which: str
    line: int


@dataclass
class LineGroup:
    statements: list[Any]
    line: int


AstNode = Any
