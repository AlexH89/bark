from __future__ import annotations

from pbark.lexer import TokenType
from pbark.parser import ast_node as AstNode
from pbark.parser import keywords as Keywords


class InputParser:
    def __init__(self, parser) -> None:
        self._parser = parser

    def parse_exit_line(self):
        if not self._parser.current_line_starts_with_any(Keywords.EXIT_KEYWORDS):
            return None
        line = self._parser.peek().line
        self._parser.consume_until_line_end()
        return AstNode.Exit(line)

    def parse_wait_line(self):
        if not self._parser.line_has_word(Keywords.WAIT_KEYWORDS):
            return None
        line = self._parser.peek().line
        seconds = self._find_number_on_line()
        if seconds is None:
            seconds = 1.0
        self._parser.consume_until_line_end()
        return AstNode.Wait(seconds, line)

    def parse_listen_line(self):
        from pbark.errors import BarkError

        if not self._parser.line_has_word(Keywords.STDIN_KEYWORDS):
            return None
        line = self._parser.peek().line
        dog = self._find_dog_on_line()
        if dog is None:
            raise BarkError(
                line, "Who is listening? Name the dog first, like the labrador listens."
            )
        self._parser.consume_until_line_end()
        return AstNode.Listen(dog, line)

    def _find_number_on_line(self) -> float | None:
        offset = 0
        while not self._parser.is_at_end_or_line_end_offset(offset):
            token = self._parser.peek_at(offset)
            if token.is_(TokenType.NUMBER):
                return float(self._parser.normalise(token.value))
            offset += 1
        return None

    def _find_dog_on_line(self) -> str | None:
        offset = 0
        while not self._parser.is_at_end_or_line_end_offset(offset):
            token = self._parser.peek_at(offset)
            if not token.is_(TokenType.IDENTIFIER):
                offset += 1
                continue
            word = self._parser.normalise(token.value)
            if Keywords.is_dog_subject_reference(word, token.value):
                return word
            offset += 1
        return None
