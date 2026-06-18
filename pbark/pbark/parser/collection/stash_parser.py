from __future__ import annotations

from pbark.config.config_loader import ConfigLoader
from pbark.errors import BarkError
from pbark.lexer import TokenType
from pbark.parser import ast_node as AstNode
from pbark.parser import keywords as Keywords
from pbark.parser.collection import stash_spots as StashSpots
from pbark.parser.expression.value_parser import ValueParser
from pbark.parser.parse_expression import NumberLiteral, StringLiteral


class StashParser:
    def __init__(self, parser) -> None:
        self._parser = parser

    def parse_stash_line(self):
        line = self._parser.peek().line
        stash = self._find_stash_on_line()
        if stash is None:
            return None
        ConfigLoader.require_stash(stash, line)
        if self._parser.line_has_word(Keywords.HOLDS_WORDS):
            items = self._parse_items_after_holds()
            if not items:
                raise BarkError(line, "The jar is open but nothing went inside. How dare you?")
            self._parser.consume_until_line_end()
            return AstNode.StashInit(stash, items, line)
        if self._parser.line_has_word(Keywords.STASH_ADD_WORDS) and not self._line_has_from():
            item = self._parse_item_after_stash_add()
            if item is None:
                raise BarkError(
                    line,
                    "You didn't say what will be added to the jar for the dogs to steal later!",
                )
            self._parser.consume_until_line_end()
            return AstNode.StashAppend(stash, item, line)
        if self._parser.line_has_word(Keywords.DROPS_WORDS):
            slot = self._find_jar_slot_on_line()
            if slot is None:
                raise BarkError(line, "Which item? The dogs need to know which one to steal")
            self._parser.consume_until_line_end()
            return AstNode.StashRemove(stash, slot, line)
        if not self._line_has_from() and not self._parser.line_has_word(Keywords.TAKE_FROM_STASH_WORDS):
            slot = self._find_jar_slot_on_line()
            if slot is not None:
                new_value = self._parse_new_value_after_slot(slot)
                if new_value is None:
                    return None
                self._parser.consume_until_line_end()
                return AstNode.StashSet(stash, slot, new_value, line)
        if self._line_has_null_clear():
            self._parser.consume_until_line_end()
            return AstNode.StashClear(stash, line)
        return None

    def _find_stash_on_line(self) -> str | None:
        name = ConfigLoader.resolve_collection_from_tokens(
            self._parser.all_tokens(), self._parser.cursor_index(), self._parser.line_end_index()
        )
        if name is None or not ConfigLoader.is_stash(name):
            return None
        return name

    def _line_has_from(self) -> bool:
        offset = 0
        while not self._parser.is_at_end_or_line_end_offset(offset):
            token = self._parser.peek_at(offset)
            if token.is_(TokenType.IDENTIFIER) and self._parser.normalise(token.value) in Keywords.FROM_WORDS:
                return True
            offset += 1
        return False

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

    def _find_jar_slot_on_line(self) -> str | None:
        offset = 0
        while not self._parser.is_at_end_or_line_end_offset(offset):
            token = self._parser.peek_at(offset)
            if not token.is_(TokenType.IDENTIFIER):
                offset += 1
                continue
            word = self._parser.normalise(token.value)
            if StashSpots.is_stash_spot(word):
                return word
            offset += 1
        return None

    def _parse_new_value_after_slot(self, slot: str):
        slot_at = -1
        offset = 0
        while not self._parser.is_at_end_or_line_end_offset(offset):
            token = self._parser.peek_at(offset)
            if token.is_(TokenType.IDENTIFIER) and slot == self._parser.normalise(token.value):
                slot_at = offset
                break
            offset += 1
        if slot_at < 0:
            return None
        value_start = -1
        off = slot_at + 1
        while not self._parser.is_at_end_or_line_end_offset(off):
            token = self._parser.peek_at(off)
            if token.is_(TokenType.IDENTIFIER) and Keywords.is_assign_subject_glue(
                self._parser.normalise(token.value)
            ):
                value_start = off + 1
                break
            off += 1
        if value_start < 0:
            return None
        return ValueParser(self._parser).parse_part(value_start, self._parser.count_tokens_ahead(0))

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

    def _parse_item_after_stash_add(self):
        add_at = -1
        offset = 0
        while not self._parser.is_at_end_or_line_end_offset(offset):
            token = self._parser.peek_at(offset)
            if token.is_(TokenType.IDENTIFIER) and self._parser.normalise(token.value) in Keywords.STASH_ADD_WORDS:
                add_at = offset
                break
            offset += 1
        if add_at < 0:
            return None
        off = add_at + 1
        while not self._parser.is_at_end_or_line_end_offset(off):
            token = self._parser.peek_at(off)
            if token.is_(TokenType.STRING):
                return StringLiteral(token.value)
            if token.is_(TokenType.NUMBER):
                return NumberLiteral(float(self._parser.normalise(token.value)))
            off += 1
        return None
