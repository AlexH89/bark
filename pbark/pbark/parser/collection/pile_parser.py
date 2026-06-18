from __future__ import annotations

from pbark.config.config_loader import ConfigLoader
from pbark.errors import BarkError
from pbark.lexer import TokenType
from pbark.parser import ast_node as AstNode
from pbark.parser import keywords as Keywords
from pbark.parser.parse_expression import NumberLiteral, StringLiteral


class PileParser:
    def __init__(self, parser) -> None:
        self._parser = parser

    def parse_pile_line(self):
        line = self._parser.peek().line
        pile = self._find_pile_on_line()
        if pile is None:
            return None
        ConfigLoader.require_pile(pile, line)
        if self._parser.line_has_word(Keywords.HOLDS_WORDS):
            items = self._parse_items_after_holds()
            if not items:
                raise BarkError(line, "The pile is empty on arrival. Put something in it first.")
            self._parser.consume_until_line_end()
            return AstNode.PileInit(pile, items, line)
        if self._line_has_null_clear():
            self._parser.consume_until_line_end()
            return AstNode.PileClear(pile, line)
        return None

    def _find_pile_on_line(self) -> str | None:
        name = ConfigLoader.resolve_collection_from_tokens(
            self._parser.all_tokens(), self._parser.cursor_index(), self._parser.line_end_index()
        )
        if name is None or not ConfigLoader.is_pile(name):
            return None
        return name

    def _line_has_null_clear(self) -> bool:
        offset = 0
        while not self._parser.is_at_end_or_line_end_offset(offset):
            token = self._parser.peek_at(offset)
            if token.is_(TokenType.IDENTIFIER) and Keywords.is_null_clear_word(
                self._parser.normalise(token.value)
            ):
                return True
            offset += 1
        return False

    def _parse_items_after_holds(self):
        holds_at = -1
        offset = 0
        while not self._parser.is_at_end_or_line_end_offset(offset):
            token = self._parser.peek_at(offset)
            if token.is_(TokenType.IDENTIFIER) and self._parser.normalise(token.value) in Keywords.HOLDS_WORDS:
                holds_at = offset
                break
            offset += 1
        items = []
        if holds_at < 0:
            return items
        off = holds_at + 1
        while not self._parser.is_at_end_or_line_end_offset(off):
            token = self._parser.peek_at(off)
            if token.is_(TokenType.COMMA):
                off += 1
                continue
            if token.is_(TokenType.IDENTIFIER) and self._parser.normalise(token.value) in Keywords.LIST_AND_WORDS:
                off += 1
                continue
            if token.is_(TokenType.STRING):
                items.append(StringLiteral(token.value))
            elif token.is_(TokenType.NUMBER):
                items.append(NumberLiteral(float(self._parser.normalise(token.value))))
            off += 1
        return items
