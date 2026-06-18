from __future__ import annotations

from pbark.lexer import TokenType
from pbark.parser import ast_node as AstNode
from pbark.parser import keywords as Keywords


class LoopControlParser:
    def __init__(self, parser) -> None:
        self._parser = parser

    def parse_break_line(self):
        if not self._line_starts_with(Keywords.BREAK_KEYWORDS):
            return None
        line = self._parser.peek().line
        self._parser.consume_until_line_end()
        return AstNode.Break(line)

    def parse_continue_line(self):
        if not self._line_starts_with(Keywords.CONTINUE_KEYWORDS):
            return None
        line = self._parser.peek().line
        self._parser.consume_until_line_end()
        return AstNode.Continue(line)

    def _line_starts_with(self, words: list[str]) -> bool:
        token = self._parser.peek()
        return token.is_(TokenType.IDENTIFIER) and self._parser.normalise(token.value) in words
