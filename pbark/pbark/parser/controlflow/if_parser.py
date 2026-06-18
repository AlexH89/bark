from __future__ import annotations

from dataclasses import dataclass

from pbark.errors import BarkError
from pbark.lexer import TokenType
from pbark.parser import ast_node as AstNode
from pbark.parser import keywords as Keywords
from pbark.parser.controlflow.inline_body_parser import InlineBodyParser
from pbark.parser.expression.condition_parser import ConditionParser


@dataclass
class _IfLineMarkers:
    if_start_offset: int
    then_offset: int
    otherwise_offset: int


class IfParser:
    def __init__(self, parser) -> None:
        self._parser = parser
        self._condition_parser = ConditionParser(parser)

    def parse_if_line(self):
        line = self._parser.peek().line
        branches = [self._parse_branch(line)]
        while self._parser.current_line_is_otherwise_when():
            self._parser.advance()
            branches.append(self._parse_branch(line))
        else_steps = self._parse_else_body(line)
        return AstNode.IfChain(branches, else_steps, line)

    def _parse_branch(self, line: int):
        markers = self._find_if_line_markers()
        if markers.if_start_offset < 0 or markers.then_offset < 0:
            raise BarkError(
                line,
                "The dogs look at you with tilted heads, waiting for a condition or consequence.",
            )
        condition = self._condition_parser.parse_condition(
            markers.if_start_offset + 1, markers.then_offset, line
        )
        then_steps = self._parse_then_body(markers, line)
        return AstNode.IfBranch(condition, then_steps)

    def _parse_then_body(self, markers: _IfLineMarkers, line: int):
        if self._parser.is_at_end_or_line_end_offset(markers.then_offset + 1):
            self._parser.advance_by(markers.then_offset + 1)
            self._parser.skip_newlines()
            return self._parse_multiline_then_body(line)
        token_count = self._then_body_token_count(markers)
        self._parser.advance_by(markers.then_offset + 1)
        return InlineBodyParser.parse_then_steps(
            self._parser,
            token_count,
            line,
            "Then what? The dogs are waiting for something to do.",
        )

    def _parse_multiline_then_body(self, line: int):
        steps = []
        while not self._parser.is_at_end():
            self._parser.skip_newlines()
            if self._parser.is_at_end():
                break
            if self._parser.current_line_is_multiline_if_stop():
                break
            self._parser.parse_statement_into(steps)
        if not steps:
            raise BarkError(line, "Then what? The dogs are waiting for something to do.")
        if self._parser.current_line_is_bury():
            self._parser.consume_bury_line()
        return steps

    def _parse_else_body(self, line: int):
        if not self._parser.current_line_starts_with_otherwise():
            return None
        if self._parser.current_line_is_otherwise_when():
            return None
        self._parser.advance()
        if self._parser.is_at_end_or_line_end_offset(0):
            self._parser.skip_newlines()
            return self._parse_multiline_else_body(line)
        return InlineBodyParser.parse_then_steps(
            self._parser,
            self._parser.count_tokens_ahead(0),
            line,
            "Otherwise what? The dogs need something to do.",
        )

    def _parse_multiline_else_body(self, line: int):
        steps = []
        while not self._parser.is_at_end():
            self._parser.skip_newlines()
            if self._parser.is_at_end():
                break
            if self._parser.current_line_is_bury():
                break
            self._parser.parse_statement_into(steps)
        if not steps:
            raise BarkError(line, "Otherwise what? The dogs need something to do.")
        if not self._parser.current_line_is_bury():
            raise BarkError(line, "The dog started something but forgot what he was doing")
        self._parser.consume_bury_line()
        return steps

    def _then_body_token_count(self, markers: _IfLineMarkers) -> int:
        if markers.otherwise_offset >= 0:
            return markers.otherwise_offset - (markers.then_offset + 1)
        return self._parser.count_tokens_ahead(markers.then_offset + 1)

    def _find_if_line_markers(self) -> _IfLineMarkers:
        if_start_offset = -1
        then_offset = -1
        otherwise_offset = -1
        offset = 0
        while not self._parser.is_at_end_or_line_end_offset(offset):
            token = self._parser.peek_at(offset)
            if token.is_(TokenType.IDENTIFIER):
                word = self._parser.normalise(token.value)
                if if_start_offset < 0 and word in Keywords.IF_START_WORDS:
                    if_start_offset = offset
                    offset += 1
                    continue
                if if_start_offset >= 0 and then_offset < 0 and Keywords.is_then_word(word):
                    then_offset = offset
                    offset += 1
                    continue
                if (
                    if_start_offset >= 0
                    and then_offset >= 0
                    and otherwise_offset < 0
                    and Keywords.is_otherwise_word(word)
                ):
                    otherwise_offset = offset
                    break
            offset += 1
        return _IfLineMarkers(if_start_offset, then_offset, otherwise_offset)
