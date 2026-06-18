from __future__ import annotations

from pbark.errors import BarkError
from pbark.lexer import TokenType
from pbark.parser import ast_node as AstNode
from pbark.parser import keywords as Keywords
from pbark.parser.expression.value_parser import ValueParser


class FunctionParser:
    def __init__(self, parser) -> None:
        self._parser = parser
        self._value_parser = ValueParser(parser)

    def parse_trick_line(self):
        if not self._parser.line_has_word(Keywords.EXPECTS_WORDS):
            return None
        line = self._parser.peek().line
        expects_at = self._find_word(Keywords.EXPECTS_WORDS)
        if expects_at < 0:
            return None
        name = self._find_name_before(expects_at)
        if name is None:
            raise BarkError(line, "Every trick needs a name")
        returns_at = self._find_word_after(Keywords.TRICK_RETURNS_WORDS, expects_at + 1)
        if returns_at < 0:
            raise BarkError(line, f'Trick "{name}" never says what the dogs get out of it!')
        params = self._read_params(expects_at + 1, returns_at)
        stop_at = self._find_word_after(Keywords.BURY_KEYWORDS, returns_at + 1)
        value_end = stop_at if stop_at >= 0 else self._line_end_offset()
        return_expr = self._value_parser.parse_part(returns_at + 1, value_end)
        if stop_at >= 0:
            self._parser.advance_by(stop_at + 1)
            self._parser.skip_newlines()
            steps = []
        else:
            self._parser.consume_until_line_end()
            self._parser.skip_newlines()
            steps = self._read_body_until_stop()
            if not self._parser.current_line_is_bury():
                raise BarkError(line, "The trick started but you forgot to say stop!")
            self._parser.consume_bury_line()
        return AstNode.FunctionDef(name, params, steps, return_expr, line)

    def _find_name_before(self, expects_at: int) -> str | None:
        for offset in range(expects_at):
            token = self._parser.peek_at(offset)
            if token.is_(TokenType.IDENTIFIER):
                return self._parser.normalise(token.value)
        return None

    def _read_params(self, from_: int, to: int) -> list[str]:
        params = []
        for offset in range(from_, to):
            token = self._parser.peek_at(offset)
            if not token.is_(TokenType.IDENTIFIER):
                continue
            word = self._parser.normalise(token.value)
            if word in Keywords.EXPECTS_WORDS or word in Keywords.TRICK_RETURNS_WORDS:
                continue
            if word in Keywords.LIST_AND_WORDS:
                continue
            params.append(word)
        return params

    def _line_end_offset(self) -> int:
        offset = 0
        while not self._parser.is_at_end_or_line_end_offset(offset):
            offset += 1
        return offset

    def _read_body_until_stop(self):
        steps = []
        while not self._parser.is_at_end():
            self._parser.skip_newlines()
            if self._parser.is_at_end() or self._parser.current_line_is_bury():
                break
            self._parser.parse_statement_into(steps)
        return steps

    def _find_word(self, words: list[str]) -> int:
        return self._find_word_after(words, 0)

    def _find_word_after(self, words: list[str], from_: int) -> int:
        offset = from_
        while not self._parser.is_at_end_or_line_end_offset(offset):
            token = self._parser.peek_at(offset)
            if token.is_(TokenType.IDENTIFIER) and self._parser.normalise(token.value) in words:
                return offset
            offset += 1
        return -1
