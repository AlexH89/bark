from __future__ import annotations

from dataclasses import dataclass

from pbark.config.config_loader import ConfigLoader
from pbark.errors import BarkError
from pbark.lexer import TokenType
from pbark.parser import keywords as Keywords
from pbark.parser.collection import stash_spots as StashSpots
from pbark.parser.parse_expression import (
    Binary,
    BinaryOp,
    BooleanLiteral,
    Empty,
    Field,
    FunctionCall,
    Length,
    NullLiteral,
    NumberLiteral,
    ParseExpression,
    PileAccess,
    PilePart,
    StashAccess,
    StashPart,
    StringLiteral,
    Variable,
    Not,
)


@dataclass
class _Step:
    value: ParseExpression
    next: int


class ValueParser:
    def __init__(self, parser) -> None:
        self._parser = parser

    def parse_part(self, start_offset: int, end_offset: int) -> ParseExpression:
        if start_offset >= end_offset:
            return Empty()
        return self._read_expression(start_offset, end_offset).value

    def parse_comma_separated(self, start_offset: int, end_offset: int) -> list[ParseExpression]:
        values: list[ParseExpression] = []
        pos = start_offset
        while pos < end_offset:
            while pos < end_offset and self._parser.peek_at(pos).is_(TokenType.COMMA):
                pos += 1
            if pos >= end_offset:
                break
            step = self._read_expression(pos, end_offset)
            values.append(step.value)
            pos = step.next
        return values

    def _read_expression(self, from_: int, to: int) -> _Step:
        step = self._read_multiply_or_divide(from_, to)
        while step.next < to:
            op = self._add_op_at(step.next)
            if op is None:
                break
            right = self._read_multiply_or_divide(step.next + 1, to)
            step = _Step(Binary(step.value, op, right.value), right.next)
        return step

    def _read_multiply_or_divide(self, from_: int, to: int) -> _Step:
        step = self._read_single_value(from_, to)
        while step.next < to:
            op = self._mul_op_at(step.next)
            if op is None:
                break
            right = self._read_single_value(step.next + 1, to)
            step = _Step(Binary(step.value, op, right.value), right.next)
        return step

    def _read_single_value(self, from_: int, to: int) -> _Step:
        if from_ >= to:
            return _Step(Empty(), from_)
        if self._word_at(from_, Keywords.NOT_KEYWORDS):
            inner = self._read_single_value(from_ + 1, to)
            return _Step(Not(inner.value), inner.next)
        start = self._read_number_string_or_word(from_, to)
        return self._read_words_after_value(start, to)

    def _read_number_string_or_word(self, from_: int, to: int) -> _Step:
        token = self._parser.peek_at(from_)
        if token.is_(TokenType.STRING):
            return _Step(StringLiteral(token.value), from_ + 1)
        if token.is_(TokenType.NUMBER):
            return _Step(
                NumberLiteral(float(self._parser.normalise(token.value))),
                from_ + 1,
            )
        if token.is_(TokenType.IDENTIFIER):
            return self._read_word(from_, to)
        raise BarkError(
            token.line,
            f'The dogs don\'t know what to do with "{token.value}" here.',
        )

    def _read_words_after_value(self, step: _Step, to: int) -> _Step:
        pos = step.next
        if pos < to and self._word_at(pos, Keywords.LETTERS_WORDS):
            return _Step(Length(step.value), pos + 1)
        return step

    def _read_word(self, from_: int, to: int) -> _Step:
        how_many = self._try_parse_how_many_field_count(from_, to)
        if how_many is not None:
            return how_many
        jar_count = self._try_parse_items_in_jar_count(from_, to)
        if jar_count is not None:
            return jar_count
        collection = self._read_collection(from_, to)
        if collection is not None:
            return collection
        word = self._parser.normalise(self._parser.peek_at(from_).value)
        if word in Keywords.TYPE_NOTHING_WORDS or word in Keywords.NULL_VALUES:
            return _Step(NullLiteral(), from_ + 1)
        bool_val = Keywords.parse_boolean(word)
        if bool_val is not None:
            return _Step(BooleanLiteral(bool_val), from_ + 1)
        if from_ + 1 < to and self._word_at(from_ + 1, Keywords.WITH_WORDS):
            return self._read_trick_call(word, from_ + 2, to)
        return _Step(Variable(word), from_ + 1)

    def _try_parse_how_many_field_count(self, from_: int, to: int) -> _Step | None:
        if (
            from_ + 2 >= to
            or not self._word_at(from_, Keywords.HOW_WORDS)
            or not self._word_at(from_ + 1, Keywords.MANY_WORDS)
        ):
            return None
        topic_offset = from_ + 2
        if not self._parser.peek_at(topic_offset).is_(TokenType.IDENTIFIER):
            return None
        topic = Keywords.explicit_attribute_topic(
            self._parser.normalise(self._parser.peek_at(topic_offset).value)
        )
        if topic is None:
            return None
        who_start = topic_offset + 1
        if who_start >= to:
            return None
        who = ConfigLoader.resolve_name_from_tokens(
            self._parser.all_tokens(),
            self._parser.cursor_index() + who_start,
            self._parser.cursor_index() + to,
        )
        next_pos: int
        if who is None:
            who_token = self._parser.peek_at(who_start)
            if not who_token.is_(TokenType.IDENTIFIER):
                return None
            who_word = self._parser.normalise(who_token.value)
            if not Keywords.is_dog_subject_reference(who_word, who_token.value):
                return None
            who = who_word
            next_pos = who_start + 1
        else:
            who_end = ConfigLoader.token_index_after_resolved_name(
                self._parser.all_tokens(),
                self._parser.cursor_index() + who_start,
                self._parser.cursor_index() + to,
                who,
            )
            next_pos = who_start + 1 if who_end < 0 else who_end - self._parser.cursor_index()
        if next_pos < to and Keywords.is_assign_subject_glue(
            self._parser.normalise(self._parser.peek_at(next_pos).value)
        ):
            next_pos += 1
        return _Step(Field(who, topic), next_pos)

    def _try_parse_items_in_jar_count(self, from_: int, to: int) -> _Step | None:
        if not self._word_at(from_, Keywords.COUNT_WORDS):
            return None
        pos = from_ + 1
        if pos >= to or not self._word_at(pos, Keywords.OF_WORDS):
            return None
        pos = self._find_next_phrase_word(pos + 1, to, Keywords.ITEMS_WORDS)
        if pos < 0:
            return None
        pos = self._find_next_phrase_word(pos + 1, to, Keywords.IN_WORDS)
        if pos < 0:
            return None
        after_in = pos + 1
        name = ConfigLoader.resolve_collection_from_tokens(
            self._parser.all_tokens(),
            self._parser.cursor_index() + after_in,
            self._parser.cursor_index() + to,
        )
        if name is None:
            raise BarkError(
                self._parser.peek_at(from_).line,
                "The dogs were waiting for a jar to investigate, but you never gave them one!",
            )
        after_name = after_in
        for end in range(after_in + 1, min(to, after_in + 4) + 1):
            found = ConfigLoader.resolve_collection_from_tokens(
                self._parser.all_tokens(),
                self._parser.cursor_index() + after_in,
                self._parser.cursor_index() + end,
            )
            if name == found:
                after_name = end
                break
        if ConfigLoader.is_stash(name):
            return _Step(StashAccess(name, StashPart.COUNT), after_name)
        if ConfigLoader.is_pile(name):
            return _Step(PileAccess(name, PilePart.COUNT), after_name)
        raise BarkError(
            self._parser.peek_at(from_).line,
            "The dogs are confused by what you wanted them to investigate! That was not a jar!",
        )

    def _find_next_phrase_word(self, from_: int, to: int, words: list[str]) -> int:
        for pos in range(from_, to):
            if self._word_at(pos, words):
                return pos
        return -1

    def _read_collection(self, from_: int, to: int) -> _Step | None:
        abs_start = self._parser.cursor_index() + from_
        abs_end = self._parser.cursor_index() + to
        name = ConfigLoader.resolve_collection_from_tokens(
            self._parser.all_tokens(), abs_start, abs_end
        )
        if name is None:
            return None
        after_name = self._count_tokens_in_collection_name(from_, to, name)
        if ConfigLoader.is_stash(name):
            return self._read_stash_value(name, after_name, to)
        if ConfigLoader.is_pile(name):
            return self._read_pile_value(name, after_name, to)
        return None

    def _count_tokens_in_collection_name(self, from_: int, to: int, name: str) -> int:
        for end in range(from_ + 1, min(to, from_ + 4) + 1):
            found = ConfigLoader.resolve_collection_from_tokens(
                self._parser.all_tokens(),
                self._parser.cursor_index() + from_,
                self._parser.cursor_index() + end,
            )
            if name == found:
                return end
        return from_ + 1

    def _read_stash_value(self, stash: str, from_: int, to: int) -> _Step:
        pos = self._parser.skip_ignored_identifiers(from_, to)
        if pos < to and self._parser.peek_at(pos).is_(TokenType.IDENTIFIER):
            spot = self._parser.normalise(self._parser.peek_at(pos).value)
            if StashSpots.is_stash_spot(spot):
                return _Step(StashAccess(stash, StashPart.ELEMENT, spot), pos + 1)
        if pos < to and self._word_at(pos, Keywords.COUNT_WORDS):
            return _Step(StashAccess(stash, StashPart.COUNT), pos + 1)
        return _Step(StashAccess(stash, StashPart.ALL), pos)

    def _read_pile_value(self, pile: str, from_: int, to: int) -> _Step:
        pos = self._parser.skip_ignored_identifiers(from_, to)
        return _Step(PileAccess(pile, PilePart.ALL), pos)

    def _read_trick_call(self, name: str, from_: int, to: int) -> _Step:
        args: list[ParseExpression] = []
        pos = from_
        while pos < to:
            arg = self._read_expression(pos, to)
            args.append(arg.value)
            pos = arg.next
            if pos < to and Keywords.is_list_separator(self._parser.peek_at(pos)):
                pos += 1
                continue
            break
        return _Step(FunctionCall(name, args), pos)

    def _add_op_at(self, offset: int) -> BinaryOp | None:
        token = self._parser.peek_at(offset)
        if token.is_(TokenType.PLUS):
            return BinaryOp.PLUS
        if token.is_(TokenType.MINUS):
            return BinaryOp.MINUS
        if token.is_(TokenType.IDENTIFIER):
            word = self._parser.normalise(token.value)
            if Keywords.is_math_add_keyword(word):
                return BinaryOp.PLUS
            if Keywords.is_math_subtract_keyword(word):
                return BinaryOp.MINUS
        return None

    def _mul_op_at(self, offset: int) -> BinaryOp | None:
        token = self._parser.peek_at(offset)
        if token.is_(TokenType.STAR):
            return BinaryOp.STAR
        if token.is_(TokenType.SLASH):
            return BinaryOp.SLASH
        if token.is_(TokenType.IDENTIFIER):
            word = self._parser.normalise(token.value)
            if word in Keywords.STAR_KEYWORDS:
                return BinaryOp.STAR
            if word in Keywords.SLASH_KEYWORDS:
                return BinaryOp.SLASH
        return None

    def _word_at(self, offset: int, words: list[str]) -> bool:
        token = self._parser.peek_at(offset)
        return token.is_(TokenType.IDENTIFIER) and self._parser.normalise(token.value) in words
