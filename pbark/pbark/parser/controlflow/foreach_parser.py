from __future__ import annotations

from dataclasses import dataclass

from pbark.config.config_loader import ConfigLoader
from pbark.errors import BarkError
from pbark.lexer import TokenType
from pbark.parser import ast_node as AstNode
from pbark.parser import keywords as Keywords
from pbark.parser.controlflow.inline_body_parser import InlineBodyParser


@dataclass
class _ForEachLineMarkers:
    then_offset: int


class ForEachParser:
    def __init__(self, parser) -> None:
        self._parser = parser

    def parse_for_each_line(self):
        line = self._parser.peek().line
        if not self._parser.current_line_starts_with_any(Keywords.FOR_WORDS):
            return None
        if self._find_word_offset(Keywords.EACH_WORDS) < 0:
            raise BarkError(
                line,
                "For loops start with for each, like 'for each bone from cookie jar'",
            )
        each_at = self._find_word_offset(Keywords.EACH_WORDS)
        variable = self._find_loop_variable_after(each_at)
        if variable is None:
            raise BarkError(line, "For each what? Name the loop bone or treat.")
        from_at = self._find_from_or_in_after(each_at + 1)
        if from_at < 0:
            raise BarkError(line, "For each needs from or in, like 'for each bone from cookie jar'")
        stash = ConfigLoader.resolve_collection_from_tokens(
            self._parser.all_tokens(),
            self._parser.cursor_index() + from_at + 1,
            self._parser.line_end_index(),
        )
        if stash is None or not ConfigLoader.is_stash(stash):
            raise BarkError(line, "After from, name a stash the dogs can dig through")
        ConfigLoader.require_stash(stash, line)
        markers = self._find_for_each_line_markers()
        if markers.then_offset < 0:
            raise BarkError(line, "The dogs heard the start of a command but no 'then'")
        steps = self._parse_loop_body(markers, line)
        if not self._parser.current_line_is_bury():
            raise BarkError(line, "The dog started something but forgot what he was doing")
        self._parser.consume_bury_line()
        return AstNode.ForEach(variable, stash, steps, line)

    def _find_loop_variable_after(self, each_at: int) -> str | None:
        offset = each_at + 1
        while not self._parser.is_at_end_or_line_end_offset(offset):
            token = self._parser.peek_at(offset)
            if not token.is_(TokenType.IDENTIFIER):
                offset += 1
                continue
            word = self._parser.normalise(token.value)
            if word in Keywords.FROM_WORDS or word in Keywords.IN_WORDS:
                return None
            if Keywords.is_then_word(word):
                return None
            return word
        return None

    def _find_from_or_in_after(self, start_offset: int) -> int:
        offset = start_offset
        while not self._parser.is_at_end_or_line_end_offset(offset):
            token = self._parser.peek_at(offset)
            if not token.is_(TokenType.IDENTIFIER):
                offset += 1
                continue
            word = self._parser.normalise(token.value)
            if word in Keywords.FROM_WORDS or word in Keywords.IN_WORDS:
                return offset
            offset += 1
        return -1

    def _find_for_each_line_markers(self) -> _ForEachLineMarkers:
        offset = 0
        while not self._parser.is_at_end_or_line_end_offset(offset):
            token = self._parser.peek_at(offset)
            if token.is_(TokenType.IDENTIFIER) and Keywords.is_then_word(
                self._parser.normalise(token.value)
            ):
                return _ForEachLineMarkers(offset)
            offset += 1
        return _ForEachLineMarkers(-1)

    def _find_word_offset(self, words: list[str]) -> int:
        offset = 0
        while not self._parser.is_at_end_or_line_end_offset(offset):
            token = self._parser.peek_at(offset)
            if token.is_(TokenType.IDENTIFIER) and self._parser.normalise(token.value) in words:
                return offset
            offset += 1
        return -1

    def _parse_loop_body(self, markers: _ForEachLineMarkers, line: int):
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
            raise BarkError(line, "Then what? The dogs are waiting for something to do.")
        return steps
