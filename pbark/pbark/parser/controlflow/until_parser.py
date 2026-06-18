from __future__ import annotations

from dataclasses import dataclass

from pbark.errors import BarkError
from pbark.lexer import TokenType
from pbark.parser import ast_node as AstNode
from pbark.parser import keywords as Keywords
from pbark.parser.controlflow.inline_body_parser import InlineBodyParser
from pbark.parser.expression.condition_parser import ConditionParser


@dataclass
class _UntilLineMarkers:
    until_offset: int
    then_offset: int


class UntilParser:
    def __init__(self, parser) -> None:
        self._parser = parser
        self._condition_parser = ConditionParser(parser)

    def parse_until_line(self):
        line = self._parser.peek().line
        markers = self._find_until_line_markers()
        if markers.until_offset < 0 or markers.then_offset < 0:
            raise BarkError(
                line,
                "The dogs were told to stop eventually but not when or what to do until then",
            )
        condition = self._condition_parser.parse_condition(
            markers.until_offset + 1, markers.then_offset, line
        )
        steps = self._parse_loop_body(markers, line)
        if not self._parser.current_line_is_bury():
            raise BarkError(line, "The dog started something but forgot what he was doing")
        self._parser.consume_bury_line()
        return AstNode.UntilLoop(condition, steps, line)

    def _find_until_line_markers(self) -> _UntilLineMarkers:
        until_offset = -1
        then_offset = -1
        offset = 0
        while not self._parser.is_at_end_or_line_end_offset(offset):
            token = self._parser.peek_at(offset)
            if not token.is_(TokenType.IDENTIFIER):
                offset += 1
                continue
            word = self._parser.normalise(token.value)
            if until_offset < 0 and word in Keywords.UNTIL_KEYWORDS:
                until_offset = offset
            elif until_offset >= 0 and then_offset < 0 and Keywords.is_then_word(word):
                then_offset = offset
                break
            offset += 1
        return _UntilLineMarkers(until_offset, then_offset)

    def _parse_loop_body(self, markers: _UntilLineMarkers, line: int):
        if self._parser.is_at_end_or_line_end_offset(markers.then_offset + 1):
            self._parser.advance_by(markers.then_offset + 1)
            self._parser.skip_newlines()
            return self._parse_multiline_body(line)
        token_count = self._parser.count_tokens_ahead(markers.then_offset + 1)
        self._parser.advance_by(markers.then_offset + 1)
        return InlineBodyParser.parse_then_steps(
            self._parser,
            token_count,
            line,
            "Then what? The dogs are waiting for something to do.",
        )

    def _parse_multiline_body(self, line: int):
        steps = []
        while not self._parser.is_at_end():
            self._parser.skip_newlines()
            if self._parser.is_at_end():
                break
            if self._parser.current_line_is_bury():
                break
            self._parser.parse_statement_into(steps)
        if not steps:
            raise BarkError(line, "Then what? The dogs are waiting for something to do")
        return steps
