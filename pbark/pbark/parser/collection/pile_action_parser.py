from __future__ import annotations

from pbark.config.config_loader import ConfigLoader
from pbark.errors import BarkError
from pbark.lexer import TokenType
from pbark.parser import ast_node as AstNode
from pbark.parser import keywords as Keywords
from pbark.parser.parse_expression import NumberLiteral, StringLiteral


class PileActionParser:
    def __init__(self, parser) -> None:
        self._parser = parser

    def parse_grab_top_line(self):
        pile = self._find_pile_after_from()
        if pile is None or not self._parser.line_has_word(Keywords.PILE_GRAB_FROM_WORDS):
            return None
        line = self._parser.peek().line
        if not self._line_has_top_before_from():
            raise BarkError(
                line,
                "Grab what from the pile? The dogs need to know which item they can have!",
            )
        subject = self._find_dog_on_line()
        if subject is None:
            raise BarkError(
                line, "Who is grabbing from the pile? Name the dog or use she/he first"
            )
        ConfigLoader.require_pile(pile, line)
        self._parser.consume_until_line_end()
        return AstNode.PilePop(pile, line)

    def parse_push_into_line(self):
        into_at = self._find_in_or_into_offset()
        if into_at < 0:
            return None
        pile = ConfigLoader.resolve_collection_from_tokens(
            self._parser.all_tokens(),
            self._parser.cursor_index() + into_at + 1,
            self._parser.line_end_index(),
        )
        if pile is None or not ConfigLoader.is_pile(pile):
            return None
        if not self._parser.line_has_word(Keywords.PILE_PUSH_INTO_WORDS):
            return None
        line = self._parser.peek().line
        item = self._find_item_before_in(into_at)
        if item is None:
            raise BarkError(line, "Toss what into the pile? The dogs are curious and want to know!")
        subject = self._find_dog_on_line()
        if subject is None:
            raise BarkError(
                line,
                "Who is putting something in the pile? Name the dog or use she/he first",
            )
        ConfigLoader.require_pile(pile, line)
        self._parser.consume_until_line_end()
        return AstNode.PilePush(pile, item, line)

    def _find_pile_after_from(self) -> str | None:
        line_start = self._parser.cursor_index()
        line_end = self._parser.line_end_index()
        for from_at in range(line_start, line_end):
            token = self._parser.all_tokens()[from_at]
            if not token.is_(TokenType.IDENTIFIER):
                continue
            if self._parser.normalise(token.value) not in Keywords.FROM_WORDS:
                continue
            pile = ConfigLoader.resolve_collection_from_tokens(
                self._parser.all_tokens(), from_at + 1, line_end
            )
            if pile is not None and ConfigLoader.is_pile(pile):
                return pile
        return None

    def _line_has_top_before_from(self) -> bool:
        line_start = self._parser.cursor_index()
        line_end = self._parser.line_end_index()
        for from_at in range(line_start, line_end):
            token = self._parser.all_tokens()[from_at]
            if not token.is_(TokenType.IDENTIFIER) or self._parser.normalise(token.value) not in Keywords.FROM_WORDS:
                continue
            for offset in range(from_at - 1, line_start - 1, -1):
                before = self._parser.all_tokens()[offset]
                if not before.is_(TokenType.IDENTIFIER):
                    continue
                if self._parser.normalise(before.value) in Keywords.TOP_WORDS:
                    return True
            return False
        return False

    def _find_in_or_into_offset(self) -> int:
        offset = 0
        while not self._parser.is_at_end_or_line_end_offset(offset):
            token = self._parser.peek_at(offset)
            if not token.is_(TokenType.IDENTIFIER):
                offset += 1
                continue
            if self._parser.normalise(token.value) in Keywords.IN_WORDS:
                return offset
            offset += 1
        return -1

    def _find_item_before_in(self, into_at: int):
        for offset in range(into_at - 1, -1, -1):
            token = self._parser.peek_at(offset)
            if token.is_(TokenType.STRING):
                return StringLiteral(token.value)
            if token.is_(TokenType.NUMBER):
                return NumberLiteral(float(self._parser.normalise(token.value)))
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
